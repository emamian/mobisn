package edu.ndsu.cs.mobisn;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

public class BTMobiClient implements Runnable, DiscoveryListener {
	/** Describes this server */
	private static final UUID PICTURES_SERVER_UUID = new UUID(
			"F0E0D0C0B0A000908070605040302010", false);

	/** The attribute id of the record item with images names. */
	private static final int IMAGES_NAMES_ATTRIBUTE_ID = 0x4321;

	/** Shows the engine is ready to work. */
	private static final int READY = 0;

	/** Shows the engine is searching bluetooth devices. */
	private static final int DEVICE_SEARCH = 1;

	/** Shows the engine is searching bluetooth services. */
	private static final int SERVICE_SEARCH = 2;

	/** Keeps the current state of engine. */
	private int state = READY;

	/** Keeps the discovery agent reference. */
	private DiscoveryAgent discoveryAgent;

	/** Keeps the parent reference to process specific actions. */
	private GUIMobiClient parent;

	/** Becomes 'true' when this component is finalized. */
	private boolean isClosed;

	/** Process the search/download requests. */
	private Thread processorThread;

	/** Collects the remote devices found during a search. */
	private Vector /* RemoteDevice */devices = new Vector();

	/** Collects the services found during a search. */
	private Vector /* ServiceRecord */records = new Vector();

	/** Keeps the device discovery return code. */
	private int discType;

	/** Keeps the services search IDs (just to be able to cancel them). */
	private int[] searchIDs;

	/** Keeps the image name to be load. */
	private String profileNameToLoad;

	/** Keeps the table of {name, Service} to process the user choice. */
	private Hashtable base = new Hashtable();

	/** Informs the thread the download should be canceled. */
	private boolean isDownloadCanceled;

	/** Optimization: keeps service search pattern. */
	private UUID[] uuidSet;

	/** Optimization: keeps attributes list to be retrieved. */
	private int[] attrSet;

	private boolean isBTReady;

	/**
	 * Constructs the bluetooth server, but it is initialized in the different
	 * thread to "avoid dead lock".
	 * 
	 * @throws BluetoothStateException
	 */
	BTMobiClient(GUIMobiClient parent) throws BluetoothStateException {
		this.parent = parent;
		isBTReady = false;

		// create/get a local device and discovery agent
		LocalDevice localDevice = LocalDevice.getLocalDevice();
		discoveryAgent = localDevice.getDiscoveryAgent();

		// remember we've reached this point.
		isBTReady = true;

		// we have to initialize a system in different thread...
		processorThread = new Thread(this);
		processorThread.start();
	}

	/**
	 * Process the search/download requests.
	 */
	public void run() {

		// nothing to do if no bluetooth available
		if (!isBTReady) {
			return;
		}

		// initialize some optimization variables
		uuidSet = new UUID[2];

		// ok, we are interesting in btspp services only
		uuidSet[0] = new UUID(0x1101);

		// and only known ones, that allows pictures
		uuidSet[1] = PICTURES_SERVER_UUID;

		// we need an only service attribute actually
		attrSet = new int[1];

		// it's "images names" one
		attrSet[0] = IMAGES_NAMES_ATTRIBUTE_ID;

		// start processing the images search/download
		processImagesSearchDownload();
	}

	/**
	 * Processes images search/download until component is closed or system
	 * error has happen.
	 */
	private synchronized void processImagesSearchDownload() {
		System.out.println("process search download");
		while (!isClosed) {
			// wait for new search request from user
			state = READY;

			try {
				System.out.println("bt_client ready state");
				wait();
			} catch (InterruptedException e) {
				System.err.println("Unexpected interruption: " + e);

				return;
			}

			// check the component is destroyed
			if (isClosed) {
				return;
			}
			System.out.println("search devices");
			// search for devices
			if (!searchDevices()) {
				return;
			} else if (devices.size() == 0) {
				continue;
			}

			System.out.println("search services");
			// search for services now
			if (!searchServices()) {
				return;
			} else if (records.size() == 0) {
				continue;
			}
			System.out.println("present results");
			// ok, something was found - present the result to user now
			if (!presentUserSearchResults()) {
				// services are found, but no names there
				continue;
			}
			// the several download requests may be processed
			while (true) {
				// this download is not canceled, right?
				isDownloadCanceled = false;

				// ok, wait for download or need to wait for next search
				try {
					wait();
				} catch (InterruptedException e) {
					System.err.println("Unexpected interruption: " + e);

					return;
				}

				// check the component is destroyed
				if (isClosed) {
					return;
				}

				// this means "go to the beginning"
				if (profileNameToLoad == null) {
					break;
				}

				// load selected image data
				Profile p = loadProfile();

				// this should never happen - monitor is taken...
				if (isClosed) {
					return;
				}

				if (isDownloadCanceled) {
					continue; // may be next image to be download
				}

				if (p == null) {
					parent.informLoadError("Can't load profile: "
							+ profileNameToLoad);

					continue; // may be next image to be download
				}

				// ok, show image to user
				parent.showProfile(p, profileNameToLoad);

				// may be next image to be download
				continue;
			}
		}
	}

	private Profile loadProfile() {

		if (!base.containsKey(profileNameToLoad))
			return null;
		Vector v = (Vector) base.get(profileNameToLoad);
		Profile p = (Profile) v.elementAt(0);
		return p;
	}

	/**
	 * Search for bluetooth devices.
	 * 
	 * @return false if should end the component work.
	 */
	private boolean searchDevices() {
		// ok, start a new search then
		state = DEVICE_SEARCH;
		devices.removeAllElements();

		try {
			discoveryAgent.startInquiry(DiscoveryAgent.GIAC, this);
		} catch (BluetoothStateException e) {
			System.err.println("Can't start inquiry now: " + e);
			parent.informSearchError("Can't start device search");

			return true;
		}

		try {
			wait(); // until devices are found
		} catch (InterruptedException e) {
			System.err.println("Unexpected interruption: " + e);

			return false;
		}

		// this "wake up" may be caused by 'destroy' call
		if (isClosed) {
			return false;
		}

		// no?, ok, let's check the return code then
		switch (discType) {
		case INQUIRY_ERROR:
			parent.informSearchError("Device discovering error...");

			// fall through
		case INQUIRY_TERMINATED:
			// make sure no garbage in found devices list
			devices.removeAllElements();

			// nothing to report - go to next request
			break;

		case INQUIRY_COMPLETED:

			if (devices.size() == 0) {
				parent.informSearchError("No devices in range");
			}

			// go to service search now
			break;

		default:
			// what kind of system you are?... :(
			System.err.println("system error:"
					+ " unexpected device discovery code: " + discType);
			destroy();

			return false;
		}

		return true;
	}

	/**
	 * Destroy a work with bluetooth - exits the accepting thread and close
	 * notifier.
	 */
	void destroy() {
		synchronized (this) {
			isClosed = true;
			isDownloadCanceled = true;
			notify();
		}

		// wait for acceptor thread is done
		try {
			processorThread.join();
		} catch (InterruptedException e) {
		} // ignore
	}

	/**
	 * Search for proper service.
	 * 
	 * @return false if should end the component work.
	 */
	private boolean searchServices() {
		state = SERVICE_SEARCH;
		records.removeAllElements();
		searchIDs = new int[devices.size()];

		boolean isSearchStarted = false;

		for (int i = 0; i < devices.size(); i++) {
			RemoteDevice rd = (RemoteDevice) devices.elementAt(i);

			try {
				searchIDs[i] = discoveryAgent.searchServices(attrSet, uuidSet,
						rd, this);
			} catch (BluetoothStateException e) {
				System.err.println("Can't search services for: "
						+ rd.getBluetoothAddress() + " due to " + e);
				searchIDs[i] = -1;

				continue;
			}

			isSearchStarted = true;
		}

		// at least one of the services search should be found
		if (!isSearchStarted) {
			parent.informSearchError("Can't search services.");

			return true;
		}

		try {
			wait(); // until services are found
		} catch (InterruptedException e) {
			System.err.println("Unexpected interruption: " + e);

			return false;
		}

		// this "wake up" may be caused by 'destroy' call
		if (isClosed) {
			return false;
		}

		// actually, no services were found
		if (records.size() == 0) {
			parent.informSearchError("No proper services were found");
		}

		return true;
	}

	/**
	 * Gets the collection of the images titles (names) from the services,
	 * prepares a hashtable to match the image name to a services list, presents
	 * the images names to user finally.
	 * 
	 * @return false if no names in found services.
	 */
	private boolean presentUserSearchResults() {
		try {
			base.clear();

			for (int i = 0; i < records.size(); i++) {
				ServiceRecord sr = (ServiceRecord) records.elementAt(i);

				// get the attribute with images names
				DataElement de = sr
						.getAttributeValue(IMAGES_NAMES_ATTRIBUTE_ID);

				if (de == null) {
					System.err.println("Unexpected service - missed attribute");

					continue;
				}

				// get the images names from this attribute
				Enumeration deEnum = (Enumeration) de.getValue();

				Hashtable h = new Hashtable();
				while (deEnum.hasMoreElements()) {
					de = (DataElement) deEnum.nextElement();

					String name = (String) de.getValue();
					// System.out.println("name is : " + name);
					int idx = -1;
					try {
						idx = name.indexOf(":");
						// System.out.println("index: " + idx);

					} catch (Exception e) {
						// TODO: handle exception
						System.err.println("error in tag :" + name);
						e.printStackTrace();
						continue;

					}
					if (idx == -1) {
						continue;
					}
					String tag = name.substring(0, idx);
					String value = name.substring(idx + 1);
					// System.out.println("result->" + tag + " " + value);
					h.put(tag, value);
				}
				Profile p = new Profile();
				if (!p.loadFromHashtable(h)) {
					System.err.println("some fields are missing: " + h);
					continue;
				}
				Vector v = new Vector(3);
				v.insertElementAt(p, 0);
				v.insertElementAt(sr, 1);
				v.insertElementAt(h.get("interests"), 2);
				base.put(p.getFullName(), v);

			}

			return parent.showImagesNames(base);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/** Cancel's the devices/services search. */
	void cancelSearch() {
		synchronized (this) {
			if (state == DEVICE_SEARCH) {
				discoveryAgent.cancelInquiry(this);
			} else if (state == SERVICE_SEARCH) {
				for (int i = 0; i < searchIDs.length; i++) {
					discoveryAgent.cancelServiceSearch(searchIDs[i]);
				}
			}
		}
	}

	/** Sets the request to search the devices/services. */
	void requestSearch() {
		System.out.println("notify search");
		synchronized (this) {
			notify();
		}
	}

	/**
	 * Invoked by system when a new remote device is found - remember the found
	 * device.
	 */
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		// same device may found several times during single search
		if (devices.indexOf(btDevice) == -1) {
			devices.addElement(btDevice);
		}
	}

	/**
	 * Invoked by system when device discovery is done.
	 * <p>
	 * Remember the discType and process its evaluation in another thread.
	 */
	public void inquiryCompleted(int discType) {
		System.out.println("inquiry complete");
		this.discType = discType;

		synchronized (this) {
			notify();
		}
	}

	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		for (int i = 0; i < servRecord.length; i++) {
			records.addElement(servRecord[i]);
			System.out.println("1 service discovered");
		}
	}

	public void serviceSearchCompleted(int transID, int respCode) {
		try {
			System.out.println("service search complete");
			// first, find the service search transaction index
			int index = -1;

			for (int i = 0; i < searchIDs.length; i++) {
				if (searchIDs[i] == transID) {
					index = i;

					break;
				}
			}

			// error - unexpected transaction index
			if (index == -1) {
				System.err.println("Unexpected transaction index: " + transID);
				// process the error case here
			} else {
				searchIDs[index] = -1;
			}

			/*
			 * Actually, we do not care about the response code - if device is
			 * not reachable or no records, etc.
			 */

			// make sure it was the last transaction
			for (int i = 0; i < searchIDs.length; i++) {
				if (searchIDs[i] != -1) {
					return;
				}
			}

			// ok, all of the transactions are completed
			synchronized (this) {
				notify();
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	void requestLoad(String name) {
		synchronized (this) {
			profileNameToLoad = name;
			notify();
		}
	}
}

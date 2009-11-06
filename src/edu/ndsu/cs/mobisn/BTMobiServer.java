package edu.ndsu.cs.mobisn;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.ServiceRegistrationException;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class BTMobiServer implements Runnable {

	Profile myProfile;

	/** Describes this server */
	private static final UUID MOBISN_SERVER_UUID = new UUID(
			"F0E0D0C0B0A000908070605040302010", false);

	/** The attribute id of the record item with images names. */
	private static final int IMAGES_NAMES_ATTRIBUTE_ID = 0x4321;

	/** Keeps the local device reference. */
	private LocalDevice localDevice;

	/** Accepts new connections. */
	private StreamConnectionNotifier notifier;

	/** Keeps the information about this server. */
	private ServiceRecord record;

	/** Keeps the parent reference to process specific actions. */
	private MobisnMIDlet parent;

	/** Becomes 'true' when this component is finalized. */
	private boolean isClosed;

	/** Creates notifier and accepts clients to be processed. */
	private Thread accepterThread;

	/** Process the particular client from queue. */
	private ClientProcessor processor;

	/** Optimization: keeps the table of data elements to be published. */
	private final Hashtable dataElements = new Hashtable();

	private boolean isBTReady;

	/**
	 * Constructs the bluetooth server, but it is initialized in the different
	 * thread to "avoid dead lock".
	 * @throws Exception 
	 */
	BTMobiServer(MobisnMIDlet parent) throws Exception {
		this.parent = parent;
		this.myProfile = parent.getProfile();
		isBTReady = false;
		init();
		// we have to initialize a system in different thread...
	}
	public boolean startThread(){
		try {
			accepterThread = new Thread(this);
			accepterThread.start();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void init() throws Exception {
			// create/get a local device
			localDevice = LocalDevice.getLocalDevice();

			// set we are discoverable
			if (!localDevice.setDiscoverable(DiscoveryAgent.GIAC)) {
				// Some implementations always return false, even if
				// setDiscoverable successful
				// throw new IOException("Can't set discoverable mode...");
			}

			// prepare a URL to create a notifier
			StringBuffer url = new StringBuffer("btspp://");

			// indicate this is a server
			url.append("localhost").append(':');

			// add the UUID to identify this service
			url.append(MOBISN_SERVER_UUID.toString());

			// add the name for our service
			url.append(";name=MobiSN Server");

			// request all of the client not to be authorized
			// some devices fail on authorize=true
			url.append(";authorize=false");

			// create notifier now
			notifier = (StreamConnectionNotifier) Connector
					.open(url.toString());

			// and remember the service record for the later updates
			record = localDevice.getRecord(notifier);

			// create a special attribute with images names
			DataElement base = new DataElement(DataElement.DATSEQ);
			record.setAttributeValue(IMAGES_NAMES_ATTRIBUTE_ID, base);

			// remember we've reached this point.
			isBTReady = true;

	}

	/**
	 * Accepts a new client and send him/her a requested image.
	 */
	public void run() {
		
		// nothing to do if no bluetooth available
		if (!isBTReady) {
			return;
		}

		// ok, start processor now
		processor = new ClientProcessor();

		// ok, start accepting connections then
		while (!isClosed) {
			StreamConnection conn = null;

			try {
				conn = notifier.acceptAndOpen();
			} catch (IOException e) {
				// wrong client or interrupted - continue anyway
				continue;
			}

			processor.addConnection(conn);
		}
	}

	/**
	 * Reads the image name from the specified connection and sends this image
	 * through this connection, then close it after all.
	 */
	private void processConnection(StreamConnection conn) {

		/*
		 * // read the image name first String imgName = readImageName(conn);
		 * 
		 * // check this image is published and get the image file name imgName
		 * = parent.getImageFileName(imgName);
		 * 
		 * // load image data into buffer to be send byte[] imgData =
		 * getImageData(imgName);
		 * 
		 * // send image data now sendImageData(imgData, conn); // supposed to
		 * be sendProfileOntology
		 * 
		 * // close connection and good-bye try { conn.close(); } catch
		 * (IOException e) { } // ignore
		 */
	}

	/**
	 * Organizes the queue of clients to be processed, processes the clients one
	 * by one until destroyed.
	 */
	private class ClientProcessor implements Runnable {
		private Thread processorThread;
		private Vector queue = new Vector();

		ClientProcessor() {
			processorThread = new Thread(this);
			processorThread.start();
		}

		public void run() {
			while (!isClosed) {
				// wait for new task to be processed
				synchronized (this) {
					if (queue.size() == 0) {
						try {
							wait();
						} catch (InterruptedException e) {
							System.err.println("Unexpected exception: " + e);
							destroy(false);

							return;
						}
					}
				}

				// send the image to specified connection
				StreamConnection conn;

				synchronized (this) {
					// may be awaked by "destroy" method.
					if (isClosed) {
						return;
					}

					conn = (StreamConnection) queue.firstElement();
					queue.removeElementAt(0);
					processConnection(conn);
				}
			}
		}

		/** Adds the connection to queue and notifies the thread. */
		void addConnection(StreamConnection conn) {
			synchronized (this) {
				queue.addElement(conn);
				notify();
			}
		}

		/** Closes the connections and . */
		void destroy(boolean needJoin) {
			StreamConnection conn;

			synchronized (this) {
				notify();

				while (queue.size() != 0) {
					conn = (StreamConnection) queue.firstElement();
					queue.removeElementAt(0);

					try {
						conn.close();
					} catch (IOException e) {
					} // ignore
				}
			}

			// wait until dispatching thread is done
			try {
				processorThread.join();
			} catch (InterruptedException e) {
			} // ignore
		}
	}

	public boolean publishProfile(boolean isPublished) {
		DataElement de;
		if (isPublished) {
			// put profiles summery in serviceRecord
			de = myProfile.getServiceRecordElement();
		} else {
			de = new DataElement(DataElement.DATSEQ);
		}

		record.setAttributeValue(IMAGES_NAMES_ATTRIBUTE_ID, de);

		try {
			localDevice.updateRecord(record);
		} catch (ServiceRegistrationException e) {
			System.err.println("Can't update profile on serviceRecord");

			return false;
		}
		return true;
	}
	/**
     * Destroy a work with bluetooth - exits the accepting
     * thread and close notifier.
     */
    void destroy() {
        isClosed = true;

        // finalize notifier work
        if (notifier != null) {
            try {
                notifier.close();
            } catch (IOException e) {
            } // ignore
        }

        // wait for acceptor thread is done
        try {
            accepterThread.join();
        } catch (InterruptedException e) {
        } // ignore

        // finalize processor
        if (processor != null) {
            processor.destroy(true);
        }

        processor = null;
    }
}

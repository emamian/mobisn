package edu.ndsu.cs.mobisn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

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
	private GUIMobiServer parent;

	/** Becomes 'true' when this component is finalized. */
	private boolean isClosed;

	/** Creates notifier and accepts clients to be processed. */
	private Thread accepterThread;

	/** Process the particular client from queue. */
	private ClientProcessor processor;

	private boolean isBTReady;

	private boolean profileOnline = false;

	public boolean isProfileOnline() {
		return profileOnline;
	}

	/**
	 * Constructs the bluetooth server, but it is initialized in the different
	 * thread to "avoid dead lock".
	 * 
	 * @throws Exception
	 */
	BTMobiServer(GUIMobiServer parent) throws Exception {
		this.parent = parent;
		this.myProfile = parent.getProfile();
		isBTReady = false;

		// create/get a local device
		localDevice = LocalDevice.getLocalDevice();

		// set we are discoverable
		if (!localDevice.setDiscoverable(DiscoveryAgent.GIAC)) {
			// Some implementations always return false, even if
			// setDiscoverable successful
			throw new IOException("Can't set discoverable mode...");
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
		notifier = (StreamConnectionNotifier) Connector.open(url.toString());

		// and remember the service record for the later updates
		record = localDevice.getRecord(notifier);

		// create a special attribute with images names
		DataElement base = new DataElement(DataElement.DATSEQ);
		record.setAttributeValue(IMAGES_NAMES_ATTRIBUTE_ID, base);

		// remember we've reached this point.
		isBTReady = true;
		System.out.println("BTServer initialized");

		// we have to initialize a system in different thread...
		accepterThread = new Thread(this);
		accepterThread.start();
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
				System.out.println("new conneccion: " + conn.toString());
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
		System.out.println("process connection");
		InputStream in = null;

		try {
			in = conn.openInputStream();

			int cmd = in.read(); // 'name' length is 1 byte
			switch (cmd) {
			case 1: // send profile image
				System.out.println("server command 1: send image");
				sendMyProfileImage(conn);
				break;
			case 2: // get SMS
				System.out.println("server command 2: receive sms");
				String sms = receiveSMS(in);
				parent.receivedNewSMS(sms, conn);
				break;
			default:

			}
		} catch (IOException e) {
			System.err.println("error in accepting command ");
			System.err.println(e);
			e.printStackTrace();
		}

		// close input stream anyway
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
			} // ignore
		}
		// close input stream anyway
		if (conn != null) {
			try {
				conn.close();
			} catch (IOException e) {
			} // ignore
		}
	}

	private String receiveSMS(InputStream in) {
		System.out.println("receiving sms ... ");
		String SMS = null;

		try {

			int length = in.read(); // 'name' length is 1 byte
			System.out.println("sms lengh: " + length);
			if (length <= 0) {
				throw new IOException("Can't read name length");
			}

			byte[] nameData = new byte[length];
			length = 0;

			while (length != nameData.length) {
				int n = in.read(nameData, length, nameData.length - length);

				if (n == -1) {
					throw new IOException("Can't read name data");
				}

				length += n;
			}

			SMS = new String(nameData);
			System.out.println("sms received : " + SMS);
		} catch (IOException e) {
			System.err.println(e);
		}
		return SMS;
	}

	private void sendMyProfileImage(StreamConnection conn) {
		System.out.println("sending profile image");
		byte[] imgData = getImageData(parent.getProfile().getImagePath());
		System.out.println("imagepath: "+parent.getProfile().getImagePath()+" imagedata length : "+imgData.length);
		sendImageData(imgData, conn);

	}

	/** Send image data. */
	private void sendImageData(byte[] imgData, StreamConnection conn) {
		if (imgData == null) {
			return;
		}

		OutputStream out = null;

		try {
			out = conn.openOutputStream();
			out.write(imgData.length >> 8);
			out.write(imgData.length & 0xff);
			out.write(imgData);
			out.flush();
		} catch (IOException e) {
			System.err.println("Can't send image data: " + e);
		}

		// close output stream anyway
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
			} // ignore
		}
	}

	/** Reads images data from MIDlet archive to array. */
	private byte[] getImageData(String imgName) {
		if (imgName == null) {
			return null;
		}

		InputStream in = getClass().getResourceAsStream(imgName);

		// read image data and create a byte array
		byte[] buff = new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);

		try {
			while (true) {
				int length = in.read(buff);

				if (length == -1) {
					break;
				}

				baos.write(buff, 0, length);
			}
		} catch (IOException e) {
			System.err.println("Can't get image data: imgName=" + imgName
					+ " :" + e);

			return null;
		}

		return baos.toByteArray();
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
			if(isPublished)
				System.out.println("profile online: "+myProfile.getFamily());
			else
				System.out.println("profile offline: "+myProfile.getFamily());
				
			profileOnline  = isPublished;
		} catch (ServiceRegistrationException e) {
			System.err.println("Can't update profile on serviceRecord");
			return false;
		}
		return true;
	}

	/**
	 * Destroy a work with bluetooth - exits the accepting thread and close
	 * notifier.
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

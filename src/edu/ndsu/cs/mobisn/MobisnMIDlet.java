package edu.ndsu.cs.mobisn;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;

public class MobisnMIDlet extends MIDlet implements CommandListener {

	private RoutingTable routingTable = new RoutingTable();
	/** The messages are shown in this demo this amount of time. */
	static final int ALERT_TIMEOUT = 2000;

	/** Soft button for exiting the demo. */
	private final Command EXIT_CMD = new Command("Exit", Command.EXIT, 2);

	/** Soft button for launching a client or sever. */
	private final Command OK_CMD = new Command("Ok", Command.SCREEN, 1);

	/** A list of menu items */
	private static final String[] elements = { "System Management",
			"Group Management", "Profile Management", "Interests", "messages" };

	private static final String[] elementsImgs = { "System Management.jpg",
			"Group Management.jpg", "Profile Management.jpg", "", "" };

	/** A menu list instance */
	private final List menu = new List("MobiSN Demo", List.IMPLICIT, elements,
			null);

	/** A GUI part of client that receives image from client */
	private GUIMobiClient mobiClient = null;

	/** A GUI part of server that publishes images. */
	private GUIMobiServer mobiServer = null;

	private Profile profile;
	private GUIProfile myProfileScreen;
	private GUIInterests interestsScreen;
	private GUIMessages inboxScreen;

	private Form loadingForm = new Form("Loading MobiSN");

	private Hashtable base = new Hashtable();
	private Hashtable messages = new Hashtable();

	private BTDiscoveryClient discoveryClient = null;

	private boolean inMainMenu = false;
	private String profileRecord = "profileData";	
	private String[] profileArray = {"","",""};

	// private static final Logger logger = Logger.getLogger("BTMobiClient");
	/**
	 * Constructs main screen of the MIDlet.
	 */
	public MobisnMIDlet() {
		// BasicConfigurator.configure();
		// logger.info("salam");
	}

	/**
	 * Responds to commands issued on "client or server" form.
	 * 
	 * @param c
	 *            command object source of action
	 * @param d
	 *            screen object containing the item action was performed on
	 */
	public void commandAction(Command c, Displayable d) {
		if (c == EXIT_CMD) {
			destroyApp(true);
			notifyDestroyed();

			return;
		}

		switch (menu.getSelectedIndex()) {
		case 0:
			mobiServer.show();
			break;

		case 1:
			mobiClient.show();
			break;
		case 2:
			myProfileScreen.show();
			break;

		case 3:
			interestsScreen.show();
			break;

		case 4:
			inboxScreen.show();
			break;

		default:
			System.err.println("Unexpected choice...");
			break;
		}

	}

	/**
	 * Destroys the application.
	 */
	protected void destroyApp(boolean unconditional) {
		if (mobiServer != null) {
			mobiServer.destroy();
		}

		if (mobiClient != null) {
			mobiClient.destroy();
		}
		if (discoveryClient != null)
			discoveryClient.destroy();
	}

	/**
	 * Returns the displayable object of this screen - it is required for Alert
	 * construction for the error cases.
	 */
	Displayable getDisplayable() {
		return menu;
	}

	public Profile getProfile() {
		return profile;
	}

	/**
	 * Does nothing. Redefinition is required by MIDlet class.
	 */
	protected void pauseApp() {
	}

	/** Shows main menu of MIDlet on the screen. */
	void show() {
		menu.set(4, "messages (" + messages.size() + ")", null);
		Display.getDisplay(this).setCurrent(menu);
		inMainMenu = true;
	}

	/**
	 * Creates the demo view and action buttons.
	 */
	public void startApp() {
		try {
			init();
			show();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("could not start Midlet (startApp()");
		}
	}

	private void init() {
		try {

			loadingForm.addCommand(EXIT_CMD);
			loadingForm.setCommandListener(this);
			loadingForm.append("Loading...");
			
			//If can't load from disk, load random person...
			if(!this.loadProfileDataFromDisk()){
				profile = new Profile(); //random profile.
			}
			else {
				profile = new Profile(profileArray[0],profileArray[1],profileArray[2]);
			}
			try {
				mobiServer = new GUIMobiServer(this);
			} catch (Exception e) {
				System.err.println("Can't initialize bluetooth server: " + e);
				e.printStackTrace();
				showLoadErr("Can't initialize bluetooth server");
				return;
			}
			discoveryClient = new BTDiscoveryClient(this);
			try {
				mobiClient = new GUIMobiClient(this,discoveryClient);
			} catch (Exception e) {
				System.err.println("Can't initialize bluetooth client: " + e);
				e.printStackTrace();
				showLoadErr("Can't initialize bluetooth client");
				return;
			}
			// start discovery agent

			menu.addCommand(EXIT_CMD);
			menu.addCommand(OK_CMD);
			menu.setCommandListener(this);
			menu.setTitle(profile.getFullName());

			String menuImagesPrefix = "/menu/";
			for (int i = 0; i < menu.size(); i++) {
				if (elementsImgs[i] != "") {
					menu.set(i, elements[i], Image.createImage(menuImagesPrefix
							+ elementsImgs[i]));
				}
			}

			myProfileScreen = new GUIProfile(this);
			interestsScreen = new GUIInterests(this);
			inboxScreen = new GUIMessages(this);

			menu.setSelectedIndex(1, true);

		} catch (BluetoothStateException e) {
			System.err.println("could not discovery agent.");
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("could not initialize DemoMidlet.");
			e.printStackTrace();
		}

	}
	private boolean loadProfileDataFromDisk(){
		String profiledata = Profile.LoadRecord(profileRecord);
		System.out.println("Loaded profile data" + profiledata);
		String del = ":";
		int prevPos = 0;
		int pos = profiledata.indexOf(del);	//find delimiter between name and family.   
		//System.out.println("pos" + pos);
		if(profiledata != ""){	
			//if we have data, load the array...
			profileArray[0] = profiledata.substring(prevPos, pos);
			prevPos = pos+1; //set previous position increment past delimiter 
			//System.out.println("PrevPos is" + prevPos);
			pos = profiledata.indexOf(del, prevPos);	//move to the next delimiter between Family and age.
			profileArray[1] = profiledata.substring(prevPos, pos);  // Get family data
			profileArray[2] = profiledata.substring(pos+1);
			return true;
		}
		else
			return false;
	}
	private void showLoadErr(String resMsg) {
		Alert al = new Alert("Error", resMsg, null, AlertType.ERROR);
		al.setTimeout(MobisnMIDlet.ALERT_TIMEOUT);
		loadingForm.append(resMsg);
		Display.getDisplay(this).setCurrent(al, loadingForm);
	}

	public void receivedNewSMS(String sms, StreamConnection conn) {
		// TODO Auto-generated method stub
		System.out.println("received sms :" + sms);
		Date d = new Date();
		MessageWrapper mw = new MessageWrapper(sms, conn, d.toString());
		try {
			String senderDeviceID = RemoteDevice.getRemoteDevice(conn)
					.getBluetoothAddress();
			mw.setSenderDeviceID(senderDeviceID);
			// System.out.println("new message from device: " + senderDeviceID);
			// make notice of the sender in base hashtable
			if (base.containsKey(senderDeviceID)) {
				FriendWrapper fw = (FriendWrapper) base.get(senderDeviceID);
				fw.setHasMessages(true);
				String senderName = ((FriendWrapper) base.get(senderDeviceID))
						.getProfile().getFullName();
				mw.setSenderFullName(senderName);
			} else {
				System.out.println("message from unknown sender: "
						+ senderDeviceID);
				// TODO: should add the device to base , also its infromation
				// (srevice
				// record info)
			}
			messages.put(senderDeviceID + d.toString(), mw);
			if (inMainMenu) {
				show();
			}
		} catch (IOException e1) {
			System.err.println("could not find senders bluetooth address");
			e1.printStackTrace();
		}
	}

	public void rePublishProfile() {
		mobiServer.rePublish();
	}

	public Hashtable getMessages() {
		return this.messages;
	}

	public void updateMessagesInfo() {
		Enumeration keys = messages.keys();

		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			MessageWrapper mw = (MessageWrapper) messages.get(key);
			if (base.containsKey(mw.getSenderDeviceID())) {
				FriendWrapper fw = (FriendWrapper) base.get(mw
						.getSenderDeviceID());
				// if we did not know we have messages from this device,
				// set that we have messages from this, so we won't delete it
				fw.setHasMessages(true);

				Profile p = (Profile) loadFromBase(mw.getSenderDeviceID())
						.getProfile();
				// set sender info for this message
				mw.setSenderFullName(p.getFullName());
			}
		}
	}

	public void informDiscoverySearchError(String string) {
		System.err.println("device discovery agent: " + string);
	}

	public void clearBase() {
		Enumeration keys = base.keys();

		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			FriendWrapper fw = (FriendWrapper) base.get(key);
			if (!fw.hasMessages()) {
				base.remove(key);
			}
			fw.setOnline(false); // temporarily set offline until a BT agent
			// update it
		}
	}

	/**
	 * @param records
	 *            a vector of 'ServiceRecord' objects from discovery agent of
	 *            BTclient
	 */
	public void updateBase(Vector records) {
		try {
			clearBase(); // clear devices that we don't have messages from
			clearRoutingTable();
			for (int i = 0; i < records.size(); i++) {
				ServiceRecord sr = (ServiceRecord) records.elementAt(i);

				// get the attribute with images names
				DataElement de = sr
						.getAttributeValue(BTMobiClient.MOBISN_PROFILE_ATTRIBUTE_ID);
				DataElement deRoutingTable = sr
						.getAttributeValue(BTMobiClient.MOBISN_RT_ATTRIBUTE_ID);

				System.out.println("input Routing Table: ------");
				if (de == null) {
					System.out.println("no elemtn in routing table");
				} else
					getDataElementString(de);
				System.out.println("input Routing Table: ------end");

				if (deRoutingTable == null) {
					System.err
							.println("Unexpected serviceRecord - missed attribute routing table");
				} else {
					routingTable.loadFromDataElement(deRoutingTable);
				}
				if (de == null) {
					System.err.println("Unexpected service - missed attribute");

					continue;
				}

				// get the images names from this attribute
				Enumeration deEnum = (Enumeration) de.getValue();

				Hashtable h = new Hashtable();
				while (deEnum.hasMoreElements()) {
					DataElement tmp = (DataElement) deEnum.nextElement();
//					if(tmp.getDataType() == DataElement.STRING)
//						System.out.println("yest");
					String name = (String) tmp.getValue();
//					System.out.println("name is : " + name);
					int idx = -1;
					try {
						idx = name.indexOf(":");
						// System.out.println("index: " + idx);

					} catch (Exception e) {
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
				Profile p = null;
				try {
					p = new Profile();
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				if (p != null && !p.loadFromHashtable(h)) {
					System.err.println("some fields are missing: " + h);
					continue;
				}
				FriendWrapper fw = new FriendWrapper(true, p, sr, (String) h
						.get("interests"));
				// if (base.containsKey(fw.getKey())) {
				// base.remove(fw.getKey());
				// }
				base.put(fw.getKey(), fw);
			}

		} catch (Exception e) {
			System.err.println("error in updating base");
			e.printStackTrace();
		}
	}

	private void clearRoutingTable() {
		routingTable.clear();
	}

	// private void updateRoutingTable(RoutingTable rt) {
	// for (int i = 1; i < RoutingTable.MAX_LEVEL; i++) {
	// Hashtable h = rt.getLevel(i);
	// routingTable.addHashLevel(i+1, h);
	// }
	// }

	public Vector getBaseOnlineKeys() {
		Enumeration keys = base.keys();
		Vector ret = new Vector();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			FriendWrapper fw = (FriendWrapper) base.get(key);
			if (fw.isOnline()) {
				ret.addElement(key);
			}
		}
		return ret;
	}

	public FriendWrapper loadFromBase(String profileKeyToLoad) {
		if (base.containsKey(profileKeyToLoad))
			return (FriendWrapper) base.get(profileKeyToLoad);
		return null;
	}

	public void changeDisplay(Displayable d) {
		Display.getDisplay(this).setCurrent(d);
		inMainMenu = false;
	}

	public void changeDisplay(Alert al, Displayable destination) {
		Display.getDisplay(this).setCurrent(al, destination);
		inMainMenu = false;
	}

	// returns routing table
	public DataElement getMyRT() {
		DataElement de = new DataElement(DataElement.DATSEQ);

		Enumeration keys = base.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			FriendWrapper fw = (FriendWrapper) base.get(key);
			if (fw.isOnline()) {
				DataElement tmp;
				tmp = new DataElement(DataElement.STRING,
						(fw.getKey() + ":" + fw.getInterests()));
				de.addElement(tmp);
			}
		}
		System.out.println("Routing Table: ------");
		if (de == null) {
			System.out.println("no elemtn in routing table");
		} else
			getDataElementString(de);
		System.out.println("Routing Table: ------end");
		return de;
	}

	public String getDataElementString(DataElement de) {
		switch (de.getDataType()) {
		case DataElement.DATSEQ:
			String ret = "{dataSeq:";
			Enumeration en = (Enumeration) de.getValue();
			while (en.hasMoreElements()) {
				DataElement d = (DataElement) en.nextElement();
				ret += getDataElementString(d) + ",";
			}
			ret += "}";
			return ret;
		case DataElement.STRING:
			return (String) de.getValue();
		default:
			return "unknowm DataElement type: " + de.toString();
		}
	}
}

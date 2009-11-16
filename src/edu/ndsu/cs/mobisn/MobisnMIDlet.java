package edu.ndsu.cs.mobisn;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;


public class MobisnMIDlet extends MIDlet implements CommandListener {
	/** The messages are shown in this demo this amount of time. */
	static final int ALERT_TIMEOUT = 2000;

	/** Soft button for exiting the demo. */
	private final Command EXIT_CMD = new Command("Exit", Command.EXIT, 2);

	/** Soft button for launching a client or sever. */
	private final Command OK_CMD = new Command("Ok", Command.SCREEN, 1);

	/** A list of menu items */
	private static final String[] elements = { "System Management", "Group Management", "Profile Management","Interests" };

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

	private Form loadingForm = new Form("Loading MobiSN");

	private Hashtable base = new Hashtable();
//	private static final Logger logger = Logger.getLogger("BTMobiClient");
	/**
	 * Constructs main screen of the MIDlet.
	 */
	public MobisnMIDlet() {
//		BasicConfigurator.configure();
//		logger.info("salam");
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
			try {
				Vector v = Interests.getVectorFromString(profile.getInterestsVectorString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			myProfileScreen.show();
			break;

		case 3:
			interestsScreen.show();
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
		Display.getDisplay(this).setCurrent(menu);
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
			
			profile = new Profile();
			try {
				mobiServer = new  GUIMobiServer(this);
			} catch (Exception e) {
				System.err.println("Can't initialize bluetooth server: " + e);
				e.printStackTrace();
				showLoadErr("Can't initialize bluetooth server");
				return;
			}
			try {
				mobiClient = new  GUIMobiClient(this);
			} catch (Exception e) {
				System.err.println("Can't initialize bluetooth client: " + e);
				e.printStackTrace();
				showLoadErr("Can't initialize bluetooth client");
				return;
			}
			menu.addCommand(EXIT_CMD);
			menu.addCommand(OK_CMD);
			menu.setCommandListener(this);
			menu.setTitle(profile.getFullName());
			myProfileScreen = new GUIProfile(this);
			interestsScreen = new GUIInterests(this);
			
			menu.setSelectedIndex(1, true);
			
		} catch (Exception e) {
			System.err.println("could not initialize DemoMidlet.");
			e.printStackTrace();
		}
		
	}

		private void showLoadErr(String resMsg) {
			Alert al = new Alert("Error", resMsg, null, AlertType.ERROR);
			al.setTimeout(MobisnMIDlet.ALERT_TIMEOUT);
			loadingForm.append(resMsg);
			Display.getDisplay(this).setCurrent(al, loadingForm);
		}

		public void receivedNewSMS(String sms, StreamConnection conn) {
			// TODO Auto-generated method stub
			System.out.println("received sms :"+sms );
			try {
				System.out.println(RemoteDevice.getRemoteDevice(conn).getBluetoothAddress());
				System.out.println(RemoteDevice.getRemoteDevice(conn).getFriendlyName(true));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		public Hashtable getBase() {
			// TODO Auto-generated method stub
			return base;
		}
}

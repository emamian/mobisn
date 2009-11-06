package edu.ndsu.cs.mobisn;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
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

	/** value is true after creating the server/client */
	private boolean isInit = false;

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

	/**
	 * Constructs main screen of the MIDlet.
	 */
	public MobisnMIDlet() {
		try {

			menu.addCommand(EXIT_CMD);
			menu.addCommand(OK_CMD);
			menu.setCommandListener(this);
			profile = new Profile();
			menu.setTitle(profile.getFullName());
			myProfileScreen = new GUIProfile(this);
			interestsScreen = new GUIInterests(this);
		} catch (Exception e) {
			System.err.println("could not initialize DemoMidlet.");
			e.printStackTrace();
		}
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
			if (mobiServer != null) {
				mobiServer.destroy();
			}
			mobiServer = new GUIMobiServer(this);

			break;

		case 1:
			if(mobiClient != null)
				mobiClient.destroy();
			mobiClient = new GUIMobiClient(this);
			break;
		case 2:
			myProfileScreen.show();
			break;

		case 3:
			interestsScreen.show();
			break;

		default:
			System.err.println("Unexpected choice...");

			break;
		}

		isInit = true;
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

			if (!isInit) {
				show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("could not start Midlet (startApp()");
		}
	}

}

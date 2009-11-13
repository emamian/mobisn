package edu.ndsu.cs.mobisn;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;

public class GUIMobiServer implements CommandListener {
	/** Keeps the help message of this demo. */
	private final String helpText = "The server is started by default.\n\n"
			+ "No profile are published initially. Change this by corresponding"
			+ " commands - the changes have an effect immediately.\n\n"
			+ "If profile is removed from the published list, it can't "
			+ "be downloaded.";

	/** This command goes to demo main screen. */
	private final Command backCommand = new Command("Back", Command.BACK, 2);

	/** Adds the selected image to the published list. */
	private final Command publishProfileCommand = new Command(
			"Publish profile", Command.SCREEN, 1);

	/** Removes the selected image from the published list. */
	private final Command removeCommand = new Command("Remove profile",
			Command.SCREEN, 1);

	/** Shows the help message. */
	private final Command helpCommand = new Command("Help", Command.HELP, 1);

	/** The list control to configure images. */
	private final List imagesList = new List("Configure Server", List.IMPLICIT);

	/** The help screen for the server. */
	private final Alert helpScreen = new Alert("Help");

	/** Keeps the parent MIDlet reference to process specific actions. */
	private MobisnMIDlet parent;

	/** This object handles the real transmission. */
	private BTMobiServer bt_server;

	/** shows profile is online and viewable by others */
	private boolean isProfilePublished = false;

	private Profile profile;

	public GUIMobiServer(MobisnMIDlet parent) throws Exception {
		super();
		this.parent = parent;
		this.profile = parent.getProfile();
		bt_server = new BTMobiServer(this);

		// prepare main screen
		imagesList.addCommand(backCommand);
		imagesList.addCommand(publishProfileCommand);
		imagesList.addCommand(removeCommand);
		imagesList.addCommand(helpCommand);
		setProfileOnlineMessage(false);
		imagesList.setCommandListener(this);

		// prepare help screen
		helpScreen.addCommand(backCommand);
		helpScreen.setTimeout(Alert.FOREVER);
		helpScreen.setString(helpText);
		helpScreen.setCommandListener(this);
		Ticker t = new Ticker("Change your online status!");
		imagesList.setTicker(t);
	}

	public void show(){
		Display.getDisplay(parent).setCurrent(imagesList);
	}
	/**
	 * Process the command event.
	 * 
	 * @param c
	 *            - the issued command.
	 * @param d
	 *            - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		if ((c == backCommand) && (d == imagesList)) {
			destroy();
			parent.show();

			return;
		}

		if ((c == backCommand) && (d == helpScreen)) {
			Display.getDisplay(parent).setCurrent(imagesList);

			return;
		}

		if (c == helpCommand) {
			Display.getDisplay(parent).setCurrent(helpScreen);

			return;
		}

		if (c == publishProfileCommand) {
			if (!bt_server.publishProfile(true)) {
				// either a bad record or SDDB is busy
				Alert al = new Alert("Error",
						"Can't update base (publish profile)", null,
						AlertType.ERROR);
				al.setTimeout(MobisnMIDlet.ALERT_TIMEOUT);
				Display.getDisplay(parent).setCurrent(al, imagesList);
				return;
			}
			setProfileOnlineMessage(true);
			isProfilePublished = true;
			return;
		}
		if (c == removeCommand) {
			if (!bt_server.publishProfile(false)) {
				// either a bad record or SDDB is busy
				Alert al = new Alert("Error",
						"Can't update base (publish profile)", null,
						AlertType.ERROR);
				al.setTimeout(MobisnMIDlet.ALERT_TIMEOUT);
				Display.getDisplay(parent).setCurrent(al, imagesList);
				return;
			}
			setProfileOnlineMessage(false);
			isProfilePublished = false;
			return;
		}

	}

	public void destroy() {
		// TODO Auto-generated method stub
		// destroy the running thread of bt_server
		// i.e. finalize the bluetooth server work
		 bt_server.destroy();
	}

	public Profile getProfile() {
		return this.profile;
	}

	private void setProfileOnlineMessage(boolean isOnline) {
		imagesList.deleteAll();
		if (isOnline) {
			imagesList.append("your profile is online!", null);
		} else {
			imagesList.append("your profile is offline!", null);
		}
	}
}

package edu.ndsu.cs.mobisn;
import javax.microedition.io.StreamConnection;
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
	private final List serverConfigurationList = new List("Configure Server", List.IMPLICIT);

	/** The help screen for the server. */
	private final Alert helpScreen = new Alert("Help");

	/** Keeps the parent MIDlet reference to process specific actions. */
	private MobisnMIDlet parent;

	/** This object handles the real transmission. */
	private BTMobiServer bt_server;


	private Profile profile;

	public GUIMobiServer(MobisnMIDlet parent) throws Exception {
		super();
		this.parent = parent;
		this.profile = parent.getProfile();
		bt_server = new BTMobiServer(this);

		// prepare main screen
		serverConfigurationList.addCommand(backCommand);
		serverConfigurationList.addCommand(helpCommand);
		serverConfigurationList.setCommandListener(this);

		publishProfile(true);

		// prepare help screen
		helpScreen.addCommand(backCommand);
		helpScreen.setTimeout(Alert.FOREVER);
		helpScreen.setString(helpText);
		helpScreen.setCommandListener(this);
		Ticker t = new Ticker("Change your online status!");
		serverConfigurationList.setTicker(t);
	}

	private void publishProfile(boolean isPublished) {
		if (!bt_server.publishProfile(isPublished)) {
			// either a bad record or SDDB is busy
			Alert al = new Alert("Error",
					"Can't update base ("+(isPublished?"publish":"remove")+" profile)", null,
					AlertType.ERROR);
			al.setTimeout(MobisnMIDlet.ALERT_TIMEOUT);
			parent.changeDisplay(al, serverConfigurationList);
			return;
		}
		if(isPublished){
			serverConfigurationList.removeCommand(publishProfileCommand);
			serverConfigurationList.addCommand(removeCommand);
		}else{
			serverConfigurationList.removeCommand(removeCommand);
			serverConfigurationList.addCommand(publishProfileCommand);
		}
		setProfileOnlineMessage(isPublished);
		System.out.println("profile is "+(isPublished?"published":"removed"));
	}

	public void show(){
		parent.changeDisplay(serverConfigurationList);
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
		if ((c == backCommand) && (d == serverConfigurationList)) {
			parent.show();
			return;
		}

		if ((c == backCommand) && (d == helpScreen)) {
			parent.changeDisplay(serverConfigurationList);

			return;
		}

		if (c == helpCommand) {
			parent.changeDisplay(helpScreen);

			return;
		}

		if (c == publishProfileCommand) {
			publishProfile(true);
			return;
		}
		if (c == removeCommand) {
			publishProfile(false);
			return;
		}

	}

	public void destroy() {
		// destroy the running thread of bt_server
		// i.e. finalize the bluetooth server work
		 bt_server.destroy();
	}

	public Profile getProfile() {
		return this.profile;
	}

	private void setProfileOnlineMessage(boolean isOnline) {
		serverConfigurationList.deleteAll();
		if (isOnline) {
			serverConfigurationList.append("your profile is online!", null);
		} else {
			serverConfigurationList.append("your profile is offline!", null);
		}
	}

	public void receivedNewSMS(String sms, StreamConnection conn) {
		// TODO Auto-generated method stub
		parent.receivedNewSMS(sms,conn);
	}

	public boolean isProfileOnline() {
		return bt_server.isProfileOnline();
	}

	public void rePublish() {
		if(bt_server.isProfileOnline()){
			bt_server.publishProfile(true);
			System.out.println("profile republished");
		}
		
	}
}

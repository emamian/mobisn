package edu.ndsu.cs.mobisn;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

public class GUIMobiClient implements CommandListener {
	/** This command goes to demo main screen. */
	private final Command SCR_MAIN_BACK_CMD = new Command("Back", Command.BACK,
			2);

	/** Starts the proper services search. */
	private final Command SCR_MAIN_SEARCH_CMD = new Command("Find", Command.OK,
			1);
	/** Cancels the device/services discovering. */
	private final Command SCR_SEARCH_CANCEL_CMD = new Command("Cancel",
			Command.BACK, 2);

	/** This command goes to client main screen. */
	private final Command SCR_PROFILES_BACK_CMD = new Command("Back",
			Command.BACK, 2);

	private final Command SCR_PROFILES_LOAD_CMD = new Command("Load",
			Command.OK, 1);

	private final Command SCR_SHOW_BACK_CMD = new Command("Back", Command.BACK,
			2);

	// send sms message.
	private final Command SCR_PROFILES_SMS_CMD = new Command("Send Text",
			Command.ITEM, 2);
	private final Command SMS_SEND_CMD = new Command("Send", Command.SCREEN, 1);
	private final Command SMS_CANCEL_CMD = new Command("Cancel", Command.BACK,
			1);

	/** The main screen of the client part. */
	private final Form mainScreen = new Form("Profile Viewer");

	/** The screen with found images names. */
	private final List listScreen = new List("Profile Viewer", List.IMPLICIT);

	/** The screen with download image. */
	private final Form friendDetailScreen = new Form("Profile Viewer");

	// sms textbox
	private TextBox smsBox = null;

	/** Keeps the parent MIDlet reference to process specific actions. */
	private MobisnMIDlet parent;

	private BTMobiClient bt_client;

	private Vector listScreenKeys = new Vector();

	GUIMobiClient(MobisnMIDlet parent) throws BluetoothStateException {
		this.parent = parent;
		mainScreen.addCommand(SCR_MAIN_BACK_CMD);
		mainScreen.addCommand(SCR_MAIN_SEARCH_CMD);
		mainScreen.setCommandListener(this);
		bt_client = new BTMobiClient(this);
		listScreen.addCommand(SCR_PROFILES_BACK_CMD);
		listScreen.addCommand(SCR_PROFILES_LOAD_CMD);
		listScreen.addCommand(SCR_PROFILES_SMS_CMD);
		listScreen.setCommandListener(this);
		friendDetailScreen.addCommand(SCR_SHOW_BACK_CMD);
		friendDetailScreen.setCommandListener(this);
		StringItem si = new StringItem("Ready for friend search!", null);
		si.setLayout(StringItem.LAYOUT_CENTER | StringItem.LAYOUT_VCENTER);
		mainScreen.append(si);

		smsBox = new TextBox("SMS", "Text message to send", 256, TextField.ANY);
		smsBox.addCommand(SMS_SEND_CMD);
		smsBox.addCommand(SMS_CANCEL_CMD);
		smsBox.setCommandListener(this);

	}

	public void show() {
		Display.getDisplay(parent).setCurrent(mainScreen);
	}

	/**
	 * Process the command events.
	 * 
	 * @param c
	 *            - the issued command.
	 * @param d
	 *            - the screen object the command was issued for.
	 */
	public void commandAction(Command c, Displayable d) {
		// back to demo main screen
		if (c == SCR_MAIN_BACK_CMD) {
			parent.show();

			return;
		}

		// starts (device/services) search
		if (c == SCR_MAIN_SEARCH_CMD) {
			Form f = new Form("Searching...");
			f.addCommand(SCR_SEARCH_CANCEL_CMD);
			f.setCommandListener(this);
			f.append(new Gauge("Searching in progress...", false,
					Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING));
			Display.getDisplay(parent).setCurrent(f);
			bt_client.requestSearch();

			return;
		}

		// cancels device/services search
		if (c == SCR_SEARCH_CANCEL_CMD) {
			bt_client.cancelSearch();
			Display.getDisplay(parent).setCurrent(mainScreen);

			return;
		}
		// back to client main screen
		if (c == SCR_PROFILES_BACK_CMD) {
			bt_client.requestLoad(null);
			Display.getDisplay(parent).setCurrent(mainScreen);

			return;
		}
		// starts image download
		if (c == SCR_PROFILES_LOAD_CMD) {
			Form f = new Form("Loading...");
			// f.addCommand(SCR_LOAD_CANCEL_CMD);
			f.setCommandListener(this);
			f.append(new Gauge("Loading Profile...", false, Gauge.INDEFINITE,
					Gauge.CONTINUOUS_RUNNING));
			Display.getDisplay(parent).setCurrent(f);

			List l = (List) d;
			try {
				String friendKey = (String) listScreenKeys.elementAt(l
						.getSelectedIndex());
				bt_client.requestLoad(friendKey);

			} catch (Exception e) {
				System.err
						.println("index of selected friend out of bound of listScreenKeys");
				e.printStackTrace();
			}

			return;
		}
		// back to client main screen
		if (c == SCR_SHOW_BACK_CMD || c == SMS_CANCEL_CMD) {
			Display.getDisplay(parent).setCurrent(listScreen);

			return;
		}
		if (c == SCR_PROFILES_SMS_CMD) {
			this.showTextSendForm(true);
			return;
		}

		if (c == SMS_SEND_CMD) {
			System.out.println("trying to send sms");
			// TODO capture the text string from the textbox.getString() and
			// send to server selected...
			Form f = new Form("Sending SMS...");
			// f.addCommand(SCR_LOAD_CANCEL_CMD);
			f.setCommandListener(this);
			f.append(new Gauge("Sending SMS  ...", false, Gauge.INDEFINITE,
					Gauge.CONTINUOUS_RUNNING));
			Display.getDisplay(parent).setCurrent(f);
			List l = listScreen;
			try {
				String friendKey = (String) listScreenKeys.elementAt(l
						.getSelectedIndex());
				String sms = smsBox.getString();
				System.out.println("sms is: " + sms);
				bt_client.sendSMS(sms, friendKey);

			} catch (Exception e) {
				System.err
						.println("sms: index of selected friend out of bound of listScreenKeys");
				e.printStackTrace();
			}
			Display.getDisplay(parent).setCurrent(listScreen);
			return;
		}
	}

	private void showTextSendForm(boolean show) {
		if (show) {
			Display.getDisplay(parent).setCurrent(smsBox);
		}
	}

	/**
	 * Informs the error during the profile search.
	 */
	void informSearchError(String resMsg) {
		showAlertGoTo(resMsg, mainScreen);
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

	/**
	 * Shows the available profile names.
	 * 
	 * @returns false if no profile names were found actually
	 */
	boolean showFriendsNames(Hashtable base) {
		Enumeration keys = base.keys();

		// no images actually
		if (!keys.hasMoreElements()) {
			informSearchError("No profiles found");

			return false;
		}

		// prepare the list to be shown
		while (listScreen.size() != 0) {
			listScreen.delete(0);
		}
		listScreenKeys.removeAllElements();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			Profile myProfile = parent.getProfile();
			String othersInterests = (String) ((FriendWrapper) base.get(key))
					.getInterests();
			double relevance;
			try {
				relevance = myProfile.getRelevance(othersInterests);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			if (relevance < -1.0)
				return false;
			Profile friend = (Profile) ((FriendWrapper) base.get(key)).getProfile();

			listScreenKeys.addElement(key);
			listScreen.append(friend.getFullName() + "(relevance:" + relevance
					+ ")", null);
		}

		if (listScreen.size() != listScreenKeys.size()) {
			System.err
					.println("list of friends' profiles in screen and keys don't match");
			return false;
		}
		Display.getDisplay(parent).setCurrent(listScreen);

		return true;
	}

	/**
	 * Informs the error during the selected image load.
	 */
	void informLoadError(String resMsg) {
		showAlertGoTo(resMsg, listScreen);
	}

	private void showAlertGoTo(String Msg, Displayable destination) {
		Alert al = new Alert("Error", Msg, null, AlertType.ERROR);
		al.setTimeout(MobisnMIDlet.ALERT_TIMEOUT);
		Display.getDisplay(parent).setCurrent(al, destination);
	}

	/**
	 * Shows the downloaded image.
	 */
	void showFriendProfile(Profile p, String pName) {
		friendDetailScreen.deleteAll();
		p.showIn(friendDetailScreen);
		Display.getDisplay(parent).setCurrent(friendDetailScreen);

	}

	public Hashtable getBase() {
		return parent.getBase();
	}
}

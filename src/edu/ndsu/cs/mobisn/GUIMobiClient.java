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
	private final Command SCR_IMAGES_BACK_CMD = new Command("Back",
			Command.BACK, 2);

	private final Command SCR_IMAGES_LOAD_CMD = new Command("Load", Command.OK,
			1);
	// send sms message.
	private final Command SCR_IMAGES_SMS_CMD = new Command("Send Text",
			Command.ITEM, 2);

	private final Command SCR_SHOW_BACK_CMD = new Command("Back", Command.BACK,
			2);
	private final Command SMS_SEND_CMD = new Command("Send", Command.SCREEN, 1);
	private final Command SMS_CANCEL_CMD = new Command("Cancel", Command.BACK,
			1);

	/** The main screen of the client part. */
	private final Form mainScreen = new Form("Profile Viewer");

	/** The screen with found images names. */
	private final List listScreen = new List("Profile Viewer", List.IMPLICIT);

	/** The screen with download image. */
	private final Form imageScreen = new Form("Profile Viewer");
	private Form smsForm = null;
	TextBox textbox;

	/** Keeps the parent MIDlet reference to process specific actions. */
	private MobisnMIDlet parent;

	private BTMobiClient bt_client;

	GUIMobiClient(MobisnMIDlet parent) throws BluetoothStateException {
		this.parent = parent;
		mainScreen.addCommand(SCR_MAIN_BACK_CMD);
		mainScreen.addCommand(SCR_MAIN_SEARCH_CMD);
		mainScreen.setCommandListener(this);
		bt_client = new BTMobiClient(this);
		listScreen.addCommand(SCR_IMAGES_BACK_CMD);
		listScreen.addCommand(SCR_IMAGES_LOAD_CMD);
		listScreen.addCommand(SCR_IMAGES_SMS_CMD);
		listScreen.setCommandListener(this);
		imageScreen.addCommand(SCR_SHOW_BACK_CMD);
		imageScreen.setCommandListener(this);
		StringItem si = new StringItem("Ready for friend search!", null);
		si.setLayout(StringItem.LAYOUT_CENTER | StringItem.LAYOUT_VCENTER);
		mainScreen.append(si);
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
			destroy();
			parent.show();

			return;
		}

		// starts images (device/services) search
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
		if (c == SCR_IMAGES_BACK_CMD) {
			bt_client.requestLoad(null);
			Display.getDisplay(parent).setCurrent(mainScreen);

			return;
		}
		// starts image download
		if (c == SCR_IMAGES_LOAD_CMD) {
			Form f = new Form("Loading...");
			// f.addCommand(SCR_LOAD_CANCEL_CMD);
			f.setCommandListener(this);
			f.append(new Gauge("Loading Profile...", false, Gauge.INDEFINITE,
					Gauge.CONTINUOUS_RUNNING));
			Display.getDisplay(parent).setCurrent(f);

			List l = (List) d;
			bt_client.requestLoad(l.getString(l.getSelectedIndex()));

			return;
		}
		// back to client main screen
		if (c == SCR_SHOW_BACK_CMD || c == SMS_CANCEL_CMD) {
			Display.getDisplay(parent).setCurrent(listScreen);

			return;
		}
		if (c == SCR_IMAGES_SMS_CMD) {
			this.showTextSendForm(true);
		}
		if (c == SMS_SEND_CMD) {
			// TODO capture the text string from the textbox.getString() and
			// send to server selected...

			this.informSearchError("Text Message Sent");
		}
	}

	private void showTextSendForm(boolean show) {

		if (textbox == null) {
			textbox = new TextBox("SMS", "Text message to send", 256,
					TextField.ANY);
		}
		textbox.addCommand(SMS_SEND_CMD);
		textbox.addCommand(SMS_CANCEL_CMD);
		textbox.setCommandListener(this);

		// profileScreen.setCommandListener(this);
		if (show) {
			Display.getDisplay(parent).setCurrent(textbox);
		}
	}

	/**
	 * Informs the error during the images search.
	 */
	void informSearchError(String resMsg) {
		Alert al = new Alert("Error", resMsg, null, AlertType.ERROR);
		al.setTimeout(MobisnMIDlet.ALERT_TIMEOUT);
		Display.getDisplay(parent).setCurrent(al, mainScreen);
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

	/**
	 * Shows the available images names.
	 * 
	 * @returns false if no images names were found actually
	 */
	boolean showImagesNames(Hashtable base) {
		Enumeration keys = base.keys();

		// no images actually
		if (!keys.hasMoreElements()) {
			informSearchError("No profile names in found services");

			return false;
		}

		// prepare the list to be shown
		while (listScreen.size() != 0) {
			listScreen.delete(0);
		}

		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			Profile myProfile = parent.getProfile();
			String othersInterests = (String) ((Vector) base.get(key))
					.elementAt(2);
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
			listScreen.append(key + "(relevance:" + relevance + ")", null);
		}

		Display.getDisplay(parent).setCurrent(listScreen);

		return true;
	}

	/**
	 * Informs the error during the selected image load.
	 */
	void informLoadError(String resMsg) {
		Alert al = new Alert("Error", resMsg, null, AlertType.ERROR);
		al.setTimeout(MobisnMIDlet.ALERT_TIMEOUT);
		Display.getDisplay(parent).setCurrent(al, listScreen);
	}

	/**
	 * Shows the downloaded image.
	 */
	void showProfile(Profile p, String pName) {
		imageScreen.deleteAll();
		p.showIn(imageScreen);
		Display.getDisplay(parent).setCurrent(imageScreen);

	}
}

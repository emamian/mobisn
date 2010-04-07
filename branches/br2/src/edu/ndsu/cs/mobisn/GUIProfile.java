package edu.ndsu.cs.mobisn;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

public class GUIProfile implements CommandListener, ItemCommandListener {
	MobisnMIDlet parent;
	Profile profile;
	private final Command PROFILE_BACK_CMD = new Command("Back", Command.BACK, 1);
	private final Command ITEM_EDIT_CMD = new Command("Edit", Command.ITEM, 1);
	private final Command EDIT_DONE_CMD = new Command("Done", Command.SCREEN, 1);
	private final Command EDIT_CANCEL_CMD = new Command("Cancel", Command.BACK,
			1);
	Form profileScreen = null;
	private Vector items = new Vector();
	int currentEdit = -1;
	TextBox textbox;
	private String profileRecord = "profileData";

	public GUIProfile(MobisnMIDlet parent) {
		super();
		this.parent = parent;
		this.profile = parent.getProfile();
	}

	public void commandAction(Command c, Displayable d) {
		if (c == PROFILE_BACK_CMD) {
			parent.show();
			return;
		}
		if (c == EDIT_DONE_CMD) {
			if(setProfileProp(textbox.getString())){
				parent.rePublishProfile();
				this.saveProfileToDisk();
				System.out.println("set profile done");
			}
			else
				System.out.println("could not set profile");
			show();
			return;
		}
		if (c == EDIT_CANCEL_CMD) {
			show();
			return;
		}
	}

	private void saveProfileToDisk() {
		Profile.SaveRecord(profileRecord, this.profile.getID()+":"+this.profile.getImagePath());
		System.out.println("Saved Profile Record");
	}

	public void show() {
		if (profileScreen == null) {
			profileScreen = new Form("my profile");
			profileScreen.addCommand(PROFILE_BACK_CMD);
			profileScreen.setCommandListener(this);
		}
		profileScreen.deleteAll();
		loadProfileData();
		saveProfileToDisk(); //For a new person, save the record so we have it on disk right away.
		for (int i = 0; i < items.size(); i++) {
			Item item = (Item) items.elementAt(i);
			profileScreen.append(item);
		}
		parent.changeDisplay(profileScreen);
	}

	private void loadProfileData() {
		items.removeAllElements();
		Item item;
		item = new ImageItem("Image: ", profile.getImage(),ImageItem.LAYOUT_DEFAULT,profile.getName());
		items.insertElementAt(item, 0);
		item = new StringItem("Name: ", profile.getName());
		items.insertElementAt(item, 1);
		item = new StringItem("Family: ", profile.getFamily());
		items.insertElementAt(item, 2);
		item = new StringItem("Age: ", profile.getAge());
		items.insertElementAt(item, 3);
		//load from disk, if blank, load default.
		String interestData = profile.loadStringInterests();
		if(interestData == "")
		{
			//load default.
			interestData = profile.getInterestsVectorString();
		}
		//item = new StringItem("interests: ", profile.getInterestsVectorString());
		
//		item = new StringItem("interests: ", interestData);
//		items.insertElementAt(item, 4);
		
		for (int i = 0; i < items.size(); i++) {
			Item t = (Item) items.elementAt(i);
			t.addCommand(ITEM_EDIT_CMD);
			t.setItemCommandListener(this);
		}
	}

	public void commandAction(Command c, Item item) {
		if (c == ITEM_EDIT_CMD) {
			try {
				int idx = items.indexOf(item);
				if(idx==0) // image 
					return;
				currentEdit = idx;
				showTextBox(item, idx);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void showTextBox(Item item, int i) {
		textbox = new TextBox(item.getLabel(), getProfileProp(i), 256,
				TextField.ANY);
		textbox.addCommand(EDIT_DONE_CMD);
		textbox.addCommand(EDIT_CANCEL_CMD);
		textbox.setCommandListener(this);

		profileScreen.setCommandListener(this);
		parent.changeDisplay(textbox);

	}

	private String getProfileProp(int i) {
		switch (i) {
		case 0:
			return "image";
		case 1:
			return profile.getName();
		case 2:
			return profile.getFamily();
		case 3:
			return profile.getAge();

		default:
			return "unknown";
		}
	}

	private boolean setProfileProp(String s) {
		System.out.println(s);
		switch (currentEdit) {
		case 1:
			profile.setName(s);
			return true;
		case 2:
			profile.setFamily(s);
			return true;
		case 3:
			profile.setAge(s);
			return true;

		default:
			return false;
		}
	}
}

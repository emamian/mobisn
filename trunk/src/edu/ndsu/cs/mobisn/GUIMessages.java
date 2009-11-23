package edu.ndsu.cs.mobisn;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;

public class GUIMessages implements CommandListener{

	MobisnMIDlet parent;
	List list = new List("Messages", List.EXCLUSIVE);
	Form messageForm = new Form("Message");
	private Vector listKeys = new Vector();
	private Hashtable messages;
	
	private final Command LIST_BACK_CMD = new Command("Back",
			Command.BACK, 2);
	private final Command MSG_BACK_CMD = new Command("Back",
			Command.BACK, 2);

	private final Command LIST_LOAD_CMD = new Command("Load",
			Command.OK, 1);
	private final Command MSG_REPLY_CMD = new Command("Reply",
			Command.OK, 1);

	
	public GUIMessages(MobisnMIDlet parent) {
		super();
		this.parent = parent;
		list.addCommand(LIST_BACK_CMD);
		list.addCommand(LIST_LOAD_CMD);
		list.setCommandListener(this);
		
		messageForm.addCommand(MSG_BACK_CMD);
		messageForm.addCommand(MSG_REPLY_CMD);
		messageForm.setCommandListener(this);
		
	}

	public void commandAction(Command c, Displayable d) {
		if(c == LIST_BACK_CMD){
			parent.show();
		return;
		} 
		if(c == LIST_LOAD_CMD){
			showMessage(list.getSelectedIndex());
			return;
		}
		if(c == MSG_BACK_CMD){
			show();
			return;
		}
		if(c == MSG_REPLY_CMD){
			System.out.println("should show reply forms");
			return;
		}
			
	}

	private void showMessage(int selectedIndex) {
		if(selectedIndex>listKeys.size() || selectedIndex<0){
			System.out.println("message index out of bound");
			show();
			return;
		}
		MessageWrapper mw = (MessageWrapper)messages.get(listKeys.elementAt(selectedIndex));
		messageForm.deleteAll();
		messageForm.append(new StringItem("sender: ", mw.getSenderFullName()));
		messageForm.append(new StringItem("time: ", mw.getTime()));
		messageForm.append(new StringItem("body: ", mw.getSms()));
		parent.changeDisplay(messageForm);
		
	}

	public void show(){
		list.deleteAll();
		listKeys.removeAllElements();
		parent.updateMessagesInfo();
		Hashtable h = parent.getMessages();
		messages = h;
		Enumeration keys = h.keys();
		
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			MessageWrapper mw = (MessageWrapper) h.get(key);
			listKeys.addElement(key);
			
			String title = mw.getSenderFullName()+" ("+mw.getTime()+")";
			list.append(title, null);
		}
		parent.changeDisplay(list);
	}
}

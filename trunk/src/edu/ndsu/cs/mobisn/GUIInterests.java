package edu.ndsu.cs.mobisn;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

public class GUIInterests implements CommandListener {
	private MobisnMIDlet parent;
	private NodeData interest;
	private List lst = null;
	private Image[] checked = { Image.createImage("/uncheck.jpg"),Image.createImage("/check.jpg")};

	/** This command goes to client main screen. */
	private final Command INTRST_BACK_CMD = new Command("Back", Command.BACK, 2);
	private final Command INTRST_DETAIL_CMD = new Command("detail", Command.ITEM, 1);

	public GUIInterests(MobisnMIDlet parent) throws Exception {
		super();
		if (parent == null)
			throw new Exception("parent for GUIInterest is null");
		this.parent = parent;
		if (parent.getProfile() == null)
			throw new Exception("profile for GUIInterest is null");
		this.interest = parent.getProfile().getRootInterest();
	}

	public void commandAction(Command c, Displayable d) {
		// level up in interest tree or go to main screen
		if (c == INTRST_BACK_CMD) {
			NodeData f = interest.getFather();
			if (f == null) {
				parent.show();
				return;
			}
			this.interest = f;
			show();
			return;
		} else if (c == INTRST_DETAIL_CMD) {
			int idx = lst.getSelectedIndex();
			if(idx <0 || idx >= interest.getChilds().size()){
				showAlert("index out of range");
				return;
			}
			NodeData nd = (NodeData) interest.getChilds().elementAt(idx);
			if(nd.getChilds().size()<=0){
				showAlert(nd.getTitle()+" has no detail");
				return;
			}
			interest = nd;
			show();
			return;
		}
		// change the selectin of interest in profile
		if (c == List.SELECT_COMMAND) {
			int idx = lst.getSelectedIndex();
			try {
				NodeData nd = (NodeData) interest.getChilds().elementAt(idx);
				nd.setInterested(!nd.isInterested());
				lst.set(idx, lst.getString(idx), checked[(nd.isInterested()?1:0)]);
			} catch (Exception e) {
				System.err.println("somthing wrong with selected index:"+idx + "in interest "+interest.getTitle());
				e.printStackTrace();
			}
			return;
		}
		System.out.println("unknown command");
	}

	private void showAlert(String msg) {
			Alert al = new Alert("Error", msg, null, AlertType.ERROR);
			al.setTimeout(MobisnMIDlet.ALERT_TIMEOUT);
			Display.getDisplay(parent).setCurrent(al, lst);
	}

	boolean setInterest(NodeData node) {
		if (node == null)
			return false;
		this.interest = node;
		return true;
	}

	public void show() {

		/*
		 * if(frm == null){ frm = new Form(interest.getTitle());
		 * frm.addCommand(INTRST_BACK_CMD); } frm.deleteAll();
		 * 
		 * if (chGroup == null) { chGroup = new ChoiceGroup("",
		 * ChoiceGroup.MULTIPLE); chGroup.addCommand(INTRST_VIEW_CMD); }
		 * chGroup.deleteAll(); chGroup.setLabel(interest.getTitle()); int size
		 * = interest.getChilds().size(); Vector childs = interest.getChilds();
		 * boolean[] selectedFlags = new boolean[size]; for (int i = 0; i <
		 * size; i++) { NodeData ch = (NodeData) childs.elementAt(i);
		 * chGroup.append(ch.getTitle(), null);
		 * selectedFlags[i]=ch.isInterested(); }
		 * chGroup.setSelectedFlags(selectedFlags);
		 * chGroup.setItemCommandListener(this); frm.append(chGroup);
		 * frm.setItemStateListener(this);
		 * Display.getDisplay(parent).setCurrent(frm);
		 */
		if (lst == null) {
			lst = new List(interest.getTitle(), List.IMPLICIT);
			lst.addCommand(INTRST_BACK_CMD);
			lst.addCommand(INTRST_DETAIL_CMD);
			lst.setCommandListener(this);
		}
		lst.deleteAll();
		lst.setTitle(interest.getTitle());
		int size = interest.getChilds().size();
		Vector childs = interest.getChilds();
		for (int i = 0; i < size; i++) {
			NodeData ch = (NodeData) childs.elementAt(i);
			lst.append(ch.getTitle(), checked[(ch.isInterested()?1:0)]);
		}
		Display.getDisplay(parent).setCurrent(lst);
	}

	// public void itemStateChanged(Item item) {
	// // TODO Auto-generated method stub
	// if (item == chGroup) {
	// // int idx = chGroup.getSelectedIndex();
	// boolean[] flags = new boolean[chGroup.size()];
	// chGroup.getSelectedFlags(flags);
	// Vector childs = interest.getChilds();
	// for (int i = 0; i < flags.length; i++) {
	// ((NodeData) childs.elementAt(i)).setInterested(flags[i]);
	// System.out
	// .println(((NodeData) childs.elementAt(i)).getTitle()
	// + (((NodeData) childs.elementAt(i))
	// .isInterested() ? " is selected"
	// : " is not selected"));
	// }
	// return;
	// }
	// }

}

package edu.ndsu.cs.mobisn;

import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Screen;

public class MainMenu implements CommandListener {

	/** A list of menu items */
	private static final String[] elements = { "System Management",
			"Group Management", "Profile Management", "Interests", "Messages" };
	private static final String[] elementsImgs = { "System Management.png",
			"Group Management.png", "Profile Management.png", "Interests.png",
			"Messages.png" };
	private static boolean USE_GRAPHICAL_MENU = true;

	/** Soft button for exiting the demo. */
	private final Command EXIT_CMD = new Command("Exit", Command.EXIT, 2);
	/** Soft button for launching a client or sever. */
	private final Command OK_CMD = new Command("Ok", Command.SCREEN, 1);


	private Form loadingForm = new Form("Loading MobiSN");
	private boolean isGraphicMenu = true;
	private List menu;
	private MenuCanvas canvas;

	private MobisnMIDlet parent;
	private int selectedIndex = -1;
	private int inboxCount = 0;
	private Image images[];

	public MainMenu(MobisnMIDlet parent) {
		this.parent = parent;
		loadingForm.addCommand(EXIT_CMD);
		loadingForm.setCommandListener(this);
		loadingForm.append("Loading...");
		images = new Image[elements.length];
	}

	// whether using graphical menu (true) or simple menu (false)
	public static boolean checkDimension(Displayable d) {
		// TODO
		return USE_GRAPHICAL_MENU;
		// if (d.getWidth() < 100 || d.getHeight() < 120)
		// return false;
		// return true;
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
			parent.destroyApp(true);
			parent.notifyDestroyed();

			return;
		}
		if (c == OK_CMD) {

			// will be moved to menu and canvas themselves
			if (!isGraphicMenu)
				parent.menuCommand(menu.getSelectedIndex());
			else {
				parent.menuCommand(canvas.getSelectedIndex());
			}
			// -----
		}
	}

	public void finishInit() throws IOException {
		this.isGraphicMenu = checkDimension(Display.getDisplay(parent)
				.getCurrent());
		String menuImagesPrefix = "/menu/";
		if (!isGraphicMenu) {
			for (int i = 0; i < images.length; i++) {
				if (elementsImgs[i] != "") {
					System.out.println("image "+i+" created");
					images[i] = Image.createImage(menuImagesPrefix
							+ elementsImgs[i]);
				} else {
					System.out.println("image "+i+" not created");
					images[i] = null;
				}
			}
			menu = new List("MobiSN Demo", List.IMPLICIT, elements, images);
			menu.addCommand(EXIT_CMD);
			menu.addCommand(OK_CMD);
			menu.setCommandListener(this);
			menu.setTitle(parent.getProfile().getFullName());
			// for (int i = 0; i < menu.size(); i++) {
			// if (elementsImgs[i] != "") {
			// menu.set(i, elements[i], Image.createImage(menuImagesPrefix
			// + elementsImgs[i]));
			// }
			// }
		} else {
			canvas = new MenuCanvas(parent, elements, elementsImgs);
			canvas.addCommand(EXIT_CMD);
			canvas.addCommand(OK_CMD);
			canvas.setCommandListener(this);
			canvas.setTitle(parent.getProfile().getFullName());
		}

		setSelected(4);
		// show();
	}

	private void show() {
		if (!isGraphicMenu) {
			System.out.println("showing regular menu");
			if (inboxCount == 0)
				menu.set(4, "messages", images[4]);
			else
				menu.set(4, "messages (" + inboxCount + ")", images[4]);
			Display.getDisplay(parent).setCurrent(menu);
		} else {
			System.out.println("showing graphic menu");
			canvas.setInboxNumber(inboxCount);
			canvas.show();
		}
	}

	public void show(int messagesSize) {
		inboxCount = messagesSize;
		show();
	}

	private void setSelected(int selected) {
		this.selectedIndex = selected;
		if (!isGraphicMenu) {
			menu.setSelectedIndex(selected, true);
		} else
			canvas.setSelected(selectedIndex);
	}

	public void showLoadErr(String resMsg) {
		Alert al = new Alert("Error", resMsg, null, AlertType.ERROR);
		al.setTimeout(MobisnMIDlet.ALERT_TIMEOUT);
		loadingForm.append(resMsg);
		// Display.getDisplay(parent).setCurrent(loadingForm);
		System.err.println(resMsg);
		Display.getDisplay(parent).setCurrent(al, loadingForm);
	}
}

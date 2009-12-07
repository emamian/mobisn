package edu.ndsu.cs.mobisn;

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class MenuCanvas extends Canvas {
	private static int UNIT_WIDTH = 64;
	private static int UNIT_HEIGHT = 64;
	private static int SPACING = 12;
	private static int TITLE_AREA_HEIGHT = 30;
	private static int BORDER = 5;

	private MobisnMIDlet parent;
	private int selectedIndex = -1;
	private int inboxCount = 0;
	private int width = 0;
	private int height = 0;
	Image[] images;
	private String[] elements;
	private Font headerFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD,
			Font.SIZE_MEDIUM);

	public MenuCanvas(MobisnMIDlet parent, String[] elems, String[] elementsImgs)
			throws IOException {
		super();
		this.parent = parent;
		this.images = new Image[elems.length];
		this.elements = elems;
		int size = elems.length;
		String menuImagesPrefix = "/graphicMenu/";
		for (int i = 0; i < size; i++) {
			// if (elementsImgs[i] != "") {
			images[i] = Image.createImage(menuImagesPrefix + (i + 1) + ".png");
			// } else
			// images[i] = Image.createImage(menuImagesPrefix + "na.jpg");
		}
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelected(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	public void setInboxNumber(int messagesSize) {
		inboxCount = messagesSize;
	}

	protected void paint(Graphics g) {
		try {

//			System.out.println("canvas paint");
			Displayable d = Display.getDisplay(parent).getCurrent();
			setWH(d.getWidth(), d.getHeight());
//			System.out.println("crawing area " + width + ":" + height);
			g.setColor(255, 255, 255); // background
			g.fillRect(0, 0, width, height);
			g.setColor(180, 122, 122);
			// g.drawString("salam", 50, 50, Graphics.TOP | Graphics.LEFT);
			drawMenu(g);
		} catch (Exception e) {
			System.err.println("drawing graphic menu error");
			e.printStackTrace();
		}
	}

	protected void keyPressed(int key) {
		// TODO Auto-generated method stub
		int keyCode = getGameAction(key);
//		System.out.println("key pressed " + keyCode);
		switch (keyCode) {
		case Canvas.DOWN:
		case Canvas.RIGHT:
			if (++selectedIndex >= images.length)
				selectedIndex = 0;
			break;
		case Canvas.UP:
		case Canvas.LEFT:
			if (--selectedIndex < 0)
				selectedIndex = images.length - 1;
			break;
		case Canvas.FIRE:
			parent.menuCommand(selectedIndex);
			break;

		default:
			System.err.println("unknown key pressed");
			break;
		}
		repaint();
		super.keyPressed(keyCode);
	}

	public void show() {
		// TODO Auto-generated method stub
		Display.getDisplay(parent).setCurrent(this);
		this.repaint();

	}

	private void drawMenu(Graphics g) {
		// TODO Auto-generated method stub
		if (g == null)
			System.err.println("g is null");
		int size = images.length;
		// SPACING
		int x = SPACING, y = SPACING + TITLE_AREA_HEIGHT;
		// System.out.println("dimension:" + width + ":" + height + " length:" +
		// images.length);
		for (int i = 0; i < size; i++) {
			// System.out.println("drawing " + i + ": " + x + ":" + y);
			// highlight selected index
			if (i == selectedIndex) {
				g.setColor(0);
				g.setFont(headerFont);
				String s = elements[i];
				if (s == "Messages")
					s += " (" + inboxCount + ")";
				g.drawString(s, width / 2, 5, Graphics.TOP | Graphics.HCENTER);
				g.setColor(220, 172, 172);
				int b = BORDER;
				g.fillRoundRect(x - b, y - b, UNIT_WIDTH + b + b, UNIT_HEIGHT
						+ b + b, 10, 10);
			}
			if (images[i] == null)
				System.err.println("image " + i + " is null");
			else {
				g.drawImage(images[i], x, y, Graphics.TOP | Graphics.LEFT);

			}

			x += UNIT_WIDTH + SPACING;
			if (x + UNIT_WIDTH + SPACING > width) {
				x = SPACING;
				y += UNIT_HEIGHT + SPACING;
			}
		}

	}

	// set width and height
	public void setWH(int w, int h) {
		this.width = w;
		this.height = h;
//		System.out.println("dimensition set to " + width + ":" + height);
	}

}

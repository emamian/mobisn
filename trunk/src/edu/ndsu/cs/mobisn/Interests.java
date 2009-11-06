package edu.ndsu.cs.mobisn;

import java.util.Vector;

public class Interests {
	private NodeData root;

	Interests() {

		root = new NodeData("Interests");
		NodeData ch;
		ch = new NodeData("music");
		ch.addChild(new NodeData("jazz"));
		ch.addChild(new NodeData("pop"));
		ch.addChild(new NodeData("rock"));
		ch.addChild(new NodeData("metal"));
		root.addChild(ch);

		ch = new NodeData("sports");
		ch.addChild(new NodeData("basketball"));
		ch.addChild(new NodeData("football"));
		ch.addChild(new NodeData("swimming"));
		ch.setInterested(true);
		root.addChild(ch);

		ch = new NodeData("art");
		ch.addChild(new NodeData("drawing"));
		ch.addChild(new NodeData("theatre"));
		root.addChild(ch);

		setAllDepths();
	}

	public String getVectorString() {
		Vector v = new Vector();
		String s = "";
		v.addElement(root);
		while (!v.isEmpty()) {
			NodeData g = (NodeData) v.firstElement();
			v.removeElementAt(0);
			// s +=
			// g.getTitle()+":"+((g.isInterested())?String.valueOf(1.0/(double)g.getDepth()):"0");//g.name;
			s += ((g.isInterested()) ? String.valueOf(1.0 / (double) g
					.getDepth()) : "0");// g.name;
			for (int i = 0; i < g.getChilds().size(); i++) {
				v.addElement(g.getChilds().elementAt(i));
			}
			if (!v.isEmpty())
				s += ",";
		}
		return s;
	}

	public Vector getVector() {
		Vector v = new Vector();
		Vector ret = new Vector();
		v.addElement(root);
		while (!v.isEmpty()) {
			NodeData g = (NodeData) v.firstElement();
			ret.addElement(((g.isInterested()) ? String
					.valueOf(1.0 / (double) g.getDepth()) : "0"));
			v.removeElementAt(0);

			for (int i = 0; i < g.getChilds().size(); i++) {
				v.addElement(g.getChilds().elementAt(i));
			}
		}
		return ret;
	}

	public void setAllDepths() {
		root.setDepth(0);
	}

	public NodeData getRoot() {
		return root;
	}

	public static Vector getVectorFromString(String s) throws Exception {
		Vector v = new Vector();
		int pos = 0;
		int size = s.length();
		System.out.println("s is " + s);
		if (size == 0 || s == "")
			throw new Exception();
		int comma = s.indexOf(",", pos);
		while (comma != -1) {
			System.out.println("adding" + s.substring(pos, comma));
			v.addElement(s.substring(pos, comma));
			pos = comma + 1;
			comma = s.indexOf(",", pos);
			System.out.println("pos:" + pos + " comma" + comma);
		}
		if (!s.substring(pos).equals("")) {
			v.addElement(s.substring(pos));
		}
		System.out.println("the end");
		System.out.println("vector size is " + v.size());
		return v;
	}
}

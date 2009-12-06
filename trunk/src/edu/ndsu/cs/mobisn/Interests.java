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
		ch.setInterested(true); // it is jst random
		root.addChild(ch);

		ch = new NodeData("art");
		ch.addChild(new NodeData("drawing"));
		ch.addChild(new NodeData("theatre"));
		root.addChild(ch);

		setAllDepths();
	}

	//for loading from file
	public boolean loadFromString(String s) {
		Vector v = new Vector();
		v.addElement(root);
		int index = 0;
		int len = s.length();
		while (!v.isEmpty()) {
			if (index >= len)
				return false;
			NodeData g = (NodeData) v.firstElement();
			v.removeElementAt(0);
			if (s.charAt(index) == '1') {
				g.setInterested(true);
			} else if (s.charAt(index) == '0') {
				g.setInterested(false);
			} else {
				System.err.println("unknown character in interest string");
				return false;
			}

			index++;

			for (int i = 0; i < g.getChilds().size(); i++) {
				v.addElement(g.getChilds().elementAt(i));
			}
		}
		return true;
	}

	public String getInterestsString() {
		Vector v = new Vector();
		String s = "";
		v.addElement(root);
		while (!v.isEmpty()) {
			NodeData g = (NodeData) v.firstElement();
			v.removeElementAt(0);
			// s +=
			// g.getTitle()+":"+((g.isInterested())?String.valueOf(1.0/(double)g.getDepth()):"0");//g.name;
			// s += ((g.isInterested()) ? String.valueOf(1.0 / (double) g
			// .getDepth()) : "0");// g.name;
			s += (g.isInterested() ? "1" : "0");
			for (int i = 0; i < g.getChilds().size(); i++) {
				v.addElement(g.getChilds().elementAt(i));
			}
			// if (!v.isEmpty())
			// s += ",";
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
					.valueOf(1.0 / (double) g.getDepth()) : "0.0"));
			v.removeElementAt(0);

			for (int i = 0; i < g.getChilds().size(); i++) {
				v.addElement(g.getChilds().elementAt(i));
			}
		}
		return ret;
	}

	public void setAllDepths() {
		root.setDepth(1);
	}

	public NodeData getRoot() {
		return root;
	}

	// public static Vector getVectorFromString(String s) throws Exception {
	// Vector v = new Vector();
	// int pos = 0;
	// int size = s.length();
	// if (size == 0 || s == "")
	// throw new Exception();
	// int comma = s.indexOf(",", pos);
	// while (comma != -1) {
	// v.addElement(s.substring(pos, comma));
	// pos = comma + 1;
	// comma = s.indexOf(",", pos);
	// }
	// if (!s.substring(pos).equals("")) {
	// v.addElement(s.substring(pos));
	// }
	// return v;
	// }

	public Vector getVectorFromString(String s) {
		int index = 0;
		Vector v = new Vector();
		Vector ret = new Vector();
		v.addElement(root);
		while (!v.isEmpty() || index < s.length()) {
			NodeData g = (NodeData) v.firstElement();
			if (s.charAt(index) == '1') {
				ret.addElement(String.valueOf(1.0 / (double) g.getDepth()));
			} else if (s.charAt(index) == '0') {
				ret.addElement("0.0");
			} else
				System.err.println("unknown character in interest string");

			index++;
			v.removeElementAt(0);

			for (int i = 0; i < g.getChilds().size(); i++) {
				v.addElement(g.getChilds().elementAt(i));
			}
		}
		// System.out.println(ret.toString());
		return ret;
	}

	public static double getRelevance(Vector v1, Vector v2) {
		return cosineAngle(v1, v2);
	}

	private static double cosineAngle(Vector v1, Vector v2) {
		double nominator = 0.0;
		// System.out.println("relevalnce: "+v1.toString()+" -> "+v2.toString());

		if (v1.size() != v2.size())
			return -2.0;
		for (int i = 0; i < v1.size(); i++) {
			Double d1 = Double.valueOf(v1.elementAt(i).toString());
			Double d2 = Double.valueOf(v2.elementAt(i).toString());
			nominator += d1.doubleValue() * d2.doubleValue();
		}
		double sizes = sizeOfVector(v1) * sizeOfVector(v2);
		if (sizes == 0.0)
			return -3.0;
		return nominator / sizes;
	}

	private static double sizeOfVector(Vector v) {
		double size = 0.0;
		for (int i = 0; i < v.size(); i++) {
			double d = Double.parseDouble(v.elementAt(i).toString());
			size += d * d;

		}
		size = Math.sqrt(size);
		return size;
	}

	public void setRoot(NodeData _root) {
		this.root = _root;
	}
}

package edu.ndsu.cs.mobisn;
import java.util.Vector;

public class Interests {
	NodeData head;

	Interests(){

		NodeData head = new NodeData("head");
		NodeData ch = new NodeData("music");
		ch.addChild(new NodeData("jazz"));
		ch.addChild(new NodeData("pop"));
		ch.addChild(new NodeData("rock"));
		ch.addChild(new NodeData("metal"));
		head.addChild(ch);
		
		ch = new NodeData("sports");
		ch.addChild(new NodeData("basketball"));
		ch.addChild(new NodeData("football"));
		ch.addChild(new NodeData("swimming"));
		head.addChild(ch);
		
		ch = new NodeData("art");
		ch.addChild(new NodeData("drawing"));
		ch.addChild(new NodeData("theatre"));
		head.addChild(ch);
		
		this.head = head;
	}
	
	public String toString() {
		// TODO Auto-generated method stub
		Vector v = new Vector();
		String s = "";
		v.addElement(head);
		while (!v.isEmpty()) {
			NodeData g = (NodeData) v.firstElement();
			v.removeElementAt(0);
			s += (g.isInterested())?"1":"0";//g.name;
			for (int i = 0; i < g.getChilds().size(); i++) {
				v.addElement(g.getChilds().elementAt(i));				
			}
			if(!v.isEmpty())
				s+=",";
		}
		return s;
	}
}

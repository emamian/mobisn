package edu.ndsu.cs.mobisn;
import java.util.Vector;

public class Interests {
	private NodeData root;

	Interests(){

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
	
	public String toString() {
		Vector v = new Vector();
		String s = "";
		v.addElement(root);
		while (!v.isEmpty()) {
			NodeData g = (NodeData) v.firstElement();
			v.removeElementAt(0);
			s += g.getTitle()+":"+((g.isInterested())?String.valueOf(1.0/(double)g.getDepth()):"0");//g.name;
			for (int i = 0; i < g.getChilds().size(); i++) {
				v.addElement(g.getChilds().elementAt(i));				
			}
			if(!v.isEmpty())
				s+=",";
		}
		return s;
	}
	
	public void setAllDepths(){
		root.setDepth(0);
	}
	
	public NodeData getRoot(){
		return root;
	}
}

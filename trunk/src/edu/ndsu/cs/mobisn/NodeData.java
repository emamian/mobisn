package edu.ndsu.cs.mobisn;
import java.util.Vector;


public class NodeData {
	private String name;
	private boolean interested=false;
	private Vector childs;
	private int depth;

	public double getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
		for (int i = 0; i < childs.size(); i++) {
			NodeData ch = (NodeData) childs.elementAt(i);
			ch.setDepth(depth+1);
		}
	}
	public NodeData(String name) {
		this.name = name;
		this.childs = new Vector();
	}
	public NodeData(String name , Vector childs){
		this.name = name;
		this.childs = childs;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isInterested() {
		return interested;
	}

	public void setInterested(boolean interested) {
		this.interested = interested;
	}

	public Vector getChilds() {
		return childs;
	}

	public void setChilds(Vector childs) {
		this.childs = childs;
	}
	public void addChild(NodeData nodeData) {
		this.childs.addElement(nodeData);
	}
}
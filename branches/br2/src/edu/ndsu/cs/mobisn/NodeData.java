package edu.ndsu.cs.mobisn;

import java.util.Vector;

public class NodeData {
	private Vector childs;
	private int depth;
	private NodeData father = null;
	private boolean interested = false;

	private String title;

	public NodeData(String name) {
		this.title = name;
		this.childs = new Vector();
	}

	public NodeData(String name, Vector childs) {
		this.title = name;
		this.childs = childs;
	}

	public void addChild(NodeData nodeData) {
		this.childs.addElement(nodeData);
		nodeData.setFather(this);
	}

	public Vector getChilds() {
		return childs;
	}

	public double getDepth() {
		return depth;
	}

	public NodeData getFather() {
		return father;
	}

	public String getTitle() {
		return title;
	}

	public boolean isInterested() {
		return interested;
	}

	public void setChilds(Vector childs) {
		this.childs = childs;
	}

	public void setDepth(int depth) {
		this.depth = depth;
		for (int i = 0; i < childs.size(); i++) {
			NodeData ch = (NodeData) childs.elementAt(i);
			ch.setDepth(depth + 1);
		}
	}

	private void setFather(NodeData nodeData) {
		this.father = nodeData;

	}

	public void setInterested(boolean interested) {
		this.interested = interested;
		if(interested){
			if(father!= null){
				father.setInterested(true);
			}
		}else{
			for (int i = 0; i < childs.size(); i++) {
				NodeData ch = (NodeData) childs.elementAt(i);
				ch.setInterested(false);
			}
		}
			
	}

}
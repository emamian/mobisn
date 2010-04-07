package edu.ndsu.cs.mobisn;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.bluetooth.DataElement;

public class RoutingTable {

	// maximum number of hops to record the routing table
	// here level 1 is a node with 2 hop distance
	// hop = level +1;
	// 1 means only my neighbours (base hashtable)
	public static final int MAX_LEVEL = 1;
	private Vector table = new Vector();
	private String delimiter = "$";

	public RoutingTable() {
		super();
		for (int i = 0; i <= MAX_LEVEL; i++) {
			table.addElement(new Hashtable());
		}
	}

	public void add(int level, RoutingElement r) {
		if (!levelOK(level))
			return;
		// don't add if it is in lower levels
		for (int i = 1; i < level; i++) {
			Hashtable h = (Hashtable) table.elementAt(i);
			if(h == null)
				continue;
			if (h.containsKey(r.getId()))
				return;
		}
		Hashtable row = (Hashtable) table.elementAt(level);
		if (row == null) {
			row = new Hashtable();
			table.insertElementAt(row, level);
		}
		row.put(r.getId(), r);

		// remove old ones if it is in higher levels
		for (int i = level + 1; i <= MAX_LEVEL; i++) {
			Hashtable h = (Hashtable) table.elementAt(i);
			if (h == null)
				continue;
			if (h.containsKey(r.getId()))
				h.remove(r.getId());
		}
	}

	private boolean levelOK(int level) {
		if (level < 1 || level > MAX_LEVEL)
			return true;
		return false;
	}

	public Hashtable getLevel(int level) {
		if (!levelOK(level)) {
			return null;
		}
		if(table.elementAt(level) == null)
			table.insertElementAt(new Hashtable(), level);
		return (Hashtable) table.elementAt(level);
	}

	public void clear() {
		for (int i = 1; i <= MAX_LEVEL; i++) {
			Hashtable h = (Hashtable) table.elementAt(i);
			if (h != null)
				h.clear();
		}
	}

	public void addHashLevel(int level, Hashtable h) {
		// add each item to the level
		if (!levelOK(level))
			return;
		if (h.isEmpty())
			return;
		Enumeration en = h.elements();
		while (en.hasMoreElements()) {
			RoutingElement rte = (RoutingElement) en.nextElement();
			add(level, rte);
		}
	}

	public void show() {
		// level 1 is the base hashtable and might not be shown if called with
		// current programs routing table
		System.out.println("routing table:----------");
		for (int i = 1; i <= MAX_LEVEL; i++) {
			Hashtable h = getLevel(i);
			if (h != null) {
				String s = "";
				Enumeration en = h.elements();
				while (en.hasMoreElements()) {
					RoutingElement rte = (RoutingElement) en.nextElement();
					s += rte.getId() + ":" + rte.getInterests() + ",";
				}
				System.out.println(i + ":" + s);
			} else
				System.out.println(i + ": empty");
		}
		System.out.println("routing table:----------end");
	}

	public DataElement getDataElement() {
		DataElement ret = new DataElement(DataElement.DATSEQ);
		for (int i = 1; i <= MAX_LEVEL; i++) {
			Hashtable h = getLevel(i);
			if(h == null){
//				System.err.println("h was null "+i);
				continue;
			}
			Enumeration en = h.elements();
			DataElement deLevel = new DataElement(DataElement.DATSEQ);
			while (en.hasMoreElements()) {
				RoutingElement rte = (RoutingElement) en.nextElement();
				DataElement de = new DataElement(DataElement.STRING, rte
						.getId()
						+ delimiter + rte.getInterests());
				deLevel.addElement(de);
			}
			ret.addElement(deLevel);
		}
		return ret;
	}

	public void loadFromDataElement(DataElement rt) {
		if (rt == null) {
			System.err
					.println("Unexpected service - missed attribute routing table");
			return;
		}

		// get the images names from this attribute
		Enumeration deEnum = (Enumeration) rt.getValue();

		// iterate through each level of routing table
		while (deEnum.hasMoreElements()) {
			DataElement de = (DataElement) deEnum.nextElement();
			if (de.getDataType() != DataElement.DATSEQ) {
				System.err
						.println("unexpected dataelement in routing table data element");
				continue;
			}
			Hashtable h = new Hashtable();
			Enumeration en = (Enumeration) de.getValue();

			// starting from 2 because it is neighbors routing table
			int level = 2;

			// iterate through each row in specified level
			while (en.hasMoreElements() && level < MAX_LEVEL) {
				DataElement row = (DataElement) en.nextElement();
				String name = (String) row.getValue();
				int idx = -1;
				try {
					idx = name.indexOf(delimiter);

				} catch (Exception e) {
					System.err.println("error in routing table tag :" + name);
					e.printStackTrace();
					continue;

				}
				if (idx == -1) {
					continue;
				}
				// split tag:value parts
				String tag = name.substring(0, idx);
				String value = name.substring(idx + 1);
				// System.out.println("result->" + tag + " " + value);
				h.put(tag, value);
			}
			this.addHashLevel(level, h);
			level++;
		}
	}
}

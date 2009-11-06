package edu.ndsu.cs.mobisn;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import javax.bluetooth.DataElement;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

public class Profile{

	private String age;
	private String family;
	private String name;
	private Interests interests = new Interests();

	public Profile() {
		super();
		Random r = new Random();
		switch (r.nextInt(4)) {
		case 0:
			this.name = "Peyman";
			this.family = "Emamian" + r.nextInt(1000);
			this.age = "25";
			break;
		case 1:
			this.name = "Jen";
			this.family = "Li" + r.nextInt(1000);
			this.age = "20";
			break;
		case 2:
			this.name = "Ryan";
			this.family = "Carlson" + r.nextInt(1000);
			this.age = "21";
			break;
		case 3:
			this.name = "Chao";
			this.family = "Liu" + r.nextInt(1000);
			this.age = "24";
			break;
		default:
			this.name = "John";
			this.family = "Smith" + r.nextInt(1000);
			this.age = "29";
		}
	}

	public Profile(String name, String family, String age) {
		super();
		this.name = name;
		this.family = family;
		this.age = age;
	}

	public String getAge() {
		return age;
	}

	public String getFamily() {
		return family;
	}

	public String getFullName() {
		return name + " " + family;
	}

	public String getName() {
		return name;
	}

	public DataElement getServiceRecordElement() {
		DataElement base = new DataElement(DataElement.DATSEQ);

		DataElement de;
		de = new DataElement(DataElement.STRING, "name:" + name);
		base.addElement(de);
		de = new DataElement(DataElement.STRING, "family:" + family);
		base.addElement(de);
		de = new DataElement(DataElement.STRING, "age:" + age);
		base.addElement(de);
		de = new DataElement(DataElement.STRING, "interests:" + getInterestsVectorString());
		base.addElement(de);

		return base;
	}

	// should correspond to getServiceRecordElement() function
	public boolean loadFromHashtable(Hashtable h) {

		if (!h.containsKey("name"))
			return false;
		this.name = (String) h.get("name");

		if (!h.containsKey("family"))
			return false;
		this.family = (String) h.get("family");

		if (!h.containsKey("age"))
			return false;
		this.age = (String) h.get("age");
		
		return true;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void showIn(Form f) {
		if (f == null) {
			f = new Form("Profile View");
		}
		f.deleteAll();
		Item[] items = new Item[3];
		// items[0] = new ImageItem(null,
		// imageManager.getJpg(myProfile.getPic()),
		// ImageItem.LAYOUT_DEFAULT, null);
		items[0] = new StringItem("name:", name);
		f.append(items[0]);
		items[1] = new StringItem("family:", family);
		f.append(items[1]);
		items[2] = new StringItem("age", age);
		f.append(items[2]);
	}

	public String getInterestsVectorString() {
		return interests.getVectorString();
	}

	public NodeData getRootInterest(){
		return interests.getRoot();
	}
	
	public double getRelevance(String interestsVectorString) throws Exception{
		Vector v = Interests.getVectorFromString(interestsVectorString);
		Vector myVector = interests.getVector();
		return cosineAngle(myVector, v);
	}
	private double cosineAngle(Vector v1 , Vector v2){
		double nominator = 0.0;
		System.out.println("relevalnce: "+v1.toString()+" -> "+v2.toString());
		
		if(v1.size() != v2.size())
			return -2.0;
		for (int i = 0; i < v1.size(); i++) {
			Double d1 = Double.valueOf(v1.elementAt(i).toString());
			Double d2 = Double.valueOf(v2.elementAt(i).toString());
			nominator += d1.doubleValue()*d2.doubleValue();
			System.out.println(d1+"*"+d2+"->"+nominator);
		}
		double sizes = sizeOfVector(v1)*sizeOfVector(v2);
		if(sizes == 0.0)
			return -3.0;
		return nominator/sizes;
	}
	private double sizeOfVector(Vector v){
		double size = 0.0;
		for (int i = 0; i < v.size(); i++) {
			double d = Double.parseDouble(v.elementAt(i).toString());
			size += d*d;
			
		}
		size = Math.sqrt(size);
		return size;
	}
}

package edu.ndsu.cs.mobisn;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import javax.bluetooth.DataElement;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

public class Profile {

	private String age;
	private String family;
	private Image image = null;
	private String imagePath = "";
	private Interests interests = new Interests();
	private String name;

	public Profile() throws IOException {
		super();
		Random r = new Random();
		switch (r.nextInt(4)) {
		case 0:
			this.name = "Peyman";
			this.family = "Emamian" + r.nextInt(1000);
			this.age = "25";
			this.imagePath = "/profiles/peyman.jpg";
			break;
		case 1:
			this.name = "Jen";
			this.family = "Li" + r.nextInt(1000);
			this.age = "20";
			this.imagePath = "/profiles/jen.jpg";
			break;
		case 2:
			this.name = "Ryan";
			this.family = "Carlson" + r.nextInt(1000);
			this.age = "21";
			this.imagePath = "/profiles/na.jpg";
			break;
		case 3:
			this.name = "Chao";
			this.family = "Liu" + r.nextInt(1000);
			this.age = "24";
			this.imagePath = "/profiles/chao.jpg";
			break;
		default:
			this.name = "John";
			this.family = "Smith" + r.nextInt(1000);
			this.age = "29";
			this.imagePath = "/profiles/na.jpg";
		}
		this.image = Image.createImage(imagePath);
	}

	public Profile(String name, String family, String age) {
		super();
		this.name = name;
		this.family = family;
		this.age = age;
	}

	private double cosineAngle(Vector v1, Vector v2) {
		double nominator = 0.0;
		// System.out.println("relevalnce: "+v1.toString()+" -> "+v2.toString());

		if (v1.size() != v2.size())
			return -2.0;
		for (int i = 0; i < v1.size(); i++) {
			Double d1 = Double.valueOf(v1.elementAt(i).toString());
			Double d2 = Double.valueOf(v2.elementAt(i).toString());
			nominator += d1.doubleValue() * d2.doubleValue();
			// System.out.println(d1+"*"+d2+"->"+nominator);
		}
		double sizes = sizeOfVector(v1) * sizeOfVector(v2);
		if (sizes == 0.0)
			return -3.0;
		return nominator / sizes;
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

	public String getID() {
		String d = ":";
		return name + d + family + d + age;
	}

	public Image getImage() {
		return image;
	}

	public String getImagePath() {
		return imagePath;
	}

	public String getInterestsVectorString() {
		return interests.getVectorString();
	}

	public String getName() {
		return name;
	}

	public double getRelevance(String interestsVectorString) throws Exception {
		Vector v = Interests.getVectorFromString(interestsVectorString);
		Vector myVector = interests.getVector();
		return cosineAngle(myVector, v);
	}

	public NodeData getRootInterest() {
		return interests.getRoot();
	}

	public DataElement getServiceRecordElement() {
		DataElement ret = new DataElement(DataElement.DATSEQ);

		DataElement de;
		de = new DataElement(DataElement.STRING, "name:" + name);
		ret.addElement(de);
		de = new DataElement(DataElement.STRING, "family:" + family);
		ret.addElement(de);
		de = new DataElement(DataElement.STRING, "age:" + age);
		ret.addElement(de);
		de = new DataElement(DataElement.STRING, "interests:"
				+ getInterestsVectorString());
		ret.addElement(de);

		return ret;
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

	public void setImage(Image image) {
		this.image = image;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void showIn(Form f) {
		if (f == null) {
			f = new Form("Profile View");
		}
		f.deleteAll();
		Vector v = new Vector();
		v.addElement(new ImageItem("iamge:", image,ImageItem.LAYOUT_DEFAULT,"image"));
		v.addElement( new StringItem("name:", name));
		v.addElement(new StringItem("family:", family));
		v.addElement(new StringItem("age", age));
		
		for (int i = 0; i < v.size(); i++) {
			f.append((Item)v.elementAt(i));
		}
	}

	private double sizeOfVector(Vector v) {
		double size = 0.0;
		for (int i = 0; i < v.size(); i++) {
			double d = Double.parseDouble(v.elementAt(i).toString());
			size += d * d;

		}
		size = Math.sqrt(size);
		return size;
	}
}

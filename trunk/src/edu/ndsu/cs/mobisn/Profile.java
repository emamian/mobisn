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
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

public class Profile {

	private String age;
	private String family;
	private Image image = null;
	private String imagePath = "";
	private Interests interests = new Interests();	
	private String name;
	private String recordName = "InterestsData";
	private int RecordID = 0;

	public Profile() throws IOException {
		super();
		Random r = new Random();
		switch (r.nextInt(4)) {
		case 0:
			this.name = "Peyman";
			this.family = "Emamian " + r.nextInt(1000);
			this.age = "25";
			this.imagePath = "/profiles/peyman.jpg";
			break;
		case 1:
			this.name = "Jen";
			this.family = "Li " + r.nextInt(1000);
			this.age = "20";
			this.imagePath = "/profiles/jen.jpg";
			break;
		case 2:
			this.name = "Ryan";
			this.family = "Carlson " + r.nextInt(1000);
			this.age = "21";
			this.imagePath = "/profiles/Ryan.jpg";
			break;
		case 3:
			this.name = "Chao";
			this.family = "Liu " + r.nextInt(1000);
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

	public Profile(String name, String family, String age, String imagePath) throws IOException {
		super();
		this.name = name;
		this.family = family;
		this.age = age;
		this.imagePath = imagePath;
		this.image = Image.createImage(imagePath);
		this.interests.loadFromString(this.loadStringInterests());
	}

	public void saveStringInterests()
	{
		String record = interests.getInterestsString();
		try{
			//Question just delete the RecordID from last save?  Or make it simple and delete the whole recordStore...
			//TODO this isn't working now...
			//if(RecordID == 0){
				RecordStore.deleteRecordStore(recordName + name);
				System.out.println("DeletedREcordStore");
			//}
//			else {
//				RecordStore rs1 = RecordStore.openRecordStore(recordName + name, true);
//				rs1.deleteRecord(RecordID);
//				rs1.closeRecordStore();				
//			}
		}
		catch(RecordStoreNotFoundException rse){
			//swallow it.
			System.out.println("RS not found yet.");
			//e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		//after cleaning up old recordStore.
		try
		{
			RecordStore rs = RecordStore.openRecordStore(recordName + name, true);
			RecordID = rs.addRecord(record.getBytes(), 0, record.getBytes().length); // Add Record
			
			rs.closeRecordStore();
			System.out.println("Save interests succeeded for Record"+recordName + name);

		}
		catch (Exception e)
		{
			System.out.println("Error in saving record" + e.getMessage());
		}
		
	}
	
	public String loadStringInterests() {
		String record = "1000000000000";
		RecordStore rs = null;
		try
		{
			//RecordStore 
			rs = RecordStore.openRecordStore(recordName + name, false);
			byte[] bb = rs.getRecord(1);
			
			record = new String(bb,0, bb.length);
			//record = new NodeData()
			System.out.println("Profile record loaded from disk:" + recordName + name);
			rs.closeRecordStore();
			System.out.println("Closed recordStore");			
			rs = null;
		}
		catch(RecordStoreNotFoundException rse){
			//swallow it.
			System.out.println("RS not found yet.");
			//e.printStackTrace();
		}
		catch(RecordStoreNotOpenException rsno){
			//don't care
			System.out.println("RS not open when trying close");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return record;
		}
				
		return record;
	}

	public static String LoadRecord(String recordName){ //throws RecordStoreNotFoundException, RecordStoreException{
		String ret = "";
		RecordStore rs = null;
		try {
			rs = RecordStore.openRecordStore(recordName, false);
			byte[] bb = rs.getRecord(1);
			ret = new String(bb,0,bb.length);
			rs.closeRecordStore();
			rs = null;
		}
		catch(RecordStoreNotFoundException rse){
			System.err.println("recode : "+recordName+" not found");
//			rse.printStackTrace();
		}
		catch(RecordStoreException e){
			e.printStackTrace();
		}
		return ret;
	}
	public static void SaveRecord(String recordName, String recordData){
		int lRecordID = 0;
		try{
			RecordStore.deleteRecordStore(recordName);
			System.out.println("DeletedRecordStore" + recordName);
		}
		catch(RecordStoreNotFoundException rse){
			//swallow it.
			System.out.println("RS not found yet.");
			//e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		try
		{
			RecordStore rs = RecordStore.openRecordStore(recordName, true);
			lRecordID = rs.addRecord(recordData.getBytes(), 0, recordData.getBytes().length); // Add Record
			
			rs.closeRecordStore();
			System.out.println("Save Succeeded for Record"+recordName + " RecID: " + lRecordID);

		}
		catch (Exception e)
		{
			System.out.println("Error in saving record" + e.getMessage());
		}
		
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
		return interests.getInterestsString();
	}
	public Vector getInterestsVectorFromString(String s){
		return interests.getVectorFromString(s);
	}

	public String getName() {
		return name;
	}

	public double getRelevance(String interestsVectorString) throws Exception {
		Vector v = interests.getVectorFromString(interestsVectorString);
		Vector myVector = interests.getVector();
		return Interests.getRelevance(myVector, v);
	}

	public NodeData getRootInterest() {
		return interests.getRoot();
	}

	public void setRootInterests(NodeData root){
		interests.setRoot(root);
		return;
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
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
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
}

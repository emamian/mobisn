package edu.ndsu.cs.mobisn;

import javax.microedition.io.StreamConnection;

public class MessageWrapper {

	String sms="";
	StreamConnection conn=null;
	String time = "";
	String senderDeviceID = "";
	public String getSenderDeviceID() {
		return senderDeviceID;
	}
	public void setSenderDeviceID(String senderDeviceID) {
		this.senderDeviceID = senderDeviceID;
	}
	String senderFullName = "name not available";
	public MessageWrapper(String sms, StreamConnection conn, String time) {
		super();
		this.sms = sms;
		this.conn = conn;
		this.time = time;
	}
	public String getSenderFullName() {
		return senderFullName;
	}
	public void setSenderFullName(String senderFullName) {
		this.senderFullName = senderFullName;
	}
	public String getSms() {
		return sms;
	}
	public StreamConnection getConn() {
		return conn;
	}
	public String getTime() {
		return time;
	}
	
}

package edu.ndsu.cs.mobisn;

import javax.bluetooth.ServiceRecord;

public class FriendWrapper {

	private boolean online=false;
	private Profile profile ;
	private ServiceRecord serviceRecord;
	private String interests;
	private boolean messages = false;
	
	public FriendWrapper(boolean isOnline, Profile p, ServiceRecord sr,
			String interests) {
		super();
		this.online = isOnline;
		this.profile = p;
		this.serviceRecord = sr;
		this.interests = interests;
	}
	public String getKey(){
		return serviceRecord.getHostDevice().getBluetoothAddress();
	}
	public boolean isOnline() {
		return online;
	}
	public Profile getProfile() {
		return profile;
	}
	public ServiceRecord getServiceRecord() {
		return serviceRecord;
	}
	public String getInterests() {
		return interests;
	}
	public void setOnline(boolean online) {
		this.online = online;
	}
	public void setHasMessages(boolean messages) {
		this.messages = messages;
	}
	public boolean hasMessages() {
		return messages;
	}
	
}

package edu.ndsu.cs.mobisn;

public class RoutingElement {

	private String id;
	private String interests;
	public RoutingElement(String id, String interests) {
		super();
		this.id = id;
		this.interests = interests;
	}
	public String getId() {
		return id;
	}
	public String getInterests() {
		return interests;
	}
	
}

package it.osm.gtfs.model;

import java.util.HashSet;
import java.util.Set;

public class Route {
	private String id;
	private String shortName;
	private String longName;
	private Set<String> shapesIDs;
	
	public Route(String id, String shortName, String longName) {
		super();
		this.id = id;
		this.shortName = shortName;
		this.longName = longName;
		shapesIDs = new HashSet<String>();
	}

	public String getId() {
		return id;
	}

	public String getShortName() {
		return shortName;
	}

	public String getLongName() {
		return longName;
	}

	public Set<String> getShapesIDs() {
		return shapesIDs;
	}
	
	public void putShape(String id){
		shapesIDs.add(id);
	}
}

package it.osm.gtfs.model;

public class Relation extends StopsList{
	public String name;
	public Integer version;
	
	public Relation(String id) {
		super(id);
	}
}
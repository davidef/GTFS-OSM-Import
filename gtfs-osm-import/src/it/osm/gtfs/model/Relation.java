package it.osm.gtfs.model;

import java.util.ArrayList;
import java.util.List;

public class Relation extends StopsList{
	public String name;
	public Integer version;
	public List<OSMRelationWayMember> wayMembers = new ArrayList<Relation.OSMRelationWayMember>();
	
	public Relation(String id) {
		super(id);
	}
	
	public static class OSMRelationWayMember{
		public OSMWay way;
		public Boolean backward;
	}
	
	public static class OSMWay {
		private long id;
		public List<OSMNode> nodes = new ArrayList<Relation.OSMNode>();
		public boolean oneway = false;
		
		public OSMWay(long id){
			this.id = id;
		}
		
		public long getId() {
			return id;
		}
	}
	
	public static class OSMNode {
		private Double lat;
		private Double lon;
		
		public OSMNode(Double lat, Double lon) {
			super();
			this.lat = lat;
			this.lon = lon;
		}

		public Double getLat() {
			return lat;
		}

		public Double getLon() {
			return lon;
		}
	}
}
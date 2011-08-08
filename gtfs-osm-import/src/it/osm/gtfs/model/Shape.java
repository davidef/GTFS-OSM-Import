package it.osm.gtfs.model;

import java.util.Map;
import java.util.TreeMap;

public class Shape {

	private String id;
	private Map<Long, ShapePoint> points;
	
	public Shape(String id) {
		super();
		this.id = id;
		points = new TreeMap<Long, Shape.ShapePoint>();
	}

	public void pushPoint(Long seq, Double lat, Double lon){
		points.put(seq, new ShapePoint(seq, lat, lon));
	}

	public String getId() {
		return id;
	}
	
	public String getGPX(String desc){
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\"?><gpx version=\"1.0\" creator=\"GTFS-import\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">");
		for (Long p:points.keySet()){
			buffer.append("<wpt lat=\"");
			buffer.append(points.get(p).getLat());
			buffer.append("\" lon=\"");
			buffer.append(points.get(p).getLon());
			buffer.append("\"><name>");
			buffer.append(desc);
			buffer.append("</name><desc><![CDATA[");
			buffer.append(desc);
			buffer.append("]]></desc></wpt>");
		}
		buffer.append("</gpx>");
		return buffer.toString();
	}

	public class ShapePoint {
		private Long seq;
		private Double lat;
		private Double lon;
		
		public ShapePoint(Long seq, Double lat, Double lon) {
			super();
			this.seq = seq;
			this.lat = lat;
			this.lon = lon;
		}
		
		public Long getSeq() {
			return seq;
		}
		public Double getLat() {
			return lat;
		}
		public Double getLon() {
			return lon;
		}
	}
}


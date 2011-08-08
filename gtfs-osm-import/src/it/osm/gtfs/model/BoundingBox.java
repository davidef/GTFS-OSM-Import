package it.osm.gtfs.model;

import it.osm.gtfs.output.IElementCreator;

import java.util.Collection;

import org.w3c.dom.Element;

public class BoundingBox {
	private Double minLat;
	private Double minLon;
	private Double maxLat; 
	private Double maxLon;
	
	public BoundingBox(Collection<? extends Stop> stops){
		minLat = stops.iterator().next().getLat();
		minLon = stops.iterator().next().getLon(); 
		maxLat = stops.iterator().next().getLat(); 
		maxLon = stops.iterator().next().getLon();

		for (Stop s:stops){
			minLat = Math.min(minLat, s.getLat());
			minLon = Math.min(minLon, s.getLon()); 
			maxLat = Math.max(maxLat, s.getLat()); 
			maxLon = Math.max(maxLon, s.getLon());
		}
	}

	@Override
	public String toString() {
		return "BoundingBox [minLat=" + minLat + ", minLon=" + minLon
				+ ", maxLat=" + maxLat + ", maxLon=" + maxLon + "]";
	}
	
	public String getXAPIQuery(){
		return "[bbox=" + minLon +"," + minLat + "," + maxLon +"," + maxLat + "]";
	}

	public String getXMLTag() {
		return "<bounds minlat='" + minLat + "' minlon='" + minLon + "' maxlat='" + maxLat + "' maxlon='" + maxLon + "' origin='OpenStreetMap server' />";
	}

	public Element getXMLTag(IElementCreator document) {
		Element e = document.createElement("bounds");
		e.setAttribute("minlat", minLat.toString());
		e.setAttribute("minLon", minLon.toString());
		e.setAttribute("maxlat", maxLat.toString());
		e.setAttribute("maxlon", maxLon.toString());
		return e;
	}

}

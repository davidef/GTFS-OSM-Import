package it.osm.gtfs.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class StopTimes {
	private String id;
	private Map<Long, Stop> stops;
	private Boolean valid = true;

	public StopTimes(String id) {
		super();
		this.id = id;
		stops = new TreeMap<Long, Stop>();
	}


	public Boolean isValid() {
		return valid;
	}

	public void invalidate(){
		valid = false;
	}

	public String getId() {
		return id;
	}

	public void pushPoint(Long seq, Stop stop){
		stops.put(seq, stop);
	}

	public String getRelationAsStopList(Trip t, Route r){
		StringBuffer buffer = new StringBuffer();
		for (Stop s:stops.values()){
			buffer.append(s.getCode() + " " + s.getName()  + "\n");
		}
		return buffer.toString();
	}


	public Map<Long, Stop> getStops() {
		return stops;
	}


	public boolean equalsStops(StopTimes o) {
		if (stops.size() != o.stops.size())
			return false;
		for (Long key: o.stops.keySet())
			if (!stops.get(key).equals(o.stops.get(key)))
				return false;
		return true;
	}
	
	public int getStopsAffinity(StopTimes o) {
		int affinity = 0;
		if (stops.size() == o.stops.size())
			affinity += stops.size();
		for (Stop s:stops.values())
			if (o.stops.containsValue(s))
				affinity+= stops.size() - Math.abs((getKeysByValue(stops, s) - getKeysByValue(o.stops, s)));
		
		return affinity;
	}
	
	private static <T, E> T getKeysByValue(Map<T, E> map, E value) {
	     for (Entry<T, E> entry : map.entrySet()) {
	         if (entry.getValue().equals(value)) {
	             return entry.getKey();
	         }
	     }
	     return null;
	}
	
	public static class Relation extends StopTimes{
		public String name;
		
		public Relation(String id) {
			super(id);
		}
	}
}

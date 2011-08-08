package it.osm.gtfs.plugins;

import it.osm.gtfs.model.Stop;

public interface GTFSPlugin {
	/**
	 * Apply changes to the bus stop name before generating OSM Import file
	 */
	public String fixBusStopName(String stopName);
	
	/**
	 * Allow to exclude some stops from importing
	 */
	public Boolean isValidStop(Stop s);
}

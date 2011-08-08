package it.osm.gtfs.plugins;

import it.osm.gtfs.model.Stop;

public class DefaultPlugin implements GTFSPlugin {

	@Override
	public String fixBusStopName(String busStopName) {
		return busStopName;
	}

	@Override
	public Boolean isValidStop(Stop s) {
		return true;
	}

}

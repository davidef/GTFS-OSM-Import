package it.osm.gtfs.plugins;

import it.osm.gtfs.model.Stop;

public class GTTTurinPlugin implements GTFSPlugin {
	public String fixBusStopName(String busStopName){
		busStopName = busStopName.replace('"', '\'').replaceAll("Fermata [\\d]* - ", "").replaceAll("FERMATA [\\d]* - ", "")
		.replaceAll("Fermata ST[\\d]* - ", "").replaceAll("Fermata S00[\\d]* - ", "");
		if (Character.isUpperCase(busStopName.charAt(1))){
			String[] words = busStopName.split("\\s");
			StringBuffer buffer = new StringBuffer();
			for (String s : words) {
				buffer.append(capitalize(s) + " ");
			}
			return buffer.toString();
		}
		return busStopName;
	}

	private static String capitalize(String string) {
		if (string.length() == 0) return string;
		return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
	}

	@Override
	public Boolean isValidStop(Stop gs) {
		/*try{
			Integer.parseInt(gs.getCode());
		}catch(Exception e){
			System.err.println("Warning not numeric ref: " + gs.getCode() + " " + gs.getName() + " " + gs.getGtfsId());
		}*/
		return true;
	}
}

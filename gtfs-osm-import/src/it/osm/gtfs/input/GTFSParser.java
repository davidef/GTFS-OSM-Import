package it.osm.gtfs.input;

import it.osm.gtfs.model.Route;
import it.osm.gtfs.model.Shape;
import it.osm.gtfs.model.Stop;
import it.osm.gtfs.model.StopTimes;
import it.osm.gtfs.model.Trip;
import it.osm.gtfs.model.Stop.GTFSStop;
import it.osm.gtfs.utils.GTFSImportSetting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class GTFSParser {
	public static List<GTFSStop> readBusStop(String fName) throws IOException{
		List<GTFSStop> result = new ArrayList<GTFSStop>();

		String thisLine;
		String [] elements;
		int stopIdKey=-1, stopNameKey=-1, stopCodeKey=-1, stopLatKey=-1, stopLonKey=-1;

		BufferedReader br = new BufferedReader(new FileReader(fName));
		boolean isFirstLine = true;
		Hashtable<String, Integer> keysIndex = new Hashtable<String, Integer>();
		while ((thisLine = br.readLine()) != null) { 
			if (isFirstLine) {
				isFirstLine = false;
				thisLine = thisLine.replace("\"", "");
				String[] keys = thisLine.split(",");
				for(int i=0; i<keys.length; i++){
					if(keys[i].equals("stop_id")) stopIdKey = i;
					else if(keys[i].equals("stop_name")) stopNameKey = i;
					else if(keys[i].equals("stop_lat")) stopLatKey = i;
					else if(keys[i].equals("stop_lon")) stopLonKey = i;
					else if(keys[i].equals("stop_code")) stopCodeKey = i;
					// gtfs stop_url is mapped to source_ref tag in OSM
					else if(keys[i].equals("stop_url")){
						keysIndex.put("source_ref", i);
					}
					else {
						String t = "gtfs_"+keys[i];
						keysIndex.put(t, i);
					}
				}
				//GTFS Brescia: if code isn't present we use id as code
				if (stopCodeKey == -1)
					stopCodeKey = stopIdKey;
			}
			else {
				thisLine = thisLine.trim();

				if(thisLine.contains("\"")) {
					String[] temp = thisLine.split("\"");
					for(int x=0; x<temp.length; x++){
						if(x%2==1) temp[x] = temp[x].replace(",", "");
					}
					thisLine = "";
					for(int x=0; x<temp.length; x++){
						thisLine = thisLine + temp[x];
					}
				}
				elements = thisLine.split(",");

				if (elements[stopCodeKey].length() > 0){
					GTFSStop gs = new GTFSStop(elements[stopIdKey],elements[stopCodeKey],Double.valueOf(elements[stopLatKey]),Double.valueOf(elements[stopLonKey]), elements[stopNameKey]);
					if (GTFSImportSetting.getInstance().getPlugin().isValidStop(gs)){
						result.add(gs);
					}
				}
			}
		} 
		return result;
	}

	public static List<Trip> readTrips(String fName, Map<String, StopTimes> stopTimes) throws IOException{
		List<Trip> result = new ArrayList<Trip>();

		String thisLine;
		String [] elements;
		int shape_id=-1, route_id=-1, trip_id=-1, trip_headsign=-1;

		BufferedReader br = new BufferedReader(new FileReader(fName));
		boolean isFirstLine = true;
		while ((thisLine = br.readLine()) != null) { 
			if (isFirstLine) {
				isFirstLine = false;
				thisLine = thisLine.replace("\"", "");
				String[] keys = thisLine.split(",");
				for(int i=0; i<keys.length; i++){
					if(keys[i].equals("route_id")) route_id = i;
					else if(keys[i].equals("trip_headsign")) trip_headsign = i;
					else if(keys[i].equals("shape_id")) shape_id = i;
					else if(keys[i].equals("trip_id")) trip_id = i;
				}
				//                    System.out.println(stopIdKey+","+stopNameKey+","+stopLatKey+","+stopLonKey);
			}
			else {
				thisLine = thisLine.trim();

				if(thisLine.contains("\"")) {
					String[] temp = thisLine.split("\"");
					for(int x=0; x<temp.length; x++){
						if(x%2==1) temp[x] = temp[x].replace(",", "");
					}
					thisLine = "";
					for(int x=0; x<temp.length; x++){
						thisLine = thisLine + temp[x];
					}
				}
				elements = thisLine.split(",");

				if (elements[shape_id].length() > 0){
					result.add(new Trip(elements[trip_id],elements[route_id],elements[shape_id], (trip_headsign > -1) ? elements[trip_headsign] : "", stopTimes.get(elements[trip_id])));
				}
			}
		} 
		return result;
	}

	public static Map<String, Shape> readShapes(String fName) throws IOException{
		Map<String, Shape> result = new TreeMap<String, Shape>();

		String thisLine;
		String [] elements;
		int shape_id=-1, shape_pt_lat=-1, shape_pt_lon=-1, shape_pt_sequence=-1;

		BufferedReader br = new BufferedReader(new FileReader(fName));
		boolean isFirstLine = true;
		while ((thisLine = br.readLine()) != null) { 
			if (isFirstLine) {
				isFirstLine = false;
				thisLine = thisLine.replace("\"", "");
				String[] keys = thisLine.split(",");
				for(int i=0; i<keys.length; i++){
					if(keys[i].equals("shape_id")) shape_id = i;
					else if(keys[i].equals("shape_pt_lat")) shape_pt_lat = i;
					else if(keys[i].equals("shape_pt_lon")) shape_pt_lon = i;
					else if(keys[i].equals("shape_pt_sequence")) shape_pt_sequence = i;
				}
				//                    System.out.println(stopIdKey+","+stopNameKey+","+stopLatKey+","+stopLonKey);
			}
			else {
				thisLine = thisLine.trim();

				if(thisLine.contains("\"")) {
					String[] temp = thisLine.split("\"");
					for(int x=0; x<temp.length; x++){
						if(x%2==1) temp[x] = temp[x].replace(",", "");
					}
					thisLine = "";
					for(int x=0; x<temp.length; x++){
						thisLine = thisLine + temp[x];
					}
				}
				elements = thisLine.split(",");

				if (elements[shape_id].length() > 0){
					Shape s = result.get(elements[shape_id]);
					if (s == null){
						s = new Shape(elements[shape_id]);
						result.put(elements[shape_id], s);
					}
					s.pushPoint(Long.parseLong(elements[shape_pt_sequence]), Double.parseDouble(elements[shape_pt_lat]), Double.parseDouble(elements[shape_pt_lon]));
				}
			}
		} 
		return result;
	}

	public static Map<String, Route> readRoutes(String fName) throws IOException{
		Map<String, Route> result = new HashMap<String, Route>();

		String thisLine;
		String [] elements;
		int route_id=-1, route_short_name=-1, route_long_name=-1;

		BufferedReader br = new BufferedReader(new FileReader(fName));
		boolean isFirstLine = true;
		while ((thisLine = br.readLine()) != null) { 
			if (isFirstLine) {
				isFirstLine = false;
				thisLine = thisLine.replace("\"", "");
				String[] keys = thisLine.split(",");
				for(int i=0; i<keys.length; i++){
					if(keys[i].equals("route_id")) route_id = i;
					else if(keys[i].equals("route_short_name")) route_short_name = i;
					else if(keys[i].equals("route_long_name")) route_long_name = i;
				}
			}
			else {
				thisLine = thisLine.trim();

				if(thisLine.contains("\"")) {
					String[] temp = thisLine.split("\"");
					for(int x=0; x<temp.length; x++){
						if(x%2==1) temp[x] = temp[x].replace(",", "");
					}
					thisLine = "";
					for(int x=0; x<temp.length; x++){
						thisLine = thisLine + temp[x];
					}
				}
				elements = thisLine.split(",");

				if (elements[route_id].length() > 0){
					result.put(elements[route_id], new Route(elements[route_id],elements[route_short_name],elements[route_long_name]));
				}
			}
		} 
		return result;
	}

	public static Map<String, StopTimes> readStopTimes(String fName, Map<String, Stop> osmstops) throws IOException{
		Map<String, StopTimes> result = new TreeMap<String, StopTimes>();
		Set<String> missingStops = new HashSet<String>();
		int count = 0;

		String thisLine;
		String [] elements;
		int trip_id=-1, stop_id=-1, stop_sequence=-1;

		BufferedReader br = new BufferedReader(new FileReader(fName));
		boolean isFirstLine = true;
		while ((thisLine = br.readLine()) != null) { 
			count ++;
			if (count % 100000 == 0)
				System.err.println("Stop Times Readed so far: " + count);

			if (isFirstLine) {
				isFirstLine = false;
				thisLine = thisLine.replace("\"", "");
				String[] keys = thisLine.split(",");
				for(int i=0; i<keys.length; i++){
					if(keys[i].equals("trip_id")) trip_id = i;
					else if(keys[i].equals("stop_id")) stop_id = i;
					else if(keys[i].equals("stop_sequence")) stop_sequence = i;
				}
			} else {
				thisLine = thisLine.trim();

				if(thisLine.contains("\"")) {
					String[] temp = thisLine.split("\"");
					for(int x=0; x<temp.length; x++){
						if(x%2==1) temp[x] = temp[x].replace(",", "");
					}
					thisLine = "";
					for(int x=0; x<temp.length; x++){
						thisLine = thisLine + temp[x];
					}
				}
				elements = thisLine.split(",");

				if (elements[trip_id].length() > 0){
					StopTimes s = result.get(elements[trip_id]);
					if (s == null){
						s = new StopTimes(elements[trip_id]);
						result.put(elements[trip_id], s);
					}
					
					String gtfsID = elements[stop_id];
					if (osmstops.get(gtfsID) != null){
						s.pushPoint(Long.parseLong(elements[stop_sequence]), osmstops.get(gtfsID));
					}else{
						s.invalidate();
						if (!missingStops.contains(gtfsID)){
							missingStops.add(gtfsID);
							System.err.println("Warning: No stop found with gtfs_id = " + gtfsID + ". This Trip " + elements[trip_id] + " and maybe others won't be generated !!");
						}
					}
				}
			}
		} 
		if (missingStops.size() > 0)
			System.err.println("Warning: Some stops weren't found, not all trip have been generated.");
		return result;
	}
	
	public static Map<String, List<Trip>> groupTrip(List<Trip> trips, Map<String, Route> routes, Map<String, StopTimes> stopTimes){
		Collections.sort(trips);
		Map<String, List<Trip>> result = new HashMap<String, List<Trip>>();
		for (Trip t:trips){
			Route r = routes.get(t.getRouteID());
			StopTimes s = stopTimes.get(t.getTripID());

			if (s.isValid()){
				List<Trip> set = result.get(r.getShortName());
				if (set == null){
					set = new ArrayList<Trip>();
					result.put(r.getShortName(), set);
				}
				set.add(t);
			}
		}
		return result;
	}
}

package it.osm.gtfs.command;

import it.osm.gtfs.input.GTFSParser;
import it.osm.gtfs.input.OSMParser;
import it.osm.gtfs.model.Route;
import it.osm.gtfs.model.Stop;
import it.osm.gtfs.model.StopTimes;
import it.osm.gtfs.model.Trip;
import it.osm.gtfs.model.StopTimes.Relation;
import it.osm.gtfs.utils.GTFSImportSetting;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class GTFSGenerateRoutesDiff {
	public static void run() throws ParserConfigurationException, SAXException, IOException{
		List<Stop> osmStops = OSMParser.readOSMStops(GTFSImportSetting.getInstance().getOSMPath() +  GTFSImportSetting.OSM_STOP_FILE_NAME);
		Map<String, Stop> osmstopsGTFSId = OSMParser.applyGTFSIndex(osmStops);
		Map<String, Stop> osmstopsOsmID = OSMParser.applyOSMIndex(osmStops);
		List<Relation> osmRels = OSMParser.readOSMRelations(GTFSImportSetting.getInstance().getOSMPath() +  GTFSImportSetting.OSM_RELATIONS_FILE_NAME, osmstopsOsmID);

		Map<String, Route> routes = GTFSParser.readRoutes(GTFSImportSetting.getInstance().getGTFSPath() +  GTFSImportSetting.GTFS_ROUTES_FILE_NAME);
		Map<String, StopTimes> stopTimes = GTFSParser.readStopTimes(GTFSImportSetting.getInstance().getGTFSPath() +  GTFSImportSetting.GTFS_STOP_TIME_FILE_NAME, osmstopsGTFSId);
		List<Trip> trips = GTFSParser.readTrips(GTFSImportSetting.getInstance().getGTFSPath() +  GTFSImportSetting.GTFS_TRIPS_FILE_NAME, stopTimes);

		//looking from mapping gtfs trip into existing osm relations
		List<Relation> osmRelationNotFoundInGTFS = new LinkedList<StopTimes.Relation>(osmRels);
		List<Relation> osmRelationFoundInGTFS = new LinkedList<StopTimes.Relation>();

		Map<String, List<Trip>> grouppedTrips = GTFSParser.groupTrip(trips, routes, stopTimes);
		Set<String> keys = new TreeSet<String>(grouppedTrips.keySet());
		Map<Relation, Affinity> affinities = new HashMap<StopTimes.Relation, GTFSGenerateRoutesDiff.Affinity>();

		for (String k:keys){
			List<Trip> allTrips = grouppedTrips.get(k);
			Set<Trip> uniqueTrips = new HashSet<Trip>(allTrips);

			for (Trip trip:uniqueTrips){
				StopTimes s = stopTimes.get(trip.getTripID());
				Relation found = null;
				for (Relation r: osmRelationNotFoundInGTFS){
					if (r.equalsStops(s)){
						found = r;
					}
					int affinity = r.getStopsAffinity(s);
					Affinity oldAff = affinities.get(r);
					if (oldAff == null){
						oldAff = new Affinity();
						oldAff.trip = trip;
						oldAff.affinity = affinity;
						affinities.put(r, oldAff);
					}else if (oldAff.affinity < affinity){
						oldAff.trip = trip;
						oldAff.affinity = affinity;
					}
				}
				if (found != null){
					osmRelationNotFoundInGTFS.remove(found);
					osmRelationFoundInGTFS.add(found);
				}else{
					Route r = routes.get(trip.getRouteID());
					System.err.println("Warning tripid: " + trip.getTripID() + " (" + trip.getName() + ") not found in OSM, detail below." );
					System.err.println("Detail: shapeid" + trip.getShapeID() + " shortname: " + r.getShortName() + " longname:" + r.getLongName());
				}
			}
		}

		System.out.println("---");
		for (Relation r:osmRelationFoundInGTFS){
			System.out.println("Relation " + r.getId() + " (" + r.name + ") matched in GTFS ");
		}
		for (Relation r:osmRelationNotFoundInGTFS){
			System.out.println("---");
			Affinity affinityGTFS = affinities.get(r);
			System.out.println("Relation " + r.getId() + " (" + r.name + ") NOT matched in GTFS ");
			System.out.println("Best match (" + affinityGTFS.affinity + "): id: " + affinityGTFS.trip.getTripID() + " " + routes.get(affinityGTFS.trip.getRouteID()).getShortName() + " " + affinityGTFS.trip.getName());
			StopTimes stopGTFS = stopTimes.get(affinityGTFS.trip.getTripID());
			StopTimes stopOSM = r;
			long max = Math.max(stopGTFS.getStops().size(), stopOSM.getStops().size());
			System.out.println("Progressivo \tGTFS\tOSM");
			for (long f = 1; f < max ; f++){
				Stop gtfs= stopGTFS.getStops().get(new Long(f));
				Stop osm = stopOSM.getStops().get(new Long(f));
				System.out.println("Stop # " + f + "\t" + ((gtfs != null) ? gtfs.getCode() : "-") + "\t" + ((osm != null) ? osm.getCode() : "-") + ((gtfs != null) && (osm != null) &&  gtfs.getCode().equals(osm.getCode()) ? "" : "*"));
			}
		}

	}

	private static class Affinity{
		public Trip trip;
		public int affinity;
	}
}

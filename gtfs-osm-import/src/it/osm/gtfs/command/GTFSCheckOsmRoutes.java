package it.osm.gtfs.command;

import it.osm.gtfs.input.OSMParser;
import it.osm.gtfs.model.Relation;
import it.osm.gtfs.model.StopsList;
import it.osm.gtfs.model.Relation.OSMRelationWayMember;
import it.osm.gtfs.model.Stop;
import it.osm.gtfs.model.Relation.OSMNode;
import it.osm.gtfs.utils.GTFSImportSetting;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class GTFSCheckOsmRoutes {
	public static void run(String osmId) throws ParserConfigurationException, SAXException, IOException {
		System.err.println("Warning: this command is still in alpha stage an check only some aspects of the relations.");
		List<Stop> osmStops = OSMParser.readOSMStops(GTFSImportSetting.getInstance().getOSMPath() +  GTFSImportSetting.OSM_STOP_FILE_NAME);
		Map<String, Stop> osmstopsOsmID = OSMParser.applyOSMIndex(osmStops);
		List<Relation> osmRels = OSMParser.readOSMRelations(new File(GTFSImportSetting.getInstance().getOSMPath() +  GTFSImportSetting.OSM_RELATIONS_FILE_NAME), osmstopsOsmID);

		for (Relation r:osmRels){
			try{
				if (osmId == null || osmId.length() == 0)
					check(r, false);
				else if (osmId.equals(r.getId()))
					check(r, true);
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private static void check(Relation r, Boolean debug) {
		if (r.getStops().size() <= 1)
			throw new IllegalArgumentException("Relation " + r.getId() + " has less than 2 stop.");

		OSMNode previous = null;
		for (OSMRelationWayMember m:r.wayMembers){
			if (previous != null && !previous.equals(m.way.nodes.get((m.backward) ? m.way.nodes.size() - 1 : 0))){
				throw new IllegalArgumentException("Relation " + r.getId() + " has a gap. (Current way: " + m.way.getId() + ")");
			}
			previous = m.way.nodes.get((!m.backward) ? m.way.nodes.size() - 1 : 0);
		}

		if (debug){
			StopsList stopOSM = r;
			for (long f = 1; f <= r.getStops().size() ; f++){
				Stop osm = stopOSM.getStops().get(new Long(f));
				System.out.println("Stop # " + f + "\t" + osm.getCode() + "\t" + osm.getOSMId() + "\t" + osm.getName());
			}
		}
	}
}

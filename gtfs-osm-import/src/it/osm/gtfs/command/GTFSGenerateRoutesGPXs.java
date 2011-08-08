package it.osm.gtfs.command;

import it.osm.gtfs.input.GTFSParser;
import it.osm.gtfs.model.Route;
import it.osm.gtfs.model.Shape;
import it.osm.gtfs.model.StopTimes;
import it.osm.gtfs.model.Trip;
import it.osm.gtfs.utils.GTFSImportSetting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class GTFSGenerateRoutesGPXs {
	public static void run() throws IOException, ParserConfigurationException, SAXException {
		Map<String, Route> rt = GTFSParser.readRoutes(GTFSImportSetting.getInstance().getGTFSPath() + GTFSImportSetting.GTFS_ROUTES_FILE_NAME);
		Map<String, Shape> sh = GTFSParser.readShapes(GTFSImportSetting.getInstance().getGTFSPath() + GTFSImportSetting.GTFS_SHAPES_FILE_NAME);
		Set<Trip> trips = new HashSet<Trip>(GTFSParser.readTrips(GTFSImportSetting.getInstance().getGTFSPath() + GTFSImportSetting.GTFS_TRIPS_FILE_NAME, new HashMap<String, StopTimes>()));
		
		int id = 10000;
		
		new File(GTFSImportSetting.getInstance().getOutputPath() + "gpx").mkdirs();
		
		for (Trip t:trips){
			Route r = rt.get(t.getRouteID());
			Shape s = sh.get(t.getShapeID());
			
			FileOutputStream f = new FileOutputStream(GTFSImportSetting.getInstance().getOutputPath() + "/gpx/r" + id++ + " " + r.getShortName().replace("/", "B") + " " + t.getName().replace("/", "_") + ".gpx");
			f.write(s.getGPX(r.getShortName()).getBytes());
			f.close();
		}
	}
}

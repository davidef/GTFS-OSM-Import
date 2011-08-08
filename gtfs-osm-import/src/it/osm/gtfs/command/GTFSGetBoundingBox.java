package it.osm.gtfs.command;

import it.osm.gtfs.input.GTFSParser;
import it.osm.gtfs.model.BoundingBox;
import it.osm.gtfs.model.Stop.GTFSStop;
import it.osm.gtfs.utils.GTFSImportSetting;

import java.io.IOException;
import java.util.List;

public class GTFSGetBoundingBox {

	public static void run() throws IOException {
		List<GTFSStop> gtfs = GTFSParser.readBusStop(GTFSImportSetting.getInstance().getGTFSPath() + GTFSImportSetting.GTFS_STOP_FILE_NAME);
		BoundingBox bb = new BoundingBox(gtfs);

		System.out.println("GTFS " + bb);
		//Bus
		System.out.println("XAPI links buses: http://open.mapquestapi.com/xapi/api/0.6/node" + bb.getXAPIQuery() + "[highway=bus_stop]");
		//Tram
		System.out.println("XAPI links trams: http://open.mapquestapi.com/xapi/api/0.6/node" + bb.getXAPIQuery() + "[railway=tram_stop]");
		//Metro
		System.out.println("XAPI links trams: http://open.mapquestapi.com/xapi/api/0.6/node" + bb.getXAPIQuery() + "[railway=station]");
	}

}

package it.osm.gtfs.command;

import it.osm.gtfs.input.GTFSParser;
import it.osm.gtfs.model.BoundingBox;
import it.osm.gtfs.model.Stop.GTFSStop;
import it.osm.gtfs.utils.DownloadUtils;
import it.osm.gtfs.utils.GTFSImportSetting;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GTFSGetBusStopFromOSM {
	public static void run() throws IOException, InterruptedException {
		List<GTFSStop> gtfs = GTFSParser.readBusStop(GTFSImportSetting.getInstance().getGTFSPath() + GTFSImportSetting.GTFS_STOP_FILE_NAME);
		BoundingBox bb = new BoundingBox(gtfs);

		String urlbus = "http://open.mapquestapi.com/xapi/api/0.6/node" + bb.getXAPIQuery() + "[highway=bus_stop]";
		String urltrm = "http://open.mapquestapi.com/xapi/api/0.6/node" + bb.getXAPIQuery() + "[railway=tram_stop]";
		String urlmtr = "http://open.mapquestapi.com/xapi/api/0.6/node" + bb.getXAPIQuery() + "[railway=station]";
		File filebus = new File(GTFSImportSetting.getInstance().getOSMPath() + "tmp_bus.osm");
		File filetrm = new File(GTFSImportSetting.getInstance().getOSMPath() + "tmp_tram.osm");
		File filemtr = new File(GTFSImportSetting.getInstance().getOSMPath() + "tmp_metro.osm");
		File fileout = new File(GTFSImportSetting.getInstance().getOSMPath() + GTFSImportSetting.OSM_STOP_FILE_NAME);

		DownloadUtils.downlod(urlbus, filebus);
		DownloadUtils.downlod(urltrm, filetrm);
		DownloadUtils.downlod(urlmtr, filemtr);

		System.out.println("Merging files.");
		String[] commands = new String[]{GTFSImportSetting.getInstance().getOsmosisPath() + "osmosis",
				"--read-xml", filebus.getAbsolutePath(),
				"--read-xml", filetrm.getAbsolutePath(),
				"--read-xml", filemtr.getAbsolutePath(),
				"--merge","--merge",
				"--write-xml",fileout.getAbsolutePath()};

		Process child = Runtime.getRuntime().exec(commands);
		child.waitFor();
		if (child.exitValue() != 0)
			System.err.println("Error " + child.exitValue() + " occurred while running osmosis.");
	}
}

package it.osm.gtfs.command;

import it.osm.gtfs.utils.DownloadUtils;
import it.osm.gtfs.utils.GTFSImportSetting;

import java.io.File;
import java.io.IOException;

public class GTFSGetRelationsFromOSM {
	public static void run() throws IOException, InterruptedException {
		String urlrel = "http://open.mapquestapi.com/xapi/api/0.6/relation[network=" + GTFSImportSetting.getInstance().getNetwork() +  "]";
		File filerel = new File(GTFSImportSetting.getInstance().getOSMPath() + GTFSImportSetting.OSM_RELATIONS_FILE_NAME);
		
		DownloadUtils.downlod(urlrel, filerel);
	}
}

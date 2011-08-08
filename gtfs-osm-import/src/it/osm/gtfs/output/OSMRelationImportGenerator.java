package it.osm.gtfs.output;

import it.osm.gtfs.model.BoundingBox;
import it.osm.gtfs.model.Route;
import it.osm.gtfs.model.Stop;
import it.osm.gtfs.model.StopTimes;
import it.osm.gtfs.model.Trip;
import it.osm.gtfs.utils.GTFSImportSetting;

public class OSMRelationImportGenerator {

	//FIXME: refactor
	public static String getRelation(BoundingBox bb, StopTimes stopTimes, Trip t, Route r){
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\"?><osm version='0.5' generator='JOSM'>");
		buffer.append(bb.getXMLTag());
		buffer.append("<relation id='-" + Math.round(Math.random()*100000) +  "'>\n");
		for (Stop s:stopTimes.getStops().values()){
			buffer.append("<member type='node' ref='" + s.originalXMLNode.getAttributes().getNamedItem("id").getNodeValue() + "' role='stop' />\n");
		}
		buffer.append("<tag k='direction' v='" + t.getName() + "' />\n");
		buffer.append("<tag k='name' v='" + r.getShortName() + ": " + r.getLongName().replaceAll("'", "\'") + "' />\n");
		buffer.append("<tag k='network' v='" + GTFSImportSetting.getInstance().getNetwork() + "' />\n");
		buffer.append("<tag k='operator' v='" + GTFSImportSetting.getInstance().getOperator() + "' />\n");
		buffer.append("<tag k='ref' v='" + r.getShortName() + "' />\n");
		buffer.append("<tag k='route' v='bus' />\n");
		buffer.append("<tag k='type' v='route' />\n");
		buffer.append("</relation>");
		buffer.append("</osm>");

		return buffer.toString();
	}
}

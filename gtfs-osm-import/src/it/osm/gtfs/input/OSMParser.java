package it.osm.gtfs.input;

import it.osm.gtfs.model.Stop;
import it.osm.gtfs.model.StopTimes.Relation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OSMParser {

	public static Map<String, Stop> applyGTFSIndex(List<Stop> stops) throws ParserConfigurationException, SAXException, IOException{
		final Map<String, Stop> result = new TreeMap<String, Stop>();

		for (Stop s:stops){
			if (s.getGtfsId() != null && s.getGtfsId() != ""){
				result.put(s.getGtfsId(), s);
			}
		}

		return result;
	}

	public static Map<String, Stop> applyOSMIndex(List<Stop> stops) throws ParserConfigurationException, SAXException, IOException{
		final Map<String, Stop> result = new TreeMap<String, Stop>();

		for (Stop s:stops){
			if (s.getOSMId() != null){
				result.put(s.getOSMId(), s);
			}
		}

		return result;
	}

	public static List<Stop> readOSMStops(String fileName) throws ParserConfigurationException, SAXException, IOException{
		List<Stop> result = new ArrayList<Stop>();
		Set<String> refBuses = new HashSet<String>();
		Set<String> refRails = new HashSet<String>();

		File file = new File(fileName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();

		NodeList nodeLst = doc.getElementsByTagName("node");

		for (int s = 0; s < nodeLst.getLength(); s++) {
			Node fstNode = nodeLst.item(s);
			Stop st = new Stop(null, null, Double.valueOf(fstNode.getAttributes().getNamedItem("lat").getNodeValue()), Double.valueOf(fstNode.getAttributes().getNamedItem("lon").getNodeValue()), null);
			st.originalXMLNode = fstNode;
			NodeList att = fstNode.getChildNodes();
			for (int t = 0; t < att.getLength(); t++) {
				Node attNode = att.item(t);
				if (attNode.getAttributes() != null){
					if (attNode.getAttributes().getNamedItem("k").getNodeValue().equals("ref"))
						st.setCode(attNode.getAttributes().getNamedItem("v").getNodeValue());
					if (attNode.getAttributes().getNamedItem("k").getNodeValue().equals("name"))
						st.setName(attNode.getAttributes().getNamedItem("v").getNodeValue());
					if (attNode.getAttributes().getNamedItem("k").getNodeValue().equals("gtfs_id"))
						st.setGtfsId(attNode.getAttributes().getNamedItem("v").getNodeValue());
					if (attNode.getAttributes().getNamedItem("k").getNodeValue().equals("highway") &&
							attNode.getAttributes().getNamedItem("v").getNodeValue().equals("bus_stop"))
						st.setIsRailway(false);
					if (attNode.getAttributes().getNamedItem("k").getNodeValue().equals("railway") &&
							attNode.getAttributes().getNamedItem("v").getNodeValue().equals("tram_stop"))
						st.setIsRailway(true);
					if (attNode.getAttributes().getNamedItem("k").getNodeValue().equals("railway") &&
							attNode.getAttributes().getNamedItem("v").getNodeValue().equals("station"))
						st.setIsRailway(true);
				}
			}

			if (st.isRailway() == null)
				throw new IllegalArgumentException("Unknow node type. We support only highway=bus_stop, railway=tram_stop and railway=station");

			//Check duplicate ref in osm (FIXME:check only within 5 km or so)
			if (st.getCode() != null)
				if (st.isRailway()){
					if (refRails.contains(st.getCode()))
						System.err.println("Warning: The ref " + st.getCode() + " is used in more than one node this may lead to bad import.");
					else
						refRails.add(st.getCode());
				}else{
					if (refBuses.contains(st.getCode()))
						System.err.println("Warning: The ref " + st.getCode() + " is used in more than one node this may lead to bad import.");
					else
						refBuses.add(st.getCode());
				}

			result.add(st);
		}

		return result;
	}

	public static List<Relation> readOSMRelations(String fileName, Map<String, Stop> stopsWithOSMIndex) throws ParserConfigurationException, SAXException, IOException{
		List<Relation> result = new ArrayList<Relation>();

		File file = new File(fileName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();

		NodeList relationLst = doc.getElementsByTagName("relation");

		for (int s = 0; s < relationLst.getLength(); s++) {
			Node fstNode = relationLst.item(s);
			Relation st = new Relation(fstNode.getAttributes().getNamedItem("id").getNodeValue());
			long seq = 1;
			boolean failed = false;
			NodeList att = fstNode.getChildNodes();
			for (int t = 0; t < att.getLength(); t++) {
				Node attNode = att.item(t);
				if (attNode.getNodeType() == Element.ELEMENT_NODE &&
						attNode.getNodeName().equals("member") && 
						attNode.getAttributes().getNamedItem("type").getNodeValue().equals("node") &&
						attNode.getAttributes().getNamedItem("role").getNodeValue().equals("stop")){
					Stop stop = stopsWithOSMIndex.get(attNode.getAttributes().getNamedItem("ref").getNodeValue());
					if (stop == null){
						System.err.println("Warning: Node " + attNode.getAttributes().getNamedItem("ref").getNodeValue() + " not found.");
						failed = true;
					}
					st.pushPoint(seq++, stop);
				}else if (attNode.getNodeType() == Element.ELEMENT_NODE &&
						attNode.getNodeName().equals("tag") && 
						attNode.getAttributes().getNamedItem("k").getNodeValue().equals("name")){
					st.name = attNode.getAttributes().getNamedItem("v").getNodeValue();
				}
			}
			if (!failed)
				result.add(st);
			else
				System.err.println("Warning: failed to parse relation " + st.name);
		}

		return result;
	}
}

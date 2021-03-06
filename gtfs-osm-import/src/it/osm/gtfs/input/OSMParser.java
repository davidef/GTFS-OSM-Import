/**
   Licensed under the GNU General Public License version 3
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.gnu.org/licenses/gpl-3.0.html

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 **/
package it.osm.gtfs.input;

import it.osm.gtfs.model.Relation;
import it.osm.gtfs.model.Relation.OSMNode;
import it.osm.gtfs.model.Relation.OSMRelationWayMember;
import it.osm.gtfs.model.Relation.OSMWay;
import it.osm.gtfs.model.Relation.RelationType;
import it.osm.gtfs.model.Stop;
import it.osm.gtfs.utils.OSMDistanceUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

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
		Multimap<String, Stop> refBuses = HashMultimap.create();
		Multimap<String, Stop> refRails = HashMultimap.create();

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
					if (attNode.getAttributes().getNamedItem("k").getNodeValue().equals("public_transport") &&
							attNode.getAttributes().getNamedItem("v").getNodeValue().equals("stop_position") &&
							st.isRailway() == null)
						st.setIsStopPosition(true);
					if (attNode.getAttributes().getNamedItem("k").getNodeValue().equals("train") &&
							attNode.getAttributes().getNamedItem("v").getNodeValue().equals("yes"))
						st.setIsRailway(true);
					if (attNode.getAttributes().getNamedItem("k").getNodeValue().equals("tram") &&
							attNode.getAttributes().getNamedItem("v").getNodeValue().equals("yes"))
						st.setIsRailway(true);
					if (attNode.getAttributes().getNamedItem("k").getNodeValue().equals("bus") &&
							attNode.getAttributes().getNamedItem("v").getNodeValue().equals("yes"))
						st.setIsRailway(false);
				}
			}

			
			if (st.isRailway() == null)
				if (st.isStopPosition())
					continue; //ignore unsupported stop positions (like ferries)
				else
					throw new IllegalArgumentException("Unknow node type for node: " + st.getOSMId() + ". We support only highway=bus_stop, public_transport=stop_position, railway=tram_stop and railway=station");

			//Check duplicate ref in osm
			if (st.getCode() != null){
				if (st.isStopPosition() == null || st.isStopPosition() == false){
					if (st.isRailway()){
						if (refRails.containsKey(st.getCode())){
							for (Stop existingStop:refRails.get(st.getCode())){
								if (OSMDistanceUtils.distVincenty(st.getLat(), st.getLon(), existingStop.getLat(), existingStop.getLon()) < 500)
									System.err.println("Warning: The ref " + st.getCode() + " is used in more than one node within 500m this may lead to bad import." +
											" (nodes ids:" + st.getOSMId() + "," + existingStop.getOSMId() + ")");
							}
						}

						refRails.put(st.getCode(), st);
					}else{
						if (refBuses.containsKey(st.getCode())){
							for (Stop existingStop:refBuses.get(st.getCode())){
								if (OSMDistanceUtils.distVincenty(st.getLat(), st.getLon(), existingStop.getLat(), existingStop.getLon()) < 500)
									System.err.println("Warning: The ref " + st.getCode() + " is used in more than one node within 500m this may lead to bad import." +
											" (nodes ids:" + st.getOSMId() + "," + existingStop.getOSMId() + ")");
							}
						}
						refBuses.put(st.getCode(), st);
					}
				}
			}
			result.add(st);
		}

		return result;
	}

	public static List<Relation> readOSMRelations(File file, Map<String, Stop> stopsWithOSMIndex) throws ParserConfigurationException, SAXException, IOException{
		NodeParser nodeParser;
		{
			XMLReader xr = XMLReaderFactory.createXMLReader();
			nodeParser = new NodeParser();
			xr.setContentHandler(nodeParser);
			xr.setErrorHandler(nodeParser);
			xr.parse(new InputSource(new FileReader(file)));
		}

		WayParser wayParser;
		{
			XMLReader xr = XMLReaderFactory.createXMLReader();
			wayParser = new WayParser(nodeParser.result);
			xr.setContentHandler(wayParser);
			xr.setErrorHandler(wayParser);
			xr.parse(new InputSource(new FileReader(file)));
		}

		RelationParser relationParser;
		{
			XMLReader xr = XMLReaderFactory.createXMLReader();
			relationParser = new RelationParser(stopsWithOSMIndex, wayParser.result);
			xr.setContentHandler(relationParser);
			xr.setErrorHandler(relationParser);
			xr.parse(new InputSource(new FileReader(file)));
		}


		if (relationParser.missingNodes.size() > 0 || relationParser.failedRelationIds.size() > 0){
			System.err.println("Failed to parse some relations. Relations id: " + StringUtils.join(relationParser.failedRelationIds, ", "));
			System.err.println("Failed to parse some relations. Missing nodes: " + StringUtils.join(relationParser.missingNodes, ", "));
		}

		return relationParser.result;
	}

	private static class NodeParser extends DefaultHandler{
		private Map<Long, OSMNode> result = new HashMap<Long, OSMNode>();

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (localName.equals("node")){
				result.put(Long.parseLong(attributes.getValue("id")),
						new OSMNode(Double.parseDouble(attributes.getValue("lat")),
								Double.parseDouble(attributes.getValue("lon"))));
			}
		}
	}

	private static class WayParser extends DefaultHandler{
		Map<Long, OSMNode> nodes;

		Map<Long, OSMWay> result = new HashMap<Long, Relation.OSMWay>();
		OSMWay currentWay;

		private WayParser(Map<Long, OSMNode> nodes) {
			super();
			this.nodes = nodes;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			if (localName.equals("way")){
				currentWay = new OSMWay(Long.parseLong(attributes.getValue("id")));
			}else if(currentWay != null && localName.equals("nd")){
				currentWay.nodes.add(nodes.get(Long.parseLong(attributes.getValue("ref"))));
			}else if(currentWay != null && localName.equals("tag")){
				if (attributes.getValue("k").equals("oneway")){
					if (attributes.getValue("v").equals("yes") ||
							attributes.getValue("v").equals("true")){
						currentWay.oneway = true;
					}else if (attributes.getValue("v").equals("no") ||
							attributes.getValue("v").equals("false")){
						currentWay.oneway = false;
					}else{
						System.err.println("Unhandled oneway attribute: " + attributes.getValue("v") + " way id: " + currentWay.getId());
					}
				}else if (attributes.getValue("k").equals("junction")){
					if (attributes.getValue("v").equals("roundabout")){
						currentWay.oneway = true;
					}
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (localName.equals("way")){
				result.put(currentWay.getId(), currentWay);
				currentWay = null;
			}
		}
	}

	private static class RelationParser extends DefaultHandler{
		private Map<String, Stop> stopsWithOSMIndex;
		private Map<Long, OSMWay> ways;

		private List<Relation> result = new ArrayList<Relation>();
		private List<String> failedRelationIds = new ArrayList<String>();
		private List<String> missingNodes = new ArrayList<String>();

		private Relation currentRelation;
		private long seq = 1;
		private boolean failed = false;

		private RelationParser(Map<String, Stop> stopsWithOSMIndex, Map<Long, OSMWay> ways) {
			super();
			this.stopsWithOSMIndex = stopsWithOSMIndex;
			this.ways = ways;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			if (localName.equals("relation")){
				currentRelation = new Relation(attributes.getValue("id"));
				currentRelation.setVersion(Integer.parseInt(attributes.getValue("version")));
				seq = 1;
				failed = false;
			}else if(currentRelation != null && localName.equals("member")){
				String type = attributes.getValue("type");
				String role = attributes.getValue("role");

				if (type.equals("node")){
					if (role.equals("stop")){
						Stop stop = stopsWithOSMIndex.get( attributes.getValue("ref"));
						if (stop == null){
							System.err.println("Warning: Node " +  attributes.getValue("ref") + " not found.");
							missingNodes.add(attributes.getValue("ref"));
							failed = true;
						}
						currentRelation.pushPoint(seq++, stop, "");
					}else{
						System.err.println("Warning: Relation " + currentRelation.getId() + " has an unsupported member of type node.");
					}
				}else if (type.equals("way")){
					if (role.equals("forward")){
						OSMRelationWayMember member = new OSMRelationWayMember();
						member.way = ways.get(Long.parseLong(attributes.getValue("ref")));
						member.backward = false;
						currentRelation.getWayMembers().add(member);
					}else if (role.equals("backward")){
						OSMRelationWayMember member = new OSMRelationWayMember();
						member.way = ways.get(Long.parseLong(attributes.getValue("ref")));
						member.backward = true;
						currentRelation.getWayMembers().add(member);
					}else{
						OSMRelationWayMember member = new OSMRelationWayMember();
						member.way = ways.get(Long.parseLong(attributes.getValue("ref")));
						member.backward = null;
						currentRelation.getWayMembers().add(member);
					}
				}else{
					System.err.println("Warning: Relation " + currentRelation.getId() + " has an unsupported member of unknown type .");
				}
			}else if (currentRelation != null && localName.equals("tag")){
				String key = attributes.getValue("k");
				if (key.equals("name"))
					currentRelation.setName(attributes.getValue("v"));
				else if (key.equals("ref"))
					currentRelation.setRef(attributes.getValue("v"));
				else if (key.equals("from"))
					currentRelation.setFrom(attributes.getValue("v"));
				else if (key.equals("to"))
					currentRelation.setTo(attributes.getValue("v"));
				else if (key.equals("route"))
					try{
						currentRelation.setType(RelationType.parse(attributes.getValue("v")));
					}catch (IllegalArgumentException e){
						System.err.println(e.getMessage());
						failed = true;
					}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (localName.equals("relation")){
				if (!failed){
					result.add(currentRelation);
				}else{
					failedRelationIds.add(currentRelation.getId());
					System.err.println("Warning: failed to parse relation " + currentRelation.getId() + " " + currentRelation.getName());
				}
				currentRelation = null;
			}
		}

	}
}

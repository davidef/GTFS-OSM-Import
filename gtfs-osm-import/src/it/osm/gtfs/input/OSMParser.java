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
import it.osm.gtfs.model.Stop;
import it.osm.gtfs.utils.OSMDistanceUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
				}
			}

			if (st.isRailway() == null)
				throw new IllegalArgumentException("Unknow node type. We support only highway=bus_stop, railway=tram_stop and railway=station");

			//Check duplicate ref in osm
			if (st.getCode() != null)
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

			result.add(st);
		}

		return result;
	}

	public static List<Relation> readOSMRelations(File file, Map<String, Stop> stopsWithOSMIndex) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();

		Map<Long, OSMWay> ways = readWays(doc);

		List<Relation> result = new ArrayList<Relation>();
		NodeList relationLst = doc.getElementsByTagName("relation");
		for (int s = 0; s < relationLst.getLength(); s++) {
			Node fstNode = relationLst.item(s);
			Relation st = new Relation(fstNode.getAttributes().getNamedItem("id").getNodeValue());
			st.version = Integer.parseInt(fstNode.getAttributes().getNamedItem("version").getNodeValue());
			long seq = 1;
			boolean failed = false;
			NodeList att = fstNode.getChildNodes();
			for (int t = 0; t < att.getLength(); t++) {
				Node attNode = att.item(t);
				if (attNode.getNodeType() == Element.ELEMENT_NODE && attNode.getNodeName().equals("member")){
					if (attNode.getAttributes().getNamedItem("type").getNodeValue().equals("node")){
						if (attNode.getAttributes().getNamedItem("role").getNodeValue().equals("stop")){
							Stop stop = stopsWithOSMIndex.get(attNode.getAttributes().getNamedItem("ref").getNodeValue());
							if (stop == null){
								System.err.println("Warning: Node " + attNode.getAttributes().getNamedItem("ref").getNodeValue() + " not found.");
								failed = true;
							}
							st.pushPoint(seq++, stop);
						}else{
							System.err.println("Warning: Relation " + st.getId() + " has an unsupported member of type node.");
						}
					}else if (attNode.getAttributes().getNamedItem("type").getNodeValue().equals("way")){
						if (attNode.getAttributes().getNamedItem("role").getNodeValue().equals("forward")){
							OSMRelationWayMember member = new OSMRelationWayMember();
							member.way = ways.get(Long.parseLong(attNode.getAttributes().getNamedItem("ref").getNodeValue()));
							member.backward = false;
							st.wayMembers.add(member);
						}else if (attNode.getAttributes().getNamedItem("role").getNodeValue().equals("backward")){
							OSMRelationWayMember member = new OSMRelationWayMember();
							member.way = ways.get(Long.parseLong(attNode.getAttributes().getNamedItem("ref").getNodeValue()));
							member.backward = true;
							st.wayMembers.add(member);
						}else{
							System.err.println("Warning: Relation " + st.getId() + " has an unsupported member of type way.");
						}
					}else{
						System.err.println("Warning: Relation " + st.getId() + " has an unsupported member of unknown type .");
					}
				}else if (attNode.getNodeType() == Element.ELEMENT_NODE &&
						attNode.getNodeName().equals("tag") && 
						attNode.getAttributes().getNamedItem("k").getNodeValue().equals("name")){
					st.name = attNode.getAttributes().getNamedItem("v").getNodeValue();
				}
			}
			if (!failed)
				result.add(st);
			else
				System.err.println("Warning: failed to parse relation " + st.getId() + " " + st.name);
		}

		return result;
	}

	private static Map<Long, OSMWay> readWays(Document doc){
		Map<Long, OSMNode> nodes = readNodes(doc);
		Map<Long, OSMWay> result = new HashMap<Long, Relation.OSMWay>();
		NodeList relationLst = doc.getElementsByTagName("way");
		for (int s = 0; s < relationLst.getLength(); s++) {
			Node fstNode = relationLst.item(s);
			OSMWay way = new OSMWay(Long.parseLong(fstNode.getAttributes().getNamedItem("id").getNodeValue()));
			NodeList att = fstNode.getChildNodes();
			for (int t = 0; t < att.getLength(); t++) {
				Node attNode = att.item(t);
				if (attNode.getNodeType() == Element.ELEMENT_NODE && attNode.getNodeName().equals("nd")){
					way.nodes.add(nodes.get(Long.parseLong(attNode.getAttributes().getNamedItem("ref").getNodeValue())));
				}else if (attNode.getNodeType() == Element.ELEMENT_NODE &&
						attNode.getNodeName().equals("tag") && 
						attNode.getAttributes().getNamedItem("k").getNodeValue().equals("oneway")){
					if (attNode.getAttributes().getNamedItem("v").getNodeValue().equals("yes") ||
							attNode.getAttributes().getNamedItem("v").getNodeValue().equals("true")){
						way.oneway = true;
					}else if (attNode.getAttributes().getNamedItem("v").getNodeValue().equals("no") ||
							attNode.getAttributes().getNamedItem("v").getNodeValue().equals("false")){
						way.oneway = false;
					}/*else if (attNode.getAttributes().getNamedItem("v").getNodeValue().equals("-1")){ //FIXME: seem not to work
						Collections.reverse(way.nodes);
						way.oneway = true;
					}*/else{
						System.err.println("Unhandled oneway attribute: " + attNode.getAttributes().getNamedItem("v").getNodeValue() + " way id: " + way.getId());
					}
					//FIXME: handle junction roundabout
				}
			}
			result.put(way.getId(), way);
		}
		return result;
	}

	private static Map<Long, OSMNode> readNodes(Document doc){
		Map<Long, OSMNode> result = new HashMap<Long, OSMNode>();
		NodeList relationLst = doc.getElementsByTagName("node");
		for (int s = 0; s < relationLst.getLength(); s++) {
			Node fstNode = relationLst.item(s);
			result.put(Long.parseLong(fstNode.getAttributes().getNamedItem("id").getNodeValue()),
					new OSMNode(Double.parseDouble(fstNode.getAttributes().getNamedItem("lat").getNodeValue()),
							Double.parseDouble(fstNode.getAttributes().getNamedItem("lon").getNodeValue())));
		}
		return result;
	}
}

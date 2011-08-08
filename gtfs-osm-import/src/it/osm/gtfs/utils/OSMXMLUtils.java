package it.osm.gtfs.utils;

import it.osm.gtfs.output.IElementCreator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OSMXMLUtils {

	public static void addTag(Element node, String key, String value) {
		node.setAttribute("action", "modify");
		Element e = node.getOwnerDocument().createElement("tag");
		e.setAttribute("k", key);
		e.setAttribute("v", value);
		node.appendChild(e);
	}
	
	public static void addTagIfNotExisting(Element node, String key, String value) {
		NodeList childs = node.getChildNodes();
		for (int t = 0; t < childs.getLength(); t++) {
			Node attNode = childs.item(t);
			if (attNode.getAttributes() != null){
				if (attNode.getAttributes().getNamedItem("k").getNodeValue().equals(key)){
					return;
				}
			}
		}
		addTag(node, key, value);
	}

	public static Element createTagElement(IElementCreator document, String key, String value){
		Element tag = document.createElement("tag");
		tag.setAttribute("k", key);
		tag.setAttribute("v", value);
		return tag;
	}
}

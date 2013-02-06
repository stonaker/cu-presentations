package edu.cu.integration;

import org.apache.xmlbeans.XmlObject;

import net.sf.json.*;
import net.sf.json.xml.XMLSerializer;


public class JSONUtility {

	 public static String xmlToJson2(XmlObject xml)
	 {
	 XMLSerializer xmlSerializer = new XMLSerializer();
	 xmlSerializer.setSkipNamespaces( true );
	 xmlSerializer.setTrimSpaces( true );
	 xmlSerializer.setRemoveNamespacePrefixFromElements(true);
	 JSON json = xmlSerializer.read( xml.toString() );
	 return json.toString();
	 }
}
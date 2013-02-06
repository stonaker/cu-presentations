package edu.cu.integration;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

public class JSONToXMLConverter {

	 public static String Json2Xml(String jsonString)
	 {
		 
		 JSONObject json = JSONObject.fromObject(jsonString);
		 XMLSerializer xmlSerializer = new XMLSerializer();
		 xmlSerializer.setTypeHintsEnabled(false);
		 String xml = xmlSerializer.write( json );  
		 try
		 {
			 XmlObject xmlObject = org.apache.xmlbeans.XmlObject.Factory.parse(xml);
		 }
		 catch (XmlException ex)
		 {
			 XmlObject xmlObject = null;
		 }
		 return xml;
	 }
	 
	 public static XmlObject Json2XmlObject(String jsonString)
	 {
		 
		 JSONObject json = JSONObject.fromObject(jsonString);
		 XMLSerializer xmlSerializer = new XMLSerializer();
		 xmlSerializer.setTypeHintsEnabled(false);
		 xmlSerializer.setForceTopLevelObject(false);

		 String xml = xmlSerializer.write( json );  
		 XmlObject xmlObject = null;
		 try
		 {
			 xmlObject = org.apache.xmlbeans.XmlObject.Factory.parse(xml);
		 }
		 catch (XmlException ex)
		 {
			 xmlObject = null;
			 Log logger = LogFactory.getLog("edu.cu.integration.JSONToXMLConverter");
			 logger.error("Couldn't create xml from Json String. " + ex.getMessage());
		 }
		 return xmlObject;
	 }
	 
}

/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000.objects;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.energyict.cbo.ApplicationException;
import com.energyict.genericprotocolimpl.actarisace4000.ActarisACE4000;
import com.energyict.genericprotocolimpl.actarisace4000.objects.tables.EnableTable;
import com.energyict.genericprotocolimpl.actarisace4000.objects.xml.XMLTags;

/**
 * @author gna
 *
 */
public class ObjectFactory {
	
	private ActarisACE4000 		aace;
	private Acknowledge 		acknowledge;
	private FirmwareVersion 	firmwareVersion = null;
	private Serialnumber		serialnumber 	= null;
	private AutoPushConfig		autoPushConfig	= null;
	private FullMeterConfig		fullMeterConfig	= null;

	/**
	 * 
	 */
	public ObjectFactory(ActarisACE4000 aace) {
		this.aace = aace;
	}
	
	public FirmwareVersion requestFirmwareVersion(){
		if(firmwareVersion == null){
			firmwareVersion = new FirmwareVersion(this);
			firmwareVersion.setTrackingID(getAace().getTracker());
			firmwareVersion.prepareXML();
			firmwareVersion.request();
		}
		return firmwareVersion;
	}
	
	public FullMeterConfig requestFullMeterConfig(){
		if(fullMeterConfig == null){
			fullMeterConfig = new FullMeterConfig(this);
			fullMeterConfig.setTrackingID(getAace().getTracker());
			fullMeterConfig.prepareXML();
			fullMeterConfig.request();
		}
		return fullMeterConfig;
	}
	
	public AutoPushConfig setAutoPushConfig(){
		if(autoPushConfig == null){
			autoPushConfig = new AutoPushConfig(this);
			autoPushConfig.setTrackingID(getAace().getTracker());
			autoPushConfig.prepareXML(EnableTable.enabled, "920", "1040", true);
			autoPushConfig.request();
		}
		return autoPushConfig;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public ActarisACE4000 getAace() {
		return aace;
	}

	public void setAace(ActarisACE4000 aace) {
		this.aace = aace;
	}
	
	public void parseXML(String xml) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));
			Element topElement = document.getDocumentElement();
			parseElements(topElement);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new ParserConfigurationException("Failed to make a new builder from the documentBuilderfactory");
		} catch (SAXException e) {
			e.printStackTrace();
			throw new SAXException("Could not parse the received xmlString.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("The received xml is not a valid inputSource");
		}
	}
	
	private void parseElements(Element element){
		String nodeName = element.getNodeName();
		if(nodeName.equalsIgnoreCase(XMLTags.mPush)){
			NodeList nodes = element.getElementsByTagName(XMLTags.meterData);
			Element md = (Element)nodes.item(0);
			
			if(md.getNodeName().equalsIgnoreCase(XMLTags.meterData)){
				NodeList mdNodeList = md.getChildNodes();
				
				for(int i = 0; i < mdNodeList.getLength(); i++){
					Element mdElement = (Element)mdNodeList.item(i);
					
					if(mdElement.getNodeName().equalsIgnoreCase(XMLTags.resFirmware))
						firmwareVersion.setElement(mdElement);
					
				}
			}
			else
				throw new ApplicationException("Unknown tag found in xml responce: " + nodes.item(0).getNodeName());
		}
		else
			throw new ApplicationException("Unknown tag found in xml responce: " + element.getNodeName());
	}

}

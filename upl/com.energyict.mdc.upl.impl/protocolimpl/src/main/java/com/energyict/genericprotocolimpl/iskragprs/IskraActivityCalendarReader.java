package com.energyict.genericprotocolimpl.iskragprs;

import java.io.IOException;
import java.io.InputStream;
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
import com.energyict.genericprotocolimpl.common.tou.ActivityCalendar;
import com.energyict.genericprotocolimpl.common.tou.OctetString;

public class IskraActivityCalendarReader implements com.energyict.genericprotocolimpl.common.tou.ActivityCalendarReader {

	private ActivityCalendar activityCalendar;
	
	public IskraActivityCalendarReader(ActivityCalendar activityCalendar) {
		this.activityCalendar = activityCalendar;
	}
	
	public void read(InputStream stream) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(stream);
			Element topElement = document.getDocumentElement();
			read(topElement);
		} catch (SAXException e) {
			throw new ApplicationException(e);
		} catch (ParserConfigurationException e) {
			throw new ApplicationException(e);
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}
	
	protected void read(Element element){
		String nodeName = element.getNodeName();
		if ("P2LPCTariff".equals(nodeName)){
			readActivityCalendar(element);
		}
		throw new ApplicationException("Unknown tag found in xml userfile: " + element.getNodeName());
	}
	
	protected void readActivityCalendar(Element element){

	}
	
	protected void readCalendarName(Element element){
		NodeList names = element.getElementsByTagName("Calendar");
		if (names.getLength() != 0){
			activityCalendar.setPassiveCalendarName(
					new OctetString(((Element) names.item(0)).getAttribute("Name")));
		} else {
			throw new ApplicationException("No calendar name found");
		}
		activityCalendar.setPassiveCalendarName(new OctetString(element.getAttribute("Name")));
		
			
	}
	
}

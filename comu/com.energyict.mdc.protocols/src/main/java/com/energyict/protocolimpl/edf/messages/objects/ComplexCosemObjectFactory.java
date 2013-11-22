package com.energyict.protocolimpl.edf.messages.objects;

import com.energyict.mdc.common.ApplicationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.IOException;
import java.io.StringReader;

public class ComplexCosemObjectFactory {

	public static ComplexCosemObject createCosemObject(String xml){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));
			Element topElement = document.getDocumentElement();
			return createCosemObject(topElement);
		} catch (SAXException e) {
			throw new ApplicationException(e);
		} catch (ParserConfigurationException e) {
			throw new ApplicationException(e);
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	public static ComplexCosemObject createCosemObject(Element element){
		if (element.getNodeName() == MovingPeak.ELEMENTNAME){
			return new MovingPeak(element);
		}
		if (element.getNodeName() == MeterClock.ELEMENTNAME){
			return new MeterClock(element);
		}
		if (element.getNodeName() == ActivityCalendar.ELEMENTNAME){
			return new ActivityCalendar(element);
		}
		if (element.getNodeName() == SeasonProfile.ELEMENTNAME){
			return new SeasonProfile(element);
		}
		if (element.getNodeName() == WeekProfile.ELEMENTNAME){
			return new WeekProfile(element);
		}
		if (element.getNodeName() == DayProfile.ELEMENTNAME){
			return new DayProfile(element);
		}
		if (element.getNodeName() == DemandManagement.ELEMENTNAME){
			return new DemandManagement(element);
		}
		if (element.getNodeName() == MeterIdentification.ELEMENTNAME){
			return new MeterIdentification(element);
		}
		if (element.getNodeName() == FtpServerId.ELEMENTNAME){
			return new FtpServerId(element);
		}
		throw new ApplicationException("Cannot determine ComplexCosemObject Type");
	}

}

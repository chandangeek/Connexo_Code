package com.energyict.protocolimpl.edf.messages;

import com.energyict.mdc.common.ApplicationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public class MessageContentFactory {

	public static MessageContent createMessageContent(String xml){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));
			Element topElement = document.getDocumentElement();
			return createMessageContent(topElement);
		} catch (SAXException e) {
			throw new ApplicationException(e);
		} catch (ParserConfigurationException e) {
			throw new ApplicationException(e);
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	public static MessageContent createMessageContent(Element element){
		if (element.getNodeName() == MessageWriteRegister.ELEMENTNAME){
			return new MessageWriteRegister(element);
		}
		if (element.getNodeName() == MessageReadIndexes.ELEMENTNAME){
			return new MessageReadIndexes(element);
		}
		if (element.getNodeName() == MessageDiscoverMeters.ELEMENTNAME){
			return new MessageDiscoverMeters(element);
		}
		if (element.getNodeName() == MessageReadBillingValues.ELEMENTNAME){
			return new MessageReadBillingValues(element);
		}
		if (element.getNodeName() == MessageReadLoadProfiles.ELEMENTNAME){
			return new MessageReadLoadProfiles(element);
		}
		if (element.getNodeName() == MessageReadLogBook.ELEMENTNAME){
			return new MessageReadLogBook(element);
		}
		if (element.getNodeName() == MessageReadRegister.ELEMENTNAME){
			return new MessageReadRegister(element);
		}
		if (element.getNodeName() == MessageExecuteAction.ELEMENTNAME){
			return new MessageExecuteAction(element);
		}
		if (element.getNodeName() == MessagePostXmlFile.ELEMENTNAME){
			return new MessagePostXmlFile(element);
		}
		if (element.getNodeName() == MessagePerformanceTest.ELEMENTNAME){
			return new MessagePerformanceTest(element);
		}
		if (element.getNodeName() == MessageReadRegisterList.ELEMENTNAME){
			return new MessageReadRegisterList(element);
		}

		throw new ApplicationException("Cannot determine messageContent Type");
	}

}

package com.energyict.protocolimpl.iec1107.abba230;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FirmwareXMLHandler extends DefaultHandler {

	final int DEBUG=1;
	
	ABBA230DataIdentityFactory abba230DataIdentityFactory;
	
	public FirmwareXMLHandler(ABBA230DataIdentityFactory abba230DataIdentityFactory) {
		this.abba230DataIdentityFactory=abba230DataIdentityFactory;
	}

	public void startDocument() throws SAXException {
		
		if (DEBUG>=1)
			System.out.println("start document");
	}

	public void endDocument() throws SAXException {
		
		if (DEBUG>=1)
			System.out.println("end document");
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (DEBUG>=1)
			System.out.print(qName+" --> ");
		int len = attributes.getLength();
		for (int i=0;i<len;i++) {
			if (DEBUG>=1)
				System.out.print(attributes.getQName(i)+"="+attributes.getValue(i)+" ");
		}
		if (abba230DataIdentityFactory!=null) {
			if ((attributes.getLength()==3) && 
				(attributes.getQName(0).compareTo("id")==0) &&
				(attributes.getQName(1).compareTo("packet")==0) &&
				(attributes.getQName(2).compareTo("data")==0)) {
				
				int retry=0;
				while(true) {
					try {
						abba230DataIdentityFactory.setDataIdentityHex(attributes.getValue(0), Integer.parseInt(attributes.getValue(1),16), attributes.getValue(2));
						break;
					}
					catch(IOException e) {
						if (e.getMessage().indexOf("ERR6")>=0) {
							if (retry++>=2) {
								throw new SAXException("Fail after 1 retry, ",e);
							}
							else {
								try {
									Thread.sleep(1000);
								}
								catch(InterruptedException ex) {
									// absorb
								}
								if (DEBUG>=1)
									System.out.println("ERR6 received, retry...");
							}
						}
						else
							throw new SAXException(e);
					}
				}
			}
		}
		if (DEBUG>=1)
			System.out.println();
	}
	
}

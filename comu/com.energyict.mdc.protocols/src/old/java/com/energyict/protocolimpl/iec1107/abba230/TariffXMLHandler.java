package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

public class TariffXMLHandler extends DefaultHandler {

	final int DEBUG=0;
	long timeout;

	ABBA230DataIdentityFactory abba230DataIdentityFactory;

	public TariffXMLHandler(ABBA230DataIdentityFactory abba230DataIdentityFactory) {
		this.abba230DataIdentityFactory=abba230DataIdentityFactory;
	}

	public void startDocument() throws SAXException {

		if (DEBUG>=1){
			System.out.println("start document");
		}
	}

	public void endDocument() throws SAXException {

		if (DEBUG>=1) {
			System.out.println("end document");
		}
	}

	private static final int AUTHENTICATE_REARM_FIRMWARE=60000;

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (DEBUG>=1) {
			System.out.print(qName+" --> ");
		}
		int len = attributes.getLength();
		for (int i=0;i<len;i++) {
			if (DEBUG>=1) {
				System.out.print(attributes.getQName(i)+"="+attributes.getValue(i)+" ");
			}
		}
		if (abba230DataIdentityFactory!=null) {
				if ((attributes.getLength()==3) &&
					(attributes.getQName(0).compareTo("id")==0) &&
					(attributes.getQName(1).compareTo("packet")==0) &&
					(attributes.getQName(2).compareTo("data")==0)) {


					int retry=0;
					while(true) {
						try {

				            if (((long) (System.currentTimeMillis() - timeout)) > 0) {
				                timeout = System.currentTimeMillis() + AUTHENTICATE_REARM_FIRMWARE; // arm again...
				    			if (DEBUG>=1) {
									System.out.println("Authenticate...");
								}
								try {
									abba230DataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().authenticate();
								} catch (IOException e1) {
									throw new SAXException(e1);
								}
				            }
				            abba230DataIdentityFactory.setDataIdentityHex2(attributes.getValue(0), Integer.parseInt(attributes.getValue(1),16), attributes.getValue(2));
				            break;

						}
			            catch(FlagIEC1107ConnectionException e) {
							if (retry++>=5) {
								throw new SAXException("Fail after 4 retries, ",e);
							}
							else {
								if (DEBUG>=1) {
									System.out.println("FlagIEC1107ConnectionException exception received, retry...");
								}
							}
		                }
			            catch(IOException e) {
							throw new SAXException(e);
						}
					}
				}
		}
		if (DEBUG>=1) {
			System.out.println();
		}
	}

}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

public class FirmwareXMLHandler extends DefaultHandler {

	final int DEBUG=0;
	long timeout;
	ABBA230DataIdentityFactory abba230DataIdentityFactory;
	int pageNumber;
	int pageChecksum;

	public FirmwareXMLHandler(ABBA230DataIdentityFactory abba230DataIdentityFactory) {
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

	private int readChecksum(int page) throws IOException {
    	ABBA230DataIdentity di = new ABBA230DataIdentity("004", 2, 64, false, abba230DataIdentityFactory);
    	byte[] data = di.read(false,2,page);
    	return ProtocolUtils.getInt(data,0,2);
    }

	public void endElement(String uri,String localName,String qName) throws SAXException {
		if (abba230DataIdentityFactory!=null) {
			if (qName.compareTo("Page") == 0) {
				int readCheckSum;
				try {
					if (pageChecksum == -1) {
						return;
					}
					readCheckSum = readChecksum(pageNumber);
					if (readCheckSum != pageChecksum) {
						if (DEBUG>=1) {
							System.out.println("Error in checksum for page "+pageNumber);
						}
						throw new SAXException("Error in checksum for page "+pageNumber);
					}
					else {
						if (DEBUG>=1) {
							System.out.println("Checksum for page "+pageNumber+" ok");
						}
					}
				} catch (IOException e) {
					if (DEBUG>=1) {
						System.out.println("Error reading checksum for page "+pageNumber+", "+e.getMessage());
					}
					throw new SAXException(e);
				}

			}
		}
	}


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
			if (qName.compareTo("Page") == 0) {

				pageNumber = Integer.parseInt(attributes.getValue(0));

				if (attributes.getLength()==2) {
					pageChecksum = Integer.parseInt(attributes.getValue(1),16);
				} else {
					pageChecksum = -1;
				}

				if (DEBUG>=1) {
					System.out.println("pageNumber="+pageNumber+", pageChecksum=0x"+Integer.toHexString(pageChecksum));
				}
			} // Page tag
			else if (qName.compareTo("Write") == 0) {
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
							if (e.getMessage().indexOf("ERR6")>=0) {
								if (retry++>=2) {
									throw new SAXException("Fail after 1 retry, ",e);
								}
								else {
									try {
										abba230DataIdentityFactory.getProtocolLink().getFlagIEC1107Connection().authenticate();
									} catch (IOException e1) {
										throw new SAXException(e1);
									}
									if (DEBUG>=1) {
										System.out.println("ERR6 received, retry...");
									}
								}
							} else {
								throw new SAXException(e);
							}
						}
					}
				}
			} // Write tag
		}
		if (DEBUG>=1) {
			System.out.println();
		}
	}

}

package com.energyict.protocolimpl.iec1107.abba230;

import java.io.*;

import javax.xml.parsers.*;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FirmwareSaxParser {

	ABBA230DataIdentityFactory abba230DataIdentityFactory;

	public FirmwareSaxParser(ABBA230DataIdentityFactory abba230DataIdentityFactory) {
		this.abba230DataIdentityFactory=abba230DataIdentityFactory;
	}
	
	protected void start(String str) {
		start(str,true);
	}
	protected void start(String str,boolean isfileRef) {
		try {
			if (isfileRef) {
				File file = new File(str);
				byte[] data = new byte[(int)file.length()];
				FileInputStream fis;
				fis = new FileInputStream(file);
				fis.read(data);
				fis.close();
				parse(new String(data));
			}
			else parse(str);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void parse(String data) {
        try {
            byte[] bai = data.getBytes();
            InputStream is = (InputStream) new ByteArrayInputStream(bai);
            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            FirmwareXMLHandler myHandler = new FirmwareXMLHandler(abba230DataIdentityFactory);
            saxParser.parse(is, myHandler);
            
        } catch (ParserConfigurationException e) {
        	e.printStackTrace();
        } catch (SAXException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
         }
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FirmwareSaxParser o = new FirmwareSaxParser(null);
		o.start("C:/Documents and Settings/kvds/My Documents/projecten/ESB/firmware.xml",true);
		//o.start("C:/Documents and Settings/kvds/My Documents/projecten/ESB/tariff2.xml");
		//o.start("C:/Documents and Settings/kvds/My Documents/projecten/ESB/tariff3.xml");
		

	}

}

package com.energyict.protocolimpl.iec1107.abba230;

import java.io.*;

import javax.xml.parsers.*;

import org.xml.sax.SAXException;

import com.energyict.cbo.NestedIOException;

public class TariffSaxParser {

	ABBA230DataIdentityFactory abba230DataIdentityFactory;

	public TariffSaxParser(ABBA230DataIdentityFactory abba230DataIdentityFactory) {
		this.abba230DataIdentityFactory=abba230DataIdentityFactory;
	}
	
	protected void start(String str) throws IOException {
		start(str,true);
	}
	protected void start(String str,boolean isfileRef) throws IOException {
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
			throw new NestedIOException(e);
		} 
	}
	
	private void parse(String data) throws IOException {
        try {
            byte[] bai = data.getBytes();
            InputStream is = (InputStream) new ByteArrayInputStream(bai);
            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            TariffXMLHandler myHandler = new TariffXMLHandler(abba230DataIdentityFactory);
            saxParser.parse(is, myHandler);
            
        } catch (ParserConfigurationException e) {
        	throw new NestedIOException(e);
        } catch (SAXException e) {
        	throw new NestedIOException(e);
        }
	}
	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		TariffSaxParser o = new TariffSaxParser(null);
//		o.start("C:/Documents and Settings/kvds/My Documents/projecten/ESB/tariff1.xml");
//		o.start("C:/Documents and Settings/kvds/My Documents/projecten/ESB/tariff2.xml");
//		o.start("C:/Documents and Settings/kvds/My Documents/projecten/ESB/tariff3.xml");
//		
//
//	}

}

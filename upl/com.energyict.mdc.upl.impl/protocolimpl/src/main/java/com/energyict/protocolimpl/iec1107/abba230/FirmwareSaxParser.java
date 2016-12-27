package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.mdc.io.NestedIOException;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class FirmwareSaxParser {

	ABBA230DataIdentityFactory abba230DataIdentityFactory;

	public FirmwareSaxParser(ABBA230DataIdentityFactory abba230DataIdentityFactory) {
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
			else {
				parse(str);
			}

		} catch (FileNotFoundException e) {
			throw new NestedIOException(e);
		}
	}

	private void parse(String data) throws IOException {
        try {
            byte[] bai = data.getBytes();
            InputStream is = new ByteArrayInputStream(bai);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            FirmwareXMLHandler myHandler = new FirmwareXMLHandler(abba230DataIdentityFactory);
            saxParser.parse(is, myHandler);

        } catch (ParserConfigurationException | SAXException e) {
        	throw new NestedIOException(e);
        }
	}

}
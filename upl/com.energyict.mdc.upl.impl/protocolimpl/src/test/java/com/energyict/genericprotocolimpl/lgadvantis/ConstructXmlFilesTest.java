package com.energyict.genericprotocolimpl.lgadvantis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import junit.framework.TestCase;

public class ConstructXmlFilesTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testXmlFileReader(){
		try {
			String xml = "<?xml version=\"1.0\"?>/n" +
			"<releve-cpl version=\"1.0\"/n>" +
			"<telerel-rq ident=\"040000000001\"/n>" +
			"<write-rq>/n" +
			"<exp-cmd>/n" +
			"<start-task0/>/n" +
			"</exp-cmd>/n" +
			"</write-rq>/n" +
			"</telerel-rq>/n" +
			"</releve-cpl>/n";
			
			File file = new File("C:/tmp/testfile.xml");
			FileOutputStream output = new FileOutputStream(file);
			PrintStream p =  new PrintStream(output);
			p.println(xml);
			p.close();
			
			String cXml = ConstructXmlFiles.getXmlFile("C:/tmp/testfile.xml");
			
			assertEquals(xml,cXml);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
}

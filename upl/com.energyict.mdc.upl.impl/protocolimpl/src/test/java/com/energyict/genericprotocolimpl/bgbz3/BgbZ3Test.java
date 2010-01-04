/**
 * 
 */
package com.energyict.genericprotocolimpl.bgbz3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.energyict.dialer.connection.ConnectionException;

/**
 * @author gna
 * @since 23-dec-2009
 * 
 */
public class BgbZ3Test {

	/** The xml file we are testing with */
	private final static String xmlString = "<?xml version='1.0' encoding='ISO-8859-1' ?>" +
			"<Logger><Meter Sn=''>" +
			"<Channel Obis='1-0:1.8.0.255'>2722</Channel>" +
			"<Channel Obis='0-0:0.0.0.0'>6</Channel>" +
			"<Channel Obis='1-0:15.7.0.255'>68</Channel>" +
			"<Channel Obis='1-0:3.7.0.255'>105</Channel>" +
			"<Channel Obis='1-0:13.7.0.255'>539</Channel>" +
			"<Channel Obis='1-0:32.7.0.255'>2377</Channel>" +
			"<Channel Obis='1-0:52.7.0.255'>2347</Channel>" +
			"<Channel Obis='1-0:72.7.0.255'>2369</Channel>" +
			"<Channel Obis='1-0:31.7.0.255'>18</Channel>" +
			"<Channel Obis='1-0:51.7.0.255'>18</Channel>" +
			"<Channel Obis='1-0:71.7.0.255'>18</Channel>" +
			"<Channel Obis='1-0:132.7.0.255'>3</Channel>" +
			"<Channel Obis='1-0:131.7.0.255'>2</Channel>" +
			"<Channel Obis='1-0:130.7.0.255'>2</Channel>" +
			"<Channel Obis='1-0:129.7.0.255'>5</Channel>" +
			"<Channel Obis='1-0:33.7.0.255'>547</Channel>" +
			"<Channel Obis='1-0:53.7.0.255'>547</Channel>" +
			"<Channel Obis='1-0:73.7.0.255'>547</Channel>" +
			"<Channel Obis='1-0:133.7.0.255'>3500</Channel>" +
			"</Meter></Logger>";
	
	/** The used Logger */
	private static Logger logger;
	/** An instance of the class under test */
	private static BgbZ3 bgbZ3;
	
	@BeforeClass
	public static void setUpOnce() {
		logger = Logger.getLogger("global");
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		bgbZ3 = new BgbZ3();
	}

	/**
	 * Test the requestData method
	 * The method should readin the xml file from the meter
	 */
	@Test
	public void requestDataTest() {
		try {
			File file = new File(BgbZ3Test.class.getResource("/com/energyict/genericprotocolimpl/bgbz3/tempdata.xml").getFile());
			URL tempURL = file.toURL();
			bgbZ3.setUrl(tempURL);
			bgbZ3.requestData();
			
			assertEquals(0,xmlString.compareToIgnoreCase(bgbZ3.getResponseData().toString()));
			
		} catch (Exception e) {
			logger.log(Level.ALL, e.getMessage());
			fail();
		}
	}

	/**
	 * Construct the URL from an IP-address
	 */
	@Test
	public void constructUrlTest() {
		try {
			bgbZ3.constructUrl("10.0.0.37");
			assertEquals(bgbZ3.getUrl(), new URL("http://10.0.0.37/ChannelData.xml"));
		} catch (ConnectionException e) {
			logger.log(Level.ALL, e.getMessage());
			fail();
		} catch (MalformedURLException e) {
			logger.log(Level.ALL, e.getMessage());
			fail();
		}
	}
}

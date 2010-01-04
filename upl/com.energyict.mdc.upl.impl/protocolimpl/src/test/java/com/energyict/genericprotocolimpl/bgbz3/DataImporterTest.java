/**
 * 
 */
package com.energyict.genericprotocolimpl.bgbz3;


import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author gna
 * @since 24-dec-2009
 *
 */
public class DataImporterTest {
	
	/** An instance of the class under test */
	private DataImporter di;

	/**
	 * Convert the ObisCode from the device (containing dashes and colons) to a dotted notation obisCode
	 */
	@Test
	public void convertToDottedObisCodeTest(){
		
		di = new DataImporter(null);
		String obisCode = "1-0:1.8.0.255";
		assertTrue("1.0.1.8.0.255".equalsIgnoreCase(di.convertToDottedObisCode(obisCode)));
		
	}

}

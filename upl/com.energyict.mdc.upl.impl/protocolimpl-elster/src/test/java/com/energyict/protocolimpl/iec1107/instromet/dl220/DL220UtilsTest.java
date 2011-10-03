/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;


import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import org.junit.*;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author gna
 * @since 5-mrt-2010
 *
 */
public class DL220UtilsTest {

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test to conversion from minutes and hours to seconds
	 */
	@Test
	public final void convertQuantityToSecondsTest(){
		String[] quantity = {"60", "Minuten"};
		int seconden = DL220Utils.convertQuantityToSeconds(quantity);
		assertEquals(3600, seconden);
		
		quantity = new String[]{"60", "Stunden"};
		seconden = DL220Utils.convertQuantityToSeconds(quantity);
		assertEquals(216000, seconden);
	}
	
	/**
	 * Test to fetch the correct next records
	 */
	@Test
	public final void getNextRecordTest(){
		String[] expectedIntervals = {"(8488)(504)(2010-03-07,09:15:00)(130)(130)(0)(15)(0x8105)(CRC Ok)",
				"(8489)(505)(2010-03-07,09:20:00)(130)(130)(0)(15)(0x8105)(CRC Ok)",
				"(8490)(506)(2010-03-07,09:25:00)(130)(130)(0)(15)(0x8105)(CRC Ok)",
				"(8491)(507)(2010-03-07,09:30:00)(130)(130)(0)(15)(0x8105)(CRC Ok)",
				"(8492)(508)(2010-03-07,09:35:00)(130)(130)(0)(15)(0x8105)(CRC Ok)"};
		String rawData = "(8488)(504)(2010-03-07,09:15:00)(130)(130)(0)(15)(0x8105)(CRC Ok)\r\n" +
				"(8489)(505)(2010-03-07,09:20:00)(130)(130)(0)(15)(0x8105)(CRC Ok)\r\n" +
				"(8490)(506)(2010-03-07,09:25:00)(130)(130)(0)(15)(0x8105)(CRC Ok)\r\n" +
				"(8491)(507)(2010-03-07,09:30:00)(130)(130)(0)(15)(0x8105)(CRC Ok)" +
				"(8492)(508)(2010-03-07,09:35:00)(130)(130)(0)(15)(0x8105)(CRC Ok)\r\n";
		int offset = 0;
		for(int i = 0; i < 5; i++){
			assertEquals(expectedIntervals[i], DL220Utils.getNextRecord(rawData, offset, 9));
			offset += expectedIntervals[i].length();
		}
		
	}
	
	/**
	 * Test to see if you get the desired value from a bracket-string.<br>
	 * Intervals are returned in this format from the DL220
	 */
	@Test
	public final void getTextBetweenBracketsStartingFromTest(){
		String text = "(one)(two)(three)(four)(five)";
		int index = 2;
		assertEquals("three", DL220Utils.getTextBetweenBracketsFromIndex(text, index));
		index = 0;
		assertEquals("one", DL220Utils.getTextBetweenBracketsFromIndex(text, index));
		index = 4;
		assertEquals("five", DL220Utils.getTextBetweenBracketsFromIndex(text, index));
		try {
			index = 7;
			DL220Utils.getTextBetweenBracketsFromIndex(text, index);
		} catch (IllegalArgumentException e) {
			if(!e.getLocalizedMessage().equalsIgnoreCase("Could not return the request text, index to large(7).")){
				fail("Received a not-excpected exception: " + e);
			}
		}
	}
	
	/**
	 * Test to count the number of the same expression in one string
	 */
	@Test
	public final void countNumberOfSameChars(){
		String text = "FFAABCDFFESFF";
		String regex1 = "F";
		String regex2 = "FF";
		assertEquals(6, DL220Utils.countNumberOfSameChars(text, regex1, regex1.length()));
		assertEquals(3, DL220Utils.countNumberOfSameChars(text, regex2, regex2.length()));
			
	}
	
	/**
	 * Test to calculate the number of objects in the capturedObject String
	 */
	@Test
	public final void getNumberOfObjectsTest(){
		try {
			String capturedObjects = "(GONr)(AONr)(Zeit)(V1.G)(V1.P)(St.1)(StSy)(Er)(Check)";
			assertEquals(9, DL220Utils.getNumberOfObjects(capturedObjects));
			
			try {
				capturedObjects = "(GONr)(AONr)(Zeit)(V1.G)(V1.P)(St.1)(StSy)(Er)(Check";
				DL220Utils.getNumberOfObjects(capturedObjects);
			} catch (Exception e) {
				if(!(e.getLocalizedMessage().indexOf("Number of open brackets does not match the number of closed brackets (9 - 8)") > -1)){
					e.printStackTrace();
					fail();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	/**
	 * Test the Unit conversion
	 */
	@Test
	public final void getUnitFromStringTest(){
		assertEquals(Unit.get(BaseUnit.CUBICMETER, 0), DL220Utils.getUnitFromString("m3"));
		assertEquals(Unit.get(BaseUnit.WATT), DL220Utils.getUnitFromString("W"));
		assertEquals(Unit.get(BaseUnit.WATTHOUR), DL220Utils.getUnitFromString("Wh"));
		assertEquals(Unit.get(BaseUnit.WATT, 3), DL220Utils.getUnitFromString("kW"));
		assertEquals(Unit.get(BaseUnit.WATTHOUR, 3), DL220Utils.getUnitFromString("kWh"));
	}
}

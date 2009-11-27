package com.energyict.protocolimpl.modbus.socomec.a40;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;

/**
 * Test class for the ProfileData parser
 * 
 * <pre>
 * TODO: 
 * - multiple channels 
 * - powerUp/PowerDown in multiple blocks !!
 * </pre>
 * 
 * @author gna
 * 
 */
public class SocomecProfileParserTest {

	/**
	 * This testBlock contains 54 values
	 */
	private static int[] profileMemoryBlock1 = new int[] { 32, 32, 32, 32, 33,
			32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
			32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
			32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	/**
	 * This testBlock contains 100 values
	 */
	private static int[] profileMemoryBlock2 = new int[] { 18, 18, 18, 19, 19,
			19, 19, 19, 19, 19, 19, 19, 19, 19, 18, 18, 19, 19, 19, 19, 19, 18,
			18, 18, 19, 18, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19,
			19, 18, 19, 19, 19, 19, 19, 19, 19, 19, 18, 18, 19, 19, 19, 19, 19,
			24, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
			32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
			32, 32, 32, 32, 32, 32, 32, 32, 32, 32 };

	/**
	 * This testBlock contains a PowerUp/PowerDown AND 62 intervals
	 */
	private static int[] profileMemoryBlock3 = new int[] { 32, 32, 32, 32, 33,
			32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
			32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
			32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
			32, 32, 32, 32, 32, 60177, 4112, 9737, 64282, 2587, 2057, 6, 64282,
			2590, 9, 33, 33, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	/**
	 * This testBlock contains multiple powerUp/Downs
	 */
	private static int[] profileMemoryBlock4 = new int[] { 32, 32, 32, 32, 32,
			32, 32, 32, 32, 32, 32, 60177, 4112, 9737, 64282, 2587, 2057, 6,
			64282, 2590, 9, 33, 33, 60186, 2826, 12809, 64282, 3074, 11273, 15,
			64282, 3087, 9, 60186, 3094, 14601, 64282, 3095, 2057, 8, 64282,
			3102, 9, 19, 19, 19, 60186, 3356, 3849, 64282, 3356, 6921, 1,
			64282, 3358, 9, 19, 468, 522, 521, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0 };

	/** This testBlock contains multiple powerUps/Downs in one interval */
	private static int[] profileMemoryBlock5 = new int[] { 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 499,
			520, 523, 60187, 2063, 8713, 64283, 2063, 12809, 60187, 2064, 1545,
			64283, 2064, 6665, 60187, 2064, 14089, 64283, 2065, 4105, 443,
			64283, 2078, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0 };

	/** This testBlock contains a powerUp at the end of the block */
	private static int[] profileMemoryBlock6_1 = new int[] { 64283, 2065, 4105,
			443, 64283, 2078, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	/** This testBlock contains a powerDown at the start of the block */
	private static int[] profileMemoryBlock6_2 = new int[] { 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			499, 520, 523, 60187, 2063, 8713, 64283, 2063, 12809, 60187, 2064,
			1545, 64283, 2064, 6665, 60187, 2064, 14089 };

	/** The current {@link SocomecProfileParser} used */
	private static SocomecProfileParser profileParser;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		profileParser = new SocomecProfileParser();
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test the profileParser block
	 */
	@Test
	public void parseProfileDataBlockTest() {

		/* With no startDate the parser should fail */
		try {
			profileParser.parseProfileDataBlock(profileMemoryBlock1);
		} catch (IOException e) {
			assertTrue(e.getMessage().equals("StartDate can not be empty."));
		}

		/* With no memoryPointer the parser should return an empty list */
		Date lastUpdate;
		List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
		try {
			lastUpdate = new Date(Long.valueOf("1258388100000"));
			profileParser.setLastUpdate(lastUpdate);
			profileParser.parseProfileDataBlock(profileMemoryBlock1);
			intervalDatas = profileParser.getIntervalDatas();
			assertEquals(0, intervalDatas.size());
		} catch (NumberFormatException e1) {
			fail(e1.getMessage());
		} catch (IOException e1) {
			fail(e1.getMessage());
		}

		/* Two normal blocks are read sequentially */
		try {
			intervalDatas = new ArrayList<IntervalData>();
			lastUpdate = new Date(Long.valueOf("1258388100000"));
			profileParser.setLastUpdate(lastUpdate);
			profileParser.addMemoryPointer(53);
			profileParser.setIntervalLength(900);
			profileParser.parseProfileDataBlock(profileMemoryBlock1);
			intervalDatas = profileParser.getIntervalDatas();
			assertEquals(52, intervalDatas.size());

			assertEquals(lastUpdate, intervalDatas.get(0).getEndTime());

			assertEquals(new Date(Long.valueOf("1258342200000")), intervalDatas
					.get(51).getEndTime());

			profileParser.addMemoryPointer(99);
			profileParser.parseProfileDataBlock(profileMemoryBlock2);
			intervalDatas = profileParser.getIntervalDatas();
			assertEquals(new Date(Long.valueOf("1258341300000")), intervalDatas
					.get(52).getEndTime());
			assertEquals(151, intervalDatas.size());
			assertEquals(200, profileParser.getVirtualMemory().length);

		} catch (NumberFormatException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		/* Test for powerUps/PowerDowns */
		try {
			profileParser = new SocomecProfileParser();
			intervalDatas = new ArrayList<IntervalData>();
			lastUpdate = new Date(Long.valueOf("1259233200000"));
			profileParser.setLastUpdate(lastUpdate);
			profileParser.addMemoryPointer(72);
			profileParser.setIntervalLength(900);
			profileParser.parseProfileDataBlock(profileMemoryBlock3);
			intervalDatas = profileParser.getIntervalDatas();
			assertEquals(62, intervalDatas.size());

			profileParser = new SocomecProfileParser();
			profileParser
					.setLastUpdate(new Date(Long.valueOf("1259245800000")));
			profileParser.addMemoryPointer(59);
			profileParser.setIntervalLength(900);
			profileParser.parseProfileDataBlock(profileMemoryBlock4);
			intervalDatas = profileParser.getIntervalDatas();
			assertEquals(22, intervalDatas.size());
		} catch (NumberFormatException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		/* Test for multiple PU/PD in one interval */
		try {
			profileParser = new SocomecProfileParser();
			intervalDatas = new ArrayList<IntervalData>();
			lastUpdate = new Date(Long.valueOf("1259310600000"));
			profileParser.setLastUpdate(lastUpdate);
			profileParser.addMemoryPointer(52);
			profileParser.setIntervalLength(900);
			profileParser.parseProfileDataBlock(profileMemoryBlock5);
			intervalDatas = profileParser.getIntervalDatas();
			assertEquals(30, intervalDatas.size());

			/* Test the meterEvents */
			assertEquals(6, profileParser.getMeterEvents().size());
			assertEquals(profileParser.getMeterEvents().get(0).getEiCode(),
					MeterEvent.POWERUP);
			assertEquals(profileParser.getMeterEvents().get(5).getEiCode(),
					MeterEvent.POWERDOWN);

			/* Check the intervalStatusses */
			assertEquals(IntervalData.POWERDOWN | IntervalData.POWERUP,
					intervalDatas.get(0).getEiStatus());

			/* Test for PU/PD in different blocks */
			try {
				profileParser = new SocomecProfileParser();
				intervalDatas = new ArrayList<IntervalData>();
				lastUpdate = new Date(Long.valueOf("1259310600000"));
				profileParser.setLastUpdate(lastUpdate);
				profileParser.addMemoryPointer(6);
				profileParser.setIntervalLength(900);
				profileParser.parseProfileDataBlock(profileMemoryBlock6_1);
				intervalDatas = profileParser.getIntervalDatas();
				assertEquals(1, intervalDatas.size());
				/* Check the intervalStatusses */
				assertEquals(0, intervalDatas.get(0).getEiStatus());
				// add second part
				profileParser.addMemoryPointer(profileMemoryBlock6_2.length);
				profileParser.parseProfileDataBlock(profileMemoryBlock6_2);
				intervalDatas = profileParser.getIntervalDatas();
				assertEquals(30, intervalDatas.size());
				/* Check the intervalStatusses */
				assertEquals(IntervalData.POWERDOWN | IntervalData.POWERUP,
						intervalDatas.get(0).getEiStatus());

			} catch (NumberFormatException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			}
		} catch (NumberFormatException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

	}

}

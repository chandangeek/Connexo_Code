package com.energyict.protocolimpl.iec1107.emh.lzqj;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.siemens7ED62.SCTMDumpData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author gna
 * @since 4-feb-2010
 *
 */
public class LZQJProfileTest {

	private static Logger logger;
	private static LZQJ lzqj;
	private static LZQJRegistry lzqjRegistry;
	private static LZQJProfile lzqjProfile;
	private static Properties properties;

	@BeforeClass
	public static void setUpOnce() {
		try {
			logger = Logger.getLogger("global");
			lzqj = new LZQJ();
			properties = new Properties();
			properties.setProperty("ChannelMap", "0+8,0+8,0+8,0+8");
			properties.setProperty("FixedProfileTimeZone", "0");
			properties.setProperty("ProfileInterval", "300");
			lzqj.setUPLProperties(properties);
			lzqj.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
			lzqjRegistry = new LZQJRegistry(lzqj, lzqj);
			lzqjProfile = new LZQJProfile(lzqj, lzqj, lzqjRegistry);
		} catch (MissingPropertyException e) {
			if (logger != null) {
				logger.log(Level.INFO, e.getMessage());
			}
			fail();
		} catch (InvalidPropertyException e) {
			if (logger != null) {
				logger.log(Level.INFO, e.getMessage());
			}
			fail();
		}
	}

	@Test
	public final void getProfileDataTestSummerToWinter() {

		try {

			System.out.println("TimeZone : " + lzqj.getTimeZone());
			File file = new File(SCTMDumpData.class.getClassLoader().getResource(
					"com/energyict/protocolimpl/iec1107/emh/lzqj/LZQJProfileSummerToWinter.bin").getFile());
			FileInputStream fis = new FileInputStream(file);
			byte[] content = new byte[(int) file.length()];
			fis.read(content);
			fis.close();
			lzqj.profileHelperSetter(true);
			ProfileData pd = lzqjProfile.buildProfileData(content);

			logger.info(pd.toString());
			assertEquals(new BigDecimal(new BigInteger("18875"), 3), ((IntervalValue)pd.getIntervalData(34).getIntervalValues().get(0)).getNumber());
			assertEquals(new Date(Long.valueOf("1256432100000")), pd.getIntervalData(34).getEndTime());
			assertEquals(new BigDecimal(new BigInteger("18876"), 3), ((IntervalValue)pd.getIntervalData(35).getIntervalValues().get(0)).getNumber());
			assertEquals(new Date(Long.valueOf("1256432400000")), pd.getIntervalData(35).getEndTime());
			assertEquals(new BigDecimal(new BigInteger("18877"), 3), ((IntervalValue)pd.getIntervalData(36).getIntervalValues().get(0)).getNumber());
			assertEquals(new Date(Long.valueOf("1256432700000")), pd.getIntervalData(36).getEndTime());

		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			fail();
		}
	}

	@Test
	public final void getProfileDataTestWinterToSummer() {

		try {

			System.out.println("TimeZone : " + lzqj.getTimeZone());
			File file = new File(SCTMDumpData.class.getClassLoader().getResource(
					"com/energyict/protocolimpl/iec1107/emh/lzqj/LZQJProfileWinterToSummer.bin").getFile());
			FileInputStream fis = new FileInputStream(file);
			byte[] content = new byte[(int) file.length()];
			fis.read(content);
			fis.close();
			lzqj.profileHelperSetter(true);
			ProfileData pd = lzqjProfile.buildProfileData(content);

			logger.info(pd.toString());
			assertEquals(new BigDecimal(new BigInteger("18850"), 3), ((IntervalValue)pd.getIntervalData(9).getIntervalValues().get(0)).getNumber());
			assertEquals(new Date(Long.valueOf("1269737700000")), pd.getIntervalData(9).getEndTime());
			assertEquals(new BigDecimal(new BigInteger("18851"), 3), ((IntervalValue)pd.getIntervalData(10).getIntervalValues().get(0)).getNumber());
			assertEquals(new Date(Long.valueOf("1269738000000")), pd.getIntervalData(10).getEndTime());
			assertEquals(new BigDecimal(new BigInteger("18852"), 3), ((IntervalValue)pd.getIntervalData(11).getIntervalValues().get(0)).getNumber());
			assertEquals(new Date(Long.valueOf("1269738300000")), pd.getIntervalData(11).getEndTime());

		} catch (IOException e) {
			logger.log(Level.INFO, e.getMessage());
			fail();
		}
	}
}

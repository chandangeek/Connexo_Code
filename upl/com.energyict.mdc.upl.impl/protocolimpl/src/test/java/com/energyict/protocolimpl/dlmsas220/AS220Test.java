/**
 *
 */
package com.energyict.protocolimpl.dlmsas220;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import com.energyict.protocolimpl.dlms.as220.AS220;

/**
 * @author jme
 *
 */
public class AS220Test {

	private static final String	ELSTER_DEVICE_ID	= "GEC";

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#AS220()}.
	 */
	@Test
	@Ignore
	public final void testAS220() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#getgMeter()}.
	 */
	@Test
	public final void testGetgMeter() {
		AS220 as220 = new AS220();
		assertNotNull(as220.getgMeter());
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#geteMeter()}.
	 */
	@Test
	public final void testGeteMeter() {
		AS220 as220 = new AS220();
		assertNotNull(as220.geteMeter());
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#setTime()}.
	 */
	@Test
	@Ignore
	public final void testSetTime() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#getTime()}.
	 */
	@Test
	@Ignore
	public final void testGetTime() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#getProtocolVersion()}.
	 */
	@Test
	public final void testGetProtocolVersion() {
		assertNotNull(new AS220().getProtocolVersion());
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#getFirmwareVersion()}.
	 */
	@Test
	@Ignore
	public final void testGetFirmwareVersion() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#readRegister(com.energyict.obis.ObisCode)}.
	 */
	@Test
	@Ignore
	public final void testReadRegister() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#translateRegister(com.energyict.obis.ObisCode)}.
	 */
	@Test
	@Ignore
	public final void testTranslateRegister() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#getMessaging()}.
	 */
	@Test
	@Ignore
	public final void testGetMessaging() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#getMessageCategories()}.
	 */
	@Test
	@Ignore
	public final void testGetMessageCategories() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#writeMessage(com.energyict.protocol.messaging.Message)}.
	 */
	@Test
	@Ignore
	public final void testWriteMessage() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#writeTag(com.energyict.protocol.messaging.MessageTag)}.
	 */
	@Test
	@Ignore
	public final void testWriteTag() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#writeValue(com.energyict.protocol.messaging.MessageValue)}.
	 */
	@Test
	@Ignore
	public final void testWriteValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#applyMessages(java.util.List)}.
	 */
	@Test
	@Ignore
	public final void testApplyMessages() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.AS220#queryMessage(com.energyict.protocol.MessageEntry)}.
	 */
	@Test
	@Ignore
	public final void testQueryMessage() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#DLMSSNAS220()}.
	 */
	@Test
	@Ignore
	public final void testDLMSSNAS220() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getDeviceID()}.
	 */
	@Test
	public final void testGetDeviceID() {
		assertNotNull(new AS220().getDeviceID());
		assertEquals(ELSTER_DEVICE_ID, new AS220().getDeviceID());
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getDLMSConnection()}.
	 */
	@Test
	@Ignore
	public final void testGetDLMSConnection() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#init(java.io.InputStream, java.io.OutputStream, java.util.TimeZone, java.util.logging.Logger)}.
	 */
	@Test
	@Ignore
	public final void testInit() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getProfileInterval()}.
	 */
	@Test
	@Ignore
	public final void testGetProfileInterval() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getNumberOfChannels()}.
	 */
	@Test
	@Ignore
	public final void testGetNumberOfChannels() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getMeterReading(java.lang.String)}.
	 */
	@Test
	@Ignore
	public final void testGetMeterReadingString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getMeterReading(int)}.
	 */
	@Test
	@Ignore
	public final void testGetMeterReadingInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#connect()}.
	 */
	@Test
	@Ignore
	public final void testConnect() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getRegistersInfo(int)}.
	 */
	@Test
	@Ignore
	public final void testGetRegistersInfo() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#disconnect()}.
	 */
	@Test
	@Ignore
	public final void testDisconnect() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#requestSAP()}.
	 */
	@Test
	@Ignore
	public final void testRequestSAP() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getProfileData(boolean)}.
	 */
	@Test
	@Ignore
	public final void testGetProfileDataBoolean() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getProfileData(java.util.Date, boolean)}.
	 */
	@Test
	@Ignore
	public final void testGetProfileDataDateBoolean() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getProfileData(java.util.Date, java.util.Date, boolean)}.
	 */
	@Test
	@Ignore
	public final void testGetProfileDataDateDateBoolean() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getLowLevelSecurity()}.
	 */
	@Test
	@Ignore
	public final void testGetLowLevelSecurity() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#setProperties(java.util.Properties)}.
	 */
	@Test
	@Ignore
	public final void testSetProperties() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getRegister(java.lang.String)}.
	 */
	@Test
	@Ignore
	public final void testGetRegister() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#setRegister(java.lang.String, java.lang.String)}.
	 */
	@Test
	@Ignore
	public final void testSetRegister() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#initializeDevice()}.
	 */
	@Test
	@Ignore
	public final void testInitializeDevice() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getRequiredKeys()}.
	 */
	@Test
	@Ignore
	public final void testGetRequiredKeys() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getOptionalKeys()}.
	 */
	@Test
	@Ignore
	public final void testGetOptionalKeys() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#requestConfigurationProgramChanges()}.
	 */
	@Test
	@Ignore
	public final void testRequestConfigurationProgramChanges() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#requestTimeZone()}.
	 */
	@Test
	@Ignore
	public final void testRequestTimeZone() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#isRequestTimeZone()}.
	 */
	@Test
	@Ignore
	public final void testIsRequestTimeZone() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getTimeZone()}.
	 */
	@Test
	@Ignore
	public final void testGetTimeZone() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#setCache(java.lang.Object)}.
	 */
	@Test
	@Ignore
	public final void testSetCache() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getCache()}.
	 */
	@Test
	@Ignore
	public final void testGetCache() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#fetchCache(int)}.
	 */
	@Test
	@Ignore
	public final void testFetchCache() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#updateCache(int, java.lang.Object)}.
	 */
	@Test
	@Ignore
	public final void testUpdateCache() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getFileName()}.
	 */
	@Test
	public final void testGetFileName() {
		assertNotNull(new AS220().getFileName());
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#enableHHUSignOn(com.energyict.dialer.core.SerialCommunicationChannel)}.
	 */
	@Test
	@Ignore
	public final void testEnableHHUSignOnSerialCommunicationChannel() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#enableHHUSignOn(com.energyict.dialer.core.SerialCommunicationChannel, boolean)}.
	 */
	@Test
	@Ignore
	public final void testEnableHHUSignOnSerialCommunicationChannelBoolean() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getHHUDataReadout()}.
	 */
	@Test
	@Ignore
	public final void testGetHHUDataReadout() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#release()}.
	 */
	@Test
	@Ignore
	public final void testRelease() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getMeterConfig()}.
	 */
	@Test
	@Ignore
	public final void testGetMeterConfig() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getLogger()}.
	 */
	@Test
	@Ignore
	public final void testGetLogger() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getCosemObjectFactory()}.
	 */
	@Test
	@Ignore
	public final void testGetCosemObjectFactory() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getReference()}.
	 */
	@Test
	@Ignore
	public final void testGetReference() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getStoredValues()}.
	 */
	@Test
	@Ignore
	public final void testGetStoredValues() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getChannelMap()}.
	 */
	@Test
	@Ignore
	public final void testGetChannelMap() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#isDebug()}.
	 */
	@Test
	public final void testIsDebug() {
		assertFalse("AS220 protocol checkedin in debug mode!", new AS220().isDebug());
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#setDebug(boolean)}.
	 */
	@Test
	@Ignore
	public final void testSetDebug() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#setDstFlag(int)}.
	 */
	@Test
	@Ignore
	public final void testSetDstFlag() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getDstFlag()}.
	 */
	@Test
	@Ignore
	public final void testGetDstFlag() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#setiRoundtripCorrection(int)}.
	 */
	@Test
	@Ignore
	public final void testSetiRoundtripCorrection() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.dlms.as220.DLMSSNAS220#getRoundTripCorrection()}.
	 */
	@Test
	@Ignore
	public final void testGetRoundTripCorrection() {
		fail("Not yet implemented"); // TODO
	}

}

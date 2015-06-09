package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.exceptions.ComServerExceptionFactoryProvider;
import com.energyict.mdc.exceptions.DefaultComServerExceptionFactoryProvider;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.SMSFrame;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author sva
 * @since 28/06/13 - 11:38
 */
@RunWith(MockitoJUnitRunner.class)
public class SmsHandlerTest {

    @Mock
    DeviceIdentifier deviceIdentifier;

    /**
     * TODO: update with additional tests for following cases
     *
     * Query against register
     * ACK
     * NACK
     */

    @BeforeClass
    public static void doBefore() {
        ComServerExceptionFactoryProvider.instance.set(new DefaultComServerExceptionFactoryProvider());
        CollectedDataFactoryProvider.instance.set(new DefaultCollectedDataFactoryProvider());
    }

    @Test(expected = CTRException.class)
    public void testHandlingOfUnsupportedSMS() throws Exception {
        SMSFrame smsFrame = new SMSFrame().parse(ProtocolTools.getBytesFromHexString("000000FF5600123456789000000F00000002002000060A0A150E0A000101400F00000001000000010A0A150E0A0001013A0F00000001000000010A0A150E0A000101460F00000001000000010A0A0A0E0A000101350F00000001000000010A0A080E0A000101350F00000001000000010A0A070E0A00010135FF0000000100000001", ""), 0);
        SmsHandler handler = new SmsHandler(deviceIdentifier, getAllRelevantProperties());

        // Business methods
        try {
            handler.parseSMSFrame(smsFrame);
        } catch (CTRException e) {
            assertEquals("Unexpected CTRException message", "Unrecognized data structure in SMS. Expected array of event records, trace_C response, tableDEC(F), ACK or NACK response.", e.getMessage());
            throw e;
        }
    }

    @Test(expected = CTRException.class)
    public void testMismatchInCallHomeId() throws Exception {
        SMSFrame smsFrame = new SMSFrame().parse(ProtocolTools.getBytesFromHexString("0000003B5600123456789000000F00000002002000060A0A150E0A000101400F00000001000000010A0A150E0A0001013A0F00000001000000010A0A150E0A000101460F00000001000000010A0A0A0E0A000101350F00000001000000010A0A080E0A000101350F00000001000000010A0A070E0A00010135FF0000000100000001", ""), 0);
        SmsHandler handler = new SmsHandler(deviceIdentifier, getAllRelevantProperties());

        // Business methods
        try {
            handler.parseSMSFrame(smsFrame);
        } catch (CTRException e) {
            assertEquals("Unexpected CTRException message", "Expected callHomeId 66554433221100, but the callHomeId in the sms was 12345678900000", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testProcessDECTableData() throws Exception {
        SMSFrame smsFrame = new SMSFrame().parse(ProtocolTools.getBytesFromHexString("0000003B3300665544332211000A0A0A0D260001000000010101010101010102010201020102010203040000010203040000010203040000010203040102030400000102000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", ""), 0);
        SmsHandler handler = new SmsHandler(deviceIdentifier, getAllRelevantProperties());

        // Business methods
        handler.parseSMSFrame(smsFrame);
        List<CollectedData> collectedDataList = handler.getCollectedDataList();

        // Asserts
        assertThat(collectedDataList).hasSize(16);
        assertCollectedRegister(collectedDataList.get(0), "7.0.128.4.0.255", null, "66554433221100");
        assertCollectedRegister(collectedDataList.get(1), "0.0.96.10.4.255", new Quantity("1", Unit.getUndefined()), "Mains power not available");
        assertCollectedRegister(collectedDataList.get(2), "7.0.13.0.0.255", new Quantity("257", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(3), "7.0.13.2.0.255", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(4), "7.0.128.1.0.255", new Quantity("3362048.1", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(5), "7.0.43.0.0.255", new Quantity("660.49",Unit.get(15, -2)), null);
        assertCollectedRegister(collectedDataList.get(6), "7.0.43.1.0.255", new Quantity("1976.32", Unit.get(15, -2)), null);
        assertCollectedRegister(collectedDataList.get(7), "7.0.42.0.0.255", new Quantity("1976.32", Unit.get("bar")), null);
        assertCollectedRegister(collectedDataList.get(8), "7.0.41.0.0.255", new Quantity("66051", Unit.get("K")), null);
        assertCollectedRegister(collectedDataList.get(9), "7.0.52.0.0.255", new Quantity("0.0001", Unit.getUndefined()), null);
        assertCollectedRegister(collectedDataList.get(10), "7.0.53.0.1.255", new Quantity("1976.33", Unit.getUndefined()), null);
        assertCollectedRegister(collectedDataList.get(11), "7.0.0.9.4.255", new Quantity("515", Unit.get("s")), null);
        assertCollectedRegister(collectedDataList.get(12), "7.0.128.8.0.255", new Quantity("1024", Unit.getUndefined()), null);
        assertCollectedRegister(collectedDataList.get(13), "0.0.96.10.1.255", new Quantity("0", Unit.getUndefined()), "To be configured");
        assertCollectedRegister(collectedDataList.get(14), "0.0.96.10.2.255", new Quantity("258", Unit.getUndefined()), "Factory conditions [2], Default values [3], Status change [4], Remote volume configuration seal [9], Remote analysis configuration seal [10], Download program [11], Restore default password [12], ");
        assertCollectedRegister(collectedDataList.get(15), "0.0.96.12.5.255", new Quantity("0", Unit.getUndefined()), null);
    }

    @Test
    public void testProcessDECFTableData() throws Exception {
        SMSFrame smsFrame = new SMSFrame().parse(ProtocolTools.getBytesFromHexString("0000003B34006655443322110005050505050101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010101010000000000000000000000000000000000000000000000000000000000", ""), 0);
        SmsHandler handler = new SmsHandler(deviceIdentifier, getAllRelevantProperties());

        // Business methods
        handler.parseSMSFrame(smsFrame);
        List<CollectedData> collectedDataList = handler.getCollectedDataList();

        // Asserts
        assertEquals("Expecting 19 CollectedData objects", 19, collectedDataList.size());
        assertCollectedRegister(collectedDataList.get(0), "7.0.128.4.0.255", null, "66554433221100");
        assertCollectedRegister(collectedDataList.get(1), "0.0.96.10.4.255", new Quantity("257", Unit.getUndefined()), "Mains power not available, Temperature out of range");
        assertCollectedRegister(collectedDataList.get(2), "7.0.128.8.0.255", new Quantity("257", Unit.getUndefined()), null);
        assertCollectedRegister(collectedDataList.get(3), "7.0.13.2.0.255", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(4), "7.0.128.1.0.255", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(5), "7.0.128.2.1.255", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(6), "7.0.128.2.2.255", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(7), "7.0.128.2.3.255", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(8), "7.0.13.2.1.255", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(9), "7.0.13.2.2.255", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(10), "7.0.13.2.3.255", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(11), "7.0.13.26.0.0", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(12), "7.0.128.5.0.0", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(13), "7.0.128.6.1.0", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(14), "7.0.128.6.2.0", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(15), "7.0.128.6.3.0", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(16), "7.0.13.2.1.0", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(17), "7.0.13.2.2.0", new Quantity("1684300.9", Unit.get("m3")), null);
        assertCollectedRegister(collectedDataList.get(18), "7.0.13.2.3.0", new Quantity("1677721.6", Unit.get("m3")), null);
    }

    @Test
    public void testProcessEventData() throws Exception {
        SMSFrame smsFrame = new SMSFrame().parse(ProtocolTools.getBytesFromHexString("0000003B5600665544332211000F00000002002000060A0A150E0A000101400F00000001000000010A0A150E0A0001013A0F00000001000000010A0A150E0A000101460F00000001000000010A0A0A0E0A000101350F00000001000000010A0A080E0A000101350F00000001000000010A0A070E0A00010135FF0000000100000001", ""), 0);
        SmsHandler handler = new SmsHandler(deviceIdentifier, getAllRelevantProperties());

        // Business methods
        handler.parseSMSFrame(smsFrame);
        List<CollectedData> collectedDataList = handler.getCollectedDataList();

        // Asserts
        assertEquals("Expecting only 1 CollectedData object", 1, collectedDataList.size());
        assertTrue("Expecting a CollectedData object of type DeviceLogBook", collectedDataList.get(0) instanceof DeviceLogBook);
        DeviceLogBook deviceLogBook = (DeviceLogBook) collectedDataList.get(0);
        assertEquals("Unexpected logbook identifier", "Identifier for logbook with obiscode '0.0.99.98.0.255' on " + deviceIdentifier.toString(), deviceLogBook.getLogBookIdentifier().toString());
        assertEquals("Expecting 6 MeterProtocolEvents", 6, deviceLogBook.getCollectedMeterEvents().size());
        assertCollectedMeterEvent(deviceLogBook.getCollectedMeterEvents().get(0), 1287666600000l, 0, 64, "Event buffer full [1]", "0.0.0.0");
        assertCollectedMeterEvent(deviceLogBook.getCollectedMeterEvents().get(1), 1287666600000l, 0, 58, "Calculation error [1]", "0.0.0.0");
        assertCollectedMeterEvent(deviceLogBook.getCollectedMeterEvents().get(2), 1287666600000l, 23, 70, "Fraud attempt [1]", "0.12.43.257");
        assertCollectedMeterEvent(deviceLogBook.getCollectedMeterEvents().get(3), 1286716200000l, 0, 53, "General fault [1]", "0.0.0.0");
        assertCollectedMeterEvent(deviceLogBook.getCollectedMeterEvents().get(4), 1286543400000l, 0, 53, "General fault [1]", "0.0.0.0");
        assertCollectedMeterEvent(deviceLogBook.getCollectedMeterEvents().get(5), 1286457000000l, 0, 53, "General fault [1]", "0.0.0.0");
    }

    @Test   // Profile data Hourly - Qm [1.0.2]
    public void testProcessHourlyMeasuredFlowProfileData() throws Exception {
        SMSFrame smsFrame = new SMSFrame().parse(ProtocolTools.getBytesFromHexString("000000215300665544332211000D0703083606401002360101020D07015000000000D0000000D0000000D000000090000000900000009000000050000000500000005000000050000000500000005000000050000000500000005000000050000000500000005000000090000000D0000000900000005000000050000000500000001000000054E7F9A8367D", ""), 0);
        SmsHandler handler = new SmsHandler(deviceIdentifier, getAllRelevantProperties());

        // Business methods
        handler.parseSMSFrame(smsFrame);
        List<CollectedData> collectedDataList = handler.getCollectedDataList();

        // Asserts
        assertEquals("Expecting the collectedDataList to contain 1 element", 1, collectedDataList.size());
        assertTrue("Expecting a CollectedData element of type DeviceLoadProfile", collectedDataList.get(0) instanceof DeviceLoadProfile);

        DeviceLoadProfile collectedProfile = (DeviceLoadProfile) collectedDataList.get(0);
        assertTrue("Expecting the LoadProfileIdentifier to be of type LoadProfileIdentifierByObisCodeAndDevice", collectedProfile.getLoadProfileIdentifier() instanceof LoadProfileIdentifierByObisCodeAndDevice);

        LoadProfileIdentifierByObisCodeAndDevice loadProfileIdentifier = (LoadProfileIdentifierByObisCodeAndDevice) collectedProfile.getLoadProfileIdentifier();
        assertEquals("The LoadProfileIdentifier of the collected profile doesn't match.", "deviceIdentifier = deviceIdentifier and ObisCode = 0.0.99.1.0.255", loadProfileIdentifier.toString());

        List<IntervalData> collectedIntervalData = collectedProfile.getCollectedIntervalData();
        assertEquals("Expecting 24 IntervalData elements", 24, collectedIntervalData.size());

        IntervalData firstIntervalData = collectedIntervalData.get(0);
        assertEquals(1372658400000l, firstIntervalData.getEndTime().getTime());
        assertEquals(32, firstIntervalData.getEiStatus());
        assertEquals(208, firstIntervalData.getProtocolStatus());
        assertEquals(1, firstIntervalData.getValueCount());
        assertEquals(0, ((IntervalValue) firstIntervalData.getIntervalValues().get(0)).getNumber().intValue());
        assertEquals(32, ((IntervalValue) firstIntervalData.getIntervalValues().get(0)).getEiStatus());
        assertEquals(208, ((IntervalValue) firstIntervalData.getIntervalValues().get(0)).getProtocolStatus());

        IntervalData lastIntervalData = collectedIntervalData.get(23);
        assertEquals(1372741200000l, lastIntervalData.getEndTime().getTime());
        assertEquals(32, lastIntervalData.getEiStatus());
        assertEquals(80, lastIntervalData.getProtocolStatus());
        assertEquals(1, lastIntervalData.getValueCount());
        assertEquals(0, ((IntervalValue) lastIntervalData.getIntervalValues().get(0)).getNumber().intValue());
        assertEquals(32, ((IntervalValue) lastIntervalData.getIntervalValues().get(0)).getEiStatus());
        assertEquals(80, ((IntervalValue) lastIntervalData.getIntervalValues().get(0)).getProtocolStatus());


        List<ChannelInfo> channelInfos = collectedProfile.getChannelInfo();
        assertEquals("Expecting only 1 channel", 1, channelInfos.size());
        ChannelInfo channelInfo = channelInfos.get(0);
        assertEquals("Wrong channelInfo id", 0, channelInfo.getId());
        assertEquals("Wrong channelInfo name", "7.0.43.0.0.255", channelInfo.getName());
        assertEquals("Wrong channelInfo unit", "m3/h", channelInfo.getUnit().toString());

        assertThat(collectedProfile.getIssues()).isEmpty();
        assertTrue("Expecting doStoreOlderValues to be true", collectedProfile.isDoStoreOlderValues());
        assertEquals("Expecting resultType 'Supported'", ResultType.Supported.ordinal(), collectedProfile.getResultType().ordinal());
    }

    @Test   // Profile data Hourly - P [4.0.2]
    public void testProcessHourlyPressureProfileData() throws Exception {
        SMSFrame smsFrame = new SMSFrame().parse(ProtocolTools.getBytesFromHexString("000000215300665544332211000D0703083506401002360104020D070150000000001300000013000000130000001300000013000000130000001300000013000000130000001300000013000000130000001300000013000000130000001300000013000000130000001300000013000000130000001300000013000000130000001000000000D4F658FAB6", ""), 0);
        SmsHandler handler = new SmsHandler(deviceIdentifier, getAllRelevantProperties());

        // Business methods
        handler.parseSMSFrame(smsFrame);
        List<CollectedData> collectedDataList = handler.getCollectedDataList();

        // Asserts
        assertEquals("Expecting the collectedDataList to contain 1 element", 1, collectedDataList.size());
        assertTrue("Expecting a CollectedData element of type DeviceLoadProfile", collectedDataList.get(0) instanceof DeviceLoadProfile);

        DeviceLoadProfile collectedProfile = (DeviceLoadProfile) collectedDataList.get(0);
        assertTrue("Expecting the LoadProfileIdentifier to be of type LoadProfileIdentifierByObisCodeAndDevice", collectedProfile.getLoadProfileIdentifier() instanceof LoadProfileIdentifierByObisCodeAndDevice);

        LoadProfileIdentifierByObisCodeAndDevice loadProfileIdentifier = (LoadProfileIdentifierByObisCodeAndDevice) collectedProfile.getLoadProfileIdentifier();
        assertEquals("The LoadProfileIdentifier of the collected profile doesn't match.", "deviceIdentifier = deviceIdentifier and ObisCode = 0.0.99.1.0.255", loadProfileIdentifier.toString());

        List<IntervalData> collectedIntervalData = collectedProfile.getCollectedIntervalData();
        assertEquals("Expecting 24 IntervalData elements", 24, collectedIntervalData.size());

        IntervalData firstIntervalData = collectedIntervalData.get(0);
        assertEquals(1372658400000l, firstIntervalData.getEndTime().getTime());
        assertEquals(32, firstIntervalData.getEiStatus());
        assertEquals(19, firstIntervalData.getProtocolStatus());
        assertEquals(1, firstIntervalData.getValueCount());
        assertEquals(0, ((IntervalValue) firstIntervalData.getIntervalValues().get(0)).getNumber().intValue());
        assertEquals(32, ((IntervalValue) firstIntervalData.getIntervalValues().get(0)).getEiStatus());
        assertEquals(19, ((IntervalValue) firstIntervalData.getIntervalValues().get(0)).getProtocolStatus());

        IntervalData lastIntervalData = collectedIntervalData.get(23);
        assertEquals(1372741200000l, lastIntervalData.getEndTime().getTime());
        assertEquals(32, lastIntervalData.getEiStatus());
        assertEquals(19, lastIntervalData.getProtocolStatus());
        assertEquals(1, lastIntervalData.getValueCount());
        assertEquals(0, ((IntervalValue) lastIntervalData.getIntervalValues().get(0)).getNumber().intValue());
        assertEquals(32, ((IntervalValue) lastIntervalData.getIntervalValues().get(0)).getEiStatus());
        assertEquals(19, ((IntervalValue) lastIntervalData.getIntervalValues().get(0)).getProtocolStatus());


        List<ChannelInfo> channelInfos = collectedProfile.getChannelInfo();
        assertEquals("Expecting only 1 channel", 1, channelInfos.size());
        ChannelInfo channelInfo = channelInfos.get(0);
        assertEquals("Wrong channelInfo id", 0, channelInfo.getId());
        assertEquals("Wrong channelInfo name", "7.0.42.0.0.255", channelInfo.getName());
        assertEquals("Wrong channelInfo unit", "bar", channelInfo.getUnit().toString());

        assertThat(collectedProfile.getIssues()).isEmpty();
        assertTrue("Expecting doStoreOlderValues to be true", collectedProfile.isDoStoreOlderValues());
        assertEquals("Expecting resultType 'Supported'", ResultType.Supported.ordinal(), collectedProfile.getResultType().ordinal());
    }

    private void assertCollectedRegister(CollectedData collectedData, String obisCode, Quantity quantity, String text) {
        assertTrue("CollectedData should be an instance of DefaultDeviceRegister", collectedData instanceof DefaultDeviceRegister);

        DefaultDeviceRegister deviceRegister = (DefaultDeviceRegister) collectedData;
        assertEquals("The register identifier of the DefaultDeviceRegister doesn't match the expected one", "deviceIdentifier = deviceIdentifier and ObisCode = " + obisCode, deviceRegister.getRegisterIdentifier().toString());
        if (quantity == null) {
            assertTrue("Unexpected register quantity", deviceRegister.getCollectedQuantity() == null);
        } else {
            assertTrue("Unexpected register quantity", quantity.equals(deviceRegister.getCollectedQuantity()));
        }
        if (text == null) {
            assertTrue("Unexpected register text", deviceRegister.getText() == null);
        } else {
            assertTrue("Unexpected register text", text.equals(deviceRegister.getText()));
        }
        assertThat(deviceRegister.getIssues()).isEmpty();
        assertEquals("ResultType doesn't match", 0, deviceRegister.getResultType().ordinal());
    }

    private void assertCollectedMeterEvent(MeterProtocolEvent event, long time, int eiCode, int protocolCode, String message, String eventType) {
        assertEquals("Event time doesn't match", time, event.getTime().getTime());
        assertEquals("Event eiCode doesn't match", eiCode, event.getEiCode());
        assertEquals("Event protocolCode doesn't match", protocolCode, event.getProtocolCode());
        assertEquals("Event message doesn't match", message, event.getMessage());
        assertEquals("Event eventType doesn't match", eventType, event.getEventType().toString());
    }

    private TypedProperties getAllRelevantProperties() {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty("SerialNumber", "Serial");
        typedProperties.setProperty("callHomeId", "66554433221100");
        typedProperties.setProperty("LegacyTimeZone", "GMT+01:00");
        return typedProperties;
    }
}
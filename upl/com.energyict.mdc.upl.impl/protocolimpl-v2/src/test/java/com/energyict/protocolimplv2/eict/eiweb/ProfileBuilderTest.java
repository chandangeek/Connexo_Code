package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.cbo.LittleEndianOutputStream;
import com.energyict.cbo.TimeConstants;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.MdwInterface;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.exceptions.ComServerExceptionFactoryProvider;
import com.energyict.mdc.exceptions.DefaultComServerExceptionFactoryProvider;
import com.energyict.mdc.meterdata.CollectedDataFactoryProvider;
import com.energyict.mdc.meterdata.DefaultCollectedDataFactoryProvider;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.protocol.exceptions.CommunicationException;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDAO;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ProfileBuilder} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-23 (14:52)
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileBuilderTest {

    private static final int DEVICE_ID = 122;

    @BeforeClass
    public static void doBefore() {
        ComServerExceptionFactoryProvider.instance.set(new DefaultComServerExceptionFactoryProvider());
        CollectedDataFactoryProvider.instance.set(new DefaultCollectedDataFactoryProvider());
    }

    @Mock
    private ServerManager manager;
    @Mock
    private MdwInterface mdwInterface;
    @Mock
    private DeviceFactory deviceFactory;
    @Mock
    private Device device;

    @Before
    public void initializeMocksAndFactories () {
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.deviceFactory.find(DEVICE_ID)).thenReturn(this.device);
        when(this.mdwInterface.getDeviceFactory()).thenReturn(this.deviceFactory);
        when(this.manager.getMdwInterface()).thenReturn(this.mdwInterface);
        ManagerFactory.setCurrent(this.manager);
    }

    @Test
    public void testGetProfileDataForDefaultNumberOfChannels () throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));
        String deviceId = "221";
        packetBuilder.parse(deviceId, "FFFF", "0", "0", null, null, "123,132,213,231,312,321", "192.168.2.100", null, "0");

        // Business method
        ProfileBuilder profileBuilder = new ProfileBuilder(packetBuilder);

        // Asserts
        assertThat(profileBuilder.getConfigFile()).isEqualTo(new byte[0]);
        assertThat(profileBuilder.getMeterReadings()).isEmpty();
        assertThat(profileBuilder.getProfileData()).isNotNull();
        assertThat(profileBuilder.getProfileData().getChannelInfos()).hasSize(6);
        assertThat(profileBuilder.getProfileData().getIntervalDatas()).hasSize(1);
        IntervalData intervalData = profileBuilder.getProfileData().getIntervalData(0);
        assertThat(intervalData.getIntervalValues()).hasSize(6);
        assertThat(((IntervalValue) intervalData.getIntervalValues().get(0)).getNumber().intValue()).isEqualTo(123);
        assertThat(((IntervalValue) intervalData.getIntervalValues().get(1)).getNumber().intValue()).isEqualTo(132);
        assertThat(((IntervalValue) intervalData.getIntervalValues().get(2)).getNumber().intValue()).isEqualTo(213);
        assertThat(((IntervalValue) intervalData.getIntervalValues().get(3)).getNumber().intValue()).isEqualTo(231);
        assertThat(((IntervalValue) intervalData.getIntervalValues().get(4)).getNumber().intValue()).isEqualTo(312);
        assertThat(((IntervalValue) intervalData.getIntervalValues().get(5)).getNumber().intValue()).isEqualTo(321);
    }

    @Test
    public void testGetProfileDataForOneChannel () throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));
        String deviceId = "221";
        packetBuilder.parse(deviceId, "FFFF", "0", "0", null, "1", "696", "192.168.2.100", null, "0");

        // Business method
        ProfileBuilder profileBuilder = new ProfileBuilder(packetBuilder);

        // Asserts
        assertThat(profileBuilder.getConfigFile()).isEqualTo(new byte[0]);
        assertThat(profileBuilder.getMeterReadings()).isEmpty();
        assertThat(profileBuilder.getProfileData()).isNotNull();
        assertThat(profileBuilder.getProfileData().getChannelInfos()).hasSize(1);
        assertThat(profileBuilder.getProfileData().getIntervalDatas()).hasSize(1);
        IntervalData intervalData = profileBuilder.getProfileData().getIntervalData(0);
        assertThat(intervalData.getIntervalValues()).hasSize(1);
        assertThat(((IntervalValue) intervalData.getIntervalValues().get(0)).getNumber().intValue()).isEqualTo(696);
    }

    @Test
    public void testGetMeterReadingsForOneChannel () throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LittleEndianOutputStream leos = new LittleEndianOutputStream(os);
        byte[] versionBytes = new byte[1];
        versionBytes[0] = 0x43; // Meter reading data and state bits
        leos.write(versionBytes);
        int deviceId = DEVICE_ID;
        leos.writeLEChar(deviceId);
        leos.writeLEShort((short) 1);   // Number of records
        leos.writeLEInt(0); // ip address
        leos.writeLEShort((short) 0xFFFF); // seq: no encryption
        leos.writeLEInt(0x0001); // mask
        leos.writeLEInt(0x0022); // length: 19 header bytes + 15 actual data bytes
        this.writeData(leos, 123, this.getCorrectTimeAsInt());  // meter reading
        this.writeTariff(leos, 32);
        this.writeStateBits(leos, (short) 0xFFFF);
        this.writeData(leos, 654);  // Interval data
        leos.writeString("19 bytes of header data", 19);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));
        packetBuilder.parse(is, null);

        // Business method
        ProfileBuilder profileBuilder = new ProfileBuilder(packetBuilder);

        // Asserts
        assertThat(profileBuilder.getConfigFile()).isEqualTo(new byte[0]);
        List<BigDecimal> meterReadings = profileBuilder.getMeterReadings();
        assertThat(meterReadings).isNotNull();
        assertThat(meterReadings).hasSize(1);
        assertThat(meterReadings.get(0).toString()).isEqualTo("123");
        ProfileData profileData = profileBuilder.getProfileData();
        assertThat(profileData).isNotNull();
        assertThat(profileData.getChannelInfos()).hasSize(1);
        assertThat(profileData.getMeterEvents()).isEmpty();
        assertThat(profileData.getIntervalDatas()).hasSize(1);
        IntervalData intervalData = profileData.getIntervalDatas().get(0);
        assertThat(intervalData.getEiStatus()).isEqualTo(2103);
        assertThat(intervalData.getIntervalValues()).hasSize(1);
        IntervalValue intervalValue = (IntervalValue) intervalData.getIntervalValues().get(0);
        assertThat(intervalValue.getNumber().toString()).isEqualTo("654");
    }

    @Test
    public void testGetEventDataForOneChannel () throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LittleEndianOutputStream leos = new LittleEndianOutputStream(os);
        byte[] versionBytes = new byte[1];
        versionBytes[0] = 0x21; // Event data without state bits
        leos.write(versionBytes);
        int deviceId = DEVICE_ID;
        leos.writeLEChar(deviceId);
        leos.writeLEShort((short) 1);   // Number of records
        leos.writeLEInt(0); // ip address
        leos.writeLEShort((short) 0xFFFF); // seq: no encryption
        leos.writeLEInt(0x0001); // mask
        leos.writeLEInt(0x003D); // length: 19 header bytes + 42 actual data bytes
        String eventDescription = "For testing purposes only";
        this.writeEventData(leos, (short) 123, eventDescription);
        this.writeData(leos, this.getCorrectTimeAsInt());  // UTC stamp of interval data
        this.writeTariff(leos, 32); // code for interval data
        this.writeData(leos, 654);  // Interval data
        leos.writeString("19 bytes of header data", 19);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));
        packetBuilder.parse(is, null);

        // Business method
        ProfileBuilder profileBuilder = new ProfileBuilder(packetBuilder);

        // Asserts
        assertThat(profileBuilder.getConfigFile()).isEqualTo(new byte[0]);
        List<BigDecimal> meterReadings = profileBuilder.getMeterReadings();
        assertThat(meterReadings).isNotNull();
        assertThat(meterReadings).isEmpty();
        ProfileData profileData = profileBuilder.getProfileData();
        assertThat(profileData).isNotNull();
        assertThat(profileData.getChannelInfos()).hasSize(1);
        List<MeterEvent> meterEvents = profileData.getMeterEvents();
        assertThat(meterEvents).isNotNull();
        assertThat(meterEvents).hasSize(1);
        assertThat(meterEvents.get(0).getProtocolCode()).isEqualTo(123);
        assertThat(meterEvents.get(0).getMessage()).isEqualTo(eventDescription);
        assertThat(profileData.getIntervalDatas()).hasSize(1);
        IntervalData intervalData = profileData.getIntervalDatas().get(0);
        assertThat(intervalData.getIntervalValues()).hasSize(1);
        IntervalValue intervalValue = (IntervalValue) intervalData.getIntervalValues().get(0);
        assertThat(intervalValue.getNumber().toString()).isEqualTo("654");
    }

    @Test(expected = CommunicationException.class)
    public void testMissingInboundData () throws IOException {
        PacketBuilder packetBuilder = mock(PacketBuilder.class);
        when(packetBuilder.getData()).thenReturn(null);
        when(packetBuilder.getDeviceIdentifier()).thenReturn(mock(DeviceIdentifier.class));

        // Business method
        new ProfileBuilder(packetBuilder);

        // Expected CommunicationException because the inbound data was missing
    }

    private void writeData (LittleEndianOutputStream os, int... readings) throws IOException {
        for (Integer reading : readings) {
            os.writeLEInt(reading);
        }
    }

    private void writeEventData (LittleEndianOutputStream os, short code, String description) throws IOException {
        os.write(1);    // count, i.e. the number of events
        os.writeLEInt(this.getCorrectTimeAsInt());  // event time
        os.writeLEShort(code);  // Event code
        os.write(description.length());
        os.writeString(description, description.length());
    }

    private void writeTariff (LittleEndianOutputStream os, int tariff) throws IOException {
        os.writeByte(tariff);
    }

    private void writeStateBits (LittleEndianOutputStream os, short stateBits) throws IOException {
        os.writeLEShort(stateBits);
    }

    private int getCorrectTimeAsInt () {
        return (int) ((this.getCorrectTime().getTime() - EIWebConstants.SECONDS10YEARS) / TimeConstants.MILLISECONDS_IN_SECOND);
    }

    private Date getCorrectTime () {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 21);
        return calendar.getTime();
    }

}
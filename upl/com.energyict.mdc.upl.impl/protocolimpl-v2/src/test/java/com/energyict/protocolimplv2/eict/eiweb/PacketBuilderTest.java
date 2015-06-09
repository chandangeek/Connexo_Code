package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.cbo.LittleEndianOutputStream;
import com.energyict.cbo.Password;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.mdc.exceptions.ComServerExceptionFactoryProvider;
import com.energyict.mdc.exceptions.DefaultComServerExceptionFactoryProvider;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.meterdata.CollectedDataFactoryProvider;
import com.energyict.mdc.meterdata.DefaultCollectedDataFactoryProvider;
import com.energyict.mdc.meterdata.DeviceIpAddress;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.protocol.exceptions.CommunicationException;
import com.energyict.mdc.protocol.exceptions.DataEncryptionException;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDAO;
import com.energyict.mdc.protocol.security.SecurityProperty;
import com.energyict.mdw.core.Device;
import org.fest.assertions.core.Condition;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link PacketBuilder} component by comparing
 * the results against the results of the old com.energyict.eiwebbulk.PacketBuilder.
 * All values that are used in tested were first injected into the old component
 * and then hard coded in this test as "correct" results.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-23 (10:16)
 */
@RunWith(MockitoJUnitRunner.class)
public class PacketBuilderTest {

    private static final int DEVICE_ID = 122;

    @BeforeClass
    public static void doBefore() {
        ComServerExceptionFactoryProvider.instance.set(new DefaultComServerExceptionFactoryProvider());
        CollectedDataFactoryProvider.instance.set(new DefaultCollectedDataFactoryProvider());
    }

    @Test
    public void testParseFromUnencryptedStringValuesWithDeviceId() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));

        // Business method
        String deviceId = "221";
        packetBuilder.parse(deviceId, "FFFF", "0", "0", null, "1", "321", "192.168.2.100", null, "0");

        // Asserts
        assertThat(packetBuilder.getDeviceIdentifier().toString()).contains(deviceId);
        assertThat(packetBuilder.getNrOfAcceptedMessages()).isZero();
        assertThat(packetBuilder.getNrOfChannels()).isEqualTo(1);
        assertThat(packetBuilder.getNrOfRecords()).isEqualTo(1);
        assertThat(packetBuilder.getMask()).isEqualTo(1);
        assertThat(packetBuilder.getSeq()).isEqualTo("FFFF");
        assertThat(packetBuilder.getVersion()).isEqualTo(1);
        assertThat(packetBuilder.getData()).isNotNull();
        assertEquals(packetBuilder.getAdditionalInfo().toString(), "Device ID: 221; Serial number: null; IP address: 192.168.2.100");
    }

    @Test
    public void testParseFromUnencryptedStringValuesWithStateBits() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));

        // Business method
        String deviceId = "221";
        String statebits = "0001110";
        packetBuilder.parse(deviceId, "FFFF", "0", "0", statebits, "1", "321", "192.168.2.100", null, "0");

        // Asserts
        assertThat(packetBuilder.getDeviceIdentifier().toString()).contains(deviceId);
        assertThat(packetBuilder.getNrOfAcceptedMessages()).isZero();
        assertThat(packetBuilder.getNrOfChannels()).isEqualTo(1);
        assertThat(packetBuilder.getNrOfRecords()).isEqualTo(1);
        assertThat(packetBuilder.getMask()).isEqualTo(1);
        assertThat(packetBuilder.getSeq()).isEqualTo("FFFF");
        assertThat(packetBuilder.getVersion()).isEqualTo(3);
        assertThat(packetBuilder.getData()).isNotNull();
        assertEquals(packetBuilder.getAdditionalInfo().toString(), "Device ID: 221; Serial number: null; IP address: 192.168.2.100");
    }

    @Test
    public void testParseFromUnencryptedStringValuesWithSerialNumber() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));
        String serialNumber = "serial.number";

        // Business method
        packetBuilder.parse(null, "FFFF", "0", "0", null, "1", "321", "192.168.2.100", serialNumber, "0");

        // Asserts
        assertThat(packetBuilder.getDeviceIdentifier().toString()).contains(serialNumber);
        assertThat(packetBuilder.getNrOfAcceptedMessages()).isZero();
        assertThat(packetBuilder.getNrOfChannels()).isEqualTo(1);
        assertThat(packetBuilder.getNrOfRecords()).isEqualTo(1);
        assertThat(packetBuilder.getMask()).isEqualTo(1);
        assertThat(packetBuilder.getSeq()).isEqualTo("FFFF");
        assertThat(packetBuilder.getVersion()).isEqualTo(1);
        assertThat(packetBuilder.getData()).isNotNull();
        assertEquals(packetBuilder.getAdditionalInfo().toString(), "Device ID: null; Serial number: serial.number; IP address: 192.168.2.100");
    }

    @Test
    public void testParseFromUnencryptedStringValuesWithoutNumberOfMessages() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));
        String serialNumber = "serial.number";

        // Business method
        packetBuilder.parse(null, "FFFF", "0", "0", null, "1", "321", "192.168.2.100", serialNumber, null);

        // Asserts
        assertThat(packetBuilder.getDeviceIdentifier().toString()).contains(serialNumber);
        assertThat(packetBuilder.getNrOfAcceptedMessages()).isNull();
        assertThat(packetBuilder.getNrOfChannels()).isEqualTo(1);
        assertThat(packetBuilder.getNrOfRecords()).isEqualTo(1);
        assertThat(packetBuilder.getMask()).isEqualTo(1);
        assertThat(packetBuilder.getSeq()).isEqualTo("FFFF");
        assertThat(packetBuilder.getVersion()).isEqualTo(1);
        assertThat(packetBuilder.getData()).isNotNull();
        assertEquals(packetBuilder.getAdditionalInfo().toString(), "Device ID: null; Serial number: serial.number; IP address: 192.168.2.100");
    }

    @Test
    public void testParseFromUnencryptedStringValuesWithNonNumerialDeviceId() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));

        try {
            packetBuilder.parse("forceparsefailure", "FFFF", "0", "0", null, "1", "321", "192.168.2.100", null, "0");
        } catch (CommunicationException e) {
            if (e.getCause() == null || !(e.getCause() instanceof NumberFormatException)) {
                fail("Expected a CommunicationException with a nested NumberFormatException");
            }
        }
    }

    @Test
    public void testParseFromUnencryptedStringValuesWithNonNumerialMask() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));

        try {
            packetBuilder.parse(String.valueOf(DEVICE_ID), "FFFF", "0", "0", null, "forceparsefailure", "321", "192.168.2.100", null, "0");
        } catch (CommunicationException e) {
            if (e.getCause() == null || !(e.getCause() instanceof NumberFormatException)) {
                fail("Expected a CommunicationException with a nested NumberFormatException");
            }
        }
    }

    @Test
    public void testParseFromUnencryptedStringValuesWithNonNumerialNumberOfMessages() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));

        try {
            packetBuilder.parse(String.valueOf(DEVICE_ID), "FFFF", "0", "0", null, "1", "321", "192.168.2.100", null, "foreceparsefailure");
        } catch (CommunicationException e) {
            if (e.getCause() == null || !(e.getCause() instanceof NumberFormatException)) {
                fail("Expected a CommunicationException with a nested NumberFormatException");
            }
        }
    }

    @Test
    public void testParseFromUnencryptedNonNumericalStringValues() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));

        try {
            String deviceId = "221";
            packetBuilder.parse(deviceId, "FFFF", "0", "0", null, "1", "forceparsefailure", "192.168.2.100", null, "0");
        } catch (CommunicationException e) {
            if (e.getCause() == null || !(e.getCause() instanceof NumberFormatException)) {
                fail("Expected a CommunicationException with a nested NumberFormatException");
            }
        }
    }

    @Test
    public void testParseFromUnencryptedStringValuesWithoutMask() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));

        // Business method
        String deviceId = "221";
        packetBuilder.parse(deviceId, "FFFF", "0", "0", null, null, "123,132,213,231,312,321", "192.168.2.100", null, "0");

        // Asserts
        assertThat(packetBuilder.getDeviceIdentifier().toString()).contains(deviceId);
        assertThat(packetBuilder.getNrOfAcceptedMessages()).isZero();
        assertThat(packetBuilder.getNrOfChannels()).isEqualTo(6);
        assertThat(packetBuilder.getNrOfRecords()).isEqualTo(1);
        assertThat(packetBuilder.getMask()).isEqualTo(63);
        assertThat(packetBuilder.getSeq()).isEqualTo("FFFF");
        assertThat(packetBuilder.getVersion()).isEqualTo(1);
        assertThat(packetBuilder.getData()).isNotNull();
        assertEquals(packetBuilder.getAdditionalInfo().toString(), "Device ID: 221; Serial number: null; IP address: 192.168.2.100");
    }

    @Test(expected = DataEncryptionException.class)
    public void testParseFromUnencryptedStringValuesWithTooManyChannelValues() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));

        // Business method
        String deviceId = "221";
        packetBuilder.parse(deviceId, "FFFF", "0", "0", null, "1", "123,132", "192.168.2.100", null, "0");

        // Expected a DataEncryptionException because too many channels were specified and that is interpreted by the PacketBuilder as an error in decryption
    }

    @Test
    public void testCollectedDataFromFromUnencryptedStringValues() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));
        packetBuilder.parse("221", "FFFF", "0", "0", null, "1", "321", "192.168.2.100", null, "0");

        // Business method
        ArrayList<CollectedData> collectedData = new ArrayList<CollectedData>();
        packetBuilder.addCollectedData(collectedData);

        // Assert that we have exactly one DeviceIpAddress
        assertThat(collectedData).areExactly(1, new Condition<CollectedData>() {
            @Override
            public boolean matches(CollectedData value) {
                return value instanceof DeviceIpAddress;
            }
        });
    }

    @Test
    public void testIsTimeCorrectWithUnencryptedData() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));
        packetBuilder.parse("221", "FFFF", "0", "0", null, "1", "321", "192.168.2.100", null, "0");

        // Asserts
        assertThat(packetBuilder.isTimeCorrect(this.getCorrectTime())).isTrue();
        assertThat(packetBuilder.isTimeCorrect(this.getPastTime())).isTrue();
        assertThat(packetBuilder.isTimeCorrect(this.getFutureTime())).isTrue();
    }

    @Test
    public void testParseFromUnencryptedInputStreamWithDeviceId() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LittleEndianOutputStream leos = new LittleEndianOutputStream(os);
        byte[] versionBytes = new byte[1];
        versionBytes[0] = 1;
        leos.write(versionBytes);
        int deviceId = DEVICE_ID;
        leos.writeLEChar(deviceId);
        leos.writeLEShort((short) 1);   // Number of records
        leos.writeLEInt(0); // ip address
        leos.writeLEShort((short) 0xFFFF); // seq
        leos.writeLEInt(0x0001); // mask
        leos.writeLEInt(0x0014); // length: 19 header bytes + 1 actual data byte
        leos.writeString("19 bytes of header data", 19);
        leos.writeLEChar(321);  // The single value
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));

        // Business method
        packetBuilder.parse(is, null);

        // Asserts
        assertThat(packetBuilder.getDeviceIdentifier().toString()).contains(String.valueOf(deviceId));
        assertThat(packetBuilder.getNrOfAcceptedMessages()).isNull();
        assertThat(packetBuilder.getNrOfChannels()).isEqualTo(1);
        assertThat(packetBuilder.getNrOfRecords()).isEqualTo(1);
        assertThat(packetBuilder.getMask()).isEqualTo(1);
        assertThat(packetBuilder.getSeq()).isEqualTo("FFFF");
        assertThat(packetBuilder.getVersion()).isEqualTo(1);
        assertThat(packetBuilder.getData()).isNotNull();
        assertEquals(packetBuilder.getAdditionalInfo().toString(), "Device ID: 122; Serial number: null; IP address: 0.0.0.0");
    }

    @Test
    public void testParseFromUnencryptedInputStreamWithSerialNumber() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LittleEndianOutputStream leos = new LittleEndianOutputStream(os);
        byte[] versionBytes = new byte[1];
        versionBytes[0] = 1;
        leos.write(versionBytes);
        int deviceId = 0;
        leos.writeLEChar(deviceId);
        leos.writeLEShort((short) 1);   // Number of records
        leos.writeLEInt(0); // ip address
        leos.writeLEShort((short) 0xFFFF); // seq
        leos.writeLEInt(0x0001); // mask
        leos.writeLEInt(0x0014); // length: 19 header bytes + 1 actual data byte
        leos.writeString("19 bytes of header data", 19);
        leos.writeLEChar(321);  // The single value
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class)));
        String serialNumber = "serial.number";

        // Business method
        packetBuilder.parse(is, serialNumber);

        // Asserts
        assertThat(packetBuilder.getDeviceIdentifier().toString()).contains(serialNumber);
        assertThat(packetBuilder.getNrOfAcceptedMessages()).isNull();
        assertThat(packetBuilder.getNrOfChannels()).isEqualTo(1);
        assertThat(packetBuilder.getNrOfRecords()).isEqualTo(1);
        assertThat(packetBuilder.getMask()).isEqualTo(1);
        assertThat(packetBuilder.getSeq()).isEqualTo("FFFF");
        assertThat(packetBuilder.getVersion()).isEqualTo(1);
        assertThat(packetBuilder.getData()).isNotNull();
        assertEquals(packetBuilder.getAdditionalInfo().toString(), "Device ID: 0; Serial number: serial.number; IP address: 0.0.0.0");
    }

    @Test
    public void testParseFromEncryptedInputStreamWithDeviceId() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LittleEndianOutputStream leos = new LittleEndianOutputStream(os);
        byte[] versionBytes = new byte[1];
        versionBytes[0] = 1;
        leos.write(versionBytes);
        int deviceId = DEVICE_ID;
        leos.writeLEChar(deviceId);
        leos.writeLEShort((short) 1);   // Number of records
        leos.writeLEInt(0); // ip address
        leos.writeLEShort((short) 0x2114); // seq
        leos.writeLEInt(0x0001); // mask
        leos.writeLEInt(0x0014); // length: 19 header bytes + 1 actual data byte
        leos.writeString("19 bytes of header data", 19);
        this.writeEncryptedData("321", leos);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        InboundDAO inboundDAO = mock(InboundDAO.class);
        InboundComPort comPort = mock(InboundComPort.class);
        when(inboundDAO.getDeviceConnectionTypeProperties(any(DeviceIdentifier.class), eq(comPort))).thenReturn(TypedProperties.empty());
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(inboundDAO, comPort));

        // Business method
        packetBuilder.parse(is, null);

        // Asserts
        assertThat(packetBuilder.getDeviceIdentifier().toString()).contains(String.valueOf(deviceId));
        assertThat(packetBuilder.getNrOfAcceptedMessages()).isNull();
        assertThat(packetBuilder.getNrOfChannels()).isEqualTo(1);
        assertThat(packetBuilder.getNrOfRecords()).isEqualTo(1);
        assertThat(packetBuilder.getMask()).isEqualTo(1);
        assertThat(packetBuilder.getSeq()).isEqualTo("2114");
        assertThat(packetBuilder.getVersion()).isEqualTo(1);
        assertThat(packetBuilder.getData()).isNotNull();
        assertEquals(packetBuilder.getAdditionalInfo().toString(), "Device ID: 122; Serial number: null; IP address: 0.0.0.0");
    }

    @Test
    public void testIsTimeCorrectWithEncryptedData() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LittleEndianOutputStream leos = new LittleEndianOutputStream(os);
        byte[] versionBytes = new byte[1];
        versionBytes[0] = 1;
        leos.write(versionBytes);
        int deviceId = DEVICE_ID;
        leos.writeLEChar(deviceId);
        leos.writeLEShort((short) 1);   // Number of records
        leos.writeLEInt(0); // ip address
        leos.writeLEShort((short) 0x2114); // seq at time 14:21
        leos.writeLEInt(0x0001); // mask
        leos.writeLEInt(0x0014); // length: 19 header bytes + 1 actual data byte
        leos.writeString("19 bytes of header data", 19);
        this.writeEncryptedData("321", leos);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        InboundDAO inboundDAO = mock(InboundDAO.class);
        InboundComPort comPort = mock(InboundComPort.class);
        when(inboundDAO.getDeviceConnectionTypeProperties(any(DeviceIdentifier.class), eq(comPort))).thenReturn(TypedProperties.empty());
        PacketBuilder packetBuilder = new PacketBuilder(new EIWebCryptographer(inboundDAO, comPort));

        // Business method
        packetBuilder.parse(is, null);

        // Asserts
        assertThat(packetBuilder.isTimeCorrect(this.getCorrectTime())).isTrue();
        assertThat(packetBuilder.isTimeCorrect(this.getPastTime())).isFalse();
        assertThat(packetBuilder.isTimeCorrect(this.getFutureTime())).isFalse();
        assertEquals(packetBuilder.getAdditionalInfo().toString(), "Device ID: 122; Serial number: null; IP address: 0.0.0.0");
    }

    private Date getPastTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 20);
        return calendar.getTime();
    }

    private Date getCorrectTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 21);
        return calendar.getTime();
    }

    private Date getFutureTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 55);
        return calendar.getTime();
    }

    private void writeEncryptedData(String values, LittleEndianOutputStream os) throws IOException {
        Device device = mock(Device.class);

        TypedProperties connectionTypeProperties = TypedProperties.empty();
        connectionTypeProperties.setProperty(EIWebConnectionType.MAC_ADDRESS_PROPERTY_NAME, "mac-address");
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);

        InboundComPort comPort = mock(InboundComPort.class);
        InboundDAO inboundDAO = mock(InboundDAO.class);
        when(inboundDAO.getDeviceConnectionTypeProperties(deviceIdentifier, comPort)).thenReturn(connectionTypeProperties);
        SecurityProperty encryptionPassword = mock(SecurityProperty.class);
        when(encryptionPassword.getValue()).thenReturn(new Password("zorro"));
        when(inboundDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, comPort)).thenReturn(Arrays.asList(encryptionPassword));
        EIWebCryptographer cryptographer = new EIWebCryptographer(inboundDAO, comPort);
        Encryptor encryptor = new Encryptor(cryptographer.buildMD5Seed(deviceIdentifier, "2114"));
        for (byte rawByte : values.getBytes()) {
            os.write(encryptor.encrypt(rawByte));
        }
    }

}
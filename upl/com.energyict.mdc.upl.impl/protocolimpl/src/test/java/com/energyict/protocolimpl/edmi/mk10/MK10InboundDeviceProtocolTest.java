package com.energyict.protocolimpl.edmi.mk10;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.InboundDeviceProtocol;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cpo.Environment;
import com.energyict.cpo.TypedProperties;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 29/10/12 (14:09)
 */
@RunWith(PowerMockRunner.class)
public class MK10InboundDeviceProtocolTest {

    private static final String TIMEOUT_KEY = Environment.getDefault().getTranslation("protocol.timeout");
    private static final String RETRIES_KEY = Environment.getDefault().getTranslation("protocol.retries");

    private static final String SERIALNUMBER = "209435639";

    private static final String HEART_BEAT_PUSH_PACKET =
            "$8F$50$FF$E5$0C$7B$BB$F7$45$30$39$30$39$34$33$39" +
                    "$00$33$35$33$31$36$37$30$30$35$39$36$32$37$37$34" +
                    "$00$38$39$34$34$31$32$32$32$35$32$31$37$31$37$37" +
                    "$33$36$31$38$00$0E$FD";

    private static final String COMMISSIONING_PUSH_PACKET =
            "$8F$50$FF$E3$0C$77$9B$5F$45$30$39$30$31$33$34$36" +
                    "$00$33$35$36$31$38$37$30$33$30$30$30$32$35$31$38" +
                    "$00$4D$6B$31$30$5F$53$53$43$5F$30$31$34$35$00$31" +
                    "$30$58$58$00$31$2E$33$36$20$00$04$02$6D$40$31$31" +
                    "$61$66$2C$37$64$62$62$2C$38$2C$00$00$00$00$00$00" +
                    "$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00" +
                    "$E4$37";

    protected int count;
    protected byte[] inboundFrame;

    @Mock
    private ComChannel comChannel;
    @Mock
    private PropertySpecService propertySpecService;

    @Test
    public void heartBeatPacketHandlingTest() {
        inboundFrame = ProtocolTools.getBytesFromHexString(HEART_BEAT_PUSH_PACKET);
        mockComChannel();

        MK10InboundDeviceProtocol inboundDeviceProtocol = getProtocolInstance();
        inboundDeviceProtocol.initComChannel(comChannel);
        InboundDeviceProtocol.DiscoverResultType discoverResultType = inboundDeviceProtocol.doDiscovery();
        DeviceIdentifier deviceIdentifier = inboundDeviceProtocol.getDeviceIdentifier();

        assertEquals(InboundDeviceProtocol.DiscoverResultType.IDENTIFIER, discoverResultType);
        assertEquals(DialHomeIdDeviceIdentifier.class, deviceIdentifier.getClass());
        assertEquals("device with call home id " + SERIALNUMBER, deviceIdentifier.toString());
    }

    @Test(expected = ProtocolRuntimeException.class)
    public void commissioningPacketHandlingTest() {
        inboundFrame = ProtocolTools.getBytesFromHexString(COMMISSIONING_PUSH_PACKET);
        mockComChannel();

        MK10InboundDeviceProtocol inboundDeviceProtocol = getProtocolInstance();
        inboundDeviceProtocol.initComChannel(comChannel);
        inboundDeviceProtocol.doDiscovery();
    }

    private MK10InboundDeviceProtocol getProtocolInstance() {
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(TIMEOUT_KEY, new BigDecimal(5000));
        properties.setProperty(RETRIES_KEY, BigDecimal.ZERO);
        MK10InboundDeviceProtocol inboundDeviceProtocol = new MK10InboundDeviceProtocol(propertySpecService);
        inboundDeviceProtocol.setProperties(properties);
        return inboundDeviceProtocol;
    }

    /**
     * Method that mocks the comChannel to return the bytes of field 'inboundFrame' and to return the available length.
     * This stubs an incoming frame on the inputstream of the comChannel.
     */
    public void mockComChannel() {
        count = 0;
        when(comChannel.read()).thenReturn(nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte(), nextByte());
        Integer[] values = new Integer[inboundFrame.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = 1;
        }
        values[inboundFrame.length - 1] = 0;

        when(comChannel.available()).thenReturn(1, values);
    }

    private Integer nextByte() {
        try {
            return (inboundFrame[count++] & 0xFF);
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }
    }
}

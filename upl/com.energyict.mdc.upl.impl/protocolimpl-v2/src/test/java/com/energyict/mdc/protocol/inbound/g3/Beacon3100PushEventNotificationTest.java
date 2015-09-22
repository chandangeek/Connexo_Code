package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.exceptions.ComServerExceptionFactoryProvider;
import com.energyict.mdc.exceptions.DefaultComServerExceptionFactoryProvider;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.SynchroneousComChannel;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDAO;
import com.energyict.mdc.protocol.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.SecurityProperty;
import com.energyict.mdc.protocol.security.SecurityPropertySet;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.Beacon3100;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Beacon3100PushEventNotificationTest extends TestCase {

    private static final byte[] EVENT_SERIAL_NUMBER_READOUT = ProtocolTools.getBytesFromHexString("00010001000100A3C2004E2C000080000CFF03020509203031303534323530333730313030313632313334313537333030303239373831090C07DF0910030632243A000000120000120037095E7B224D657465724964656E746966696572223A22303230303A303046463A464530303A30313037222C22526573756C74223A22457865637574696F6E206F66207072656C696D696E6172792070726F746F636F6C206661696C65642E227D", "");
    private static final byte[] EVENT_METER_REGISTERED = ProtocolTools.getBytesFromHexString("0001000100010086C2004E2C000080000CFF03020509203031303534323530333730313030313632313334313537333030303236363435090C07DF0818010C260E310000001200001200C209414E6F6465205B303230303A303046463A464530303A303030305D205B3078303030315D206861732072656769737465726564206F6E20746865206E6574776F726B", "");
    private static final DeviceTopology DEVICE_TOPOLOGY = new DeviceTopology(null);

    @Mock
    private final DummyComChannel voidTcpComChannel = spy(new DummyComChannel());
    private final MockBeacon3100Protocol mockBeacon3100Protocol = new MockBeacon3100Protocol();
    @Mock
    protected CollectedDataFactoryProvider collectedDataFactoryProvider;
    @Mock
    protected CollectedDataFactory collectedDataFactory;
    @Mock
    private InboundDiscoveryContext context;
    @Mock
    private InboundDAO inboundDAO;

    @Before
    public void doBefore() throws IOException {
        context = mock(InboundDiscoveryContext.class);
        inboundDAO = mock(InboundDAO.class);
        when(context.getInboundDAO()).thenReturn(inboundDAO);

        when(collectedDataFactoryProvider.getCollectedDataFactory()).thenReturn(collectedDataFactory);
        CollectedDataFactoryProvider.instance.set(collectedDataFactoryProvider);
        LogBookIdentifier any = any(LogBookIdentifier.class);
        when(collectedDataFactory.createCollectedLogBook(any)).thenReturn(new DeviceLogBook(any));

        ComServerExceptionFactoryProvider.instance.set(new DefaultComServerExceptionFactoryProvider());
    }

    @Test
    public void testSerialNumberReadout() throws Exception {

        List<SecurityProperty> securityProperties = createSecurityProperties();
        when(inboundDAO.getDeviceProtocolSecurityProperties(Matchers.<DeviceIdentifier>any(), Matchers.<InboundComPort>any())).thenReturn(securityProperties);

        MockPushEventNotification discoveryProtocol = new MockPushEventNotification();
        SynchroneousComChannel comChannel = createComChannel(EVENT_SERIAL_NUMBER_READOUT);

        //Business methods
        discoveryProtocol.initComChannel(comChannel);
        discoveryProtocol.initializeDiscoveryContext(context);
        InboundDeviceProtocol.DiscoverResultType discoverResultType = discoveryProtocol.doDiscovery();

        //Asserts
        assertEquals(InboundDeviceProtocol.DiscoverResultType.DATA, discoverResultType);
        assertEquals(0, discoveryProtocol.getProvidePSKMethodCalled());
        assertEquals(new DeviceIdentifierBySerialNumber("01054250370100162134157300029781"), discoveryProtocol.getDeviceIdentifier());
        assertEquals(1, discoveryProtocol.getCollectedData().size());
        CollectedData collectedLogBook = discoveryProtocol.getCollectedData().get(0);
        assertEquals(DeviceLogBook.class, collectedLogBook.getClass());

        MeterProtocolEvent meterProtocolEvent = ((CollectedLogBook) collectedLogBook).getCollectedMeterEvents().get(0);
        assertEquals(1442386236000L, meterProtocolEvent.getTime().getTime());
        assertEquals("{\"MeterIdentifier\":\"0200:00FF:FE00:0107\",\"Result\":\"Execution of preliminary protocol failed.\"}", meterProtocolEvent.getMessage());
        assertEquals(0, meterProtocolEvent.getEiCode());
        assertEquals(55, meterProtocolEvent.getProtocolCode());

        assertEquals(0, mockBeacon3100Protocol.getTerminateMethodCalled());
        assertEquals(0, mockBeacon3100Protocol.getLogOffMethodCalled());
        verify(voidTcpComChannel, times(0)).close();
    }

    @Test
    public void testMeterRegistered() throws Exception {

        List<SecurityProperty> securityProperties = createSecurityProperties();
        when(inboundDAO.getDeviceProtocolSecurityProperties(Matchers.<DeviceIdentifier>any(), Matchers.<InboundComPort>any())).thenReturn(securityProperties);

        MockPushEventNotification discoveryProtocol = new MockPushEventNotification();
        SynchroneousComChannel comChannel = createComChannel(EVENT_METER_REGISTERED);

        //Business methods
        discoveryProtocol.initComChannel(comChannel);
        discoveryProtocol.initializeDiscoveryContext(context);
        InboundDeviceProtocol.DiscoverResultType discoverResultType = discoveryProtocol.doDiscovery();

        //Asserts
        assertEquals(InboundDeviceProtocol.DiscoverResultType.DATA, discoverResultType);
        assertEquals(0, discoveryProtocol.getProvidePSKMethodCalled());
        assertEquals(new DeviceIdentifierBySerialNumber("01054250370100162134157300026645"), discoveryProtocol.getDeviceIdentifier());
        assertEquals(2, discoveryProtocol.getCollectedData().size());
        CollectedData collectedLogBook = discoveryProtocol.getCollectedData().get(0);
        assertEquals(DeviceLogBook.class, collectedLogBook.getClass());

        MeterProtocolEvent meterProtocolEvent = ((CollectedLogBook) collectedLogBook).getCollectedMeterEvents().get(0);
        assertEquals(1440419894000L, meterProtocolEvent.getTime().getTime());
        assertEquals("Node [0200:00FF:FE00:0000] [0x0001] has registered on the network", meterProtocolEvent.getMessage());
        assertEquals(0, meterProtocolEvent.getEiCode());
        assertEquals(194, meterProtocolEvent.getProtocolCode());

        assertEquals(DEVICE_TOPOLOGY, discoveryProtocol.getCollectedData().get(1));

        assertEquals(1, mockBeacon3100Protocol.getTerminateMethodCalled());
        assertEquals(1, mockBeacon3100Protocol.getLogOffMethodCalled());
        assertEquals(1, mockBeacon3100Protocol.getDeviceTopologyMethodCalled());
        verify(voidTcpComChannel, times(1)).close();
    }

    private SynchroneousComChannel createComChannel(byte[] inboundFrame) {
        SynchroneousComChannel comChannel = new SynchroneousComChannel(new MockedInputStream(inboundFrame), mock(OutputStream.class));
        TypedProperties comChannelProperties = TypedProperties.empty();
        comChannelProperties.setProperty(ComChannelType.TYPE, ComChannelType.SocketComChannel.getType());
        comChannel.addProperties(comChannelProperties);
        comChannel.startReading();
        return comChannel;
    }

    private List<SecurityProperty> createSecurityProperties() {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getAuthenticationDeviceAccessLevelId()).thenReturn(0);
        when(securityPropertySet.getEncryptionDeviceAccessLevelId()).thenReturn(0);

        SecurityProperty clientProperty = mock(SecurityProperty.class);
        when(clientProperty.getValue()).thenReturn(BigDecimal.ONE);
        when(clientProperty.getName()).thenReturn(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString());
        when(clientProperty.getSecurityPropertySet()).thenReturn(securityPropertySet);

        ArrayList<SecurityProperty> result = new ArrayList<>();
        result.add(clientProperty);
        return result;
    }

    public class MockPushEventNotification extends Beacon3100PushEventNotification {

        private int providePSKMethodCalled = 0;

        @Override
        protected DeviceProtocol initializeGatewayProtocol(DeviceProtocolSecurityPropertySet securityPropertySet, DeviceProtocol deviceProtocol) {
            tcpComChannel = voidTcpComChannel;
            return mockBeacon3100Protocol;
        }

        @Override
        protected void providePSK(DlmsSession dlmsSession) {
            providePSKMethodCalled++;
        }

        public int getProvidePSKMethodCalled() {
            return providePSKMethodCalled;
        }
    }

    public class MockBeacon3100Protocol extends Beacon3100 {

        private int terminateMethodCalled = 0;
        private int logOffMethodCalled = 0;
        private int deviceTopologyMethodCalled = 0;

        @Override
        public CollectedTopology getDeviceTopology() {
            deviceTopologyMethodCalled++;
            return DEVICE_TOPOLOGY;
        }

        @Override
        public void logOff() {
            logOffMethodCalled++;
        }

        @Override
        public void terminate() {
            terminateMethodCalled++;
        }

        public int getDeviceTopologyMethodCalled() {
            return deviceTopologyMethodCalled;
        }

        public int getTerminateMethodCalled() {
            return terminateMethodCalled;
        }

        public int getLogOffMethodCalled() {
            return logOffMethodCalled;
        }
    }

    public class MockedInputStream extends InputStream {

        final byte[] frame;
        int readCount = 0;

        public MockedInputStream(byte[] frame) {
            this.frame = frame;
        }

        @Override
        public int read(byte[] argument) throws IOException {
            readCount++;
            if (readCount == 1) {       //First return the header
                byte[] header = ProtocolTools.getSubArray(frame, 0, 8);
                System.arraycopy(header, 0, argument, 0, argument.length);
                return argument.length;
            } else {                    //Then return the remaining frame
                byte[] frame = ProtocolTools.getSubArray(this.frame, 8);
                System.arraycopy(frame, 0, argument, 0, argument.length);
                return argument.length;
            }
        }

        @Override
        public int read() throws IOException {
            return 0;
        }
    }
}
package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.exceptions.ComServerExceptionFactoryProvider;
import com.energyict.mdc.exceptions.DefaultComServerExceptionFactoryProvider;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.protocol.SynchroneousComChannel;
import com.energyict.mdc.protocol.VoidComChannel;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDAO;
import com.energyict.mdc.protocol.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.SecurityProperty;
import com.energyict.mdc.protocol.security.SecurityPropertySet;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.RtuPlusServer;
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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PushEventNotificationTest extends TestCase {

    private static final byte[] ENCRYPTED_FRAME_WITH_AUTHENTICATION = ProtocolTools.getBytesFromHexString("000100010001007EDB08444330353946343373300000004069B91C449C059A907F83CD7E2D296789598E09B84C5189042EADC17F2B3E6F09E52E3ABC26F6ECCDD24C787653C9DFF71DF92426F198BDF479F910E58C3F33A10766B10DB7435E1DF6B434B57684F7EE5157ACA9F6658A5A7F2E2F133FDD87F3309D65CF1660BB26FEF3749F40C3", "");
    private static final byte[] ENCRYPTED_FRAME = ProtocolTools.getBytesFromHexString("000100010001008ADB0844433035394634337F200000002AD41FCCC0B439CFEBFBD7728B6E11C65855FAD2B460BD225C5FB5A9069E96F56B0602F0A689453F9FE424F1EF7FF0D9695E719FC2492763B2B9427A067ADAFAFAC7584386F8DB9B72F3A5827A34E4277E9B044840FBEC83F7FDF9BEEF915D37D8EDD07CAE83592F79ACB0EDF09343CF4CC1FF03D9D3FA505DD04A", "");

    private static final String AK = "B6C52294F40A30B9BDF9FE4270B03685";
    private static final String EK = "EFD82FCB93E5826ED805E38A6B2EC9F1";

    @Mock
    protected CollectedDataFactoryProvider collectedDataFactoryProvider;
    @Mock
    protected CollectedDataFactory collectedDataFactory;
    @Mock
    private InboundDiscoveryContext context;
    @Mock
    private InboundDAO inboundDAO;
    @Mock
    private final DummyComChannel voidTcpComChannel = spy(new DummyComChannel());

    private final MockRtuPlusServer mockRtuPlusServer = new MockRtuPlusServer();

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
    public void testJoiningAttempt() throws Exception {

        List<SecurityProperty> securityProperties = createSecurityProperties(3);
        when(inboundDAO.getDeviceProtocolSecurityProperties(Matchers.<DeviceIdentifier>any(), Matchers.<InboundComPort>any())).thenReturn(securityProperties);

        MockPushEventNotification discoveryProtocol = new MockPushEventNotification();
        SynchroneousComChannel comChannel = createComChannel(ENCRYPTED_FRAME_WITH_AUTHENTICATION);

        //Business methods
        discoveryProtocol.initComChannel(comChannel);
        discoveryProtocol.initializeDiscoveryContext(context);
        InboundDeviceProtocol.DiscoverResultType discoverResultType = discoveryProtocol.doDiscovery();

        //Asserts
        assertEquals(InboundDeviceProtocol.DiscoverResultType.DATA, discoverResultType);
        assertEquals(1, discoveryProtocol.getProvidePSKMethodCalled());
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), discoveryProtocol.getDeviceIdentifier());
        assertEquals(1, discoveryProtocol.getCollectedData().size());
        CollectedData collectedLogBook = discoveryProtocol.getCollectedData().get(0);
        assertEquals(DeviceLogBook.class, collectedLogBook.getClass());

        MeterProtocolEvent meterProtocolEvent = ((CollectedLogBook) collectedLogBook).getCollectedMeterEvents().get(0);
        assertEquals(1421244305000L, meterProtocolEvent.getTime().getTime());
        assertEquals("Joining request for node [0223:7EFF:FEFD:848D]", meterProtocolEvent.getMessage());
        assertEquals(0, meterProtocolEvent.getEiCode());
        assertEquals(197, meterProtocolEvent.getProtocolCode());

        assertEquals(1, mockRtuPlusServer.getTerminateMethodCalled());
        assertEquals(1, mockRtuPlusServer.getLogOffMethodCalled());
        verify(voidTcpComChannel, times(1)).close();
    }

    @Test
    public void testJoiningSuccess() throws Exception {

        List<SecurityProperty> securityProperties = createSecurityProperties(2);
        when(inboundDAO.getDeviceProtocolSecurityProperties(Matchers.<DeviceIdentifier>any(), Matchers.<InboundComPort>any())).thenReturn(securityProperties);

        MockPushEventNotification discoveryProtocol = new MockPushEventNotification();
        SynchroneousComChannel comChannel = createComChannel(ENCRYPTED_FRAME);

        //Business methods
        discoveryProtocol.initComChannel(comChannel);
        discoveryProtocol.initializeDiscoveryContext(context);
        InboundDeviceProtocol.DiscoverResultType discoverResultType = discoveryProtocol.doDiscovery();

        //Asserts
        assertEquals(InboundDeviceProtocol.DiscoverResultType.DATA, discoverResultType);
        assertEquals(0, discoveryProtocol.getProvidePSKMethodCalled());
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), discoveryProtocol.getDeviceIdentifier());
        assertEquals(2, discoveryProtocol.getCollectedData().size());
        CollectedData collectedLogBook = discoveryProtocol.getCollectedData().get(0);
        assertEquals(DeviceLogBook.class, collectedLogBook.getClass());

        MeterProtocolEvent meterProtocolEvent = ((CollectedLogBook) collectedLogBook).getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1427125126000L);
        assertEquals(meterProtocolEvent.getMessage(), "G3 : Node [0223:7EFF:FEFD:848C] [0x0005] has registered on the network");
        assertEquals(meterProtocolEvent.getEiCode(), 0);
        assertEquals(meterProtocolEvent.getProtocolCode(), 194);

        CollectedData deviceTopology = discoveryProtocol.getCollectedData().get(1);
        assertEquals(DeviceTopology.class, deviceTopology.getClass());

        assertEquals(1, mockRtuPlusServer.getTerminateMethodCalled());
        assertEquals(1, mockRtuPlusServer.getLogOffMethodCalled());
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

    private List<SecurityProperty> createSecurityProperties(int dataTransportLevel) {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getAuthenticationDeviceAccessLevelId()).thenReturn(5);
        when(securityPropertySet.getEncryptionDeviceAccessLevelId()).thenReturn(dataTransportLevel);

        SecurityProperty akProperty = mock(SecurityProperty.class);
        when(akProperty.getValue()).thenReturn(AK);
        when(akProperty.getName()).thenReturn(SecurityPropertySpecName.AUTHENTICATION_KEY.toString());
        when(akProperty.getSecurityPropertySet()).thenReturn(securityPropertySet);

        SecurityProperty ekProperty = mock(SecurityProperty.class);
        when(ekProperty.getValue()).thenReturn(EK);
        when(ekProperty.getName()).thenReturn(SecurityPropertySpecName.ENCRYPTION_KEY.toString());
        when(ekProperty.getSecurityPropertySet()).thenReturn(securityPropertySet);

        List<SecurityProperty> securityProperties = new ArrayList<>();
        securityProperties.add(akProperty);
        securityProperties.add(ekProperty);
        return securityProperties;
    }

    public class MockPushEventNotification extends PushEventNotification {

        private int providePSKMethodCalled = 0;

        @Override
        protected RtuPlusServer initializeGatewayProtocol(DeviceProtocolSecurityPropertySet securityPropertySet) {
            tcpComChannel = voidTcpComChannel;
            return mockRtuPlusServer;
        }

        @Override
        protected void providePSK(DlmsSession dlmsSession) {
            providePSKMethodCalled++;
        }

        public int getProvidePSKMethodCalled() {
            return providePSKMethodCalled;
        }
    }

    public class MockRtuPlusServer extends RtuPlusServer {

        private int terminateMethodCalled = 0;
        private int logOffMethodCalled = 0;

        @Override
        public CollectedTopology getDeviceTopology() {
            return new DeviceTopology(null);
        }

        @Override
        public void logOff() {
            logOffMethodCalled++;
        }

        @Override
        public void terminate() {
            terminateMethodCalled++;
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
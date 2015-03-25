package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.meterdata.CollectedDataFactory;
import com.energyict.mdc.meterdata.CollectedDataFactoryProvider;
import com.energyict.mdc.meterdata.DeviceLogBook;
import com.energyict.mdc.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.protocol.SynchroneousComChannel;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDAO;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.security.SecurityProperty;
import com.energyict.mdc.protocol.security.SecurityPropertySet;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.utils.ProtocolTools;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventPushNotificationParserTest extends TestCase {

    private static final byte[] PLAIN_FRAME = ProtocolTools.getBytesFromHexString("000100010001007AC2004E2C000080000CFF030205090F3636302D3030353435442D31313235090C07DE080D030A2A11410000001200001200C209464733203A204E6F6465205B303232333A374546463A464546443A414145395D205B3078303030365D206861732072656769737465726564206F6E20746865206E6574776F726B", "");
    private static final byte[] PLAIN_FRAME2 = ProtocolTools.getBytesFromHexString("0001000100010062C2004E2C000080000CFF030205090F3636302D3035394634332D31343235090C07DF010D020F1A03130000001200001200C5092E4A6F696E696E67207265717565737420666F72206E6F6465205B303232333A374546463A464546443A383438445DC2", "");
    private static final byte[] ENCRYPTED_FRAME = ProtocolTools.getBytesFromHexString("0001000100010072db084443303539463433672000000001ba9521c6c8f9ed4f4d5c57680a850e5e8e642f9d9a664d46765c182ee5dabc33aef24bc6b7d7e2688749d3ef523a5d521b71c27829e852f01aba7e615b56b017511c696ebc69fc8da85f94f5fea609425a6650a463fbf8fc97e20aed4f62a09aa302", "");
    private static final byte[] ENCRYPTED_FRAME2 = ProtocolTools.getBytesFromHexString("0001000100010072DB084443303539463433672000000026F13F7B1196D51210A4E9CDA0A23F724BEDE53EE4FADC914BB85826862DCD067ECE37131ADE80BE5E1E1DEF69B8550F0D218172D44E20302A16D8DF1A88DD233724A778307E1F0907CF9144A793000912912255447EE05A78D3A21C750D71378C55A4", "");
    private static final byte[] ENCRYPTED_FRAME3 = ProtocolTools.getBytesFromHexString("000100010001008ADB0844433035394634337F200000002AD41FCCC0B439CFEBFBD7728B6E11C65855FAD2B460BD225C5FB5A9069E96F56B0602F0A689453F9FE424F1EF7FF0D9695E719FC2492763B2B9427A067ADAFAFAC7584386F8DB9B72F3A5827A34E4277E9B044840FBEC83F7FDF9BEEF915D37D8EDD07CAE83592F79ACB0EDF09343CF4CC1FF03D9D3FA505DD04A", "");
    private static final byte[] ENCRYPTED_FRAME4 = ProtocolTools.getBytesFromHexString("0001000100010072DB084443303539463433672000000022B2BF8086C69DBE8831B0D183E0EA98CBF8F0EFC79C8D13C9078749A16DAB0924E53B384E624DABB4CAA9C35047762FC169C1F2250AD63EF247A5A5FA31A2E48BC39DB4A8E2776367E1FFD64ABC964923ACB913985CCE99EABF69D2D5D6F1626DF632", "");
    private static final byte[] ENCRYPTED_FRAME_WITH_AUTHENTICATION = ProtocolTools.getBytesFromHexString("000100010001007edb08444330353946343373300000004069b91c449c059a907f83cd7e2d296789598e09b84c5189042eadc17f2b3e6f09e52e3abc26f6eccdd24c787653c9dff71df92426f198bdf479f910e58c3f33a10766b10db7435e1df6b434b57684f7ee5157aca9f6658a5a7f2e2f133fdd87f3309d65cf1660bb26fef3749f40c3", "");
    private static final byte[] ENCRYPTED_FRAME_WITH_AUTHENTICATION2 = ProtocolTools.getBytesFromHexString("000100010001007EDB084443303539463433733000000003198C85F5D20BF3598D87E2913BAA1DB96B22A11D2EDCE20F5D96FA90C6B9F87504AA4922D58F93C4D954E097DE77725A3A6CA0392439CF475E905EBB58B134B894A103B0F8CFFC9A0115903A5C3DF2CABE523C67E1530976C68AA4C5F0EC5A5370CF12F02B388B5272964931339B", "");
    //TODO an authenticated frame without encryption

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

    @Before
    public void doBefore() throws IOException {
        context = mock(InboundDiscoveryContext.class);
        inboundDAO = mock(InboundDAO.class);

        List<SecurityProperty> securityProperties = createSecurityProperties(3);
        when(inboundDAO.getDeviceProtocolSecurityProperties(Matchers.<DeviceIdentifier>any(), Matchers.<InboundComPort>any())).thenReturn(securityProperties);
        when(context.getInboundDAO()).thenReturn(inboundDAO);

        when(collectedDataFactoryProvider.getCollectedDataFactory()).thenReturn(collectedDataFactory);
        CollectedDataFactoryProvider.instance.set(collectedDataFactoryProvider);
        LogBookIdentifier any = any(LogBookIdentifier.class);
        when(collectedDataFactory.createCollectedLogBook(any)).thenReturn(new DeviceLogBook(any));
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

    @Test
    public void testPlainFrame() throws IOException, SQLException, BusinessException {
        EventPushNotificationParser parser = spyParser(PLAIN_FRAME);
        parser.parseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-00545D-1125"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1407926537000L);
        assertEquals(meterProtocolEvent.getMessage(), "G3 : Node [0223:7EFF:FEFD:AAE9] [0x0006] has registered on the network");
        assertEquals(meterProtocolEvent.getEiCode(), 0);
        assertEquals(meterProtocolEvent.getProtocolCode(), 194);
    }

    @Test
    public void testPlainFrame2() throws IOException, SQLException, BusinessException {
        EventPushNotificationParser parser = spyParser(PLAIN_FRAME2);
        parser.parseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1421162763000L);
        assertEquals(meterProtocolEvent.getMessage(), "Joining request for node [0223:7EFF:FEFD:848D]");
        assertEquals(meterProtocolEvent.getEiCode(), 0);
        assertEquals(meterProtocolEvent.getProtocolCode(), 197);
    }

    @Test
    public void testEncryptedFrame() throws IOException, SQLException, BusinessException {
        List<SecurityProperty> securityProperties = createSecurityProperties(2);
        when(inboundDAO.getDeviceProtocolSecurityProperties(Matchers.<DeviceIdentifier>any(), Matchers.<InboundComPort>any())).thenReturn(securityProperties);
        EventPushNotificationParser parser = spyParser(ENCRYPTED_FRAME);
        parser.parseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1421244545000L);
        assertEquals(meterProtocolEvent.getMessage(), "Joining request for node [0223:7EFF:FEFD:848D]");
        assertEquals(meterProtocolEvent.getEiCode(), 0);
        assertEquals(meterProtocolEvent.getProtocolCode(), 197);

        EventPushNotificationParser parser2 = spyParser(ENCRYPTED_FRAME2);
        parser2.parseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser2.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent2 = parser2.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent2.getTime().getTime(), 1427125121000L);
        assertEquals(meterProtocolEvent2.getMessage(), "Joining request for node [0223:7EFF:FEFD:848C]");
        assertEquals(meterProtocolEvent2.getEiCode(), 0);
        assertEquals(meterProtocolEvent2.getProtocolCode(), 197);

        EventPushNotificationParser parser3 = spyParser(ENCRYPTED_FRAME3);
        parser3.parseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser3.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent3 = parser3.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent3.getTime().getTime(), 1427125126000L);
        assertEquals(meterProtocolEvent3.getMessage(), "G3 : Node [0223:7EFF:FEFD:848C] [0x0005] has registered on the network");
        assertEquals(meterProtocolEvent3.getEiCode(), 0);
        assertEquals(meterProtocolEvent3.getProtocolCode(), 194);

        EventPushNotificationParser parser4 = spyParser(ENCRYPTED_FRAME4);
        parser4.parseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser4.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent4 = parser4.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent4.getTime().getTime(), 1427125053000L);
        assertEquals(meterProtocolEvent4.getMessage(), "Joining request for node [0223:7EFF:FEFD:848C]");
        assertEquals(meterProtocolEvent4.getEiCode(), 0);
        assertEquals(meterProtocolEvent4.getProtocolCode(), 197);
    }

    @Test
    public void testEncryptedAndAuthenticatedFrame() throws IOException, SQLException, BusinessException {
        EventPushNotificationParser parser = spyParser(ENCRYPTED_FRAME_WITH_AUTHENTICATION);
        parser.parseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1421244305000L);
        assertEquals(meterProtocolEvent.getMessage(), "Joining request for node [0223:7EFF:FEFD:848D]");
        assertEquals(meterProtocolEvent.getEiCode(), 0);
        assertEquals(meterProtocolEvent.getProtocolCode(), 197);


        EventPushNotificationParser parser2 = spyParser(ENCRYPTED_FRAME_WITH_AUTHENTICATION2);
        parser2.parseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser2.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent2 = parser2.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent2.getTime().getTime(), 1427105996000L);
        assertEquals(meterProtocolEvent2.getMessage(), "Joining request for node [0223:7EFF:FEFD:848C]");
        assertEquals(meterProtocolEvent2.getEiCode(), 0);
        assertEquals(meterProtocolEvent2.getProtocolCode(), 197);
    }

    private EventPushNotificationParser spyParser(byte[] frame) {
        SynchroneousComChannel comChannel = new SynchroneousComChannel(new MockedInputStream(frame), mock(OutputStream.class));
        TypedProperties comChannelProperties = TypedProperties.empty();
        comChannelProperties.setProperty(ComChannelType.TYPE, ComChannelType.SocketComChannel.getType());
        comChannel.addProperties(comChannelProperties);
        comChannel.startReading();
        return spy(new EventPushNotificationParser(comChannel, context));
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
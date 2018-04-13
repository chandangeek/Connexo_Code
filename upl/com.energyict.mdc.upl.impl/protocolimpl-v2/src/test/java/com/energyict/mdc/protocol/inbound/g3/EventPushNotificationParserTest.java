package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.mdc.MockDeviceLogBook;
import com.energyict.mdc.MockDeviceTopology;
import com.energyict.mdc.channel.SynchroneousComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.upl.InboundDAO;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.security.SecurityPropertySpecTranslationKeys;
import junit.framework.TestCase;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventPushNotificationParserTest extends TestCase {

    private static final byte[] PLAIN_FRAME = ProtocolTools.getBytesFromHexString("000100010001007AC2004E2C000080000CFF030205090F3636302D3030353435442D31313235090C07DE080D030A2A11410000001200001200C209464733203A204E6F6465205B303232333A374546463A464546443A414145395D205B3078303030365D206861732072656769737465726564206F6E20746865206E6574776F726B", "");
    private static final byte[] PLAIN_FRAME2 = ProtocolTools.getBytesFromHexString("0001000100010062C2004E2C000080000CFF030205090F3636302D3035394634332D31343235090C07DF010D020F1A03130000001200001200C5092E4A6F696E696E67207265717565737420666F72206E6F6465205B303232333A374546463A464546443A383438445DC2", "");
    private static final byte[] PLAIN_FRAME3 = ProtocolTools.getBytesFromHexString("0001000100010079C2004E2C000080000CFF030203090E33343135373330303032383030331200010204090C07E0071302060927300000001200001200c209414e6f6465205b303032333a374546463a464546443a373733385d205b3078303030315d206861732072656769737465726564206f6e20746865206e6574776f726b", "");

    private static final byte[] ENCRYPTED_FRAME = ProtocolTools.getBytesFromHexString("0001000100010072db084443303539463433672000000001ba9521c6c8f9ed4f4d5c57680a850e5e8e642f9d9a664d46765c182ee5dabc33aef24bc6b7d7e2688749d3ef523a5d521b71c27829e852f01aba7e615b56b017511c696ebc69fc8da85f94f5fea609425a6650a463fbf8fc97e20aed4f62a09aa302", "");
    private static final byte[] ENCRYPTED_FRAME2 = ProtocolTools.getBytesFromHexString("0001000100010072DB084443303539463433672000000026F13F7B1196D51210A4E9CDA0A23F724BEDE53EE4FADC914BB85826862DCD067ECE37131ADE80BE5E1E1DEF69B8550F0D218172D44E20302A16D8DF1A88DD233724A778307E1F0907CF9144A793000912912255447EE05A78D3A21C750D71378C55A4", "");
    private static final byte[] ENCRYPTED_FRAME3 = ProtocolTools.getBytesFromHexString("000100010001008ADB0844433035394634337F200000002AD41FCCC0B439CFEBFBD7728B6E11C65855FAD2B460BD225C5FB5A9069E96F56B0602F0A689453F9FE424F1EF7FF0D9695E719FC2492763B2B9427A067ADAFAFAC7584386F8DB9B72F3A5827A34E4277E9B044840FBEC83F7FDF9BEEF915D37D8EDD07CAE83592F79ACB0EDF09343CF4CC1FF03D9D3FA505DD04A", "");
    private static final byte[] ENCRYPTED_FRAME4 = ProtocolTools.getBytesFromHexString("0001000100010072DB084443303539463433672000000022B2BF8086C69DBE8831B0D183E0EA98CBF8F0EFC79C8D13C9078749A16DAB0924E53B384E624DABB4CAA9C35047762FC169C1F2250AD63EF247A5A5FA31A2E48BC39DB4A8E2776367E1FFD64ABC964923ACB913985CCE99EABF69D2D5D6F1626DF632", "");
    private static final byte[] ENCRYPTED_FRAME_WITH_AUTHENTICATION = ProtocolTools.getBytesFromHexString("000100010001007edb08444330353946343373300000004069b91c449c059a907f83cd7e2d296789598e09b84c5189042eadc17f2b3e6f09e52e3abc26f6eccdd24c787653c9dff71df92426f198bdf479f910e58c3f33a10766b10db7435e1df6b434b57684f7ee5157aca9f6658a5a7f2e2f133fdd87f3309d65cf1660bb26fef3749f40c3", "");
    private static final byte[] ENCRYPTED_FRAME_WITH_AUTHENTICATION2 = ProtocolTools.getBytesFromHexString("000100010001007EDB084443303539463433733000000003198C85F5D20BF3598D87E2913BAA1DB96B22A11D2EDCE20F5D96FA90C6B9F87504AA4922D58F93C4D954E097DE77725A3A6CA0392439CF475E905EBB58B134B894A103B0F8CFFC9A0115903A5C3DF2CABE523C67E1530976C68AA4C5F0EC5A5370CF12F02B388B5272964931339B", "");
    private static final byte[] AUTHENTICATED_NOT_ENCRYPTED = ProtocolTools.getBytesFromHexString("0001000100010050DB084443303539463433451000000001C2004E2C000080000CFF030205090F3636302D3035394634332D31343235090C07DF0319030D3717390000001200021200000900FF6382B1E98C8563E5618A06", "");

    private static final byte[] BEACON_PLAIN_EVENT_SERIAL_NUMBER_READOUT = ProtocolTools.getBytesFromHexString("00010001000100A3C2004E2C000080000CFF03020509203031303534323530333730313030313632313334313537333030303239373831090C07DF0910030632243A000000120000120037095E7B224D657465724964656E746966696572223A22303230303A303046463A464530303A30313037222C22526573756C74223A22457865637574696F6E206F66207072656C696D696E6172792070726F746F636F6C206661696C65642E227D", "");
    private static final byte[] BEACON_PLAIN_EVENT_METER_REGISTERED = ProtocolTools.getBytesFromHexString("0001000100010086C2004E2C000080000CFF03020509203031303534323530333730313030313632313334313537333030303236363435090C07DF0818010C260E310000001200001200C209414E6F6465205B303230303A303046463A464530303A303030305D205B3078303030315D206861732072656769737465726564206F6E20746865206E6574776F726B", "");

    private static final byte[] BEACON_ENCRYPTED_AUTHENTICATED_NOTIFICATION = ProtocolTools.getBytesFromHexString("00010001000100f2db08454c536309405a3081e6310000000eee5f15724bc711483cc8a0daba32b2dcb1d09018ae556db75e18bbf733ccaf9bec5cbf3b2b85bfc06c27b8b279caec842262d1345ee6f2fe7f4aff110515a117489b09041929e1d93e979fc105f96ec4bd31c4c6b38883fb8423abd5a86311cf3b7135fe0ac5cf2c97be8481f6bc5632f020dfd6b272707b48a144b365231ceb1614fd73c152e868187b5bd4fb8b51da38446ceff35421bd377e15e5cb99c4d46513683dca92999ac7fb0d1b841d4c9397b8dc0b94d3fb3890179ffb23311a233d64f409564b32de1cf78cf41fe24df66eedb7184276dcc075378ce3a6f899fe14", "");
    private static final byte[] BEACON_ENCRYPTED_AUTHENTICATED_LOST_NOTIFICATION = ProtocolTools.getBytesFromHexString("000100010001006BDB08454C536309405A3060310000000D627E0E5EF96635DED38B42086E202C07DE31DD2A1145387142F16AB0A518DB82F898617878A200E7A401E2B7883BA8DBF1E20AC72EBF8C29BB78022A66ADD08763872770FC69712D22E16F82250D71F27FFFA11CD944D439C666F4", "");

    private static final String AK = "B6C52294F40A30B9BDF9FE4270B03685";
    private static final String EK = "EFD82FCB93E5826ED805E38A6B2EC9F1";

    private static final String AK_1 = "161A30B734F60189F4CFAFA3124AE361";
    private static final String EK_1 = "A2B3A3FC6AA16AD81A3934A46EDFEE33";

    private static final byte[] METER_EVENT_NOTIFICATION_1_3_0 = ProtocolTools.getBytesFromHexString("0001000100010037c2004e2c000080000cff030204090e33343135303030303030303234350910454c532d5547572d00237efffefd77361200120600200000", "");
    private static final byte[] BEACON_NOTIFICATION_1_3_0 = ProtocolTools.getBytesFromHexString("0001000100010042C2004E2C000080000CFF030205090E3334313537333030303238303033090C07E00318040A152B2C000000120017120000090F54616D706572206465746563746564", "");

    private static final byte[] METER_EVENT_NOTIFICATION_1_4_0 = ProtocolTools.getBytesFromHexString("0001000100010028C2004E2C000080000CFF030203090D53455249414C2D4E554D424552120016020209000600003039", "");
    private static final byte[] BEACON_NOTIFICATION_1_4_0 = ProtocolTools.getBytesFromHexString("000100010001004DC2004E2C000080000CFF030203091445717569706D656E742D4964656E7469666965721200010204090C07B20102050B11244E00000012007B1201C8090F4164646974696F6E616C20696E666F", "");
    private static final byte[] DATA_NOTIFICATION_ENCRYPTED_WITH_AUTHENTICATION_1_6_0 = ProtocolTools.getBytesFromHexString("0001000100010063db08454c5373000280035830000000021c18c0e78b589afefa0a404db82a1ae76963257f18279b19072edf8b57422663a03ce3e4119fff81f3ce3066d1e5c5bb77ae153301ec3b8d269a0d978e63e26209f6b02d8b3f429098d38399139eec23df8f3d", "");
    private static final byte[] DATA_NOTIFICATION_ENCRYPTED_WITH_AUTHENTICATION_2_0_0 = ProtocolTools.getBytesFromHexString("0001000100010093DB08454C536308800F6081873000000002DE183DE22826F2A3BD18F0F465A1A422C4F9540534ECA282568AFD38EE5E3A82B18E9B2321C3ED83793310CAC31BDE795BAF718A22E34E0B1BEF287D8752171C5E46A51366721E574FED6391651855611D20DB9264361CF763CCD1F993A5A669AFC482668D3BD7ADEDCA4AF2CD94A13D5B6EF45A8548D3A741ED907ACDFD08F480C5", "");
    private static final byte[] DATA_NOTIFICATION_PLAIN_1_6_0 = ProtocolTools.getBytesFromHexString("00010001000100470f000000010c07e0071302082728410000000203090e33343135373330303032383030331200010204090c07e00713020827283f0000001200021200000908506f776572207570", "");
    private static final byte[] DATA_NOTIFICATION_PLAIN_1_6_1 = ProtocolTools.getBytesFromHexString("00010001000100280F000000000C07E0040307013B3100FFC480020101010910FE80000000000000187900FFFE000009", "");
    private static final byte[] RELAYED_EVENT_NOTIFICATION_1_6_0 = ProtocolTools.getBytesFromHexString("0001001400100010c20000010000616214ff020600002000", "");
    private static final byte[] RELAYED_EVENT_NOTIFICATION_ORIGIN_HEADER_AM540_1_6_0 = ProtocolTools.getBytesFromHexString("0001001400100039c20000010000616214ff020203090e333431353733303030323937383112001402020910454c532d5547572d020000fffe00003b0600002000", "");
    private static final byte[] RELAYED_EVENT_NOTIFICATION_ORIGIN_HEADER_LINKY_COVER_1_6_0 = ProtocolTools.getBytesFromHexString("0001001b00100039c20000010000616200ff020203090e333431353733303030323935363012001b02020910454c532d5547572d02237efffefd7fcf0600200204", "");
    private static final byte[] RELAYED_DATA_NOTIFICATION_ORIGIN_HEADER_AM540_1_6_0 = ProtocolTools.getBytesFromHexString("00010014000100400f000000010c07e0071302082728410000000203090e333431353733303030323937383112001402020910454c532d5547572d020000fffe00003b0600002000", "");
    private static final byte[] RELAYED_DATA_NOTIFICATION_WRAP_AS_SERVER_1_6_0 = ProtocolTools.getBytesFromHexString("00010001000100660f000000040c07e0080101062113100000000203090e333431353733303030323830303312001302060914554e4b4e4f574e2d4d455445522d53455249414c090802237efffefd8115120010110016020202020312000109060000616200ff0f020600200004", "");
    private static final byte[] RELAYED_DATA_NOTIFICATION_WRAP_AS_SERVER_1_6_1 = ProtocolTools.getBytesFromHexString("00010014000200540f000000000c07e0040307013b3100000080020309115254552d53455249414c2d4e554d42455212001402020910454c532d5547572d020000fffe000045020101010910fe80000000000000187900fffe000009", "");
    private static final byte[] RELAY_EVENT_NOTIFICATION_1_6_0 = ProtocolTools.getBytesFromHexString("$00$01$00$13$00$10$00$39$C2$00$00$01$00$00$61$62$14$FF$02$02$03$09$0E$33$34$31$35$37$33$30$30$30$32$38$36$36$39$12$00$12$02$02$09$10$45$4C$53$2D$55$47$57$2D$02$23$7E$FF$FE$FD$AF$24$06$00$00$20$00", "$");
    private static final byte[] RELAYED_DATA_NOTIFICATION__1_8_1 = ProtocolTools.getBytesFromHexString("0001000100010074C20000280000190900FFFF0203090E33343135373330303032303830361200010204090C07E1020E0209381A2B0000001200001200C3093C4E6F6465205B443038343A423046463A464546313A394230455D2077617320756E726567697374657265642066726F6D20746865206E6574776F726B", "");
    private static final byte[] PRELIMINARY_PROTOCOL_EXECUTION__1_8_1 = ProtocolTools.getBytesFromHexString("0001000100010075C20000280000190900FFFF0203090E33343135373330303032303830361200010204090C07E1020E020A111606000000120000120036093D7B224D657465724964656E746966696572223A22443038343A423046463A464546313A39423045222C22526573756C74223A223939303030303130227D", "");
    private static final byte[] DATA_NOTIFICATION_1_8_0 = ProtocolTools.getBytesFromHexString("00010001000100520F000000000C07E1020E020A172D3D0000000203090E33343135373330303032303830361200010204090C07E1020E020A172D3A0000001200001200230913506C6561736520696E7365727420636F696E2E", "");
    private static final byte[] ENCRYPTED_DATA_NOTIFICATION_1_8_0 = ProtocolTools.getBytesFromHexString("0001000100010069DB08454C5363094055F05E30000000024B9DB05F324D5156C150E6BB9877EE4C3538125FE7263D58F6D37EBEA54649DACB249A3A219DF31B62D7EA70BEE6CDA85E87663D1B7FD472E667EA5B509401B9043CC9967D9BCB5467064A578C1FF93A9AC09CBCAF67812F82", "");
    private static final byte[] CIPHERED_RELAYED_EVENT_NOTIFICATION_AM540 = ProtocolTools.getBytesFromHexString("00010024000200860F000000000C07E10306010D10352E0000000203090F3637302D3035424444432D3136333612002402020910454C532D5547572D02237EFFFEFDAB5F0948DB08454C5365700000013D30000047E2EAEF92D3283B6D37AEAB0712530A4788D1704C4E79919BA0670202D3D6720350DCE5050C30F327643F16943FFD2D80E73440E18E2D41A519", "");


    @Mock
    protected CollectedDataFactory collectedDataFactory;
    @Mock
    private InboundDiscoveryContext context;
    @Mock
    private InboundDAO inboundDAO;

    @Before
    public void doBefore() throws IOException {
        context = mock(InboundDiscoveryContext.class);
        when(context.getLogger()).thenReturn(Logger.getAnonymousLogger());
        inboundDAO = mock(InboundDAO.class);

        Optional<DeviceProtocolSecurityPropertySet> deviceProtocolSecurityPropertySet = createDeviceProtocolSecurityPropertySet(3, AK, EK);
        doReturn(deviceProtocolSecurityPropertySet).when(context).getDeviceProtocolSecurityPropertySet(Matchers.<DeviceIdentifier>any());
        when(context.getInboundDAO()).thenReturn(inboundDAO);

        when(collectedDataFactory.createCollectedLogBook(any(LogBookIdentifier.class))).thenReturn(new MockDeviceLogBook());
        when(collectedDataFactory.createCollectedTopology(any(DeviceIdentifier.class))).thenReturn(new MockDeviceTopology(mock(DeviceIdentifier.class)));
        when(context.getCollectedDataFactory()).thenReturn(collectedDataFactory);

        when(context.getLogger()).thenReturn(Logger.getAnonymousLogger());
    }

    public void setSecurityContext_1_6() throws IOException {
        Optional<DeviceProtocolSecurityPropertySet> deviceProtocolSecurityPropertySet = createDeviceProtocolSecurityPropertySet(3, AK_1, EK_1);
        doReturn(deviceProtocolSecurityPropertySet).when(context).getDeviceProtocolSecurityPropertySet(Matchers.<DeviceIdentifier>any());
        when(context.getInboundDAO()).thenReturn(inboundDAO);
    }

    private Optional<DeviceProtocolSecurityPropertySet> createDeviceProtocolSecurityPropertySet(int dataTransportLevel, String aKey, String eKey) {
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        when(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(5);
        when(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(dataTransportLevel);

        TypedProperties securityProperties = com.energyict.mdc.upl.TypedProperties.empty();
        securityProperties.setProperty(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString(), aKey);
        securityProperties.setProperty(SecurityPropertySpecTranslationKeys.ENCRYPTION_KEY.toString(), eKey);
        when(deviceProtocolSecurityPropertySet.getSecurityProperties()).thenReturn(securityProperties);
        return Optional.of(deviceProtocolSecurityPropertySet);
    }

    @Test
    public void testPlainFrame() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(PLAIN_FRAME);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-00545D-1125"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1407926537000L);
        assertEquals(meterProtocolEvent.getMessage(), "G3 : Node [0223:7EFF:FEFD:AAE9] [0x0006] has registered on the network");
        assertEquals(meterProtocolEvent.getEiCode(), 0);
        assertEquals(meterProtocolEvent.getProtocolCode(), 194);
    }

    @Test
    public void testMeterEventNotification_1_3_0() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(METER_EVENT_NOTIFICATION_1_3_0);
        Throwable expected = null;
        try {
            //Business code
            parser.readAndParseInboundFrame();
        } catch (DataParseException e) {
            expected = e;
        }
    }

    @Test
    public void testBeaconNotification1_3_0() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(BEACON_NOTIFICATION_1_3_0);

        //Business code
        parser.readAndParseInboundFrame();

        assertEquals(parser.getDeviceIdentifier().forIntrospection().getTypeName(), "SerialNumber");
        assertEquals(parser.getDeviceIdentifier().forIntrospection().getValue("serialNumber"), "34157300028003");
        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1458814903000L);
        assertEquals(meterProtocolEvent.getMessage(), "Tamper detected");
        assertEquals(meterProtocolEvent.getEiCode(), 23);
        assertEquals(meterProtocolEvent.getProtocolCode(), 0);
    }

    // relayed supported now
    public void testMeterEventNotification_1_4_0() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(METER_EVENT_NOTIFICATION_1_4_0);
        Throwable expected = null;
        try {
            //Business code
            parser.readAndParseInboundFrame();
        } catch (DataParseException e) {
            expected = e;
        }
        assertNotNull("Parsing a relayed meter event should have thrown an exception, since it is not supported yet", expected);
    }

    @Test
    public void testBeaconNotification1_4_0() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(BEACON_NOTIFICATION_1_4_0);

        //Business code
        parser.readAndParseInboundFrame();

        assertEquals(parser.getDeviceIdentifier().forIntrospection().getTypeName(), "SerialNumber");
        assertEquals(parser.getDeviceIdentifier().forIntrospection().getValue("serialNumber"), "Equipment-Identifier");
        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 127056000L);
        assertEquals(meterProtocolEvent.getMessage(), "Additional info");
        assertEquals(meterProtocolEvent.getEiCode(), 123);
        assertEquals(meterProtocolEvent.getProtocolCode(), 456);
    }

    @Test
    public void testPlainFrameEventSerialNumberReadout() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(BEACON_PLAIN_EVENT_SERIAL_NUMBER_READOUT);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("01054250370100162134157300029781"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1442386236000L);
        assertEquals("{\"MeterIdentifier\":\"0200:00FF:FE00:0107\",\"Result\":\"Execution of preliminary protocol failed.\"}", meterProtocolEvent.getMessage());
        assertEquals(meterProtocolEvent.getEiCode(), 0);
        assertEquals(meterProtocolEvent.getProtocolCode(), 55);
    }

    @Test
    public void testPlainFrameEventMeterRegistered() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(BEACON_PLAIN_EVENT_METER_REGISTERED);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("01054250370100162134157300026645"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1440419894000L);
        assertEquals("Node [0200:00FF:FE00:0000] [0x0001] has registered on the network", meterProtocolEvent.getMessage());
        assertEquals(0, meterProtocolEvent.getEiCode());
        assertEquals(194, meterProtocolEvent.getProtocolCode());
    }

    @Test
    public void testPlainFrame2() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(PLAIN_FRAME2);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1421162763000L);
        assertEquals(meterProtocolEvent.getMessage(), "Joining request for node [0223:7EFF:FEFD:848D]");
        assertEquals(meterProtocolEvent.getEiCode(), 0);
        assertEquals(meterProtocolEvent.getProtocolCode(), 197);
    }

    @Test
    public void testPlainFrame3() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(PLAIN_FRAME3);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("34157300028003"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1468908579000L);
        assertEquals(meterProtocolEvent.getMessage(), "Node [0023:7EFF:FEFD:7738] [0x0001] has registered on the network");
        assertEquals(meterProtocolEvent.getEiCode(), 0);
        assertEquals(meterProtocolEvent.getProtocolCode(), 194);
    }

    @Test
    public void testPlainDataNotification() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(DATA_NOTIFICATION_PLAIN_1_6_0);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("34157300028003"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1468917580000L);
        assertEquals(meterProtocolEvent.getEiCode(), 2);
        assertEquals(meterProtocolEvent.getProtocolCode(), 0);
        assertEquals(meterProtocolEvent.getMessage(), "Power up");
    }

    @Test
    public void testWrapAsServerDataNotification() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(RELAYED_DATA_NOTIFICATION_WRAP_AS_SERVER_1_6_0);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("34157300028003"), parser.getDeviceIdentifier());
    }

    @Test
    public void testWrapAsServerDataNotification_1() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(RELAYED_DATA_NOTIFICATION_WRAP_AS_SERVER_1_6_1);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("RTU-SERIAL-NUMBER"), parser.getDeviceIdentifier());
    }

    @Test
    public void testOriginHeaderAM540RelayEventNotification() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(RELAYED_EVENT_NOTIFICATION_ORIGIN_HEADER_AM540_1_6_0);
        parser.readAndParseInboundFrame();
        assertEquals(new DialHomeIdDeviceIdentifier("020000FFFE00003B").toString(), parser.getDeviceIdentifier().toString());
    }

    @Test
    public void testAM540CipheredRelayEventNotification() throws IOException, SQLException {
        String ak = "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF";
        String ek = "000102030405060708090A0B0C0D0E0F";
        Optional<DeviceProtocolSecurityPropertySet> deviceProtocolSecurityPropertySet = createDeviceProtocolSecurityPropertySet(3, ak, ek);
        doReturn(deviceProtocolSecurityPropertySet).when(context).getDeviceProtocolSecurityPropertySet(Matchers.<DeviceIdentifier>any());

        EventPushNotificationParser parser = spyParser(CIPHERED_RELAYED_EVENT_NOTIFICATION_AM540);
        parser.readAndParseInboundFrame();
        assertEquals(new DialHomeIdDeviceIdentifier("02237EFFFEFDAB5F").toString(), parser.getDeviceIdentifier().toString());
        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1488809815000L);
        assertEquals(meterProtocolEvent.getEiCode(), 0);
        assertEquals(meterProtocolEvent.getProtocolCode(), 40);
        assertEquals(meterProtocolEvent.getMessage(), "Alarm generated event: Fraud attempt");
    }

    @Test
    public void testOriginHeaderLinkyCoverRelayEventNotification() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(RELAYED_EVENT_NOTIFICATION_ORIGIN_HEADER_LINKY_COVER_1_6_0);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("34157300029560"), parser.getDeviceIdentifier());
    }

    @Test
    public void testOriginHeaderAM540RelayDataNotification() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(RELAYED_DATA_NOTIFICATION_ORIGIN_HEADER_AM540_1_6_0);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("34157300029781"), parser.getDeviceIdentifier());
    }

    @Test
    public void testBeaconEncryptedNotificationWithTopology() throws IOException, SQLException, JSONException {
        String ak = "000102030405060708090A0B0C0D0E0F";
        String ek = "00112233445566778899AABBCCDDEEFF";
        Optional<DeviceProtocolSecurityPropertySet> deviceProtocolSecurityPropertySet = createDeviceProtocolSecurityPropertySet(3, ak, ek);
        doReturn(deviceProtocolSecurityPropertySet).when(context).getDeviceProtocolSecurityPropertySet(Matchers.<DeviceIdentifier>any());

        EventPushNotificationParser parser = spyParser(BEACON_ENCRYPTED_AUTHENTICATED_NOTIFICATION);
        parser.readAndParseInboundFrame();
        //assertEquals(new DeviceIdentifierBySystemTitle("ELS6309405A30"), parser.getDeviceIdentifier());
        assertEquals(new DeviceIdentifierBySerialNumber("34157300028003"), parser.getDeviceIdentifier());
        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(1488362402000L, meterProtocolEvent.getTime().getTime());
        assertEquals("{\"MeterIdentifier\":\"0223:7EFF:FEFD:AF26\",\"SAP_802_15_4_ID\":\"0x7\",\"SAP_IPV6\":\"2002:abcd::984b:ff:fe00:7\",\"SAP_IPV4\":\"172.22.0.7\",\"SAP_DLMS_GW\":\"0x18\"}", meterProtocolEvent
                .getMessage());
        assertEquals(0, meterProtocolEvent.getEiCode());
        assertEquals(194, meterProtocolEvent.getProtocolCode());


        Beacon3100PushEventNotification beacon3100PushEventNotification = new Beacon3100PushEventNotification(mock(PropertySpecService.class), collectedDataFactory);
        CollectedTopology collectedTopology = beacon3100PushEventNotification.extractTopologyUpdateFromRegisterEvent(meterProtocolEvent);

        DeviceIdentifier needle = new DialHomeIdDeviceIdentifier("02237EFFFEFDAF26");
        boolean found = false;
        for (DeviceIdentifier device : collectedTopology.getJoinedSlaveDeviceIdentifiers().keySet()) {
            if (device.forIntrospection().getValue("callHomeId").equals(needle.forIntrospection().getValue("callHomeId"))) {
                found = true;
            }
        }
        assertTrue(found);
    }


    @Test
    public void testPlainRelayEventNotification180() throws IOException, SQLException {
        DeviceIdentifier expectedIdentifier = new DeviceIdentifierBySerialNumber("34157300020806");
        EventPushNotificationParser parser = spyParser(RELAYED_DATA_NOTIFICATION__1_8_1);
        parser.readAndParseInboundFrame();
        assertEquals(1, parser.getSourceSAP());
        assertEquals(1, parser.getDestinationSAP());

        assertEquals(expectedIdentifier.toString(), parser.getDeviceIdentifier().toString());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertNotNull(meterProtocolEvent.getTime().getTime());
        assertEquals(MeterEvent.OTHER, meterProtocolEvent.getEiCode());
        assertEquals(195, meterProtocolEvent.getProtocolCode());
        assertEquals("Node [D084:B0FF:FEF1:9B0E] was unregistered from the network", meterProtocolEvent.getMessage());
    }


    @Test
    public void testPreliminaryProtocolExecution180() throws IOException, SQLException {
        DeviceIdentifier expectedIdentifier = new DeviceIdentifierBySerialNumber("34157300020806");
        EventPushNotificationParser parser = spyParser(PRELIMINARY_PROTOCOL_EXECUTION__1_8_1);
        parser.readAndParseInboundFrame();
        assertEquals(1, parser.getSourceSAP());
        assertEquals(1, parser.getDestinationSAP());

        assertEquals(expectedIdentifier.toString(), parser.getDeviceIdentifier().toString());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertNotNull(meterProtocolEvent.getTime().getTime());
        assertEquals(MeterEvent.OTHER, meterProtocolEvent.getEiCode());
        assertEquals(54, meterProtocolEvent.getProtocolCode());
        assertEquals("{\"MeterIdentifier\":\"D084:B0FF:FEF1:9B0E\",\"Result\":\"99000010\"}", meterProtocolEvent.getMessage());
    }

    @Test
    public void testDataNotification180() throws IOException, SQLException {
        DeviceIdentifier expectedIdentifier = new DeviceIdentifierBySerialNumber("34157300020806");
        EventPushNotificationParser parser = spyParser(DATA_NOTIFICATION_1_8_0);
        parser.readAndParseInboundFrame();
        assertEquals(1, parser.getSourceSAP());
        assertEquals(1, parser.getDestinationSAP());

        assertEquals(expectedIdentifier.toString(), parser.getDeviceIdentifier().toString());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertNotNull(meterProtocolEvent.getTime().getTime());
        assertEquals(MeterEvent.OTHER, meterProtocolEvent.getEiCode());
        assertEquals(35, meterProtocolEvent.getProtocolCode());
        assertEquals("Please insert coin.", meterProtocolEvent.getMessage());
    }

    @Test
    public void testEncryptedFrames180() throws IOException, SQLException {
        String ak = "00000000000000000000000000000000";
        String ek = "00000000000000000000000000000000";
        Optional<DeviceProtocolSecurityPropertySet> deviceProtocolSecurityPropertySet = createDeviceProtocolSecurityPropertySet(2, ak, ek);
        doReturn(deviceProtocolSecurityPropertySet).when(context).getDeviceProtocolSecurityPropertySet(Matchers.<DeviceIdentifier>any());
        EventPushNotificationParser parser = spyParser(ENCRYPTED_DATA_NOTIFICATION_1_8_0);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("34157300020806"), parser.getDeviceIdentifier());
        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(1487068040000L, meterProtocolEvent.getTime().getTime());
        assertEquals("PC LOAD LETTER", meterProtocolEvent.getMessage());
        assertEquals(0, meterProtocolEvent.getEiCode());
        assertEquals(35, meterProtocolEvent.getProtocolCode());
    }

    @Test
    public void testBeaconEncryptedLostNotification() throws IOException, SQLException, JSONException {
        String ak = "000102030405060708090A0B0C0D0E0F";
        String ek = "00112233445566778899AABBCCDDEEFF";
        Optional<DeviceProtocolSecurityPropertySet> deviceProtocolSecurityPropertySet = createDeviceProtocolSecurityPropertySet(3, ak, ek);
        doReturn(deviceProtocolSecurityPropertySet).when(context).getDeviceProtocolSecurityPropertySet(Matchers.<DeviceIdentifier>any());
        EventPushNotificationParser parser = spyParser(BEACON_ENCRYPTED_AUTHENTICATED_LOST_NOTIFICATION);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("34157300028003"), parser.getDeviceIdentifier());
        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(1489576684000L, meterProtocolEvent.getTime().getTime());
        assertEquals("02237EFFFEFDAF26", meterProtocolEvent.getMessage());
        assertEquals(0, meterProtocolEvent.getEiCode());
        assertEquals(203, meterProtocolEvent.getProtocolCode());


        Beacon3100PushEventNotification beacon3100PushEventNotification = new Beacon3100PushEventNotification(mock(PropertySpecService.class), collectedDataFactory);
        CollectedTopology collectedTopology = beacon3100PushEventNotification.extractNodeInformation(meterProtocolEvent.getMessage(), Beacon3100PushEventNotification.TopologyAction.REMOVE);

        DeviceIdentifier needle = new DialHomeIdDeviceIdentifier("02237EFFFEFDAF26");
        boolean found = false;
        for (DeviceIdentifier device : collectedTopology.getLostSlaveDeviceIdentifiers()) {
            if (device.forIntrospection().getValue("callHomeId").equals(needle.forIntrospection().getValue("callHomeId"))) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testPlainRelayEventNotification() throws IOException, SQLException {
        DeviceIdentifier expectedIdentifier = new DialHomeIdDeviceIdentifier("02237EFFFEFDAF24");
        EventPushNotificationParser parser = spyParser(RELAY_EVENT_NOTIFICATION_1_6_0);
        parser.readAndParseInboundFrame();
        assertEquals(19, parser.getSourceSAP());
        assertEquals(16, parser.getDestinationSAP());

        assertEquals(expectedIdentifier.toString(), parser.getDeviceIdentifier().toString());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertNotNull(meterProtocolEvent.getTime().getTime());
        assertEquals(MeterEvent.OTHER, meterProtocolEvent.getEiCode());
        assertEquals(40, meterProtocolEvent.getProtocolCode());
        assertEquals("Alarm generated event: Fraud attempt", meterProtocolEvent.getMessage());
    }

    @Test
    public void testEncryptedFrame() throws IOException, SQLException {
        Optional<DeviceProtocolSecurityPropertySet> deviceProtocolSecurityPropertySet = createDeviceProtocolSecurityPropertySet(2, AK, EK);
        doReturn(deviceProtocolSecurityPropertySet).when(context).getDeviceProtocolSecurityPropertySet(Matchers.<DeviceIdentifier>any());
        EventPushNotificationParser parser = spyParser(ENCRYPTED_FRAME);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1421244545000L);
        assertEquals(meterProtocolEvent.getMessage(), "Joining request for node [0223:7EFF:FEFD:848D]");
        assertEquals(meterProtocolEvent.getEiCode(), 0);
        assertEquals(meterProtocolEvent.getProtocolCode(), 197);

        EventPushNotificationParser parser2 = spyParser(ENCRYPTED_FRAME2);
        parser2.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser2.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent2 = parser2.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent2.getTime().getTime(), 1427125121000L);
        assertEquals(meterProtocolEvent2.getMessage(), "Joining request for node [0223:7EFF:FEFD:848C]");
        assertEquals(meterProtocolEvent2.getEiCode(), 0);
        assertEquals(meterProtocolEvent2.getProtocolCode(), 197);

        EventPushNotificationParser parser3 = spyParser(ENCRYPTED_FRAME3);
        parser3.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser3.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent3 = parser3.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent3.getTime().getTime(), 1427125126000L);
        assertEquals(meterProtocolEvent3.getMessage(), "G3 : Node [0223:7EFF:FEFD:848C] [0x0005] has registered on the network");
        assertEquals(meterProtocolEvent3.getEiCode(), 0);
        assertEquals(meterProtocolEvent3.getProtocolCode(), 194);

        EventPushNotificationParser parser4 = spyParser(ENCRYPTED_FRAME4);
        parser4.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser4.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent4 = parser4.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent4.getTime().getTime(), 1427125053000L);
        assertEquals(meterProtocolEvent4.getMessage(), "Joining request for node [0223:7EFF:FEFD:848C]");
        assertEquals(meterProtocolEvent4.getEiCode(), 0);
        assertEquals(meterProtocolEvent4.getProtocolCode(), 197);
    }

    @Test
    public void testBeaconDataNotification_EncrypWithAuth_1_6_0() throws IOException, SQLException {
        setSecurityContext_1_6();

        EventPushNotificationParser parser = spyParser(DATA_NOTIFICATION_ENCRYPTED_WITH_AUTHENTICATION_1_6_0);

        //Business code
        parser.readAndParseInboundFrame();

        assertEquals(new DeviceIdentifierBySerialNumber("34157300028003"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1468917580000L);
        assertEquals(meterProtocolEvent.getEiCode(), 2);
        assertEquals(meterProtocolEvent.getProtocolCode(), 0);
        assertEquals(meterProtocolEvent.getMessage(), "Power up");
    }

    @Test
    public void testBeaconDataNotification_EncrypWithAuth_2_0_0() throws IOException, SQLException {
        //POWER_UP EVENT for beacon with firmware 2.0.0 and above
        setSecurityContext_1_6();

        EventPushNotificationParser parser = spyParser(DATA_NOTIFICATION_ENCRYPTED_WITH_AUTHENTICATION_2_0_0);

        //Business code
        parser.readAndParseInboundFrame();

        assertEquals(new DeviceIdentifierBySerialNumber("670-05A873-1540"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1522246308000L);
        assertEquals(meterProtocolEvent.getEiCode(), 2);
        assertEquals(meterProtocolEvent.getProtocolCode(), 0);
        assertEquals(meterProtocolEvent.getMessage(), "{\"address\":\"165.195.39.121\",\"port\":4059,\"transport\":0}");
    }

    @Test
    public void testEncryptedAndAuthenticatedFrame() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(ENCRYPTED_FRAME_WITH_AUTHENTICATION);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1421244305000L);
        assertEquals(meterProtocolEvent.getMessage(), "Joining request for node [0223:7EFF:FEFD:848D]");
        assertEquals(meterProtocolEvent.getEiCode(), 0);
        assertEquals(meterProtocolEvent.getProtocolCode(), 197);


        EventPushNotificationParser parser2 = spyParser(ENCRYPTED_FRAME_WITH_AUTHENTICATION2);
        parser2.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser2.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent2 = parser2.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent2.getTime().getTime(), 1427105996000L);
        assertEquals(meterProtocolEvent2.getMessage(), "Joining request for node [0223:7EFF:FEFD:848C]");
        assertEquals(meterProtocolEvent2.getEiCode(), 0);
        assertEquals(meterProtocolEvent2.getProtocolCode(), 197);
    }

    @Test
    public void testAuthenticatedFrameNotEncrypted() throws IOException, SQLException {
        Optional<DeviceProtocolSecurityPropertySet> deviceProtocolSecurityPropertySet = createDeviceProtocolSecurityPropertySet(1, AK, EK);
        doReturn(deviceProtocolSecurityPropertySet).when(context).getDeviceProtocolSecurityPropertySet(Matchers.<DeviceIdentifier>any());

        EventPushNotificationParser parser = spyParser(AUTHENTICATED_NOT_ENCRYPTED);
        parser.readAndParseInboundFrame();
        assertEquals(new DeviceIdentifierBySerialNumber("660-059F43-1425"), parser.getDeviceIdentifier());

        MeterProtocolEvent meterProtocolEvent = parser.getCollectedLogBook().getCollectedMeterEvents().get(0);
        assertEquals(meterProtocolEvent.getTime().getTime(), 1427291723000L);
        assertEquals(meterProtocolEvent.getMessage(), "");
        assertEquals(meterProtocolEvent.getEiCode(), 2);    //Power down
        assertEquals(meterProtocolEvent.getProtocolCode(), 0);
    }


    @Test
    public void testExceptionLogging() throws IOException, SQLException {
        EventPushNotificationParser parser = spyParser(PLAIN_FRAME);

        PushEventNotification pushEventNotification = new PushEventNotification();
        pushEventNotification.initComChannel(parser.getComChannel());
        pushEventNotification.initializeDiscoveryContext(context);
        pushEventNotification.getEventPushNotificationParser().readAndParseInboundFrame();
        pushEventNotification.collectedLogBook = pushEventNotification.getEventPushNotificationParser().getCollectedLogBook();
        assertEquals(pushEventNotification.getLoggingMessage(), "Received inbound notification from [device with serial number 660-00545D-1125].  Message: 'G3 : Node [0223:7EFF:FEFD:AAE9] [0x0006] has registered on the network', protocol code: '194'.");
    }

    private EventPushNotificationParser spyParser(byte[] frame) {
        SynchroneousComChannel comChannel = new SynchroneousComChannel(new MockedInputStream(frame), mock(OutputStream.class)) {
            @Override
            public ComChannelType getComChannelType() {
                return ComChannelType.SocketComChannel;
            }
        };
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
                System.arraycopy(frame, 0, argument, 0, Math.min(argument.length, frame.length));
                return argument.length;
            }
        }

        @Override
        public int read() throws IOException {
            return 0;
        }
    }
}
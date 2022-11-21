package com.energyict.mdc.protocol.inbound.mbus;

import com.energyict.mdc.protocol.inbound.mbus.factory.AbstractMerlinFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.MerlinCollectedDataFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.events.ErrorFlagsEventsFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.events.StatusEventsFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.mappings.CellInfoMapping;
import com.energyict.mdc.protocol.inbound.mbus.factory.mappings.ErrorFlagsMapping;
import com.energyict.mdc.protocol.inbound.mbus.factory.mappings.RegisterMapping;
import com.energyict.mdc.protocol.inbound.mbus.factory.mappings.StatusEventMapping;
import com.energyict.mdc.protocol.inbound.mbus.factory.profiles.AbstractProfileFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.profiles.DailyProfileFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.profiles.HourlyProfileFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.registers.RegisterFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.status.CellInfoFactory;
import com.energyict.mdc.protocol.inbound.mbus.mocks.MockCollectedDataFactory;
import com.energyict.mdc.protocol.inbound.mbus.mocks.MockCollectedRegisterList;
import com.energyict.mdc.protocol.inbound.mbus.parser.MerlinMbusParser;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Converter;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MerlinMbusParserTest extends TestCase {

    private byte[] genericMbus = ProtocolTools.getBytesFromHexString("6887876808087278563412B43401050010000004174D370000053E00000000040764E20000052E00000000041F59000000055300000000055B00004C43055F0000D64205630000C242056BCA9B39404568F8BD17BE37FD1700020000000000000421EA8600000C6D20092A220CFD0C110202104C6D0000C181441700000000440700000000441F000000003816", 2);

    private static final byte[] DAILY_FRAME_ENCRYPTED1 = ProtocolTools.getBytesFromHexString("AF447F2D3677B0FDD7EA7A000A0A25A26FC62003678CC6CFE83A23471E8D560ABC3A039A1024F0638D1BF1ECB42BC1DDFBEA7DDB6D876F5D492B46B07B7931DDFBEA7DDB6D876F5D492B46B07B793159A3C770AE0B23BA00E2F495497E7885E8801478E5A5AEFE4E94B8F9C901A194153EF17AE142774AD546B336B8D9ECB747D74F23D5A56D34DA66ACC54A034154979300A47BDD23DC68028E75DEECA20A96CCAE13CB7C36B71134F753410CCCA9", "");

    private static final byte[] DAILY_FRAME_ENCRYPTED2 = ProtocolTools.getBytesFromHexString("AF4407070777700000007A26B80A25ABD54956745D59640C2AAAB6238F88CDB424002277A53081ECEDC0BA7EC7CC09881A89582C9003E8AEF46B4CACFCD00C881A89582C9003E8AEF46B4CACFCD00CC25BCBF4821D14072424F29554B3BFE9E5CB45EBA2E92B148B093AEFB8689F53CE9E93475F6F3DB0BF7D3D411367F47B5806EE48FE4D6AAC503BCE136847BD5D3D699A0974F61FC049902701A266B20ED13E6335CF5B6B4B0DB70F9F839CED2F", "");

    private static final byte[] DAILY_FRAME_ENCRYPTED3_REAL_DATA = ProtocolTools.getBytesFromHexString("FF 44 42 43 44 01 23 45 44 44 7A 05 F6 0F 25 E2 10 99 F9 1B 53 E0 D1 2A 43 08 3A 6B EC 02 48 23 35 32 95 2E 96 4B C5 81 5C 8E BC 44 1D 97 C5 E3 FB DB FE 10 E7 7A D6 2B 4D 5E 5C B3 91 15 AF CC 6A DB C0 F0 7D 7F 49 1E 19 88 E0 C7 04 68 77 5C 35 EB F6 51 26 A3 32 8B BB 32 49 2A 47 62 9D 3E CD 96 A5 11 8D 44 23 E1 E5 81 C8 BB 66 49 3C D6 2B 7D E2 E4 74 B4 2E BF FF EA A2 0E 98 01 5B 2F 79 AF 47 ED 29 A9 19 D5 80 A0 71 A7 1D 9A 05 F8 DA 22 80 32 E1 7A 1F F0 32 0F 77 31 35 67 F0 5F 3A 8E F3 23 B5 B8 E3 39 70 56 1E 30 FA FD F3 9A DF CF 78 6C 14 3E DC 5B 82 65 08 B8 01 34 E1 E0 99 C8 A6 B9 F1 AD 61 59 46 9E 23 CB C4 41 2A 52 98 2C 99 A9 7E 1D 7D D6 1D 81 F9 35 80 CA 1D 59 4A 4C 3E 94 89 26 2D AE 9A 8A D3 8A 8A C0 73 C2 0E A3 2B 1D BA 97 4A 19 D1 46 94 79 41 69 AC ".replace(" ", ""), 2);

    private static final byte[] DAILY_FRAME_ENCRYPTED4_REAL_DATA = ProtocolTools.getBytesFromHexString("AF 44 42 43 44 01 23 45 44 44 7A 1D 0A 0A 25 EA D2 52 8F B4 2D 09 75 42 CD EE 09 58 28 DD BD F2 B3 64 4E 4D E4 2B 37 95 37 AE 94 27 F2 20 A4 B6 AE AF FA 75 2D C0 8B 51 63 97 31 76 1A ED 00 93 F3 23 C1 4A 76 39 DA 16 45 8E DE A4 22 DA C7 2A 77 A9 02 AF 61 9B 8C 78 D9 AE 5A 2C F1 A7 8A B2 04 FE E2 F3 9A 78 1F D2 94 14 41 1A EA 80 BA 63 5E D6 56 A6 1C 33 67 39 A0 74 50 BE 15 6B D5 85 1B 42 A4 17 CE A1 F8 B1 C1 CA B9 50 7A C9 E2 A2 35 DA 09 03 74 F6 95 AC C0 73 20 60 79 50 AA 60 86 0B 26 53 75 3C 18 BE 30 EA 05 C7 2E CA 5B".replace(" ", ""), 2);

    private static final byte[] DAILY_FRAME_DECRYPTED = ProtocolTools.getBytesFromHexString("AF4407070777700000007A26B80A252F2F860D6DE500A0201220041354BF00008D04931F33E201000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000123B0000223B000004933C1200000003FD971D041300041354BF00008D04931F15D20F00000000000000000000000000000000000002FD7401000F0100B8FB0A001700000070707070707070707070707070891000F7E8D2", "");

    private static final byte[] WEEKLY_FRAME_ENCRYPTED = ProtocolTools.getBytesFromHexString ("4F4407070777700000007AAF004025C7D833417B0AAC44244B50806AA42F882A39435576BAE5693869F149C12041D5510E3F227A0BFE00282A6264A79E608C32AF479ED7C061C338BD0C4251FC67AC", "");
    private static final byte[] WEEKLY_FRAME_DECRYPTED = ProtocolTools.getBytesFromHexString ("4F4407070777700000007AAF0040252F2F84046D0000D328041354BF00008D04931F2DF301000000000000F46800000080000080000080000080000080000080000080000080000080000080000080","");

    private static final byte[] WEEKLY_FRAME_ENCRYPTED_2_REAL = ProtocolTools.getBytesFromHexString("4F 44 42 43 44 01 23 45 22 22 7A AF 00 40 25 F0 7D 63 02 F2 2C 32 6C 4A E0 C3 20 E2 2B AA A3 D1 8C C9 6B 16 D8 83 C2 5F F9 F8 41 90 97 F6 B9 85 C8 A4 DE 54 26 A8 36 23 88 17 C3 B2 95 BC 0F 51 C0 AF 93 B6 06 91 FE CA 1C F5 21 50 AC 9D 08".replace(" ", ""), 2);


    private static final byte[] NRT_FRAME_ENCRYPTED = ProtocolTools.getBytesFromHexString("2F4407070777700000007A50972025445F85D4621E087B3B3E5D3B4BD1EF6B43A25EA1C90387A941B1DBF816CF9853","");
    private static final byte[] NRT_FRAME_DECRYPTED = ProtocolTools.getBytesFromHexString("2F4407070777700000007A509720252F2F84046D130BD428123B0000223B00000493C31200000003FD971D041300F0","");

    private static String key1 = "4F A7 0B 24 46 5F 81 4A 66 76 31 77 3A 39 76 44"; //ProtocolTools.getBytesFromHexString("4FA70B24465F814A667631773A397644", "");

    private static String key2 = "01 01 01 01 01 01 01 01 01 01 01 01 01 01 01 01"; //ProtocolTools.getBytesFromHexString("4FA70B24465F814A667631773A397644", "");

    private static byte[] iv = ProtocolTools.getBytesFromHexString("00000000000000000000000000000000", "");

    @Mock
    private InboundDiscoveryContext inboundDiscoveryContext;

    private final CollectedDataFactory collectedDataFactory = new MockCollectedDataFactory();

    /* TODO: add unencrypted
    @Test
    public void testGenericMbus() throws IOException, SQLException {
        MerlinMbusParser parser = new MerlinMbusParser(new InboundContext(new MerlinLogger(Logger.getAnonymousLogger())));

        parser.parse(genericMbus);
    }
    */

    @Before
    public void setUp(){
        when(inboundDiscoveryContext.getLogger()).thenReturn(Logger.getAnonymousLogger());
        when(inboundDiscoveryContext.getCollectedDataFactory()).thenReturn(collectedDataFactory);
    }

    @Test
    public void testDailyFrameEncrypted1() throws IOException, SQLException {
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(DAILY_FRAME_ENCRYPTED1);
        inboundContext.setEncryptionKey(key1);
        parser.parse();


        assertEquals("FDB07736", parser.getTelegram().getSerialNr());
        assertEquals("7F2D", Converter.convertListToString(parser.getTelegram().getHeader().getmField().getFieldParts()));
        assertEquals("44" , parser.getTelegram().getHeader().getcField().getFieldParts().get(0));
        assertEquals("00" , parser.getTelegram().getBody().getBodyHeader().getAccessNumber());

        // date
        assertEquals("2021-10-22 09:57:56" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(2).getDataField().getParsedValue());

        // snapshot value
        assertEquals("0" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getDataField().getParsedValue());
        assertEquals("m3", parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getMultiplier());

        // snapshot value
        assertEquals("0" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getDataField().getParsedValue());
        assertEquals("m3/h", parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getVif().getMultiplier());

        // battery data
        assertEquals("1" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(11).getDataField().getParsedValue());
        assertEquals("none", parser.getTelegram().getBody().getBodyPayload().getRecords().get(11).getVif().getmUnit().getValue());
        assertEquals(0, parser.getTelegram().getBody().getBodyPayload().getRecords().get(11).getVif().getMultiplier());

        // profile
        assertEquals(23, parser.getTelegram().getBody().getBodyPayload().getRecords().get(4).getDataField().getParsedIntervals().size());
        for (int i=0; i<23; i++) {
            assertEquals(-32768, parser.getTelegram().getBody().getBodyPayload().getRecords().get(4).getDataField().getParsedIntervals().get(i).intValue());
        }

        // nightline
        assertEquals(8, parser.getTelegram().getBody().getBodyPayload().getRecords().get(10).getDataField().getParsedIntervals().size());
        for (int i=0; i<8; i++) {
            assertEquals(-32768, parser.getTelegram().getBody().getBodyPayload().getRecords().get(10).getDataField().getParsedIntervals().get(i).intValue());
        }
    }


    @Test
    public void testDailyFrameEncrypted2() throws IOException, SQLException {
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(DAILY_FRAME_ENCRYPTED2);
        inboundContext.setEncryptionKey(key1);
        parser.parse();

        assertEquals("00707707", parser.getTelegram().getSerialNr());
        assertEquals("44" , parser.getTelegram().getHeader().getcField().getFieldParts().get(0));
        assertEquals("26" , parser.getTelegram().getBody().getBodyHeader().getAccessNumber());

        // date
        assertEquals("2022-08-22 00:00:00" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(2).getDataField().getParsedValue());

        // snapshot value
        assertEquals("48980" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getDataField().getParsedValue());
        assertEquals("m3", parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getMultiplier());

        // profile data - todo

        // max flow data
        assertEquals("0" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getDataField().getParsedValue());
        assertEquals("m3/h", parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getVif().getMultiplier());

        // min flow data
        assertEquals("0" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(6).getDataField().getParsedValue());
        assertEquals("m3/h", parser.getTelegram().getBody().getBodyPayload().getRecords().get(6).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(6).getVif().getMultiplier());


    }

    private InboundDiscoveryContext getContext() {
        return inboundDiscoveryContext;
    }


    @Test
    public void testWeeklyFrameEncrypted() throws IOException, SQLException {
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(WEEKLY_FRAME_ENCRYPTED);
        inboundContext.setEncryptionKey(key1);
        parser.parse();

        assertEquals("00707707", parser.getTelegram().getSerialNr());
        assertEquals("44" , parser.getTelegram().getHeader().getcField().getFieldParts().get(0));
        assertEquals("AF" , parser.getTelegram().getBody().getBodyHeader().getAccessNumber());
        // date
        assertEquals("2022-08-19 00:00:00" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(2).getDataField().getParsedValue());

        // snapshot value
        assertEquals("48980" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getDataField().getParsedValue());
        assertEquals("m3", parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getMultiplier());

    }


    @Test
    public void testNTRFrameEncrypted() throws IOException, SQLException {
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(NRT_FRAME_ENCRYPTED);
        inboundContext.setEncryptionKey(key1);
        parser.parse();

        assertEquals("00707707", parser.getTelegram().getSerialNr());
        assertEquals("44" , parser.getTelegram().getHeader().getcField().getFieldParts().get(0));
        assertEquals("50" , parser.getTelegram().getBody().getBodyHeader().getAccessNumber());

        // date
        assertEquals("2022-08-20 11:19:00" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(2).getDataField().getParsedValue());

        // max flow data
        assertEquals("0" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getDataField().getParsedValue());
        assertEquals("m3/h", parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getMultiplier());

        // min flow data
        assertEquals("0" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(4).getDataField().getParsedValue());
        assertEquals("m3/h", parser.getTelegram().getBody().getBodyPayload().getRecords().get(4).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(4).getVif().getMultiplier());

        // back flow
        assertEquals("18" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getDataField().getParsedValue());
        assertEquals("m3", parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getVif().getMultiplier());

        // error flags
        assertEquals("[04, 13, 00]" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(6).getDataField().getFieldParts().toString());

    }

    @Test
    public void testRegisterParserDaily(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(DAILY_FRAME_ENCRYPTED2);
        inboundContext.setEncryptionKey(key1);
        parser.parse();

        CollectedRegisterList registers = new MockCollectedRegisterList();

        RegisterFactory registerFactory = new RegisterFactory(parser.getTelegram(), inboundContext, registers);
        registerFactory.extractRegisters();

        assertEquals(5, registers.getCollectedRegisters().size());

        assertEquals(48980,
                        registers.getCollectedRegisters().stream()
                            .filter(r -> RegisterMapping.INSTANTANEOUS_VOLUME.getObisCode().equals(r.getRegisterIdentifier().getRegisterObisCode()))
                            .findFirst()
                            .get()
                                .getCollectedQuantity()
                                .intValue());

    }

    @Test
    public void testRegisterParserWeekly(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(WEEKLY_FRAME_ENCRYPTED);
        inboundContext.setEncryptionKey(key1);
        parser.parse();

        CollectedRegisterList registers = new MockCollectedRegisterList();
        RegisterFactory registerFactory = new RegisterFactory(parser.getTelegram(), inboundContext, registers);
        registerFactory.extractRegisters();

        assertEquals(1, registers.getCollectedRegisters().size());

        assertEquals(48980, registers.getCollectedRegisters().get(0).getCollectedQuantity().intValue());
    }


    @Test
    public void testRegisterParserNTR() {
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(NRT_FRAME_ENCRYPTED);
        inboundContext.setEncryptionKey(key1);
        parser.parse();

        CollectedRegisterList registers = new MockCollectedRegisterList();
        RegisterFactory registerFactory = new RegisterFactory(parser.getTelegram(), inboundContext, registers);
        registerFactory.extractRegisters();

        List<CollectedRegister> collectedRegisters = registers.getCollectedRegisters();

        assertEquals(3, collectedRegisters.size());

        assertEquals(18, collectedRegisters.stream()
                .filter(r -> RegisterMapping.BACK_FLOW_WEEKLY.getObisCode().equals(r.getRegisterIdentifier().getRegisterObisCode()))
                .findFirst()
                .get()
                .getCollectedQuantity().intValue());
    }

    @Test
    public void testLoadProfileParser(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        inboundContext.setTimeZone(ZoneId.of("Europe/Athens"));
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(DAILY_FRAME_ENCRYPTED2);
        inboundContext.setEncryptionKey(key1);
        parser.parse();

        AbstractProfileFactory factory = new HourlyProfileFactory(parser.getTelegram(), inboundContext);

        for (int i=0; i<12; i++){
            if (i != 4) {
                assertFalse(factory.appliesFor(parser.getTelegram().getBody().getBodyPayload().getRecords().get(i)));
            } else {
                assertTrue(factory.appliesFor(parser.getTelegram().getBody().getBodyPayload().getRecords().get(4)));
            }
        }

        TelegramVariableDataRecord indexRecord = parser.getTelegram().getBody().getBodyPayload().getRecords().get(3);
        TelegramVariableDataRecord hourlyRecord = parser.getTelegram().getBody().getBodyPayload().getRecords().get(4);

        factory.extractProfileMetaData(hourlyRecord);

        assertTrue(factory.appliesAsIndexRecord(indexRecord));

        factory.extractLoadProfile(hourlyRecord, indexRecord);

        long startIndex = factory.getStartIndex();

        assertEquals(48980, startIndex);

        CollectedLoadProfile lp = (CollectedLoadProfile) factory.getCollectedLoadProfile();

        assertEquals(24, lp.getCollectedIntervalData().size());
    }

    @Test
    public void testMidnight(){
        Instant random = Instant.ofEpochSecond(1663854120);

        Instant midnight = AbstractMerlinFactory.toMidnightWithTimeZone(random, ZoneId.of("UTC"));
        assertEquals("2022-09-22T00:00:00Z", midnight.toString());

        Instant midnightEET = AbstractMerlinFactory.toMidnightWithTimeZone(random, ZoneId.of("Europe/Athens"));
        assertEquals("2022-09-21T21:00:00Z", midnightEET.toString());

        Instant midnightGMT = AbstractMerlinFactory.toMidnightWithTimeZone(random, ZoneId.of("Europe/Madrid"));
        assertEquals("2022-09-21T22:00:00Z", midnightGMT.toString());

        Instant midnightCET = AbstractMerlinFactory.toMidnightWithTimeZone(random, ZoneId.of("Europe/Brussels"));
        assertEquals("2022-09-21T22:00:00Z", midnightCET.toString());

    }

    private String pad(String s, int length) {
        while (s.length() < length) s = s + ' ';
        return s;
    }

    @Test
    public void dumpPackets(){
        dump(DAILY_FRAME_ENCRYPTED1, "DAILY FRAME #1", key1);
        dump(DAILY_FRAME_ENCRYPTED2, "DAILY FRAME #2", key1);
        dump(DAILY_FRAME_ENCRYPTED3_REAL_DATA, "DAILY FRAME #3", key2);
        dump(DAILY_FRAME_ENCRYPTED4_REAL_DATA, "DAILY FRAME #4", key2);

        dump(WEEKLY_FRAME_ENCRYPTED, "WEEKLY FRAME", key1);
        dump(NRT_FRAME_ENCRYPTED, "NRT FRAME", key1);

    }

    private void dump(byte[] frame, String name, String key) {
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(frame);
        inboundContext.setEncryptionKey(key);
        parser.parse();

        System.out.println(parser.getTelegram().debugOutput());

        System.out.println("***** " + name + " *****");

        String SEP = " | ";
        System.out.println(" # " + SEP + "DIF" + SEP + "DIF-FunctionType              " + SEP + "DIF-Encoding                     " + SEP + "VIF" + SEP + " VIF-Type           " + SEP + "VIF-Unit  " + SEP + "SCB" + SEP + "Sp." + SEP +" VIFEx ");
        //System.out.println("#\tDIF\t DIF-FunctionType                         \tDIF-Encoding                               \tVIF\t VIF-Type           \tVIF-Unit\tSCB\tSpacing");


        for (int i=0; i < parser.getTelegram().getBody().getBodyPayload().getRecords().size(); i++) {
            TelegramVariableDataRecord r = parser.getTelegram().getBody().getBodyPayload().getRecords().get(i);

            System.out.print(pad("" + i,3));
            System.out.print(SEP);
            System.out.print(pad(r.getDif().getFieldPartsAsString(), 3));
            System.out.print(SEP);
            System.out.print(pad(r.getDif().getFunctionType().toString(),30));
            System.out.print(SEP);
            System.out.print(pad(r.getDif().getDataFieldEncoding().toString(), 33));
            System.out.print(SEP);
            if (r.getVif() != null ) {
                System.out.print(pad(r.getVif().getFieldPartsAsString(), 3)); // VIF
                System.out.print(SEP);
                if (r.getVif().getType() != null) {
                    System.out.print(pad(r.getVif().getType().toString(), 20));
                    System.out.print(SEP);
                    System.out.print(pad(r.getVif().getmUnit().toString(), 10));
                    System.out.print(SEP);
                    if (r.getDataField().getFieldParts().size() == 3) {
                        System.out.print(r.getDataField().getFieldParts().get(1).toString()); // SCB
                        System.out.print(SEP);
                        System.out.print(r.getDataField().getFieldParts().get(2).toString()); // SPACING
                    } else {
                        System.out.print("--");
                        System.out.print(SEP);
                        System.out.print("--");
                    }

                    System.out.print(SEP);
                    for (int v = 0; v < r.getVifes().size(); v++){
                        System.out.print(r.getVifes().get(v).getFieldPartsAsString());
                        System.out.print(SEP);
                    }

                } else {
                    System.out.print(pad("-", 3));
                }
            } else {
                System.out.print(pad("-", 3));
            }

            System.out.println();
        }

        System.out.println("\n\n");
    }


    @Test
    public void testLoadProfileParserDailyLP(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        inboundContext.setTimeZone(ZoneId.of("Europe/Athens"));
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(WEEKLY_FRAME_ENCRYPTED);
        inboundContext.setEncryptionKey(key1);
        parser.parse();

        AbstractProfileFactory factory = new DailyProfileFactory(parser.getTelegram(), inboundContext);

        for (int i=0; i<5; i++){
            if (i != 4) {
                assertFalse(factory.appliesFor(parser.getTelegram().getBody().getBodyPayload().getRecords().get(i)));
            } else {
                assertTrue(factory.appliesFor(parser.getTelegram().getBody().getBodyPayload().getRecords().get(4)));
            }
        }

        TelegramVariableDataRecord indexRecord = parser.getTelegram().getBody().getBodyPayload().getRecords().get(3);
        TelegramVariableDataRecord hourlyRecord = parser.getTelegram().getBody().getBodyPayload().getRecords().get(4);

        factory.extractProfileMetaData(hourlyRecord);

        assertTrue(factory.appliesAsIndexRecord(indexRecord));

        factory.extractLoadProfile(hourlyRecord, indexRecord);

        long startIndex = factory.getStartIndex();

        assertEquals(48980, startIndex);

        CollectedLoadProfile lp = (CollectedLoadProfile) factory.getCollectedLoadProfile();

        assertEquals(14, lp.getCollectedIntervalData().size());
    }

    @Test
    public void testStatusEvents(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        inboundContext.setTimeZone(ZoneId.of("Europe/Athens"));
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(DAILY_FRAME_ENCRYPTED1);
        inboundContext.setEncryptionKey(key1);
        parser.parse();

        StatusEventsFactory eventFactory = new StatusEventsFactory(parser.getTelegram(), inboundContext);

        CollectedLogBook events = eventFactory.extractEventsFromStatus();

        assertEquals(7, events.getCollectedMeterEvents().size());
    }


    @Test
    public void testEventFlagsDaily1() {
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        inboundContext.setTimeZone(ZoneId.of("Europe/Athens"));
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(DAILY_FRAME_ENCRYPTED1);
        inboundContext.setEncryptionKey(key1);
        parser.parse();

        ErrorFlagsEventsFactory factory = new ErrorFlagsEventsFactory(parser.getTelegram(), inboundContext);

        TelegramVariableDataRecord eventRecord = parser.getTelegram().getBody().getBodyPayload().getRecords().get(8);
        assertTrue(factory.appliesFor(eventRecord));

        CollectedLogBook events = factory.extractEventsFromErrorFlags(eventRecord);

        assertEquals(2, events.getCollectedMeterEvents().size());
        assertEquals("Clock sync", events.getCollectedMeterEvents().get(0).getMessage());
        assertEquals("Battery usage indicator above or critical", events.getCollectedMeterEvents().get(1).getMessage());

        assertEquals("Fri Oct 22 12:57:56 EEST 2021", events.getCollectedMeterEvents().get(0).getTime().toString());
    }

    @Test
    public void testEventFlagsDaily2() {
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        inboundContext.setTimeZone(ZoneId.of("Europe/Athens"));
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(DAILY_FRAME_ENCRYPTED2);
        inboundContext.setEncryptionKey(key1);
        parser.parse();

        ErrorFlagsEventsFactory factory = new ErrorFlagsEventsFactory(parser.getTelegram(), inboundContext);

        TelegramVariableDataRecord eventRecord = parser.getTelegram().getBody().getBodyPayload().getRecords().get(8);
        assertTrue(factory.appliesFor(eventRecord));

        CollectedLogBook events = factory.extractEventsFromErrorFlags(eventRecord);

        assertEquals(4, events.getCollectedMeterEvents().size());
        assertEquals("Stuck meter (no consumption)", events.getCollectedMeterEvents().get(0).getMessage());
        assertEquals("Actual Removal", events.getCollectedMeterEvents().get(1).getMessage());
        assertEquals("High temp", events.getCollectedMeterEvents().get(2).getMessage());
        assertEquals("Battery usage indicator above or critical", events.getCollectedMeterEvents().get(3).getMessage());

        assertEquals("Mon Aug 22 03:00:00 EEST 2022", events.getCollectedMeterEvents().get(0).getTime().toString());
    }


    @Test
    public void testEventFlagsNTR() {
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        inboundContext.setTimeZone(ZoneId.of("Europe/Athens"));
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(NRT_FRAME_ENCRYPTED);
        inboundContext.setEncryptionKey(key1);
        parser.parse();

        ErrorFlagsEventsFactory factory = new ErrorFlagsEventsFactory(parser.getTelegram(), inboundContext);

        TelegramVariableDataRecord eventRecord = parser.getTelegram().getBody().getBodyPayload().getRecords().get(6);
        assertTrue(factory.appliesFor(eventRecord));

        CollectedLogBook events = factory.extractEventsFromErrorFlags(eventRecord);

        assertEquals(4, events.getCollectedMeterEvents().size());
        assertEquals("Stuck meter (no consumption)", events.getCollectedMeterEvents().get(0).getMessage());
        assertEquals("Actual Removal", events.getCollectedMeterEvents().get(1).getMessage());
        assertEquals("High temp", events.getCollectedMeterEvents().get(2).getMessage());
        assertEquals("Battery usage indicator above or critical", events.getCollectedMeterEvents().get(3).getMessage());

        assertEquals("Sat Aug 20 14:19:00 EEST 2022", events.getCollectedMeterEvents().get(0).getTime().toString());
    }



    @Test
    public void testDailyFrame3(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        inboundContext.setTimeZone(ZoneId.of("Europe/Athens"));
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(DAILY_FRAME_ENCRYPTED3_REAL_DATA);
        inboundContext.setEncryptionKey(key2);
        parser.parse();

        dump(DAILY_FRAME_ENCRYPTED3_REAL_DATA, "DAILY FRAME #3 - REAL", key2);

        AbstractProfileFactory factory = new HourlyProfileFactory(parser.getTelegram(), inboundContext);

        for (int i=0; i<6; i++){
            if (i != 4) {
                assertFalse(factory.appliesFor(parser.getTelegram().getBody().getBodyPayload().getRecords().get(i)));
            } else {
                assertTrue(factory.appliesFor(parser.getTelegram().getBody().getBodyPayload().getRecords().get(4)));
            }
        }

        TelegramVariableDataRecord indexRecord = parser.getTelegram().getBody().getBodyPayload().getRecords().get(3);
        TelegramVariableDataRecord hourlyRecord = parser.getTelegram().getBody().getBodyPayload().getRecords().get(4);

        factory.extractProfileMetaData(hourlyRecord);

        assertTrue(factory.appliesAsIndexRecord(indexRecord));

        factory.extractLoadProfile(hourlyRecord, indexRecord);

        long startIndex = factory.getStartIndex();

        assertEquals(42088, startIndex);

        CollectedLoadProfile lp = (CollectedLoadProfile) factory.getCollectedLoadProfile();

        assertEquals(96, lp.getCollectedIntervalData().size());
        assertEquals(42088, lp.getCollectedIntervalData().get(0).getIntervalValues().get(0).getNumber().intValue());
        assertEquals(42088 - 276, lp.getCollectedIntervalData().get(1).getIntervalValues().get(0).getNumber().intValue());
        assertEquals(42088 - 276 - 281 , lp.getCollectedIntervalData().get(2).getIntervalValues().get(0).getNumber().intValue());

        // status events
        StatusEventsFactory eventFactory = new StatusEventsFactory(parser.getTelegram(), inboundContext);
        CollectedLogBook events = eventFactory.extractEventsFromStatus();
        assertEquals(7, events.getCollectedMeterEvents().size());

        //FIX me: why we have this discrepancy, to check with R&D
        //assertEquals("Abnormal condition", events.getCollectedMeterEvents().get(0).getMessage());
        assertEquals(StatusEventMapping.POWER_LOW.getMessage(), events.getCollectedMeterEvents().get(1).getMessage());
        assertEquals(StatusEventMapping.PERMANENT_ERROR_NO.getMessage(), events.getCollectedMeterEvents().get(2).getMessage());
        assertEquals(StatusEventMapping.TEMPORARY_ERROR.getMessage(), events.getCollectedMeterEvents().get(3).getMessage());
    }



    @Test
    public void testWeeklyFrame2Real(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        inboundContext.setTimeZone(ZoneId.of("Europe/Athens"));
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(WEEKLY_FRAME_ENCRYPTED_2_REAL);
        inboundContext.setEncryptionKey(key2);
        parser.parse();

        dump(WEEKLY_FRAME_ENCRYPTED_2_REAL, "WEEKLY FRAME #2 - REAL", key2);

        AbstractProfileFactory factory = new DailyProfileFactory(parser.getTelegram(), inboundContext);

        for (int i=0; i<5; i++){
            if (i != 4) {
                assertFalse(factory.appliesFor(parser.getTelegram().getBody().getBodyPayload().getRecords().get(i)));
            } else {
                assertTrue(factory.appliesFor(parser.getTelegram().getBody().getBodyPayload().getRecords().get(4)));
            }
        }

        TelegramVariableDataRecord indexRecord = parser.getTelegram().getBody().getBodyPayload().getRecords().get(3);
        TelegramVariableDataRecord hourlyRecord = parser.getTelegram().getBody().getBodyPayload().getRecords().get(4);

        factory.extractProfileMetaData(hourlyRecord);

        assertTrue(factory.appliesAsIndexRecord(indexRecord));

        factory.extractLoadProfile(hourlyRecord, indexRecord);

        long startIndex = factory.getStartIndex();

        assertEquals(26953, startIndex);

        CollectedLoadProfile lp = (CollectedLoadProfile) factory.getCollectedLoadProfile();

        assertEquals(14, lp.getCollectedIntervalData().size());

        assertEquals(26953, lp.getCollectedIntervalData().get(0).getIntervalValues().get(0).getNumber().intValue());
        assertEquals(26953, lp.getCollectedIntervalData().get(1).getIntervalValues().get(0).getNumber().intValue());
        assertEquals(26953 - 3133, lp.getCollectedIntervalData().get(2).getIntervalValues().get(0).getNumber().intValue());
        assertEquals(26953 - 3133 - 3132, lp.getCollectedIntervalData().get(3).getIntervalValues().get(0).getNumber().intValue());
    }


    @Test
    public void testDailyFrame4(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        inboundContext.setTimeZone(ZoneId.of("Europe/Athens"));
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(DAILY_FRAME_ENCRYPTED4_REAL_DATA);
        inboundContext.setEncryptionKey(key2);
        parser.parse();

        dump(DAILY_FRAME_ENCRYPTED4_REAL_DATA, "DAILY FRAME #4 - REAL", key2);

        AbstractProfileFactory factory = new HourlyProfileFactory(parser.getTelegram(), inboundContext);

        for (int i=0; i<6; i++){
            if (i != 4) {
                assertFalse(factory.appliesFor(parser.getTelegram().getBody().getBodyPayload().getRecords().get(i)));
            } else {
                assertTrue(factory.appliesFor(parser.getTelegram().getBody().getBodyPayload().getRecords().get(4)));
            }
        }

        TelegramVariableDataRecord indexRecord = parser.getTelegram().getBody().getBodyPayload().getRecords().get(3);
        TelegramVariableDataRecord hourlyRecord = parser.getTelegram().getBody().getBodyPayload().getRecords().get(4);

        factory.extractProfileMetaData(hourlyRecord);

        assertTrue(factory.appliesAsIndexRecord(indexRecord));

        factory.extractLoadProfile(hourlyRecord, indexRecord);

        long startIndex = factory.getStartIndex();

        assertEquals(649952, startIndex);

        CollectedLoadProfile lp = (CollectedLoadProfile) factory.getCollectedLoadProfile();

        assertEquals(24, lp.getCollectedIntervalData().size());
        assertEquals(649952, lp.getCollectedIntervalData().get(0).getIntervalValues().get(0).getNumber().intValue());
        assertEquals(649952, lp.getCollectedIntervalData().get(1).getIntervalValues().get(0).getNumber().intValue());
        assertEquals(649952 , lp.getCollectedIntervalData().get(2).getIntervalValues().get(0).getNumber().intValue());

        // status events
        /*
        StatusEventsFactory eventFactory = new StatusEventsFactory(parser.getTelegram(), inboundContext);


        CollectedLogBook events = eventFactory.extractEventsFromStatus();

        assertEquals(7, events.getCollectedMeterEvents().size());

        //FIXME: why different?
        //assertEquals("Abnormal condition", events.getCollectedMeterEvents().get(0).getMessage());
        assertEquals("Power OK", events.getCollectedMeterEvents().get(1).getMessage());
        assertEquals("Permanent error", events.getCollectedMeterEvents().get(2).getMessage());
        */

        // error flags
        TelegramVariableDataRecord eventRecord = parser.getTelegram().getBody().getBodyPayload().getRecords().get(8);
        ErrorFlagsEventsFactory factoryErrorFlags = new ErrorFlagsEventsFactory(parser.getTelegram(), inboundContext);

        CollectedLogBook errorFlags = factoryErrorFlags.extractEventsFromErrorFlags(eventRecord);
        assertEquals(2, errorFlags.getCollectedMeterEvents().size());
        assertEquals(ErrorFlagsMapping.STUCK_METER.getMessage(), errorFlags.getCollectedMeterEvents().get(0).getMessage());
        assertEquals(ErrorFlagsMapping.BATTERY_USAGE_INDICATOR.getMessage(), errorFlags.getCollectedMeterEvents().get(1).getMessage());

    }


    @Test
    public void testDailyFrame3Factory(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        inboundContext.setTimeZone(ZoneId.of("Europe/Athens"));
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(DAILY_FRAME_ENCRYPTED3_REAL_DATA);
        inboundContext.setEncryptionKey(key2);
        parser.parse();

        MerlinCollectedDataFactory factory = new MerlinCollectedDataFactory(parser.getTelegram(),inboundContext);

        assertEquals(3, factory.getCollectedData().size());
    }

    @Test
    public void testDailyFrame1Factory(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        inboundContext.setTimeZone(ZoneId.of("Europe/Athens"));
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(DAILY_FRAME_ENCRYPTED1);
        inboundContext.setEncryptionKey(key1);
        parser.parse();

        MerlinCollectedDataFactory factory = new MerlinCollectedDataFactory(parser.getTelegram(),inboundContext);

        List<CollectedData> collectedData = factory.getCollectedData();

        assertEquals(4, collectedData.size());

        CollectedRegisterList collectedRegisterList = (CollectedRegisterList) factory.getCollectedData().stream()
                .filter(c -> c instanceof CollectedRegisterList)
                .findFirst()
                .get();

        assertEquals(14, collectedRegisterList.getCollectedRegisters().size());
    }


    @Test
    public void testCellIdDailyFrame1(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        inboundContext.setTimeZone(ZoneId.of("Europe/Athens"));
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(DAILY_FRAME_ENCRYPTED1);
        inboundContext.setEncryptionKey(key1);
        parser.parse();

        byte[] data = { (byte)0x00, (byte)0xA9, (byte)0x0A, (byte)0x00, (byte)0x00, (byte)0x00,
                        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xAC, (byte)0xB6, (byte)0x84,
                        (byte)0x9B, (byte)0x69, (byte)0x17, (byte)0xB4, (byte)0x5E, (byte)0x99,
                        (byte)0x63, (byte)0x91, (byte)0x33, (byte)0x63, (byte)0xD9 };

        final String PAIRED_METER_ID = "D963339163995EB417699B84B6AC";

        int cellId = (Integer) CellInfoMapping.CELL_ID.extractValue(data);
        assertEquals(43264 /*0xA900*/, cellId);

        int signalStrength = (Integer) CellInfoMapping.SIGNAL_STRENGTH.extractValue(data);
        assertEquals(10, signalStrength);

        int signalQuality = CellInfoFactory.extractSignalQuality(data);
        assertEquals(0, signalQuality);

        int transmissionPower = CellInfoFactory.extractTransmissionPower(data);
        assertEquals(0, transmissionPower);

        int extendedCodeCoverage = CellInfoFactory.extractExtendedCodeCoverage(data);
        assertEquals(0, extendedCodeCoverage);

        int accumulatedTxTime = CellInfoFactory.extractAccumulatedTxTime(data);
        assertEquals(0, accumulatedTxTime);

        int accumulatedRxTime = CellInfoFactory.extractAccumulatedRxTime(data);
        assertEquals(0, accumulatedRxTime);

        int releaseAssistEnable = CellInfoFactory.extractReleaseAssistEnable(data);
        assertEquals(0, releaseAssistEnable);

        byte[] meterId = CellInfoFactory.extractPairedMeterId(data);
        byte[] expectedMeterId = ProtocolTools.getBytesFromHexString(PAIRED_METER_ID, 2);
        assertArrayEquals( expectedMeterId, meterId);

        byte[] meterIdMapped = (byte[]) CellInfoMapping.PAIRED_METER_ID.extractValue(data);
        assertArrayEquals( expectedMeterId, meterIdMapped);

        MerlinCollectedDataFactory factory = new MerlinCollectedDataFactory(parser.getTelegram(),inboundContext);

        List<CollectedData> collectedData = factory.getCollectedData();

        CollectedRegisterList collectedRegisters = (CollectedRegisterList) collectedData.stream().filter(c -> c instanceof CollectedRegisterList).findFirst().get();

        assertEquals(43264, collectedRegisters.getCollectedRegisters().stream()
                .filter(r -> CellInfoMapping.CELL_ID.getObisCode().equals(r.getRegisterIdentifier().getRegisterObisCode()))
                .findFirst()
                .get()
                .getCollectedQuantity().intValue());


        assertEquals(PAIRED_METER_ID, collectedRegisters.getCollectedRegisters().stream()
                .filter(r -> CellInfoMapping.PAIRED_METER_ID.getObisCode().equals(r.getRegisterIdentifier().getRegisterObisCode()))
                .findFirst()
                .get()
                .getText());
    }


    @Test
    public void testCellIdDailyFrame3(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        inboundContext.setTimeZone(ZoneId.of("Europe/Athens"));
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parseHeader(DAILY_FRAME_ENCRYPTED3_REAL_DATA);
        inboundContext.setEncryptionKey(key2);
        parser.parse();


        MerlinCollectedDataFactory factory = new MerlinCollectedDataFactory(parser.getTelegram(),inboundContext);

        List<CollectedData> collectedData = factory.getCollectedData();

        CollectedRegisterList collectedRegisters = (CollectedRegisterList) collectedData.stream().filter(c -> c instanceof CollectedRegisterList).findFirst().get();

        assertEquals(38144, collectedRegisters.getCollectedRegisters().stream()
                .filter(r -> CellInfoMapping.CELL_ID.getObisCode().equals(r.getRegisterIdentifier().getRegisterObisCode()))
                .findFirst()
                .get()
                .getCollectedQuantity().intValue());

        assertEquals(4, collectedRegisters.getCollectedRegisters().stream()
                .filter(r -> CellInfoMapping.SIGNAL_STRENGTH.getObisCode().equals(r.getRegisterIdentifier().getRegisterObisCode()))
                .findFirst()
                .get()
                .getCollectedQuantity().intValue());

        assertEquals(4, collectedRegisters.getCollectedRegisters().stream()
                .filter(r -> CellInfoMapping.SIGNAL_QUALITY.getObisCode().equals(r.getRegisterIdentifier().getRegisterObisCode()))
                .findFirst()
                .get()
                .getCollectedQuantity().intValue());

        assertEquals("F80AE9C46898C141424344000102", collectedRegisters.getCollectedRegisters().stream()
                .filter(r -> CellInfoMapping.PAIRED_METER_ID.getObisCode().equals(r.getRegisterIdentifier().getRegisterObisCode()))
                .findFirst()
                .get()
                .getText());
    }


}
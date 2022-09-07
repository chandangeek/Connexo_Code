package com.energyict.mdc.protocol.inbound.mbus;

import com.energyict.mdc.protocol.inbound.mbus.parser.MerlinMbusParser;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Converter;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

@RunWith(MockitoJUnitRunner.class)
public class MerlinMbusParserTest extends TestCase {

    private byte[] genericMbus = ProtocolTools.getBytesFromHexString("6887876808087278563412B43401050010000004174D370000053E00000000040764E20000052E00000000041F59000000055300000000055B00004C43055F0000D64205630000C242056BCA9B39404568F8BD17BE37FD1700020000000000000421EA8600000C6D20092A220CFD0C110202104C6D0000C181441700000000440700000000441F000000003816", 2);

    private static final byte[] DAILY_FRAME_ENCRYPTED1 = ProtocolTools.getBytesFromHexString("AF447F2D3677B0FDD7EA7A000A0A25A26FC62003678CC6CFE83A23471E8D560ABC3A039A1024F0638D1BF1ECB42BC1DDFBEA7DDB6D876F5D492B46B07B7931DDFBEA7DDB6D876F5D492B46B07B793159A3C770AE0B23BA00E2F495497E7885E8801478E5A5AEFE4E94B8F9C901A194153EF17AE142774AD546B336B8D9ECB747D74F23D5A56D34DA66ACC54A034154979300A47BDD23DC68028E75DEECA20A96CCAE13CB7C36B71134F753410CCCA9", "");

    private static final byte[] DAILY_FRAME_ENCRYPTED2 = ProtocolTools.getBytesFromHexString("AF4407070777700000007A26B80A25ABD54956745D59640C2AAAB6238F88CDB424002277A53081ECEDC0BA7EC7CC09881A89582C9003E8AEF46B4CACFCD00C881A89582C9003E8AEF46B4CACFCD00CC25BCBF4821D14072424F29554B3BFE9E5CB45EBA2E92B148B093AEFB8689F53CE9E93475F6F3DB0BF7D3D411367F47B5806EE48FE4D6AAC503BCE136847BD5D3D699A0974F61FC049902701A266B20ED13E6335CF5B6B4B0DB70F9F839CED2F", "");

    private static final byte[] DAILY_FRAME_DECRYPTED = ProtocolTools.getBytesFromHexString("AF4407070777700000007A26B80A252F2F860D6DE500A0201220041354BF00008D04931F33E201000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000123B0000223B000004933C1200000003FD971D041300041354BF00008D04931F15D20F00000000000000000000000000000000000002FD7401000F0100B8FB0A001700000070707070707070707070707070891000F7E8D2", "");


    private static final byte[] WEEKLY_FRAME_ENCRYPTED = ProtocolTools.getBytesFromHexString ("4F4407070777700000007AAF004025C7D833417B0AAC44244B50806AA42F882A39435576BAE5693869F149C12041D5510E3F227A0BFE00282A6264A79E608C32AF479ED7C061C338BD0C4251FC67AC", "");
    private static final byte[] WEEKLY_FRAME_DECRYPTED = ProtocolTools.getBytesFromHexString ("4F4407070777700000007AAF0040252F2F84046D0000D328041354BF00008D04931F2DF301000000000000F46800000080000080000080000080000080000080000080000080000080000080000080","");

    private static final byte[] NRT_FRAME_ENCRYPTED = ProtocolTools.getBytesFromHexString("2F4407070777700000007A50972025445F85D4621E087B3B3E5D3B4BD1EF6B43A25EA1C90387A941B1DBF816CF9853","");
    private static final byte[] NRT_FRAME_DECRYPTED = ProtocolTools.getBytesFromHexString("2F4407070777700000007A509720252F2F84046D130BD428123B0000223B00000493C31200000003FD971D041300F0","");

    private static byte[] key = ProtocolTools.getBytesFromHexString("4FA70B24465F814A667631773A397644", "");
    private static byte[] iv = ProtocolTools.getBytesFromHexString("00000000000000000000000000000000", "");

    @Mock
    private InboundDiscoveryContext inboundDiscoveryContext;

    /* TODO: add unencrypted
    @Test
    public void testGenericMbus() throws IOException, SQLException {
        MerlinMbusParser parser = new MerlinMbusParser(new InboundContext(new MerlinLogger(Logger.getAnonymousLogger())));

        parser.parse(genericMbus);
    }
    */

    @Test
    public void testDailyFrameEncrypted1() throws IOException, SQLException {
        MerlinMbusParser parser = new MerlinMbusParser(new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext()));

        parser.parse(DAILY_FRAME_ENCRYPTED1);


        assertEquals("FDB07736", parser.getTelegram().getSerialNr());
        assertEquals("7F2D", Converter.convertListToString(parser.getTelegram().getHeader().getmField().getFieldParts()));
        assertEquals("44" , parser.getTelegram().getHeader().getcField().getFieldParts().get(0));
        assertEquals("00" , parser.getTelegram().getBody().getBodyHeader().getAccessNumber());

        // date
        assertEquals("2021-10-22 09:57:56" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(2).getDataField().getParsedValue());

        // snapshot value
        assertEquals("0" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getDataField().getParsedValue());
        assertEquals("m^3", parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getMultiplier());

        // snapshot value
        assertEquals("0" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getDataField().getParsedValue());
        assertEquals("m^3/h", parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getVif().getMultiplier());

        // battery data
        assertEquals("1" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(11).getDataField().getParsedValue());
        assertEquals("none", parser.getTelegram().getBody().getBodyPayload().getRecords().get(11).getVif().getmUnit().getValue());
        assertEquals(0, parser.getTelegram().getBody().getBodyPayload().getRecords().get(11).getVif().getMultiplier());
    }


    @Test
    public void testDailyFrameEncrypted2() throws IOException, SQLException {
        MerlinMbusParser parser = new MerlinMbusParser(new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext()));

        parser.parse(DAILY_FRAME_ENCRYPTED2);


        assertEquals("00707707", parser.getTelegram().getSerialNr());
        assertEquals("44" , parser.getTelegram().getHeader().getcField().getFieldParts().get(0));
        assertEquals("26" , parser.getTelegram().getBody().getBodyHeader().getAccessNumber());

        // date
        assertEquals("2022-08-22 00:00:00" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(2).getDataField().getParsedValue());

        // snapshot value
        assertEquals("48980" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getDataField().getParsedValue());
        assertEquals("m^3", parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getMultiplier());

        // profile data - todo

        // max flow data
        assertEquals("0" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getDataField().getParsedValue());
        assertEquals("m^3/h", parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getVif().getMultiplier());

        // min flow data
        assertEquals("0" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(6).getDataField().getParsedValue());
        assertEquals("m^3/h", parser.getTelegram().getBody().getBodyPayload().getRecords().get(6).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(6).getVif().getMultiplier());


    }

    private InboundDiscoveryContext getContext() {
        return inboundDiscoveryContext;
    }


    @Test
    public void testWeeklyFrameEncrypted() throws IOException, SQLException {
        MerlinMbusParser parser = new MerlinMbusParser(new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext()));

        parser.parse(WEEKLY_FRAME_ENCRYPTED);

        assertEquals("00707707", parser.getTelegram().getSerialNr());
        assertEquals("44" , parser.getTelegram().getHeader().getcField().getFieldParts().get(0));
        assertEquals("AF" , parser.getTelegram().getBody().getBodyHeader().getAccessNumber());
        // date
        assertEquals("19.8.2022 0:0" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(2).getDataField().getParsedValue());

        // snapshot value
        assertEquals("48980" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getDataField().getParsedValue());
        assertEquals("m^3", parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getMultiplier());

    }


    @Test
    public void testNTRFrameEncrypted() throws IOException, SQLException {
        MerlinMbusParser parser = new MerlinMbusParser(new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext()));

        parser.parse(NRT_FRAME_ENCRYPTED);
        assertEquals("00707707", parser.getTelegram().getSerialNr());
        assertEquals("44" , parser.getTelegram().getHeader().getcField().getFieldParts().get(0));
        assertEquals("50" , parser.getTelegram().getBody().getBodyHeader().getAccessNumber());

        // date
        assertEquals("20.8.2022 11:19" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(2).getDataField().getParsedValue());

        // max flow data
        assertEquals("0" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getDataField().getParsedValue());
        assertEquals("m^3/h", parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(3).getVif().getMultiplier());

        // min flow data
        assertEquals("0" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(4).getDataField().getParsedValue());
        assertEquals("m^3/h", parser.getTelegram().getBody().getBodyPayload().getRecords().get(4).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(4).getVif().getMultiplier());

        // back flow
        assertEquals("18" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getDataField().getParsedValue());
        assertEquals("m^3", parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getVif().getmUnit().getValue());
        assertEquals(-3, parser.getTelegram().getBody().getBodyPayload().getRecords().get(5).getVif().getMultiplier());

        // error flags
        assertEquals("[04, 13, 00]" , parser.getTelegram().getBody().getBodyPayload().getRecords().get(6).getDataField().getFieldParts().toString());

    }

}
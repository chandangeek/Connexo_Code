package com.energyict.mdc.protocol.inbound.mbus;

import com.energyict.cbo.Quantity;
import com.energyict.mdc.protocol.inbound.mbus.factory.AbstractMerlinFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.HourlyProfileFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.RegisterFactory;
import com.energyict.mdc.protocol.inbound.mbus.parser.MerlinMbusParser;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Converter;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.google.common.collect.Range;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.calls;
import static org.mockito.Mockito.when;

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

    @Mock
    CollectedDataFactory collectedDataFactory ;

    /* TODO: add unencrypted
    @Test
    public void testGenericMbus() throws IOException, SQLException {
        MerlinMbusParser parser = new MerlinMbusParser(new InboundContext(new MerlinLogger(Logger.getAnonymousLogger())));

        parser.parse(genericMbus);
    }
    */

    @Before
    public void setUp(){
        CollectedLoadProfileConfiguration lpConfig = new CollectedLoadProfileConfiguration() {
            @Override
            public ObisCode getObisCode() {
                return null;
            }

            @Override
            public String getMeterSerialNumber() {
                return null;
            }

            @Override
            public int getProfileInterval() {
                return 0;
            }

            @Override
            public void setProfileInterval(int profileInterval) {

            }

            @Override
            public int getNumberOfChannels() {
                return 0;
            }

            @Override
            public List<ChannelInfo> getChannelInfos() {
                return null;
            }

            @Override
            public void setChannelInfos(List<ChannelInfo> channelInfos) {

            }

            @Override
            public boolean isSupportedByMeter() {
                return false;
            }

            @Override
            public void setSupportedByMeter(boolean supportedByMeter) {

            }

            @Override
            public ResultType getResultType() {
                return null;
            }

            @Override
            public List<Issue> getIssues() {
                return null;
            }

            @Override
            public void setFailureInformation(ResultType resultType, Issue issue) {

            }

            @Override
            public void setFailureInformation(ResultType resultType, List<Issue> issues) {

            }

            @Override
            public boolean isConfiguredIn(DataCollectionConfiguration comTask) {
                return false;
            }
        };
        when(collectedDataFactory.createCollectedLoadProfileConfiguration(anyObject(), anyObject())).thenReturn(lpConfig);

        CollectedLoadProfile loadProfile = new CollectedLoadProfile() {
            private List<IntervalData> collectedIntervalData;

            @Override
            public List<IntervalData> getCollectedIntervalData() {
                return collectedIntervalData;
            }

            @Override
            public List<ChannelInfo> getChannelInfo() {
                return null;
            }

            @Override
            public boolean isDoStoreOlderValues() {
                return false;
            }

            @Override
            public void setDoStoreOlderValues(boolean doStoreOlderValues) {

            }

            @Override
            public boolean isAllowIncompleteLoadProfileData() {
                return false;
            }

            @Override
            public void setAllowIncompleteLoadProfileData(boolean allowIncompleteLoadProfileData) {

            }

            @Override
            public LoadProfileIdentifier getLoadProfileIdentifier() {
                return null;
            }

            @Override
            public void setCollectedIntervalData(List<IntervalData> collectedIntervalData, List<ChannelInfo> deviceChannelInfo) {
                this.collectedIntervalData = collectedIntervalData;
            }

            @Override
            public Range<Instant> getCollectedIntervalDataRange() {
                return null;
            }

            @Override
            public String getXmlType() {
                return null;
            }

            @Override
            public void setXmlType(String ignore) {

            }

            @Override
            public ResultType getResultType() {
                return null;
            }

            @Override
            public List<Issue> getIssues() {
                return null;
            }

            @Override
            public void setFailureInformation(ResultType resultType, Issue issue) {

            }

            @Override
            public void setFailureInformation(ResultType resultType, List<Issue> issues) {

            }

            @Override
            public boolean isConfiguredIn(DataCollectionConfiguration comTask) {
                return false;
            }
        };
        when(collectedDataFactory.createCollectedLoadProfile(anyObject())).thenReturn(loadProfile);


        CollectedRegister collectedRegister = new CollectedRegister() {
            private String text;
            private RegisterIdentifier registerIdentifier;
            private Quantity quantity;
            private Date readTime;

            void CollectedRegister(RegisterIdentifier registerIdentifier) {
                this.registerIdentifier = registerIdentifier;
            }

            @Override
            public Quantity getCollectedQuantity() {
                return this.quantity;
            }

            @Override
            public String getText() {
                return this.text;
            }

            @Override
            public Date getReadTime() {
                return this.readTime;
            }

            @Override
            public void setReadTime(Date readTime) {
                this.readTime = readTime;
            }

            @Override
            public Date getFromTime() {
                return null;
            }

            @Override
            public Date getToTime() {
                return null;
            }

            @Override
            public Date getEventTime() {
                return null;
            }

            @Override
            public RegisterIdentifier getRegisterIdentifier() {
                return this.registerIdentifier;
            }

            @Override
            public boolean isTextRegister() {
                return false;
            }

            @Override
            public void setCollectedTimeStamps(Date readTime, Date fromTime, Date toTime, Date eventTime) {

            }

            @Override
            public void setCollectedData(Quantity collectedQuantity) {
                this.quantity = collectedQuantity;
            }

            @Override
            public void setCollectedData(Quantity collectedQuantity, String text) {
                this.quantity = collectedQuantity;
            }

            @Override
            public void setCollectedData(String text) {
                this.text = text;
            }

            @Override
            public void setCollectedTimeStamps(Date readTime, Date fromTime, Date toTime) {

            }

            @Override
            public ResultType getResultType() {
                return null;
            }

            @Override
            public List<Issue> getIssues() {
                return null;
            }

            @Override
            public void setFailureInformation(ResultType resultType, Issue issue) {

            }

            @Override
            public void setFailureInformation(ResultType resultType, List<Issue> issues) {

            }

            @Override
            public boolean isConfiguredIn(DataCollectionConfiguration comTask) {
                return false;
            }
        };

        when(collectedDataFactory.createDefaultCollectedRegister(anyObject())).thenReturn(collectedRegister);


        CollectedRegisterList collectedRegistersList = new CollectedRegisterList() {
            private List<CollectedRegister> collectedRegisters = new ArrayList<>();

            @Override
            public void addCollectedRegister(CollectedRegister collectedRegister) {
                this.collectedRegisters.add(collectedRegister);
            }

            @Override
            public List<CollectedRegister> getCollectedRegisters() {
                return this.collectedRegisters;
            }

            @Override
            public DeviceIdentifier getDeviceIdentifier() {
                return null;
            }

            @Override
            public ResultType getResultType() {
                return null;
            }

            @Override
            public List<Issue> getIssues() {
                return null;
            }

            @Override
            public void setFailureInformation(ResultType resultType, Issue issue) {

            }

            @Override
            public void setFailureInformation(ResultType resultType, List<Issue> issues) {

            }

            @Override
            public boolean isConfiguredIn(DataCollectionConfiguration comTask) {
                return false;
            }
        };
        when(collectedDataFactory.createCollectedRegisterList(anyObject())).thenReturn(collectedRegistersList);

        when(inboundDiscoveryContext.getCollectedDataFactory()).thenReturn(collectedDataFactory);
    }

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
        MerlinMbusParser parser = new MerlinMbusParser(new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext()));

        parser.parse(DAILY_FRAME_ENCRYPTED2);


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
        MerlinMbusParser parser = new MerlinMbusParser(new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext()));

        parser.parse(WEEKLY_FRAME_ENCRYPTED);

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
        MerlinMbusParser parser = new MerlinMbusParser(new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext()));

        parser.parse(NRT_FRAME_ENCRYPTED);
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

        parser.parse(DAILY_FRAME_ENCRYPTED2);

        RegisterFactory registerFactory = new RegisterFactory(parser.getTelegram(), inboundContext);

        CollectedRegisterList registers = registerFactory.extractRegisters();

        assertEquals(5, registers.getCollectedRegisters().size());

        assertEquals(48980, registers.getCollectedRegisters().get(0).getCollectedQuantity().intValue());
    }

    @Test
    public void testRegisterParserWeekly(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parse(WEEKLY_FRAME_ENCRYPTED);

        RegisterFactory registerFactory = new RegisterFactory(parser.getTelegram(), inboundContext);

        CollectedRegisterList registers = registerFactory.extractRegisters();

        assertEquals(1, registers.getCollectedRegisters().size());

        assertEquals(48980, registers.getCollectedRegisters().get(0).getCollectedQuantity().intValue());
    }


    @Test
    public void testRegisterParserNTR(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parse(NRT_FRAME_ENCRYPTED);

        RegisterFactory registerFactory = new RegisterFactory(parser.getTelegram(), inboundContext);

        CollectedRegisterList registers = registerFactory.extractRegisters();

        assertEquals(3, registers.getCollectedRegisters().size());

        assertEquals(18, registers.getCollectedRegisters().get(0).getCollectedQuantity().intValue());
    }

    @Test
    public void testLoadProfileParser(){
        InboundContext inboundContext = new InboundContext(new MerlinLogger(Logger.getAnonymousLogger()), getContext());
        MerlinMbusParser parser = new MerlinMbusParser(inboundContext);

        parser.parse(DAILY_FRAME_ENCRYPTED2);

        HourlyProfileFactory factory = new HourlyProfileFactory(parser.getTelegram(), inboundContext);

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

        Instant midnight = AbstractMerlinFactory.toMidnight(random);

        assertEquals("2022-09-22T00:00:00Z", midnight.toString());
    }
}
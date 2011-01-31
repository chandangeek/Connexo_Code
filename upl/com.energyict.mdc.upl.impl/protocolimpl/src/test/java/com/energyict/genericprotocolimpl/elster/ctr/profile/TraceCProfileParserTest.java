package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.structure.Trace_CQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.PeriodTrace_C;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.ReferenceDate;
import com.energyict.protocol.IntervalData;
import org.junit.Test;

import java.util.List;
import java.util.TimeZone;

import static com.energyict.genericprotocolimpl.elster.ctr.profile.TraceCProfileParser.calcRefDate;
import static com.energyict.genericprotocolimpl.elster.ctr.profile.TraceCProfileParser.getStartOfGasDay;
import static com.energyict.protocolimpl.utils.ProtocolTools.*;
import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 26-jan-2011
 * Time: 16:22:53
 */
public class TraceCProfileParserTest {

    private static final byte[] RAW_DAILY_1 = getBytesFromHexString("$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$19$06$15$06$00$00$00$2C$02$01$13$0B$01$18$00$00$00$A6$3B$00$00$01$BA$00$00$01$75$00$00$01$41$00$00$01$4C$00$00$01$4A$00$00$00$0B$00$00$00$0B$00$00$01$89$00$00$01$68$00$00$01$65$00$00$01$58$00$00$01$6F$00$00$00$0D$00$00$00$0C$00$00$01$B6$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$40$08$52$9A$BC$30$0D");
    private static final byte[] RAW_DAILY_2 = getBytesFromHexString("$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$1A$06$15$06$00$00$00$2C$02$01$13$0B$01$19$00$00$00$A7$E0$00$00$01$75$00$00$01$41$00$00$01$4C$00$00$01$4A$00$00$00$0B$00$00$00$0B$00$00$01$89$00$00$01$68$00$00$01$65$00$00$01$58$00$00$01$6F$00$00$00$0D$00$00$00$0C$00$00$01$B6$00$00$01$A5$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$2D$79$18$7C$46$B4$0D");

    private static final byte[] RAW_HOURLY_1 = getBytesFromHexString("$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$19$06$15$06$00$00$00$2C$01$07$02$0B$01$18$00$00$00$00$44$03$04$20$4A$03$04$26$12$03$04$31$FC$03$04$36$D4$03$04$3B$0C$03$04$42$28$03$04$49$1C$03$04$51$82$03$04$4C$FA$03$04$48$4A$03$04$44$58$03$04$3F$A8$03$04$3A$4E$03$04$35$08$03$04$31$0C$03$04$2D$E2$03$04$2B$80$03$04$29$DC$03$04$27$8E$03$04$25$04$03$04$22$F2$03$04$21$30$03$04$20$4A$03$04$1F$50$10$00$00$00$BB$5B$BF$7D$14$BA$0D");
    private static final byte[] RAW_HOURLY_2 = getBytesFromHexString("$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$1A$06$15$06$00$00$00$2C$01$07$02$0B$01$19$00$00$00$00$44$03$04$1E$9C$03$04$23$B0$03$04$31$CA$03$04$37$C4$03$04$3F$E4$03$04$44$12$03$04$47$64$03$04$4D$04$03$04$48$F4$03$04$46$56$03$04$42$BE$03$04$3E$7C$03$04$3A$08$03$04$35$6C$03$04$31$8E$03$04$2F$04$03$04$2E$0A$03$04$2C$7A$03$04$2A$72$03$04$28$74$03$04$26$B2$03$04$25$04$03$04$23$6A$03$04$22$84$10$00$00$00$62$D9$E1$98$2E$6A$0D");

    @Test
    public void testGetIntervalData() throws Exception {
        assertEquals(15, getParser(RAW_DAILY_1).getIntervalData().size());
        assertEquals(24, getParser(RAW_HOURLY_1).getIntervalData().size());
    }

    @Test
    public void testGetDailyIntervalDataWitFromDate() throws Exception {
        assertEquals(15, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 10, 6, 0, 0, 0), null).size());
        assertEquals(14, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 11, 6, 0, 0, 0), null).size());
        assertEquals(10, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 15, 6, 0, 0, 0), null).size());
        assertEquals(5, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 20, 6, 0, 0, 0), null).size());
        assertEquals(0, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 25, 6, 0, 0, 0), null).size());
        assertEquals(0, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 26, 6, 0, 0, 0), null).size());
    }

    @Test
    public void testGetDailyIntervalDataWitToDate() throws Exception {
        assertEquals(15, getParser(RAW_DAILY_1).getIntervalData(null, createCalendar(2011, 1, 25, 6, 0, 0, 0)).size());
        assertEquals(14, getParser(RAW_DAILY_1).getIntervalData(null, createCalendar(2011, 1, 24, 6, 0, 0, 0)).size());
        assertEquals(10, getParser(RAW_DAILY_1).getIntervalData(null, createCalendar(2011, 1, 20, 6, 0, 0, 0)).size());
        assertEquals(5, getParser(RAW_DAILY_1).getIntervalData(null, createCalendar(2011, 1, 15, 6, 0, 0, 0)).size());
        assertEquals(0, getParser(RAW_DAILY_1).getIntervalData(null, createCalendar(2011, 1, 10, 6, 0, 0, 0)).size());
        assertEquals(0, getParser(RAW_DAILY_1).getIntervalData(null, createCalendar(2011, 1, 1, 6, 0, 0, 0)).size());
    }

    @Test
    public void testGetDailyIntervalDataWithDate() throws Exception {
        assertEquals(15, getParser(RAW_DAILY_1).getIntervalData(null, null).size());
        assertEquals(15, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 10, 6, 0, 0, 0), createCalendar(2011, 1, 25, 6, 0, 0, 0)).size());
        assertEquals(14, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 11, 6, 0, 0, 0), createCalendar(2011, 1, 25, 6, 0, 0, 0)).size());
        assertEquals(14, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 10, 6, 0, 0, 0), createCalendar(2011, 1, 24, 6, 0, 0, 0)).size());
        assertEquals(13, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 11, 6, 0, 0, 0), createCalendar(2011, 1, 24, 6, 0, 0, 0)).size());
        assertEquals(5, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 15, 6, 0, 0, 0), createCalendar(2011, 1, 20, 6, 0, 0, 0)).size());
        assertEquals(5, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 17, 6, 0, 0, 0), createCalendar(2011, 1, 22, 6, 0, 0, 0)).size());
        assertEquals(0, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 15, 6, 0, 0, 0), createCalendar(2011, 1, 15, 6, 0, 0, 0)).size());
        assertEquals(0, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 20, 6, 0, 0, 0), createCalendar(2011, 1, 10, 6, 0, 0, 0)).size());
    }

    @Test
    public void testGetHourlyIntervalDataWithToDate() {
        assertEquals(23, getParser(RAW_HOURLY_1).getIntervalData(null, createCalendar(2011, 1, 25, 5, 0, 0, 0)).size());
        assertEquals(15, getParser(RAW_HOURLY_1).getIntervalData(null, createCalendar(2011, 1, 24, 21, 0, 0, 0)).size());
        assertEquals(5, getParser(RAW_HOURLY_1).getIntervalData(null, createCalendar(2011, 1, 24, 11, 0, 0, 0)).size());
        assertEquals(0, getParser(RAW_HOURLY_1).getIntervalData(null, createCalendar(2011, 1, 24, 6, 0, 0, 0)).size());
        assertEquals(0, getParser(RAW_HOURLY_1).getIntervalData(null, createCalendar(2011, 1, 24, 5, 0, 0, 0)).size());
    }

    @Test
    public void testGetHourlyIntervalDataWithFromDate() {
        assertEquals(24, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 24, 6, 0, 0, 0), null).size());
        assertEquals(23, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 24, 7, 0, 0, 0), null).size());
        assertEquals(15, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 24, 15, 0, 0, 0), null).size());
        assertEquals(10, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 24, 20, 0, 0, 0), null).size());
        assertEquals(5, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 25, 1, 0, 0, 0), null).size());
        assertEquals(0, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 25, 6, 0, 0, 0), null).size());
        assertEquals(0, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 25, 7, 0, 0, 0), null).size());
    }

    @Test
    public void testGetHourlyIntervalDataWithDate() throws Exception {
        assertEquals(24, getParser(RAW_HOURLY_1).getIntervalData(null, null).size());
        assertEquals(24, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 24, 6, 0, 0, 0), createCalendar(2011, 1, 25, 6, 0, 0, 0)).size());
        assertEquals(23, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 24, 7, 0, 0, 0), createCalendar(2011, 1, 25, 6, 0, 0, 0)).size());
        assertEquals(20, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 24, 8, 0, 0, 0), createCalendar(2011, 1, 25, 4, 0, 0, 0)).size());
        assertEquals(15, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 24, 10, 0, 0, 0), createCalendar(2011, 1, 25, 1, 0, 0, 0)).size());
        assertEquals(10, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 24, 14, 0, 0, 0), createCalendar(2011, 1, 25, 0, 0, 0, 0)).size());
        assertEquals(5, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 24, 14, 0, 0, 0), createCalendar(2011, 1, 24, 19, 0, 0, 0)).size());
        assertEquals(5, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 24, 10, 0, 0, 0), createCalendar(2011, 1, 24, 15, 0, 0, 0)).size());
        assertEquals(0, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 24, 6, 0, 0, 0), createCalendar(2011, 1, 24, 6, 0, 0, 0)).size());
        assertEquals(0, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 24, 7, 0, 0, 0), createCalendar(2011, 1, 24, 7, 0, 0, 0)).size());
        assertEquals(0, getParser(RAW_HOURLY_1).getIntervalData(createCalendar(2011, 1, 24, 7, 0, 0, 0), createCalendar(2011, 1, 24, 6, 0, 0, 0)).size());
    }

    @Test
    public void testRawHourly2() throws Exception {
        List<IntervalData> intervals = getParser(RAW_HOURLY_2).getIntervalData();
    }

    @Test
    public void testRawDaily2() throws Exception {
        List<IntervalData> intervals = getParser(RAW_DAILY_2).getIntervalData();
    }

    @Test
    public void testGetFromCalendar() throws Exception {
        assertEquals(
                createCalendar(2011, 1, 11, 6, 0, 0, 0).getTimeInMillis(),
                getParser(RAW_DAILY_1).getFromCalendar().getTimeInMillis()
        );
        assertEquals(
                createCalendar(2011, 1, 24, 7, 0, 0, 0).getTimeInMillis(),
                getParser(RAW_HOURLY_1).getFromCalendar().getTimeInMillis()
        );
    }

    private TraceCProfileParser getParser(byte[] rawBytes) {
        return getParser(rawBytes, TimeZone.getDefault());
    }

    @Test
    public void testGetToCalendar() throws Exception {
        assertEquals(
                createCalendar(2011, 1, 25, 6, 0, 0, 0).getTimeInMillis(),
                getParser(RAW_DAILY_1).getToCalendar().getTimeInMillis()
        );
        assertEquals(
                createCalendar(2011, 1, 25, 6, 0, 0, 0).getTimeInMillis(),
                getParser(RAW_HOURLY_1).getToCalendar().getTimeInMillis()
        );
    }

    @Test
    public void testGetStartOfGasDay() {
        assertEquals(createCalendar(2011, 1, 20, 6, 0, 0, 0), getStartOfGasDay(createCalendar(2011, 1, 20, 6, 0, 0, 0)));
        assertEquals(createCalendar(2011, 1, 20, 6, 0, 0, 0), getStartOfGasDay(createCalendar(2011, 1, 20, 6, 0, 0, 1)));
        assertEquals(createCalendar(2011, 1, 20, 6, 0, 0, 0), getStartOfGasDay(createCalendar(2011, 1, 21, 5, 59, 59, 50)));
        assertEquals(createCalendar(2011, 1, 19, 6, 0, 0, 0), getStartOfGasDay(createCalendar(2011, 1, 20, 5, 59, 59, 50)));
        assertEquals(createCalendar(2011, 1, 19, 6, 0, 0, 0), getStartOfGasDay(createCalendar(2011, 1, 19, 6, 1, 0, 0)));
    }

    @Test
    public void testGetReferenceDate() {
        assertEquals(createRefDate(2011, 1, 19),  calcRefDate(createCalendar(2011, 1, 20, 0, 0, 0, 0), PeriodTrace_C.getHourly()));
        assertEquals(createRefDate(2011, 1, 19),  calcRefDate(createCalendar(2011, 1, 20, 5, 0, 0, 0), PeriodTrace_C.getHourly()));
        assertEquals(createRefDate(2011, 1, 20),  calcRefDate(createCalendar(2011, 1, 20, 6, 0, 0, 0), PeriodTrace_C.getHourly()));
        assertEquals(createRefDate(2011, 1, 20),  calcRefDate(createCalendar(2011, 1, 20, 6, 15, 0, 0), PeriodTrace_C.getHourly()));

        assertEquals(createRefDate(2011, 1, 24),  calcRefDate(createCalendar(2011, 1, 10, 6, 0, 0, 0), PeriodTrace_C.getDaily()));
        assertEquals(createRefDate(2011, 1, 24),  calcRefDate(createCalendar(2011, 1, 10, 9, 0, 0, 0), PeriodTrace_C.getDaily()));
        assertEquals(createRefDate(2011, 1, 24),  calcRefDate(createCalendar(2011, 1, 11, 5, 0, 0, 0), PeriodTrace_C.getDaily()));
        assertEquals(createRefDate(2011, 2, 3),  calcRefDate(createCalendar(2011, 1, 20, 6, 0, 0, 0), PeriodTrace_C.getDaily()));

        assertEquals(createRefDate(2011, 1, 29),  calcRefDate(createCalendar(2011, 1, 15, 6, 0, 0, 0), PeriodTrace_C.getDaily()));

    }

    private static ReferenceDate createRefDate(int year, int month, int day) {
        return new ReferenceDate().parse(createCalendar(year, month, day, 0, 0, 0, 0));
    }

    private static TraceCProfileParser getParser(byte[] rawFrame, TimeZone timeZone) {
        try {
            GPRSFrame frame = new GPRSFrame();
            frame.parse(rawFrame, 0);
            Data data = frame.getData();
            if (data instanceof Trace_CQueryResponseStructure) {
                return new TraceCProfileParser((Trace_CQueryResponseStructure) data, timeZone);
            } else {
                throw new IllegalArgumentException("rawFrame does not contain Trace_CQueryResponseStructure! [" + getHexStringFromBytes(rawFrame) + "]");
            }
        } catch (CTRParsingException e) {
            throw new IllegalArgumentException("Not a valid Trace_CQueryResponseStructure: " + getHexStringFromBytes(rawFrame));
        }
    }

}

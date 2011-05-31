package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.structure.Trace_CQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.PeriodTrace_C;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.ReferenceDate;
import com.energyict.protocol.IntervalData;
import org.junit.Test;

import java.math.BigDecimal;
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
    private static final byte[] RAW_DAILY_TOT_Vm = getBytesFromHexString("$0A$00$00$00$21$53$00$00$88$12$06$70$15$08$0B$03$10$0A$1E$06$FF$FF$01$21$02$02$03$0B$03$1D$00$00$02$76$01$00$00$02$75$8E$10$00$01$C2$DC$10$00$01$C5$86$10$00$01$C8$24$10$00$01$CA$A6$10$00$01$CD$04$10$00$01$CF$48$10$00$01$D1$65$10$00$01$D3$5F$10$00$01$D5$91$10$00$01$D7$B5$10$00$01$D9$DC$10$00$01$DC$57$10$00$01$DE$FA$10$00$01$E1$9D$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$D2$2F$72$4D$CC$B5$0D");
    private static final byte[] RAW_DAILY_Q_MAX_1 = getBytesFromHexString("$0A$00$00$00$21$53$00$00$88$32$04$11$48$58$0B$02$15$06$1C$06$00$00$00$17$02$01$A3$0B$01$1C$00$00$00$67$AD$00$00$00$6C$05$1E$00$00$00$58$09$14$00$00$00$70$09$19$00$00$00$70$05$1E$00$00$00$70$05$23$00$00$00$70$05$1E$00$00$00$6C$05$1E$00$00$00$70$05$28$00$00$00$64$09$14$00$00$00$70$09$19$00$00$00$70$05$32$00$00$00$74$05$37$00$00$00$70$05$37$00$00$00$70$05$32$00$00$00$6C$05$32$00$00$00$00$00$00$00$00$00$00$F5$61$92$25$04$BC$0D");
    private static final byte[] RAW_DAILY_Q_MAX_2 = getBytesFromHexString("$0A$00$00$00$21$53$00$00$88$32$04$11$48$58$0B$02$15$06$1C$06$00$00$00$17$02$01$A3$0B$02$0C$00$00$00$67$AD$00$00$00$6C$09$14$00$00$00$74$09$1E$00$00$00$70$05$32$00$00$00$74$05$32$00$00$00$78$05$37$00$00$00$74$05$37$00$00$00$70$05$32$00$00$00$48$09$14$00$00$00$74$09$1E$00$00$00$70$05$32$00$00$00$70$05$32$00$00$00$70$05$32$00$00$00$70$05$32$00$00$00$70$05$2D$00$00$00$60$09$14$00$00$00$00$00$00$00$00$00$00$4A$38$8D$F2$6E$4C$0D");

    private static final byte[] RAW_HOURLY_1 = getBytesFromHexString("$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$19$06$15$06$00$00$00$2C$01$07$02$0B$01$18$00$00$00$00$44$03$04$20$4A$03$04$26$12$03$04$31$FC$03$04$36$D4$03$04$3B$0C$03$04$42$28$03$04$49$1C$03$04$51$82$03$04$4C$FA$03$04$48$4A$03$04$44$58$03$04$3F$A8$03$04$3A$4E$03$04$35$08$03$04$31$0C$03$04$2D$E2$03$04$2B$80$03$04$29$DC$03$04$27$8E$03$04$25$04$03$04$22$F2$03$04$21$30$03$04$20$4A$03$04$1F$50$10$00$00$00$BB$5B$BF$7D$14$BA$0D");
    private static final byte[] RAW_HOURLY_2 = getBytesFromHexString("$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$1A$06$15$06$00$00$00$2C$01$07$02$0B$01$19$00$00$00$00$44$03$04$1E$9C$03$04$23$B0$03$04$31$CA$03$04$37$C4$03$04$3F$E4$03$04$44$12$03$04$47$64$03$04$4D$04$03$04$48$F4$03$04$46$56$03$04$42$BE$03$04$3E$7C$03$04$3A$08$03$04$35$6C$03$04$31$8E$03$04$2F$04$03$04$2E$0A$03$04$2C$7A$03$04$2A$72$03$04$28$74$03$04$26$B2$03$04$25$04$03$04$23$6A$03$04$22$84$10$00$00$00$62$D9$E1$98$2E$6A$0D");
    private static final byte[] RAW_HOURLY_BEFORE_DST = getBytesFromHexString("$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$1A$06$15$06$00$00$00$2C$01$07$02$0B$03$19$00$00$00$00$44$03$04$1E$9C$03$04$23$B0$03$04$31$CA$03$04$37$C4$03$04$3F$E4$03$04$44$12$03$04$47$64$03$04$4D$04$03$04$48$F4$03$04$46$56$03$04$42$BE$03$04$3E$7C$03$04$3A$08$03$04$35$6C$03$04$31$8E$03$04$2F$04$03$04$2E$0A$03$04$2C$7A$03$04$2A$72$03$04$28$74$03$04$26$B2$03$04$25$04$03$04$23$6A$03$04$22$84$10$00$00$00$62$D9$E1$98$2E$6A$0D");
    private static final byte[] RAW_HOURLY_DST = getBytesFromHexString("$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$1A$06$15$06$00$00$00$2C$01$07$02$0B$03$1A$00$00$00$00$44$03$04$1E$9C$03$04$23$B0$03$04$31$CA$03$04$37$C4$03$04$3F$E4$03$04$44$12$03$04$47$64$03$04$4D$04$03$04$48$F4$03$04$46$56$03$04$42$BE$03$04$3E$7C$03$04$3A$08$03$04$35$6C$03$04$31$8E$03$04$2F$04$03$04$2E$0A$03$04$2C$7A$03$04$2A$72$03$04$28$74$03$04$26$B2$03$04$25$04$03$04$23$6A$03$04$22$84$10$00$00$00$62$D9$E1$98$2E$6A$0D");
    private static final byte[] RAW_HOURLY_AFTER_DST = getBytesFromHexString("$0A$00$00$00$21$53$00$00$88$00$00$64$88$89$0B$01$1A$06$15$06$00$00$00$2C$01$07$02$0B$03$1B$00$00$00$00$44$03$04$1E$9C$03$04$23$B0$03$04$31$CA$03$04$37$C4$03$04$3F$E4$03$04$44$12$03$04$47$64$03$04$4D$04$03$04$48$F4$03$04$46$56$03$04$42$BE$03$04$3E$7C$03$04$3A$08$03$04$35$6C$03$04$31$8E$03$04$2F$04$03$04$2E$0A$03$04$2C$7A$03$04$2A$72$03$04$28$74$03$04$26$B2$03$04$25$04$03$04$23$6A$03$04$22$84$10$00$00$00$62$D9$E1$98$2E$6A$0D");

    @Test
    public void testGetIntervalData() throws Exception {
        assertEquals(15, getParser(RAW_DAILY_1).getIntervalData().size());
        assertEquals(24, getParser(RAW_HOURLY_1).getIntervalData().size());
    }

    @Test
    public void testGetDailyIntervalDataWithFromDate() throws Exception {
        assertEquals(15, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 10, 6, 0, 0, 0), null).size());
        assertEquals(14, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 11, 6, 0, 0, 0), null).size());
        assertEquals(10, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 15, 6, 0, 0, 0), null).size());
        assertEquals(5, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 20, 6, 0, 0, 0), null).size());
        assertEquals(0, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 25, 6, 0, 0, 0), null).size());
        assertEquals(0, getParser(RAW_DAILY_1).getIntervalData(createCalendar(2011, 1, 26, 6, 0, 0, 0), null).size());
    }

    @Test
    public void testGetDailyIntervalDataWithToDate() throws Exception {
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
        return getParser(rawBytes, TimeZone.getTimeZone("Europe/Paris"));
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
        assertEquals(createRefDate(2011, 1, 19), calcRefDate(createCalendar(2011, 1, 20, 0, 0, 0, 0), PeriodTrace_C.getHourly()));
        assertEquals(createRefDate(2011, 1, 19), calcRefDate(createCalendar(2011, 1, 20, 5, 0, 0, 0), PeriodTrace_C.getHourly()));
        assertEquals(createRefDate(2011, 1, 20), calcRefDate(createCalendar(2011, 1, 20, 6, 0, 0, 0), PeriodTrace_C.getHourly()));
        assertEquals(createRefDate(2011, 1, 20), calcRefDate(createCalendar(2011, 1, 20, 6, 15, 0, 0), PeriodTrace_C.getHourly()));

        assertEquals(createRefDate(2011, 1, 24), calcRefDate(createCalendar(2011, 1, 10, 6, 0, 0, 0), PeriodTrace_C.getDaily()));
        assertEquals(createRefDate(2011, 1, 24), calcRefDate(createCalendar(2011, 1, 10, 9, 0, 0, 0), PeriodTrace_C.getDaily()));
        assertEquals(createRefDate(2011, 1, 24), calcRefDate(createCalendar(2011, 1, 11, 5, 0, 0, 0), PeriodTrace_C.getDaily()));
        assertEquals(createRefDate(2011, 2, 3), calcRefDate(createCalendar(2011, 1, 20, 6, 0, 0, 0), PeriodTrace_C.getDaily()));

        assertEquals(createRefDate(2011, 1, 29), calcRefDate(createCalendar(2011, 1, 15, 6, 0, 0, 0), PeriodTrace_C.getDaily()));

    }

    @Test
    public void testDailyQMaxValues_1() {
        int[] values = new int[]{108, 88, 112, 112, 112, 112, 108, 112, 100, 112, 112, 116, 112, 112, 108};
        TraceCProfileParser parser = getParser(RAW_DAILY_Q_MAX_1);
        assertEquals(createCalendar(2011, 1, 15, 6, 0, 0, 0).getTime(), parser.getFromCalendar().getTime());
        assertEquals(createCalendar(2011, 1, 29, 6, 0, 0, 0).getTime(), parser.getToCalendar().getTime());
        List<IntervalData> intervalData = parser.getIntervalData();
        for (int i = 0; i < intervalData.size(); i++) {
            assertEquals(BigDecimal.valueOf(values[i]), intervalData.get(i).get(0));
        }
    }

    @Test
    public void testDailyQMaxValues_2() {
        int[] values = new int[]{108, 116, 112, 116, 120, 116, 112, 72, 116, 112, 112, 112, 112, 112, 96};
        TraceCProfileParser parser = getParser(RAW_DAILY_Q_MAX_2);
        assertEquals(createCalendar(2011, 1, 30, 6, 0, 0, 0).getTime(), parser.getFromCalendar().getTime());
        assertEquals(createCalendar(2011, 2, 13, 6, 0, 0, 0).getTime(), parser.getToCalendar().getTime());
        List<IntervalData> intervalData = parser.getIntervalData();
        for (int i = 0; i < intervalData.size(); i++) {
            assertEquals(BigDecimal.valueOf(values[i]), intervalData.get(i).get(0));
        }
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


    @Test
    public void testDST() throws Exception {

        assertEquals(createCalendar(2011, 3, 25, 7, 0, 0, 0).getTime(), getParser(RAW_HOURLY_BEFORE_DST).getFromCalendar().getTime());
        assertEquals(createCalendar(2011, 3, 26, 6, 0, 0, 0).getTime(), getParser(RAW_HOURLY_BEFORE_DST).getToCalendar().getTime());

        assertEquals(createCalendar(2011, 3, 26, 7, 0, 0, 0).getTime(), getParser(RAW_HOURLY_DST).getFromCalendar().getTime());
        assertEquals(createCalendar(2011, 3, 27, 7, 0, 0, 0).getTime(), getParser(RAW_HOURLY_DST).getToCalendar().getTime());

        assertEquals(createCalendar(2011, 3, 27, 8, 0, 0, 0).getTime(), getParser(RAW_HOURLY_AFTER_DST).getFromCalendar().getTime());
        assertEquals(createCalendar(2011, 3, 28, 7, 0, 0, 0).getTime(), getParser(RAW_HOURLY_AFTER_DST).getToCalendar().getTime());

        assertEquals(createCalendar(2011, 3, 16, 6, 0, 0, 0).getTime(), getParser(RAW_DAILY_TOT_Vm).getFromCalendar().getTime());
        assertEquals(createCalendar(2011, 3, 30, 7, 0, 0, 0).getTime(), getParser(RAW_DAILY_TOT_Vm).getToCalendar().getTime());

        assertEquals(createCalendar(2011, 1, 11, 6, 0, 0, 0).getTime(), getParser(RAW_DAILY_1).getFromCalendar().getTime());
        assertEquals(createCalendar(2011, 1, 25, 6, 0, 0, 0).getTime(), getParser(RAW_DAILY_1).getToCalendar().getTime());

        assertEquals(createCalendar(2011, 1, 12, 6, 0, 0, 0).getTime(), getParser(RAW_DAILY_2).getFromCalendar().getTime());
        assertEquals(createCalendar(2011, 1, 26, 6, 0, 0, 0).getTime(), getParser(RAW_DAILY_2).getToCalendar().getTime());

    }

    private void printProfile(TraceCProfileParser parser) {
        List<IntervalData> intervalData = parser.getIntervalData();
        for (IntervalData data : intervalData) {
            System.out.println(data);
        }
    }
}

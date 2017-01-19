package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.IReadingType;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CalculatedReadingRecord} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-26 (14:31)
 */
@RunWith(MockitoJUnitRunner.class)
public class CalculatedReadingRecordTest {

    private static final String FIFTEEN_MINS_NET_CONSUMPTION_MRID = "0.0.2.1.4.2.12.0.0.0.0.0.0.0.0.0.72.0";
    private static final String MONTHLY_NET_CONSUMPTION_MRID = "13.0.0.1.4.2.12.0.0.0.0.0.0.0.0.0.72.0";
    private static final long MY_FAVOURITE_PRIME_NUMBER = 97L;
    private static Instant JAN_1_2016_UTC = Instant.ofEpochMilli(1451606400000L);

    @Mock
    private ServerMeteringService meteringService;
    @Mock
    private IReadingType fifteenMinutesNetConsumption;
    @Mock
    private IReadingType monthlyNetConsumption;
    @Mock
    private ResultSet resultSet;

    @Mock
    private Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation;

    @Before
    public void initializeMocks() {
        when(this.fifteenMinutesNetConsumption.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(this.fifteenMinutesNetConsumption.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(this.fifteenMinutesNetConsumption.getFlowDirection()).thenReturn(FlowDirection.NET);
        when(this.fifteenMinutesNetConsumption.getMRID()).thenReturn(FIFTEEN_MINS_NET_CONSUMPTION_MRID);
        when(this.fifteenMinutesNetConsumption.toQuantity(BigDecimal.TEN)).thenReturn(Quantity.create(BigDecimal.TEN, 3, "Wh"));
        when(this.meteringService.getReadingType(FIFTEEN_MINS_NET_CONSUMPTION_MRID)).thenReturn(Optional.of(this.fifteenMinutesNetConsumption));
        when(this.monthlyNetConsumption.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);
        when(this.monthlyNetConsumption.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(this.monthlyNetConsumption.getFlowDirection()).thenReturn(FlowDirection.NET);
        when(this.monthlyNetConsumption.getMRID()).thenReturn(MONTHLY_NET_CONSUMPTION_MRID);
        when(this.monthlyNetConsumption.toQuantity(BigDecimal.TEN)).thenReturn(Quantity.create(BigDecimal.TEN, 3, "Wh"));
        when(this.monthlyNetConsumption.toQuantity(BigDecimal.valueOf(MY_FAVOURITE_PRIME_NUMBER))).thenReturn(Quantity.create(BigDecimal.valueOf(MY_FAVOURITE_PRIME_NUMBER), 3, "Wh"));
        when(this.meteringService.getReadingType(MONTHLY_NET_CONSUMPTION_MRID)).thenReturn(Optional.of(this.monthlyNetConsumption));
    }

    @Test
    public void initOnly() throws SQLException {
        CalculatedReadingRecord testInstance = this.testInstance();
        when(this.resultSet.getString(1)).thenReturn(FIFTEEN_MINS_NET_CONSUMPTION_MRID);
        BigDecimal expectedValue = BigDecimal.TEN;
        Quantity expectedQuantity = Quantity.create(expectedValue, 3, "Wh");
        when(this.resultSet.getBigDecimal(2)).thenReturn(expectedValue);
        Timestamp jan1st2016 = Timestamp.from(JAN_1_2016_UTC);
        Instant expectedIntervalStart = JAN_1_2016_UTC.minus(Duration.ofMinutes(15));
        when(this.resultSet.getTimestamp(3)).thenReturn(jan1st2016);
        when(this.resultSet.getLong(4)).thenReturn(JAN_1_2016_UTC.toEpochMilli());
        when(this.resultSet.getLong(5)).thenReturn(0L);
        when(this.resultSet.getLong(6)).thenReturn(1L);
        when(this.resultSet.getString(7)).thenReturn("1001");
        ProcessStatus expectedProcessStatus = new ProcessStatus(0);
        MeterActivation meterActivation = mock(MeterActivation.class);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainer.getZoneId()).thenReturn(ZoneId.of("Europe/Brussels"));
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getMeterActivations(any(Instant.class))).thenReturn(Collections.singletonList(meterActivation));

        //Business method
        testInstance.init(this.resultSet, deliverablesPerMeterActivation);
        testInstance.setReadingType(fifteenMinutesNetConsumption);
        testInstance.setUsagePoint(usagePoint);

        // Asserts
        assertThat(testInstance.getTimeStamp()).isEqualTo(JAN_1_2016_UTC);
        assertThat(testInstance.getReadingType()).isEqualTo(fifteenMinutesNetConsumption);
        assertThat(testInstance.getReadingTypes()).hasSize(1);
        assertThat(testInstance.getValue()).isEqualTo(expectedValue);
        assertThat(testInstance.getQuantities()).hasSize(1);
        assertThat(testInstance.getQuantity(fifteenMinutesNetConsumption)).isEqualTo(expectedQuantity);
        assertThat(testInstance.getTimePeriod()).contains(Range.openClosed(expectedIntervalStart, JAN_1_2016_UTC));
        assertThat(testInstance.getProcessStatus()).isEqualTo(expectedProcessStatus);
        assertThat(testInstance.getCount()).isEqualTo(1L);
    }

    @Test(expected = UnderlyingSQLFailedException.class)
    public void sqlExceptionIsWrapped() throws SQLException {
        CalculatedReadingRecord testInstance = this.testInstance();
        doThrow(SQLException.class).when(this.resultSet).getString(anyInt());
        doThrow(SQLException.class).when(this.resultSet).getTimestamp(anyInt());
        doThrow(SQLException.class).when(this.resultSet).getBigDecimal(anyInt());
        doThrow(SQLException.class).when(this.resultSet).getLong(anyInt());

        //Business method
        testInstance.init(this.resultSet, deliverablesPerMeterActivation);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void setDifferentReadingType() throws SQLException {
        CalculatedReadingRecord testInstance = this.testInstance();
        when(this.resultSet.getString(1)).thenReturn(FIFTEEN_MINS_NET_CONSUMPTION_MRID);
        BigDecimal expectedValue = BigDecimal.TEN;
        when(this.resultSet.getBigDecimal(2)).thenReturn(expectedValue);
        Timestamp jan1st2016 = Timestamp.from(JAN_1_2016_UTC);
        when(this.resultSet.getTimestamp(3)).thenReturn(jan1st2016);
        when(this.resultSet.getLong(4)).thenReturn(jan1st2016.getTime());
        when(this.resultSet.getLong(5)).thenReturn(0L);
        when(this.resultSet.getLong(6)).thenReturn(1L);
        when(this.resultSet.getString(7)).thenReturn("1001");
        testInstance.init(this.resultSet, deliverablesPerMeterActivation);

        //Business method
        testInstance.setReadingType(this.monthlyNetConsumption);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeDifferentReadingType() throws SQLException {
        CalculatedReadingRecord r1 = this.newTestInstance("13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0");
        CalculatedReadingRecord r2 = this.newTestInstance("0.0.2.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0");

        // Business method
        CalculatedReadingRecord.merge(r1, r2, Instant.now(), new InstantTruncaterFactory(this.meteringService), new SourceChannelSetFactory(this.meteringService));

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeDifferentUsagePoint() throws SQLException {
        CalculatedReadingRecord r1 = this.newTestInstance("0.0.2.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0");
        r1.setUsagePoint(mock(UsagePoint.class));
        CalculatedReadingRecord r2 = this.newTestInstance("0.0.2.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0");
        r2.setUsagePoint(mock(UsagePoint.class));

        // Business method
        CalculatedReadingRecord.merge(r1, r2, Instant.now(), new InstantTruncaterFactory(this.meteringService), new SourceChannelSetFactory(this.meteringService));

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalStateException.class)
    public void testMergeWithoutReadingType() throws SQLException {
        UsagePoint usagePoint = mock(UsagePoint.class);
        CalculatedReadingRecord r1 = this.newTestInstance("0.0.2.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0");
        r1.setUsagePoint(usagePoint);
        CalculatedReadingRecord r2 = this.newTestInstance("0.0.2.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0");
        r2.setUsagePoint(usagePoint);

        // Business method
        CalculatedReadingRecord.merge(r1, r2, Instant.now(), new InstantTruncaterFactory(this.meteringService), new SourceChannelSetFactory(this.meteringService));

        // Asserts: see expected exception rule
    }

    @Test
    public void testMergeOldAndMoreRecent() throws SQLException {
        String readingTypeMRID = "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0";
        UsagePoint usagePoint = mock(UsagePoint.class);
        IReadingType readingType = mock(IReadingType.class);
        when(readingType.getMRID()).thenReturn(readingTypeMRID);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        Instant old = Instant.ofEpochSecond(86400L);
        CalculatedReadingRecord r1 = this.newTestInstance(readingTypeMRID, 97L, 4L, 101L, old, "1001");   // One day
        r1.setReadingType(readingType);
        r1.setUsagePoint(usagePoint);
        Instant moreRecent = Instant.ofEpochSecond(172800L);
        CalculatedReadingRecord r2 = this.newTestInstance(readingTypeMRID, 3L, 3L, 99L, moreRecent, "1002");    // Two days
        r2.setReadingType(readingType);
        r2.setUsagePoint(usagePoint);

        // Business method
        CalculatedReadingRecord merged = CalculatedReadingRecord.merge(r1, r2, moreRecent, new InstantTruncaterFactory(this.meteringService), new SourceChannelSetFactory(this.meteringService));

        // Asserts
        assertThat(merged.getReadingType()).isEqualTo(readingType);
        assertThat(merged.getValue()).isEqualTo(BigDecimal.valueOf(100L));
        assertThat(merged.getTimeStamp()).isEqualTo(moreRecent);
        assertThat(merged.getProcessStatus()).isEqualTo(ProcessStatus.of(ProcessStatus.Flag.SUSPECT));
        assertThat(merged.getCount()).isEqualTo(200L);
        assertThat(merged.getSourceChannelSet().getSourceChannelIds()).containsOnly(1001L, 1002L);
    }

    @Test
    public void testMergeRecentAndOlder() throws SQLException {
        String readingTypeMRID = "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0";
        UsagePoint usagePoint = mock(UsagePoint.class);
        IReadingType readingType = mock(IReadingType.class);
        when(readingType.getMRID()).thenReturn(readingTypeMRID);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        Instant recent = Instant.ofEpochSecond(172800L);
        CalculatedReadingRecord r1 = this.newTestInstance(readingTypeMRID, 97L, 4L, 101L, recent, "1001");   // Two days (reading quality 4 = suspect)
        r1.setReadingType(readingType);
        r1.setUsagePoint(usagePoint);
        Instant old = Instant.ofEpochSecond(86400L);
        CalculatedReadingRecord r2 = this.newTestInstance(readingTypeMRID, 3L, 1L, 99L, old, "1001");    // One day (reading quality 3 = missing)
        r2.setReadingType(readingType);
        r2.setUsagePoint(usagePoint);

        // Business method
        CalculatedReadingRecord merged = CalculatedReadingRecord.merge(r1, r2, recent, new InstantTruncaterFactory(this.meteringService), new SourceChannelSetFactory(this.meteringService));

        // Asserts
        assertThat(merged.getReadingType()).isEqualTo(readingType);
        assertThat(merged.getValue()).isEqualTo(BigDecimal.valueOf(100L));
        assertThat(merged.getTimeStamp()).isEqualTo(recent);
        assertThat(merged.getProcessStatus()).isEqualTo(ProcessStatus.of(ProcessStatus.Flag.SUSPECT));  // 4 (suspect) = max(4, 3)
        assertThat(merged.getCount()).isEqualTo(200L);
        assertThat(merged.getSourceChannelSet().getSourceChannelIds()).containsOnly(1001L);
    }

    @Test
    public void testAtTimestamp() throws SQLException {
        String readingTypeMRID = "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0";
        UsagePoint usagePoint = mock(UsagePoint.class);
        IReadingType readingType = mock(IReadingType.class);
        when(readingType.getMRID()).thenReturn(readingTypeMRID);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        Instant may1st2016 = Instant.ofEpochMilli(1462053600000L);
        Instant june1st2016 = Instant.ofEpochMilli(1464732000000L);
        CalculatedReadingRecord r1 = this.newTestInstance(readingTypeMRID, 97L, 3L, 1L, may1st2016, "1001");
        r1.setReadingType(readingType);
        r1.setUsagePoint(usagePoint);

        // Business method
        CalculatedReadingRecord may = r1.atTimeStamp(june1st2016);

        // Asserts
        assertThat(may.getReadingType()).isEqualTo(readingType);
        assertThat(may.getValue()).isEqualTo(BigDecimal.valueOf(97L));
        assertThat(may.getTimeStamp()).isEqualTo(june1st2016);
        assertThat(may.getSourceChannelSet().getSourceChannelIds()).containsOnly(1001L);
    }

    @Test
    public void test() {

    }

    private CalculatedReadingRecord testInstance() {
        return this.newTestInstance();
    }

    private CalculatedReadingRecord newTestInstance() {
        return new CalculatedReadingRecord(new InstantTruncaterFactory(this.meteringService), new SourceChannelSetFactory(this.meteringService));
    }

    private CalculatedReadingRecord newTestInstance(String readingTypeMRID) throws SQLException {
        return this.newTestInstance(readingTypeMRID, 0, 0, 0, Instant.now(), "1");
    }

    private CalculatedReadingRecord newTestInstance(String readingTypeMRID, long value, long readingQuality, long count, Instant now, String sourceChannels) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(1)).thenReturn(readingTypeMRID);
        when(resultSet.getBigDecimal(2)).thenReturn(BigDecimal.valueOf(value));
        when(resultSet.getTimestamp(3)).thenReturn(Timestamp.from(now));
        when(resultSet.getLong(4)).thenReturn(now.toEpochMilli());
        when(resultSet.getLong(5)).thenReturn(readingQuality);
        when(resultSet.getLong(6)).thenReturn(count);
        when(resultSet.getString(7)).thenReturn(sourceChannels);
        return newTestInstance().init(resultSet, deliverablesPerMeterActivation);
    }
}

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.IReadingType;
import com.elster.jupiter.metering.impl.config.ConstantNodeImpl;
import com.elster.jupiter.metering.impl.config.ReadingTypeDeliverableNodeImpl;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CalculatedMetrologyContractDataImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-05 (12:54)
 */
@RunWith(MockitoJUnitRunner.class)
public class CalculatedMetrologyContractDataImplTest {

    private static final String MONTHLY_NET_CONSUMPTION_MRID = "13.0.0.1.4.2.12.0.0.0.0.0.0.0.0.0.72.0";

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MetrologyContract contract;
    @Mock
    private IReadingType monthlyNetConsumption;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private ReadingTypeDeliverable deliverable;

    @Mock
    private Map<MeterActivationSet, List<ReadingTypeDeliverableForMeterActivationSet>> deliverablesPerMeterActivation;

    @Before
    public void initializeMocks() {
        ZoneId zoneId = this.testZoneId();
        when(this.usagePoint.getZoneId()).thenReturn(zoneId);
        when(this.monthlyNetConsumption.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);
        when(this.monthlyNetConsumption.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(this.monthlyNetConsumption.getFlowDirection()).thenReturn(FlowDirection.NET);
        when(this.monthlyNetConsumption.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(this.monthlyNetConsumption.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(this.monthlyNetConsumption.getMRID()).thenReturn(MONTHLY_NET_CONSUMPTION_MRID);
        when(this.monthlyNetConsumption.toQuantity(BigDecimal.ZERO)).thenReturn(Quantity.create(BigDecimal.ZERO, "Wh"));
        when(this.monthlyNetConsumption.toQuantity(BigDecimal.TEN)).thenReturn(Quantity.create(BigDecimal.TEN, "Wh"));
        when(this.usagePoint.getMeterActivation(any(Instant.class))).thenReturn(Optional.of(this.meterActivation));
        when(this.meterActivation.getChannelsContainer()).thenReturn(this.channelsContainer);
        when(this.channelsContainer.getZoneId()).thenReturn(zoneId);
        when(this.deliverable.getReadingType()).thenReturn(this.monthlyNetConsumption);
        Formula formula = mock(Formula.class);
        when(formula.getExpressionNode()).thenReturn(new ReadingTypeDeliverableNodeImpl(this.deliverable));
        when(this.deliverable.getFormula()).thenReturn(formula);
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(this.deliverable));
    }

    private ZoneId testZoneId() {
        return ZoneId.of("Antarctica/McMurdo");
    }

    @Test
    public void usagePointIsCopiedInConstructor() {
        CalculatedReadingRecord r1 = mock(CalculatedReadingRecord.class);

        when(r1.getTimeStamp()).thenReturn(instant(2016, Month.MARCH, 1));
        CalculatedReadingRecord r2 = mock(CalculatedReadingRecord.class);
        when(r2.getTimeStamp()).thenReturn(instant(2016, Month.APRIL, 1));
        Map<ReadingType, List<CalculatedReadingRecord>> recordsByReadingType = new HashMap<>();
        recordsByReadingType.put(this.monthlyNetConsumption, Arrays.asList(r1, r2));

        // Business method
        CalculatedMetrologyContractDataImpl contractData = new CalculatedMetrologyContractDataImpl(this.usagePoint, this.contract, Range.all(), recordsByReadingType);

        // Asserts
        assertThat(contractData.getUsagePoint()).isEqualTo(this.usagePoint);
    }

    @Test
    public void contractIsCopiedInConstructor() {
        CalculatedReadingRecord r1 = mock(CalculatedReadingRecord.class);
        when(r1.getTimeStamp()).thenReturn(instant(2016, Month.MARCH, 1));
        CalculatedReadingRecord r2 = mock(CalculatedReadingRecord.class);
        when(r2.getTimeStamp()).thenReturn(instant(2016, Month.APRIL, 1));
        Map<ReadingType, List<CalculatedReadingRecord>> recordsByReadingType = new HashMap<>();
        recordsByReadingType.put(this.monthlyNetConsumption, Arrays.asList(r1, r2));

        // Business method
        CalculatedMetrologyContractDataImpl contractData = new CalculatedMetrologyContractDataImpl(this.usagePoint, this.contract, Range.all(), recordsByReadingType);

        // Asserts
        assertThat(contractData.getMetrologyContract()).isEqualTo(this.contract);
    }

    @Test
    public void constructorInjectUsagePointsIntoRecords() {
        CalculatedReadingRecord r1 = mock(CalculatedReadingRecord.class);
        when(r1.getTimeStamp()).thenReturn(instant(2016, Month.MARCH, 1));
        CalculatedReadingRecord r2 = mock(CalculatedReadingRecord.class);
        when(r2.getTimeStamp()).thenReturn(instant(2016, Month.APRIL, 1));
        Map<ReadingType, List<CalculatedReadingRecord>> recordsByReadingType = new HashMap<>();
        recordsByReadingType.put(this.monthlyNetConsumption, Arrays.asList(r1, r2));

        // Business method
        new CalculatedMetrologyContractDataImpl(this.usagePoint, this.contract, Range.all(), recordsByReadingType);

        // Asserts
        verify(r1).setUsagePoint(this.usagePoint);
        verify(r2).setUsagePoint(this.usagePoint);
    }

    @Test
    public void noMergeForMonthlyBoundaryRecords() {
        CalculatedReadingRecord r1 = mock(CalculatedReadingRecord.class);
        when(r1.getTimeStamp()).thenReturn(instant(2016, Month.MARCH, 1));
        CalculatedReadingRecord r2 = mock(CalculatedReadingRecord.class);
        when(r2.getTimeStamp()).thenReturn(instant(2016, Month.APRIL, 1));
        Map<ReadingType, List<CalculatedReadingRecord>> recordsByReadingType = new HashMap<>();
        recordsByReadingType.put(this.monthlyNetConsumption, Arrays.asList(r1, r2));

        // Business method
        CalculatedMetrologyContractDataImpl contractData = new CalculatedMetrologyContractDataImpl(this.usagePoint, this.contract, Range.all(), recordsByReadingType);

        // Asserts
        assertThat(contractData.getCalculatedDataFor(this.deliverable)).containsSequence(r1, r2);
    }

    @Test
    public void mergeForMidMonthRecordsInOrder() throws SQLException {
        CalculatedReadingRecord r1 = this.newRecord(MONTHLY_NET_CONSUMPTION_MRID, instant(2016, Month.MARCH, 15));
        r1.setUsagePoint(this.usagePoint);
        r1.setReadingType(this.monthlyNetConsumption);
        CalculatedReadingRecord r2 = this.newRecord(MONTHLY_NET_CONSUMPTION_MRID, instant(2016, Month.APRIL, 1));
        r2.setUsagePoint(this.usagePoint);
        r2.setReadingType(this.monthlyNetConsumption);
        Map<ReadingType, List<CalculatedReadingRecord>> recordsByReadingType = new HashMap<>();
        recordsByReadingType.put(this.monthlyNetConsumption, Arrays.asList(r1, r2));

        // Business method
        CalculatedMetrologyContractDataImpl contractData = new CalculatedMetrologyContractDataImpl(this.usagePoint, this.contract, Range.all(), recordsByReadingType);

        // Asserts
        List<? extends BaseReadingRecord> readingRecords = contractData.getCalculatedDataFor(this.deliverable);
        assertThat(readingRecords).hasSize(1);
        BaseReadingRecord readingRecord = readingRecords.get(0);
        assertThat(readingRecord.getTimeStamp()).isEqualTo(instant(2016, Month.APRIL, 1));
    }

    @Test
    public void mergeForMidMonthRecordsInReverseOrder() throws SQLException {
        CalculatedReadingRecord r1 = this.newRecord(MONTHLY_NET_CONSUMPTION_MRID, instant(2016, Month.MARCH, 15));
        r1.setUsagePoint(this.usagePoint);
        r1.setReadingType(this.monthlyNetConsumption);
        CalculatedReadingRecord r2 = this.newRecord(MONTHLY_NET_CONSUMPTION_MRID, instant(2016, Month.APRIL, 1));
        r2.setUsagePoint(this.usagePoint);
        r2.setReadingType(this.monthlyNetConsumption);
        Map<ReadingType, List<CalculatedReadingRecord>> recordsByReadingType = new HashMap<>();
        recordsByReadingType.put(this.monthlyNetConsumption, Arrays.asList(r2, r1));

        // Business method
        CalculatedMetrologyContractDataImpl contractData = new CalculatedMetrologyContractDataImpl(this.usagePoint, this.contract, Range.all(), recordsByReadingType);

        // Asserts
        List<? extends BaseReadingRecord> readingRecords = contractData.getCalculatedDataFor(this.deliverable);
        assertThat(readingRecords).hasSize(1);
        BaseReadingRecord readingRecord = readingRecords.get(0);
        assertThat(readingRecord.getTimeStamp()).isEqualTo(instant(2016, Month.APRIL, 1));
    }

    @Test
    public void mergeWithAllMidMonthRecords() throws SQLException {
        CalculatedReadingRecord r1 = this.newRecord(MONTHLY_NET_CONSUMPTION_MRID, instant(2016, Month.MARCH, 10));
        r1.setUsagePoint(this.usagePoint);
        r1.setReadingType(this.monthlyNetConsumption);
        CalculatedReadingRecord r2 = this.newRecord(MONTHLY_NET_CONSUMPTION_MRID, instant(2016, Month.MARCH, 25));
        r2.setUsagePoint(this.usagePoint);
        r2.setReadingType(this.monthlyNetConsumption);
        Map<ReadingType, List<CalculatedReadingRecord>> recordsByReadingType = new HashMap<>();
        recordsByReadingType.put(this.monthlyNetConsumption, Arrays.asList(r1, r2));

        // Business method
        CalculatedMetrologyContractDataImpl contractData = new CalculatedMetrologyContractDataImpl(this.usagePoint, this.contract, Range.all(), recordsByReadingType);

        // Asserts
        List<? extends BaseReadingRecord> readingRecords = contractData.getCalculatedDataFor(this.deliverable);
        assertThat(readingRecords).hasSize(1);
        BaseReadingRecord readingRecord = readingRecords.get(0);
        assertThat(readingRecord.getTimeStamp()).isEqualTo(instant(2016, Month.APRIL, 1));
    }

    @Test
    public void constantsOnly() throws SQLException {
        Instant april1st2016 = instant(2016, Month.APRIL, 1);
        Instant may1st2016 = instant(2016, Month.MAY, 1);
        Instant june1st2016 = instant(2016, Month.JUNE, 1);
        Instant july1st2016 = instant(2016, Month.JULY, 1);
        CalculatedReadingRecord r1 = this.newRecord(MONTHLY_NET_CONSUMPTION_MRID, BigDecimal.TEN, april1st2016);
        r1.setUsagePoint(this.usagePoint);
        r1.setReadingType(this.monthlyNetConsumption);
        Map<ReadingType, List<CalculatedReadingRecord>> recordsByReadingType = new HashMap<>();
        recordsByReadingType.put(this.monthlyNetConsumption, Collections.singletonList(r1));
        Formula formula = mock(Formula.class);
        when(formula.getExpressionNode()).thenReturn(new ConstantNodeImpl(BigDecimal.TEN));
        when(this.deliverable.getFormula()).thenReturn(formula);
        Range<Instant> period = Range.openClosed(april1st2016, july1st2016);   // Expecting monthly consumption of April, May and June

        // Business method
        CalculatedMetrologyContractDataImpl contractData = new CalculatedMetrologyContractDataImpl(this.usagePoint, this.contract, period, recordsByReadingType);

        // Asserts
        List<? extends BaseReadingRecord> readingRecords = contractData.getCalculatedDataFor(this.deliverable);
        assertThat(readingRecords).hasSize(3);
        BaseReadingRecord aprilRecord = readingRecords.get(0);
        assertThat(aprilRecord.getTimeStamp()).isEqualTo(may1st2016);
        assertThat(aprilRecord.getQuantity(0)).isEqualTo(Quantity.create(BigDecimal.TEN, "Wh"));
        BaseReadingRecord mayRecord = readingRecords.get(1);
        assertThat(mayRecord.getTimeStamp()).isEqualTo(june1st2016);
        assertThat(mayRecord.getQuantity(0)).isEqualTo(Quantity.create(BigDecimal.TEN, "Wh"));
        BaseReadingRecord juneRecord = readingRecords.get(2);
        assertThat(juneRecord.getTimeStamp()).isEqualTo(july1st2016);
        assertThat(juneRecord.getQuantity(0)).isEqualTo(Quantity.create(BigDecimal.TEN, "Wh"));
    }

    private CalculatedReadingRecord newRecord(String readingTypeMRID, Instant now) throws SQLException {
        return this.newRecord(readingTypeMRID, BigDecimal.ZERO, now);
    }

    private CalculatedReadingRecord newRecord(String readingTypeMRID, BigDecimal value, Instant now) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(1)).thenReturn(readingTypeMRID);
        when(resultSet.getBigDecimal(2)).thenReturn(value);
        when(resultSet.getTimestamp(3)).thenReturn(Timestamp.from(now));
        when(resultSet.getLong(4)).thenReturn(now.toEpochMilli());
        when(resultSet.getLong(5)).thenReturn(0L);
        when(resultSet.getLong(6)).thenReturn(30L);
        return new CalculatedReadingRecord().init(resultSet, deliverablesPerMeterActivation);
    }

    private Instant instant(int year, Month month, int dayOfMonth) {
        return LocalDate.of(year, month, dayOfMonth).atStartOfDay().atZone(this.testZoneId()).toInstant();
    }

}
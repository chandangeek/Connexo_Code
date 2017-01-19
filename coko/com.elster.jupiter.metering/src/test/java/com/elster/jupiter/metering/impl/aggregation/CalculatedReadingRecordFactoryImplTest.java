package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.impl.IReadingType;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CalculatedReadingRecordFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-26 (15:30)
 */
@RunWith(MockitoJUnitRunner.class)
public class CalculatedReadingRecordFactoryImplTest {

    private static final String FIFTEEN_MINS_NET_CONSUMPTION_MRID = "0.0.2.1.4.2.12.0.0.0.0.0.0.0.0.0.72.0";
    private static final String MONTHLY_NET_CONSUMPTION_MRID = "13.0.0.1.4.2.12.0.0.0.0.0.0.0.0.0.72.0";
    private static final long MY_FAVOURITE_PRIME_NUMBER = 97L;
    private static Instant JAN_1_2016_UTC = Instant.ofEpochMilli(1451606400000L);

    @Mock
    private DataModel dataModel;
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
        when(this.dataModel.getInstance(CalculatedReadingRecord.class))
                .thenAnswer(invocationOnMock -> new CalculatedReadingRecord(new InstantTruncaterFactory(this.meteringService), new SourceChannelSetFactory(this.meteringService)));
        when(this.fifteenMinutesNetConsumption.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(this.fifteenMinutesNetConsumption.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(this.fifteenMinutesNetConsumption.getFlowDirection()).thenReturn(FlowDirection.NET);
        when(this.fifteenMinutesNetConsumption.getMRID()).thenReturn(FIFTEEN_MINS_NET_CONSUMPTION_MRID);
        when(this.fifteenMinutesNetConsumption.toQuantity(BigDecimal.TEN)).thenReturn(Quantity.create(BigDecimal.TEN, 3, "Wh"));
        when(this.fifteenMinutesNetConsumption.toQuantity(BigDecimal.valueOf(MY_FAVOURITE_PRIME_NUMBER))).thenReturn(Quantity.create(BigDecimal.valueOf(MY_FAVOURITE_PRIME_NUMBER), 3, "Wh"));
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
    public void emptyResultSet() throws SQLException {
        when(this.resultSet.next()).thenReturn(false);

        // Business method
        Map<ReadingType, List<CalculatedReadingRecord>> recordsByReadingType = this.testInstance().consume(this.resultSet, deliverablesPerMeterActivation);

        // Asserts
        verify(this.resultSet).next();
        verify(this.resultSet, never()).getLong(anyInt());
        verify(this.resultSet, never()).getString(anyInt());
        verify(this.resultSet, never()).getTimestamp(anyInt());
        verify(this.resultSet, never()).getBigDecimal(anyInt());
        assertThat(recordsByReadingType).isEmpty();
    }

    @Test(expected = UnderlyingSQLFailedException.class)
    public void sqlExceptionIsWrapped() throws SQLException {
        when(this.resultSet.next()).thenReturn(true);
        doThrow(SQLException.class).when(this.resultSet).getString(anyInt());

        // Business method
        this.testInstance().consume(this.resultSet, deliverablesPerMeterActivation);

        // Asserts: see expected exception rule
    }

    @Test
    public void singleIntervalTwoReadingTypes() throws SQLException {
        when(this.resultSet.next()).thenReturn(true, true, false);
        when(this.resultSet.getString(1)).thenReturn(FIFTEEN_MINS_NET_CONSUMPTION_MRID, FIFTEEN_MINS_NET_CONSUMPTION_MRID, MONTHLY_NET_CONSUMPTION_MRID, MONTHLY_NET_CONSUMPTION_MRID); // Remember: once for the factory, once for the entity
        BigDecimal expectedValue1 = BigDecimal.TEN;
        Quantity expectedQuantity1 = Quantity.create(expectedValue1, 3,  "Wh");
        BigDecimal expectedValue2 = BigDecimal.valueOf(MY_FAVOURITE_PRIME_NUMBER);
        Quantity expectedQuantity2 = Quantity.create(expectedValue2, 3,  "Wh");
        when(this.resultSet.getBigDecimal(2)).thenReturn(expectedValue1, expectedValue2);
        Timestamp ts1 = Timestamp.from(JAN_1_2016_UTC);
        when(this.resultSet.getTimestamp(3)).thenReturn(ts1);
        when(this.resultSet.getLong(4)).thenReturn(ts1.getTime());
        when(this.resultSet.getLong(5)).thenReturn(1L);
        when(this.resultSet.getString(7)).thenReturn("1001");

        long expectedCountFor15minRecord = 1L;
        long expectedCountForMonthlyRecord = 1L;
        when(this.resultSet.getLong(6)).thenReturn(expectedCountFor15minRecord, expectedCountForMonthlyRecord);

        // Business method
        Map<ReadingType, List<CalculatedReadingRecord>> recordsByReadingType = this.testInstance().consume(this.resultSet, deliverablesPerMeterActivation);

        // Asserts
        verify(this.resultSet, times(3)).next();
        assertThat(recordsByReadingType).hasSize(2);
        assertThat(recordsByReadingType).containsKey(fifteenMinutesNetConsumption);
        List<CalculatedReadingRecord> fifteenMinuteRecords = recordsByReadingType.get(fifteenMinutesNetConsumption);
        assertThat(fifteenMinuteRecords).hasSize(1);
        CalculatedReadingRecord fifteenMinuteRecord = fifteenMinuteRecords.get(0);
        assertThat(fifteenMinuteRecord.getLocalDate()).isEqualTo(ts1);
        assertThat(fifteenMinuteRecord.getTimeStamp()).isEqualTo(ts1.toInstant());
        assertThat(fifteenMinuteRecord.getReadingType()).isEqualTo(fifteenMinutesNetConsumption);
        assertThat(fifteenMinuteRecord.getReadingTypes()).containsOnly(fifteenMinutesNetConsumption);
        assertThat(fifteenMinuteRecord.getReadingType(0)).isEqualTo(fifteenMinutesNetConsumption);
        assertThat(fifteenMinuteRecord.getQuantities()).hasSize(1);
        assertThat(fifteenMinuteRecord.getQuantity(fifteenMinutesNetConsumption)).isEqualTo(expectedQuantity1);
        assertThat(fifteenMinuteRecord.getQuantity(monthlyNetConsumption)).isNull();
        assertThat(fifteenMinuteRecord.getProcessStatus()).isEqualTo(new ProcessStatus(0).with(ProcessStatus.Flag.EDITED, ProcessStatus.Flag.ESTIMATED)); //readingquality = 1 (estimated or edited)
        assertThat(fifteenMinuteRecord.getCount()).isEqualTo(expectedCountFor15minRecord);
        assertThat(recordsByReadingType).containsKey(monthlyNetConsumption);
        List<CalculatedReadingRecord> monthlyRecords = recordsByReadingType.get(monthlyNetConsumption);
        assertThat(monthlyRecords).hasSize(1);
        CalculatedReadingRecord monthlyRecord = monthlyRecords.get(0);
        assertThat(monthlyRecord.getLocalDate()).isEqualTo(ts1);
        assertThat(monthlyRecord.getTimeStamp()).isEqualTo(ts1.toInstant());
        assertThat(monthlyRecord.getReadingType()).isEqualTo(monthlyNetConsumption);
        assertThat(monthlyRecord.getReadingTypes()).containsOnly(monthlyNetConsumption);
        assertThat(monthlyRecord.getReadingType(0)).isEqualTo(monthlyNetConsumption);
        assertThat(monthlyRecord.getQuantities()).hasSize(1);
        assertThat(monthlyRecord.getQuantity(monthlyNetConsumption)).isEqualTo(expectedQuantity2);
        assertThat(monthlyRecord.getQuantity(fifteenMinutesNetConsumption)).isNull();
        assertThat(monthlyRecord.getProcessStatus()).isEqualTo(new ProcessStatus(0).with(ProcessStatus.Flag.EDITED, ProcessStatus.Flag.ESTIMATED));
        assertThat(monthlyRecord.getCount()).isEqualTo(expectedCountForMonthlyRecord);
    }

    @Test
    public void multipleIntervalsForSingleReadingType() throws SQLException {
        when(this.resultSet.next()).thenReturn(true, true, false);
        when(this.resultSet.getString(1)).thenReturn(FIFTEEN_MINS_NET_CONSUMPTION_MRID, FIFTEEN_MINS_NET_CONSUMPTION_MRID); // once for the factory, once for the entity
        BigDecimal expectedValue1 = BigDecimal.TEN;
        Quantity expectedQuantity1 = Quantity.create(expectedValue1, 3,  "Wh");
        BigDecimal expectedValue2 = BigDecimal.valueOf(MY_FAVOURITE_PRIME_NUMBER);
        Quantity expectedQuantity2 = Quantity.create(expectedValue2, 3,  "Wh");
        when(this.resultSet.getBigDecimal(2)).thenReturn(expectedValue1, expectedValue2);
        Timestamp ts1 = Timestamp.from(JAN_1_2016_UTC);
        Timestamp ts2 = Timestamp.from(JAN_1_2016_UTC.plus(Duration.ofMinutes(15)));
        when(this.resultSet.getTimestamp(3)).thenReturn(ts1, ts2);
        when(this.resultSet.getLong(4)).thenReturn(ts1.getTime(), ts2.getTime());
        when(this.resultSet.getLong(5)).thenReturn(4L, 3L);
        when(this.resultSet.getLong(6)).thenReturn(1L, 1L);
        when(this.resultSet.getString(7)).thenReturn("1001");

        // Business method
        Map<ReadingType, List<CalculatedReadingRecord>> recordsByReadingType = this.testInstance().consume(this.resultSet, deliverablesPerMeterActivation);

        // Asserts
        verify(this.resultSet, times(3)).next();
        assertThat(recordsByReadingType).hasSize(1);
        assertThat(recordsByReadingType).containsKey(fifteenMinutesNetConsumption);
        List<CalculatedReadingRecord> fifteenMinuteRecords = recordsByReadingType.get(fifteenMinutesNetConsumption);
        assertThat(fifteenMinuteRecords).hasSize(2);
        CalculatedReadingRecord readingRecord1 = fifteenMinuteRecords.get(0);
        assertThat(readingRecord1.getLocalDate()).isEqualTo(ts1);
        assertThat(readingRecord1.getTimeStamp()).isEqualTo(ts1.toInstant());
        assertThat(readingRecord1.getReadingType()).isEqualTo(fifteenMinutesNetConsumption);
        assertThat(readingRecord1.getReadingTypes()).containsOnly(fifteenMinutesNetConsumption);
        assertThat(readingRecord1.getReadingType(0)).isEqualTo(fifteenMinutesNetConsumption);
        assertThat(readingRecord1.getQuantities()).hasSize(1);
        assertThat(readingRecord1.getQuantity(fifteenMinutesNetConsumption)).isEqualTo(expectedQuantity1);
        assertThat(readingRecord1.getProcessStatus()).isEqualTo(ProcessStatus.of(ProcessStatus.Flag.SUSPECT));
        assertThat(readingRecord1.getCount()).isEqualTo(1L);
        CalculatedReadingRecord readingRecord2 = fifteenMinuteRecords.get(1);
        assertThat(readingRecord2.getLocalDate()).isEqualTo(ts2);
        assertThat(readingRecord2.getTimeStamp()).isEqualTo(ts2.toInstant());
        assertThat(readingRecord2.getReadingType()).isEqualTo(fifteenMinutesNetConsumption);
        assertThat(readingRecord2.getReadingTypes()).containsOnly(fifteenMinutesNetConsumption);
        assertThat(readingRecord2.getReadingType(0)).isEqualTo(fifteenMinutesNetConsumption);
        assertThat(readingRecord2.getQuantities()).hasSize(1);
        assertThat(readingRecord2.getQuantity(fifteenMinutesNetConsumption)).isEqualTo(expectedQuantity2);
        assertThat(readingRecord2.getProcessStatus()).isEqualTo(new ProcessStatus(0));
        assertThat(readingRecord2.getCount()).isEqualTo(1L);
    }

    private CalculatedReadingRecordFactoryImpl testInstance() {
        return new CalculatedReadingRecordFactoryImpl(this.dataModel, this.meteringService);
    }
}

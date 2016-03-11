package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.impl.IReadingType;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link AggregatedReadingRecord} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-26 (14:31)
 */
@RunWith(MockitoJUnitRunner.class)
public class AggregatedReadingRecordTest {

    public static final String FIFTEEN_MINS_NET_CONSUMPTION_MRID = "0.0.2.1.4.2.12.0.0.0.0.0.0.0.0.0.72.0";
    public static final String MONTHLY_NET_CONSUMPTION_MRID = "13.0.0.1.4.2.12.0.0.0.0.0.0.0.0.0.72.0";
    public static final long MY_FAVOURITE_PRIME_NUMBER = 97L;
    private static Instant JAN_1_2016_UTC = Instant.ofEpochMilli(1451606400000L);

    @Mock
    private MeteringService meteringService;
    @Mock
    private IReadingType fifteenMinutesNetConsumption;
    @Mock
    private IReadingType monthlyNetConsumption;
    @Mock
    private ResultSet resultSet;

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
        AggregatedReadingRecord testInstance = this.testInstance();
        when(this.resultSet.getString(1)).thenReturn(FIFTEEN_MINS_NET_CONSUMPTION_MRID);
        BigDecimal expectedValue = BigDecimal.TEN;
        Quantity expectedQuantity = Quantity.create(expectedValue, 3,  "Wh");
        when(this.resultSet.getBigDecimal(2)).thenReturn(expectedValue);
        Timestamp jan1st2016 = Timestamp.from(JAN_1_2016_UTC);
        Instant expectedIntervalStart = JAN_1_2016_UTC.minus(Duration.ofMinutes(15));
        when(this.resultSet.getTimestamp(3)).thenReturn(jan1st2016);
        when(this.resultSet.getLong(4)).thenReturn(0L);
        ProcessStatus expectedProcessStatus = new ProcessStatus(0);

        //Business method
        testInstance.init(this.resultSet);

        // Asserts
        assertThat(testInstance.getTimeStamp()).isEqualTo(jan1st2016.toInstant());
        assertThat(testInstance.getReadingType()).isEqualTo(fifteenMinutesNetConsumption);
        assertThat(testInstance.getReadingTypes()).hasSize(1);
        assertThat(testInstance.getValue()).isEqualTo(expectedValue);
        assertThat(testInstance.getQuantities()).hasSize(1);
        assertThat(testInstance.getQuantity(fifteenMinutesNetConsumption)).isEqualTo(expectedQuantity);
        assertThat(testInstance.getTimePeriod()).contains(Range.openClosed(expectedIntervalStart, jan1st2016.toInstant()));
        assertThat(testInstance.getProcesStatus()).isEqualTo(expectedProcessStatus);
    }

    @Test(expected = UnderlyingSQLFailedException.class)
    public void sqlExceptionIsWrapped() throws SQLException {
        AggregatedReadingRecord testInstance = this.testInstance();
        doThrow(SQLException.class).when(this.resultSet).getString(anyInt());
        doThrow(SQLException.class).when(this.resultSet).getTimestamp(anyInt());
        doThrow(SQLException.class).when(this.resultSet).getBigDecimal(anyInt());
        doThrow(SQLException.class).when(this.resultSet).getLong(anyInt());

        //Business method
        testInstance.init(this.resultSet);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalStateException.class)
    public void addWithoutInit() throws SQLException {
        AggregatedReadingRecord testInstance = this.testInstance();
        when(this.resultSet.getString(1)).thenReturn(FIFTEEN_MINS_NET_CONSUMPTION_MRID);
        BigDecimal expectedValue = BigDecimal.TEN;
        when(this.resultSet.getBigDecimal(2)).thenReturn(expectedValue);
        Timestamp jan1st2016 = Timestamp.from(JAN_1_2016_UTC);
        when(this.resultSet.getTimestamp(3)).thenReturn(jan1st2016);
        when(this.resultSet.getLong(4)).thenReturn(0L);

        //Business method
        testInstance.addFrom(this.resultSet);

        // Asserts: see expected exception rule
    }

    @Test
    public void initAndAdd() throws SQLException {
        AggregatedReadingRecord testInstance = this.testInstance();
        when(this.resultSet.getString(1)).thenReturn(FIFTEEN_MINS_NET_CONSUMPTION_MRID, MONTHLY_NET_CONSUMPTION_MRID);
        BigDecimal expectedValue1 = BigDecimal.TEN;
        Quantity expectedQuantity1 = Quantity.create(expectedValue1, 3,  "Wh");
        BigDecimal expectedValue2 = BigDecimal.valueOf(MY_FAVOURITE_PRIME_NUMBER);
        Quantity expectedQuantity2 = Quantity.create(expectedValue2, 3,  "Wh");
        when(this.resultSet.getBigDecimal(2)).thenReturn(expectedValue1, expectedValue2);
        Timestamp ts1 = Timestamp.from(JAN_1_2016_UTC);
        Instant expectedPeriodStart = JAN_1_2016_UTC.minus(Duration.ofMinutes(15));
        when(this.resultSet.getTimestamp(3)).thenReturn(ts1);
        when(this.resultSet.getLong(4)).thenReturn(1L, 2L);
        ProcessStatus expectedProcessStatus = new ProcessStatus(3L);

        //Business methods
        testInstance.init(this.resultSet);
        testInstance.addFrom(this.resultSet);

        // Asserts
        assertThat(testInstance.getTimeStamp()).isEqualTo(ts1.toInstant());
        assertThat(testInstance.getReadingTypes()).containsOnly(fifteenMinutesNetConsumption, monthlyNetConsumption);
        assertThat(testInstance.getQuantities()).hasSize(2);
        assertThat(testInstance.getQuantity(fifteenMinutesNetConsumption)).isEqualTo(expectedQuantity1);
        assertThat(testInstance.getQuantity(monthlyNetConsumption)).isEqualTo(expectedQuantity2);
        assertThat(testInstance.getTimePeriod()).contains(Range.openClosed(expectedPeriodStart, ts1.toInstant()));
        assertThat(testInstance.getProcesStatus()).isEqualTo(expectedProcessStatus);
    }

    @Test(expected = IllegalStateException.class)
    public void initTwice() throws SQLException {
        AggregatedReadingRecord testInstance = this.testInstance();
        when(this.resultSet.getString(1)).thenReturn(FIFTEEN_MINS_NET_CONSUMPTION_MRID, MONTHLY_NET_CONSUMPTION_MRID);
        BigDecimal expectedValue1 = BigDecimal.TEN;
        BigDecimal expectedValue2 = BigDecimal.valueOf(MY_FAVOURITE_PRIME_NUMBER);
        when(this.resultSet.getBigDecimal(2)).thenReturn(expectedValue1, expectedValue2);
        Timestamp ts1 = Timestamp.from(JAN_1_2016_UTC);
        when(this.resultSet.getTimestamp(3)).thenReturn(ts1);
        when(this.resultSet.getLong(4)).thenReturn(1L, 2L);
        testInstance.init(this.resultSet);

        //Business methods
        testInstance.init(this.resultSet);

        // Asserts: see expected exception rule
    }

    private AggregatedReadingRecord testInstance() {
        return new AggregatedReadingRecord(this.meteringService);
    }

}
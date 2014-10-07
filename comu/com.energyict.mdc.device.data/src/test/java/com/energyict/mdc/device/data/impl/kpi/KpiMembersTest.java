package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.util.time.Interval;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link KpiMembers} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-07 (14:31)
 */
public class KpiMembersTest {

    private static final int INTERVAL_START_MILLIS = 1000000;
    private static final int INTERVAL_END_MILLIS = INTERVAL_START_MILLIS + DateTimeConstants.MILLIS_PER_DAY;
    private static final int KPI_INTERVAL_MINUTES = 15;
    private static final BigDecimal TARGET = BigDecimal.TEN;

    @Test
    public void getScoresForMonitoredStatusses () {
        KpiMember waiting = mockedKpiMember(TaskStatus.Waiting, new BigDecimal("97"), new BigDecimal("83"), new BigDecimal("67"));
        KpiMember pending = mockedKpiMember(TaskStatus.Pending, new BigDecimal("3"), new BigDecimal("13"), new BigDecimal("23"));
        KpiMember failed = mockedKpiMember(TaskStatus.Failed, new BigDecimal("0"), new BigDecimal("4"), new BigDecimal("10"));
        KpiMembers kpiMembers = new KpiMembers(Arrays.asList(waiting, pending, failed));

        // Business method
        List<DataCollectionKpiScore> scores = kpiMembers.getScores(this.testInterval());

        // Asserts
        assertThat(scores).hasSize(3);
        DataCollectionKpiScore first = scores.get(0);
        assertThat(first.getTarget()).isEqualTo(BigDecimal.TEN);
        assertThat(first.getValue(TaskStatus.Waiting)).isEqualTo(new BigDecimal("97"));
        assertThat(first.getValue(TaskStatus.Pending)).isEqualTo(new BigDecimal("3"));
        assertThat(first.getValue(TaskStatus.Failed)).isEqualTo(BigDecimal.ZERO);
        DataCollectionKpiScore second = scores.get(1);
        assertThat(second.getValue(TaskStatus.Waiting)).isEqualTo(new BigDecimal("83"));
        assertThat(second.getValue(TaskStatus.Pending)).isEqualTo(new BigDecimal("13"));
        assertThat(second.getValue(TaskStatus.Failed)).isEqualTo(new BigDecimal("4"));
        DataCollectionKpiScore third = scores.get(2);
        assertThat(third.getValue(TaskStatus.Waiting)).isEqualTo(new BigDecimal("67"));
        assertThat(third.getValue(TaskStatus.Pending)).isEqualTo(new BigDecimal("23"));
        assertThat(third.getValue(TaskStatus.Failed)).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void getScoresForUnorderedMonitoredStatusses () {
        KpiMember waiting = mockedKpiMember(TaskStatus.Waiting, new BigDecimal("97"), new BigDecimal("83"), new BigDecimal("67"));
        KpiMember pending = mockedKpiMember(TaskStatus.Pending, new BigDecimal("3"), new BigDecimal("13"), new BigDecimal("23"));
        KpiMember failed = mockedKpiMember(TaskStatus.Failed, new BigDecimal("0"), new BigDecimal("4"), new BigDecimal("10"));
        KpiMembers kpiMembers = new KpiMembers(Arrays.asList(failed, pending, waiting));

        // Business method
        List<DataCollectionKpiScore> scores = kpiMembers.getScores(this.testInterval());

        // Asserts
        assertThat(scores).hasSize(3);
        DataCollectionKpiScore first = scores.get(0);
        assertThat(first.getTarget()).isEqualTo(BigDecimal.TEN);
        assertThat(first.getValue(TaskStatus.Waiting)).isEqualTo(new BigDecimal("97"));
        assertThat(first.getValue(TaskStatus.Pending)).isEqualTo(new BigDecimal("3"));
        assertThat(first.getValue(TaskStatus.Failed)).isEqualTo(BigDecimal.ZERO);
        DataCollectionKpiScore second = scores.get(1);
        assertThat(second.getValue(TaskStatus.Waiting)).isEqualTo(new BigDecimal("83"));
        assertThat(second.getValue(TaskStatus.Pending)).isEqualTo(new BigDecimal("13"));
        assertThat(second.getValue(TaskStatus.Failed)).isEqualTo(new BigDecimal("4"));
        DataCollectionKpiScore third = scores.get(2);
        assertThat(third.getValue(TaskStatus.Waiting)).isEqualTo(new BigDecimal("67"));
        assertThat(third.getValue(TaskStatus.Pending)).isEqualTo(new BigDecimal("23"));
        assertThat(third.getValue(TaskStatus.Failed)).isEqualTo(BigDecimal.TEN);
    }

    private Interval testInterval() {
        return Interval.of(Instant.ofEpochMilli(INTERVAL_START_MILLIS), Instant.ofEpochMilli(INTERVAL_END_MILLIS));
    }

    private KpiMember mockedKpiMember(TaskStatus taskStatus, BigDecimal... scores) {
        DateTime timestamp = new DateTime(INTERVAL_START_MILLIS).plusMinutes(KPI_INTERVAL_MINUTES);
        KpiMember kpiMember = mock(KpiMember.class);
        when(kpiMember.getName()).thenReturn(taskStatus.name());
        when(kpiMember.getTarget(any(Date.class))).thenReturn(TARGET);
        List<KpiEntry> kpiEntries = Stream.of(scores).
                map(s -> {
                    KpiEntry kpiEntry = mock(KpiEntry.class);
                    when(kpiEntry.getScore()).thenReturn(s);
                    when(kpiEntry.getTarget()).thenReturn(TARGET);
                    when(kpiEntry.getTimestamp()).thenReturn(timestamp.toDate());
                    timestamp.plusMinutes(KPI_INTERVAL_MINUTES);
                    return kpiEntry;
                }).
                collect(Collectors.toList());
        doReturn(kpiEntries).when(kpiMember).getScores(this.testInterval());
        return kpiMember;
    }

}
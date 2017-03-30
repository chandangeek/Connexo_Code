/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.util.Ranges;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;

import com.google.common.collect.Range;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

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
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Test
    public void getScoresForMonitoredStatusses () {
        KpiMember total = mockedKpiMember(MonitoredTaskStatus.Total, HUNDRED, HUNDRED, HUNDRED);
        KpiMember success = mockedKpiMember(MonitoredTaskStatus.Success, new BigDecimal("97"), new BigDecimal("83"), new BigDecimal("67"));
        KpiMember ongoing = mockedKpiMember(MonitoredTaskStatus.Ongoing, new BigDecimal("3"),  new BigDecimal("13"), new BigDecimal("23"));
        KpiMember failed = mockedKpiMember(MonitoredTaskStatus.Failed,   new BigDecimal("0"),  new BigDecimal("4"),  new BigDecimal("10"));
        KpiMembers kpiMembers = new KpiMembers(Arrays.asList(total, success, ongoing, failed));

        // Business method
        List<DataCollectionKpiScore> scores = kpiMembers.getScores(this.testInterval());

        // Asserts
        assertThat(scores).hasSize(3);
        DataCollectionKpiScore first = scores.get(0);
        assertThat(first.getTarget()).isEqualTo(BigDecimal.TEN);
        assertThat(first.getSuccess()).isEqualTo(new BigDecimal("97"));
        assertThat(first.getOngoing()).isEqualTo(new BigDecimal("3"));
        assertThat(first.getFailed()).isEqualTo(BigDecimal.ZERO);
        DataCollectionKpiScore second = scores.get(1);
        assertThat(second.getSuccess()).isEqualTo(new BigDecimal("83"));
        assertThat(second.getOngoing()).isEqualTo(new BigDecimal("13"));
        assertThat(second.getFailed()).isEqualTo(new BigDecimal("4"));
        DataCollectionKpiScore third = scores.get(2);
        assertThat(third.getSuccess()).isEqualTo(new BigDecimal("67"));
        assertThat(third.getOngoing()).isEqualTo(new BigDecimal("23"));
        assertThat(third.getFailed()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void getScoresForUnorderedMonitoredStatusses () {
        KpiMember total = mockedKpiMember(MonitoredTaskStatus.Total, HUNDRED, HUNDRED, HUNDRED);
        KpiMember success = mockedKpiMember(MonitoredTaskStatus.Success, new BigDecimal("97"), new BigDecimal("83"), new BigDecimal("67"));
        KpiMember ongoing = mockedKpiMember(MonitoredTaskStatus.Ongoing, new BigDecimal("3"),  new BigDecimal("13"), new BigDecimal("23"));
        KpiMember failed = mockedKpiMember(MonitoredTaskStatus.Failed,   new BigDecimal("0"),  new BigDecimal("4"),  new BigDecimal("10"));
        KpiMembers kpiMembers = new KpiMembers(Arrays.asList(total, success, ongoing, failed));

        // Business method
        List<DataCollectionKpiScore> scores = kpiMembers.getScores(this.testInterval());

        // Asserts
        assertThat(scores).hasSize(3);
        DataCollectionKpiScore first = scores.get(0);
        assertThat(first.getTarget()).isEqualTo(BigDecimal.TEN);
        assertThat(first.getSuccess()).isEqualTo(new BigDecimal("97"));
        assertThat(first.getOngoing()).isEqualTo(new BigDecimal("3"));
        assertThat(first.getFailed()).isEqualTo(BigDecimal.ZERO);
        DataCollectionKpiScore second = scores.get(1);
        assertThat(second.getSuccess()).isEqualTo(new BigDecimal("83"));
        assertThat(second.getOngoing()).isEqualTo(new BigDecimal("13"));
        assertThat(second.getFailed()).isEqualTo(new BigDecimal("4"));
        DataCollectionKpiScore third = scores.get(2);
        assertThat(third.getSuccess()).isEqualTo(new BigDecimal("67"));
        assertThat(third.getOngoing()).isEqualTo(new BigDecimal("23"));
        assertThat(third.getFailed()).isEqualTo(BigDecimal.TEN);
    }

    private Range<Instant> testInterval() {
        return Ranges.closed(Instant.ofEpochMilli(INTERVAL_START_MILLIS), Instant.ofEpochMilli(INTERVAL_END_MILLIS));
    }

    private KpiMember mockedKpiMember(MonitoredTaskStatus taskStatus, BigDecimal... scores) {
        DateTime timestamp = new DateTime(INTERVAL_START_MILLIS).plusMinutes(KPI_INTERVAL_MINUTES);
        KpiMember kpiMember = mock(KpiMember.class);
        when(kpiMember.getName()).thenReturn(taskStatus.name());
        when(kpiMember.getTarget(any(Instant.class))).thenReturn(TARGET);
        List<KpiEntry> kpiEntries = Stream.of(scores).
                map(s -> {
                    KpiEntry kpiEntry = mock(KpiEntry.class);
                    when(kpiEntry.getScore()).thenReturn(s);
                    when(kpiEntry.getTarget()).thenReturn(TARGET);
                    when(kpiEntry.getTimestamp()).thenReturn(timestamp.toDate().toInstant());
                    timestamp.plusMinutes(KPI_INTERVAL_MINUTES);
                    return kpiEntry;
                }).
                collect(Collectors.toList());
        doReturn(kpiEntries).when(kpiMember).getScores(this.testInterval());
        return kpiMember;
    }

}
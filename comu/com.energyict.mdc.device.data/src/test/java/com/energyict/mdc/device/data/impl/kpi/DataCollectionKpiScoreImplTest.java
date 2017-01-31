/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DataCollectionKpiScoreImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-13 (17:17)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataCollectionKpiScoreImplTest {

    @Mock
    private KpiMember targetMember;
    @Mock
    private KpiMember successMember;
    @Mock
    private KpiMember ongoingMember;
    @Mock
    private KpiMember failedMember;
    @Mock
    private KpiEntry targetEntry;
    @Mock
    private KpiEntry successEntry;
    @Mock
    private KpiEntry ongoingEntry;
    @Mock
    private KpiEntry failedEntry;

    @Test
    public void completelyMeetsMinimumTarget() {
        Instant timestamp = Instant.ofEpochMilli(1398988800000L);    // Midnight of May 02, 2014
        when(this.targetEntry.getTimestamp()).thenReturn(timestamp);
        BigDecimal target = new BigDecimal("80");
        when(this.targetMember.getTarget(timestamp)).thenReturn(target);
        when(this.targetMember.targetIsMinimum()).thenReturn(true);
        when(this.targetMember.targetIsMaximum()).thenReturn(false);
        when(this.targetEntry.getTarget()).thenReturn(target);
        when(this.targetEntry.getScore()).thenReturn(new BigDecimal("97"));
        when(this.successEntry.getTimestamp()).thenReturn(timestamp);
        when(this.successEntry.getScore()).thenReturn(new BigDecimal("53"));
        when(this.ongoingEntry.getTimestamp()).thenReturn(timestamp);
        when(this.ongoingEntry.getScore()).thenReturn(new BigDecimal("31"));
        when(this.failedEntry.getTimestamp()).thenReturn(timestamp);
        when(this.failedEntry.getScore()).thenReturn(new BigDecimal("13"));
        List<KpiEntry> kpiEntries = Arrays.asList(this.targetEntry, this.successEntry, this.ongoingEntry, this.failedEntry);
        DataCollectionKpiScore score =
                new DataCollectionKpiScoreImpl(
                        timestamp,
                        this.targetMember,
                        Arrays.asList(MonitoredTaskStatus.Total, MonitoredTaskStatus.Success, MonitoredTaskStatus.Ongoing, MonitoredTaskStatus.Failed),
                        kpiEntries);

        // Business method
        boolean meetsTarget = score.meetsTarget();

        // Asserts
        assertThat(meetsTarget).isTrue();
    }

    @Test
    public void justsMeetsMinimumTarget() {
        Instant timestamp = Instant.ofEpochMilli(1398988800000L);    // Midnight of May 02, 2014
        when(this.targetEntry.getTimestamp()).thenReturn(timestamp);
        BigDecimal target = new BigDecimal("97");
        when(this.targetMember.getTarget(timestamp)).thenReturn(target);
        when(this.targetMember.targetIsMinimum()).thenReturn(true);
        when(this.targetMember.targetIsMaximum()).thenReturn(false);
        when(this.targetEntry.getTarget()).thenReturn(target);
        when(this.targetEntry.getScore()).thenReturn(new BigDecimal("97"));
        when(this.successEntry.getTimestamp()).thenReturn(timestamp);
        when(this.successEntry.getScore()).thenReturn(new BigDecimal("53"));
        when(this.ongoingEntry.getTimestamp()).thenReturn(timestamp);
        when(this.ongoingEntry.getScore()).thenReturn(new BigDecimal("31"));
        when(this.failedEntry.getTimestamp()).thenReturn(timestamp);
        when(this.failedEntry.getScore()).thenReturn(new BigDecimal("13"));
        List<KpiEntry> kpiEntries = Arrays.asList(this.targetEntry, this.successEntry, this.ongoingEntry, this.failedEntry);
        DataCollectionKpiScore score =
                new DataCollectionKpiScoreImpl(
                        timestamp,
                        this.targetMember,
                        Arrays.asList(MonitoredTaskStatus.Total, MonitoredTaskStatus.Success, MonitoredTaskStatus.Ongoing, MonitoredTaskStatus.Failed),
                        kpiEntries);

        // Business method
        boolean meetsTarget = score.meetsTarget();

        // Asserts
        assertThat(meetsTarget).isTrue();
    }

    @Test
    public void missesMinimumTarget() {
        Instant timestamp = Instant.ofEpochMilli(1398988800000L);    // Midnight of May 02, 2014
        when(this.targetEntry.getTimestamp()).thenReturn(timestamp);
        BigDecimal target = new BigDecimal("100");
        when(this.targetMember.getTarget(timestamp)).thenReturn(target);
        when(this.targetMember.targetIsMinimum()).thenReturn(true);
        when(this.targetMember.targetIsMaximum()).thenReturn(false);
        when(this.targetEntry.getTarget()).thenReturn(target);
        when(this.targetEntry.getScore()).thenReturn(new BigDecimal("97"));
        when(this.successEntry.getTimestamp()).thenReturn(timestamp);
        when(this.successEntry.getScore()).thenReturn(new BigDecimal("53"));
        when(this.ongoingEntry.getTimestamp()).thenReturn(timestamp);
        when(this.ongoingEntry.getScore()).thenReturn(new BigDecimal("31"));
        when(this.failedEntry.getTimestamp()).thenReturn(timestamp);
        when(this.failedEntry.getScore()).thenReturn(new BigDecimal("13"));
        List<KpiEntry> kpiEntries = Arrays.asList(this.targetEntry, this.successEntry, this.ongoingEntry, this.failedEntry);
        DataCollectionKpiScore score =
                new DataCollectionKpiScoreImpl(
                        timestamp,
                        this.targetMember,
                        Arrays.asList(MonitoredTaskStatus.Total, MonitoredTaskStatus.Success, MonitoredTaskStatus.Ongoing, MonitoredTaskStatus.Failed),
                        kpiEntries);

        // Business method
        boolean meetsTarget = score.meetsTarget();

        // Asserts
        assertThat(meetsTarget).isFalse();
    }

    @Test
    public void completelyMeetsMaximumTarget() {
        Instant timestamp = Instant.ofEpochMilli(1398988800000L);    // Midnight of May 02, 2014
        when(this.targetEntry.getTimestamp()).thenReturn(timestamp);
        BigDecimal target = new BigDecimal("80");
        when(this.targetMember.getTarget(timestamp)).thenReturn(target);
        when(this.targetMember.targetIsMaximum()).thenReturn(true);
        when(this.targetMember.targetIsMinimum()).thenReturn(false);
        when(this.targetEntry.getTarget()).thenReturn(target);
        when(this.targetEntry.getScore()).thenReturn(new BigDecimal("53"));
        when(this.successEntry.getTimestamp()).thenReturn(timestamp);
        when(this.successEntry.getScore()).thenReturn(new BigDecimal("31"));
        when(this.ongoingEntry.getTimestamp()).thenReturn(timestamp);
        when(this.ongoingEntry.getScore()).thenReturn(new BigDecimal("13"));
        when(this.failedEntry.getTimestamp()).thenReturn(timestamp);
        when(this.failedEntry.getScore()).thenReturn(new BigDecimal("6"));
        List<KpiEntry> kpiEntries = Arrays.asList(this.targetEntry, this.successEntry, this.ongoingEntry, this.failedEntry);
        DataCollectionKpiScore score =
                new DataCollectionKpiScoreImpl(
                        timestamp,
                        this.targetMember,
                        Arrays.asList(MonitoredTaskStatus.Total, MonitoredTaskStatus.Success, MonitoredTaskStatus.Ongoing, MonitoredTaskStatus.Failed),
                        kpiEntries);

        // Business method
        boolean meetsTarget = score.meetsTarget();

        // Asserts
        assertThat(meetsTarget).isTrue();
    }

    @Test
    public void justMeetsMaximumTarget() {
        Instant timestamp = Instant.ofEpochMilli(1398988800000L);    // Midnight of May 02, 2014
        when(this.targetEntry.getTimestamp()).thenReturn(timestamp);
        BigDecimal target = new BigDecimal("53");
        when(this.targetMember.getTarget(timestamp)).thenReturn(target);
        when(this.targetMember.targetIsMaximum()).thenReturn(true);
        when(this.targetMember.targetIsMinimum()).thenReturn(false);
        when(this.targetEntry.getTarget()).thenReturn(target);
        when(this.targetEntry.getScore()).thenReturn(new BigDecimal("53"));
        when(this.successEntry.getTimestamp()).thenReturn(timestamp);
        when(this.successEntry.getScore()).thenReturn(new BigDecimal("31"));
        when(this.ongoingEntry.getTimestamp()).thenReturn(timestamp);
        when(this.ongoingEntry.getScore()).thenReturn(new BigDecimal("13"));
        when(this.failedEntry.getTimestamp()).thenReturn(timestamp);
        when(this.failedEntry.getScore()).thenReturn(new BigDecimal("6"));
        List<KpiEntry> kpiEntries = Arrays.asList(this.targetEntry, this.successEntry, this.ongoingEntry, this.failedEntry);
        DataCollectionKpiScore score =
                new DataCollectionKpiScoreImpl(
                        timestamp,
                        this.targetMember,
                        Arrays.asList(MonitoredTaskStatus.Total, MonitoredTaskStatus.Success, MonitoredTaskStatus.Ongoing, MonitoredTaskStatus.Failed),
                        kpiEntries);

        // Business method
        boolean meetsTarget = score.meetsTarget();

        // Asserts
        assertThat(meetsTarget).isTrue();
    }

    @Test
    public void missesMaximumTarget() {
        Instant timestamp = Instant.ofEpochMilli(1398988800000L);    // Midnight of May 02, 2014
        when(this.targetEntry.getTimestamp()).thenReturn(timestamp);
        BigDecimal target = new BigDecimal("40");
        when(this.targetMember.getTarget(timestamp)).thenReturn(target);
        when(this.targetMember.targetIsMaximum()).thenReturn(true);
        when(this.targetMember.targetIsMinimum()).thenReturn(false);
        when(this.targetEntry.getTarget()).thenReturn(target);
        when(this.targetEntry.getScore()).thenReturn(new BigDecimal("53"));
        when(this.successEntry.getTimestamp()).thenReturn(timestamp);
        when(this.successEntry.getScore()).thenReturn(new BigDecimal("31"));
        when(this.ongoingEntry.getTimestamp()).thenReturn(timestamp);
        when(this.ongoingEntry.getScore()).thenReturn(new BigDecimal("13"));
        when(this.failedEntry.getTimestamp()).thenReturn(timestamp);
        when(this.failedEntry.getScore()).thenReturn(new BigDecimal("6"));
        List<KpiEntry> kpiEntries = Arrays.asList(this.targetEntry, this.successEntry, this.ongoingEntry, this.failedEntry);
        DataCollectionKpiScore score =
                new DataCollectionKpiScoreImpl(
                        timestamp,
                        this.targetMember,
                        Arrays.asList(MonitoredTaskStatus.Total, MonitoredTaskStatus.Success, MonitoredTaskStatus.Ongoing, MonitoredTaskStatus.Failed),
                        kpiEntries);

        // Business method
        boolean meetsTarget = score.meetsTarget();

        // Asserts
        assertThat(meetsTarget).isFalse();
    }

}
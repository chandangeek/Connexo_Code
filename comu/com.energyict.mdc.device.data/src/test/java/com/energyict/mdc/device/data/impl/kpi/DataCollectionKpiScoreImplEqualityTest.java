/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * Tests the equality aspects of the {@link DataCollectionKpiScoreImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-13 (17:42)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataCollectionKpiScoreImplEqualityTest extends EqualsContractTest {

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

    private DataCollectionKpiScoreImpl score;

    public void setupA() {
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
        this.score =
                new DataCollectionKpiScoreImpl(
                        timestamp,
                        this.targetMember,
                        Arrays.asList(MonitoredTaskStatus.Total, MonitoredTaskStatus.Success, MonitoredTaskStatus.Ongoing, MonitoredTaskStatus.Failed),
                        kpiEntries);
    }

    @Override
    protected Object getInstanceA() {
        if (this.score == null) {
            this.setupA();
        }
        return this.score;
    }

    @Override
    protected Object getInstanceEqualToA() {
        Instant timestamp = Instant.ofEpochMilli(1398988800000L);    // Midnight of May 02, 2014
        when(this.targetEntry.getTimestamp()).thenReturn(timestamp);
        BigDecimal target = new BigDecimal("50");
        when(this.targetMember.getTarget(timestamp)).thenReturn(target);
        when(this.targetMember.targetIsMinimum()).thenReturn(false);
        when(this.targetMember.targetIsMaximum()).thenReturn(true);
        when(this.targetEntry.getTarget()).thenReturn(target);
        when(this.targetEntry.getScore()).thenReturn(new BigDecimal("13"));
        when(this.successEntry.getTimestamp()).thenReturn(timestamp);
        when(this.successEntry.getScore()).thenReturn(new BigDecimal("31"));
        when(this.ongoingEntry.getTimestamp()).thenReturn(timestamp);
        when(this.ongoingEntry.getScore()).thenReturn(new BigDecimal("53"));
        when(this.failedEntry.getTimestamp()).thenReturn(timestamp);
        when(this.failedEntry.getScore()).thenReturn(new BigDecimal("97"));
        List<KpiEntry> kpiEntries = Arrays.asList(this.targetEntry, this.successEntry, this.ongoingEntry, this.failedEntry);
        return new DataCollectionKpiScoreImpl(
                        timestamp,
                        this.targetMember,
                        Arrays.asList(MonitoredTaskStatus.Total, MonitoredTaskStatus.Success, MonitoredTaskStatus.Ongoing, MonitoredTaskStatus.Failed),
                        kpiEntries);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        Instant timestamp1 = Instant.ofEpochMilli(1367452800000L);    // Midnight of May 02, 2013
        Instant timestamp2 = Instant.ofEpochMilli(1412208000000L);    // Midnight of Oct 02, 2014
        when(this.targetEntry.getTimestamp()).thenReturn(timestamp1);
        BigDecimal target = new BigDecimal("50");
        when(this.targetMember.getTarget(timestamp1)).thenReturn(target);
        when(this.targetMember.targetIsMinimum()).thenReturn(false);
        when(this.targetMember.targetIsMaximum()).thenReturn(true);
        when(this.targetEntry.getTarget()).thenReturn(target);
        when(this.targetEntry.getScore()).thenReturn(new BigDecimal("13"));
        when(this.successEntry.getTimestamp()).thenReturn(timestamp1);
        when(this.successEntry.getScore()).thenReturn(new BigDecimal("31"));
        when(this.ongoingEntry.getTimestamp()).thenReturn(timestamp1);
        when(this.ongoingEntry.getScore()).thenReturn(new BigDecimal("53"));
        when(this.failedEntry.getTimestamp()).thenReturn(timestamp1);
        when(this.failedEntry.getScore()).thenReturn(new BigDecimal("97"));
        List<KpiEntry> kpiEntries = Arrays.asList(this.targetEntry, this.successEntry, this.ongoingEntry, this.failedEntry);
        DataCollectionKpiScoreImpl score1 = new DataCollectionKpiScoreImpl(
                timestamp1,
                this.targetMember,
                Arrays.asList(MonitoredTaskStatus.Total, MonitoredTaskStatus.Success, MonitoredTaskStatus.Ongoing, MonitoredTaskStatus.Failed),
                kpiEntries);
        DataCollectionKpiScoreImpl score2 = new DataCollectionKpiScoreImpl(
                timestamp2,
                this.targetMember,
                Arrays.asList(MonitoredTaskStatus.Total, MonitoredTaskStatus.Success, MonitoredTaskStatus.Ongoing, MonitoredTaskStatus.Failed),
                kpiEntries);
        return Arrays.asList(score1, score2);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

}
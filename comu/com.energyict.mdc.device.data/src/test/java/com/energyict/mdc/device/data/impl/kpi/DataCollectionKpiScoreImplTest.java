package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
import org.joda.time.DateMidnight;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
        Date timestamp = new DateMidnight(2014, 5, 2).toDate();
        when(this.targetEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        BigDecimal target = new BigDecimal("80");
        when(this.targetMember.getTarget(timestamp.toInstant())).thenReturn(target);
        when(this.targetMember.targetIsMinimum()).thenReturn(true);
        when(this.targetMember.targetIsMaximum()).thenReturn(false);
        when(this.targetEntry.getTarget()).thenReturn(target);
        when(this.targetEntry.getScore()).thenReturn(new BigDecimal("97"));
        when(this.successEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        when(this.successEntry.getScore()).thenReturn(new BigDecimal("53"));
        when(this.ongoingEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        when(this.ongoingEntry.getScore()).thenReturn(new BigDecimal("31"));
        when(this.failedEntry.getTimestamp()).thenReturn(timestamp.toInstant());
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
        Date timestamp = new DateMidnight(2014, 5, 2).toDate();
        when(this.targetEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        BigDecimal target = new BigDecimal("97");
        when(this.targetMember.getTarget(timestamp.toInstant())).thenReturn(target);
        when(this.targetMember.targetIsMinimum()).thenReturn(true);
        when(this.targetMember.targetIsMaximum()).thenReturn(false);
        when(this.targetEntry.getTarget()).thenReturn(target);
        when(this.targetEntry.getScore()).thenReturn(new BigDecimal("97"));
        when(this.successEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        when(this.successEntry.getScore()).thenReturn(new BigDecimal("53"));
        when(this.ongoingEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        when(this.ongoingEntry.getScore()).thenReturn(new BigDecimal("31"));
        when(this.failedEntry.getTimestamp()).thenReturn(timestamp.toInstant());
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
        Date timestamp = new DateMidnight(2014, 5, 2).toDate();
        when(this.targetEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        BigDecimal target = new BigDecimal("100");
        when(this.targetMember.getTarget(timestamp.toInstant())).thenReturn(target);
        when(this.targetMember.targetIsMinimum()).thenReturn(true);
        when(this.targetMember.targetIsMaximum()).thenReturn(false);
        when(this.targetEntry.getTarget()).thenReturn(target);
        when(this.targetEntry.getScore()).thenReturn(new BigDecimal("97"));
        when(this.successEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        when(this.successEntry.getScore()).thenReturn(new BigDecimal("53"));
        when(this.ongoingEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        when(this.ongoingEntry.getScore()).thenReturn(new BigDecimal("31"));
        when(this.failedEntry.getTimestamp()).thenReturn(timestamp.toInstant());
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
        Date timestamp = new DateMidnight(2014, 5, 2).toDate();
        when(this.targetEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        BigDecimal target = new BigDecimal("80");
        when(this.targetMember.getTarget(timestamp.toInstant())).thenReturn(target);
        when(this.targetMember.targetIsMaximum()).thenReturn(true);
        when(this.targetMember.targetIsMinimum()).thenReturn(false);
        when(this.targetEntry.getTarget()).thenReturn(target);
        when(this.targetEntry.getScore()).thenReturn(new BigDecimal("53"));
        when(this.successEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        when(this.successEntry.getScore()).thenReturn(new BigDecimal("31"));
        when(this.ongoingEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        when(this.ongoingEntry.getScore()).thenReturn(new BigDecimal("13"));
        when(this.failedEntry.getTimestamp()).thenReturn(timestamp.toInstant());
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
        Date timestamp = new DateMidnight(2014, 5, 2).toDate();
        when(this.targetEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        BigDecimal target = new BigDecimal("53");
        when(this.targetMember.getTarget(timestamp.toInstant())).thenReturn(target);
        when(this.targetMember.targetIsMaximum()).thenReturn(true);
        when(this.targetMember.targetIsMinimum()).thenReturn(false);
        when(this.targetEntry.getTarget()).thenReturn(target);
        when(this.targetEntry.getScore()).thenReturn(new BigDecimal("53"));
        when(this.successEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        when(this.successEntry.getScore()).thenReturn(new BigDecimal("31"));
        when(this.ongoingEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        when(this.ongoingEntry.getScore()).thenReturn(new BigDecimal("13"));
        when(this.failedEntry.getTimestamp()).thenReturn(timestamp.toInstant());
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
        Date timestamp = new DateMidnight(2014, 5, 2).toDate();
        when(this.targetEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        BigDecimal target = new BigDecimal("40");
        when(this.targetMember.getTarget(timestamp.toInstant())).thenReturn(target);
        when(this.targetMember.targetIsMaximum()).thenReturn(true);
        when(this.targetMember.targetIsMinimum()).thenReturn(false);
        when(this.targetEntry.getTarget()).thenReturn(target);
        when(this.targetEntry.getScore()).thenReturn(new BigDecimal("53"));
        when(this.successEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        when(this.successEntry.getScore()).thenReturn(new BigDecimal("31"));
        when(this.ongoingEntry.getTimestamp()).thenReturn(timestamp.toInstant());
        when(this.ongoingEntry.getScore()).thenReturn(new BigDecimal("13"));
        when(this.failedEntry.getTimestamp()).thenReturn(timestamp.toInstant());
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
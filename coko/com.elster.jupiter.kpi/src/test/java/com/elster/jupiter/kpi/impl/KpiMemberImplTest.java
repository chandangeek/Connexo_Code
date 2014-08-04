package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.TargetStorer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KpiMemberImplTest {

    private static final Date TIMESTAMP = new DateMidnight(2002, 8, 1, DateTimeZone.UTC).toDate();
    public static final BigDecimal SCORE = BigDecimal.valueOf(700, 1);
    public static final BigDecimal TARGET = BigDecimal.valueOf(600, 1);

    private KpiMemberImpl kpiMember;
    @Mock
    private IdsService idsService;
    @Mock
    private EventService eventService;
    @Mock
    private KpiImpl kpi;
    @Mock
    private TimeSeriesDataStorer storer;
    @Mock
    private TimeSeries timeSeries;
    @Mock
    private TimeSeriesEntry timeSeriesEntry, timeSeriesEntry2;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;

    @Before
    public void setUp() {
        kpiMember = new KpiMemberImpl(idsService, eventService, dataModel, kpi, "name");
        kpiMember.setTimeSeries(timeSeries);
        kpiMember.setStatictarget(TARGET);

        when(idsService.createStorer(true)).thenReturn(storer);
        when(timeSeries.getEntry(TIMESTAMP)).thenReturn(Optional.of(timeSeriesEntry));
        when(timeSeriesEntry.getBigDecimal(0)).thenReturn(SCORE);
        when(timeSeriesEntry.getBigDecimal(1)).thenReturn(TARGET);
        when(timeSeriesEntry.getTimeStamp()).thenReturn(TIMESTAMP);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testScore() {
        kpiMember.score(TIMESTAMP, SCORE);

        verify(storer).add(timeSeries, TIMESTAMP, SCORE, TARGET);
    }

    @Test
    public void testGetScores() {
        when(timeSeries.getEntries(Interval.sinceEpoch())).thenReturn(Arrays.asList(timeSeriesEntry, timeSeriesEntry2));

        List<? extends KpiEntry> scores = kpiMember.getScores(Interval.sinceEpoch());

        assertThat(scores).hasSize(2);

        assertThat(scores.get(0).getScore()).isEqualTo(SCORE);
    }

    @Test(expected = IllegalStateException.class)
    public void testStoreTargetsOnStaticKpiMember() {
        kpiMember.getTargetStorer();
    }

    @Test
    public void testStoreTargets() {
        kpiMember.setDynamicTarget();

        TargetStorer targetStorer = kpiMember.getTargetStorer();
        targetStorer.add(TIMESTAMP, TARGET);
        targetStorer.execute();

        InOrder order = inOrder(storer);
        order.verify(storer).add(timeSeries, TIMESTAMP, null, TARGET);
        order.verify(storer).execute();

    }

    @Test
    public void testGetDynamicTarget() {
        kpiMember.setDynamicTarget();

        BigDecimal target = kpiMember.getTarget(TIMESTAMP);

        assertThat(target).isEqualTo(TARGET);
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateStaticTargetDynamicMember() {
        kpiMember.setDynamicTarget();

        kpiMember.updateTarget(BigDecimal.valueOf(480, 1));
    }

    @Test
    public void testUpdateStaticTarget() {
        kpiMember.updateTarget(BigDecimal.valueOf(480, 1));

        verify(dataModel.mapper(IKpiMember.class)).update(kpiMember);
    }

}
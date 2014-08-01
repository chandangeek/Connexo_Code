package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.google.common.base.Optional;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Date;

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
    private TimeSeriesEntry timeSeriesEntry;

    @Before
    public void setUp() {
        kpiMember = new KpiMemberImpl(idsService, eventService, kpi, "name");
        kpiMember.setTimeSeries(timeSeries);
        kpiMember.setStatictarget(TARGET);

        when(idsService.createStorer(true)).thenReturn(storer);
        when(timeSeries.getEntry(TIMESTAMP)).thenReturn(Optional.of(timeSeriesEntry));
        when(timeSeriesEntry.getBigDecimal(0)).thenReturn(SCORE);
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


}
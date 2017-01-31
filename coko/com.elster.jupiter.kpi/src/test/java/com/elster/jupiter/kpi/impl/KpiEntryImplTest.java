/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.kpi.KpiMember;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KpiEntryImplTest {

    public static final BigDecimal SCORE = BigDecimal.valueOf(5, 0);
    public static final BigDecimal TARGET = BigDecimal.valueOf(4, 0);
    @Mock
    private TimeSeriesEntry timeSeriesEntry;
    @Mock
    private KpiMember member;

    @Before
    public void setUp() {
        when(member.hasDynamicTarget()).thenReturn(true);
        when(member.targetIsMaximum()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return !((KpiMember) invocation.getMock()).targetIsMinimum();
            }
        });

        when(timeSeriesEntry.getBigDecimal(0)).thenReturn(SCORE);
        when(timeSeriesEntry.getBigDecimal(1)).thenReturn(TARGET);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testScore() {
        KpiEntryImpl kpiEntry = new KpiEntryImpl(member, timeSeriesEntry);

        assertThat(kpiEntry.getScore()).isEqualTo(SCORE);
    }

    @Test
    public void testTarget() {
        KpiEntryImpl kpiEntry = new KpiEntryImpl(member, timeSeriesEntry);

        assertThat(kpiEntry.getTarget()).isEqualTo(TARGET);
    }

    @Test
    public void testMeetsTarget() {
        when(member.targetIsMinimum()).thenReturn(true);

        KpiEntryImpl kpiEntry = new KpiEntryImpl(member, timeSeriesEntry);

        assertThat(kpiEntry.meetsTarget()).isTrue();
    }

    @Test
    public void testDoesNotMeetTarget() {
        when(member.targetIsMinimum()).thenReturn(false);

        KpiEntryImpl kpiEntry = new KpiEntryImpl(member, timeSeriesEntry);

        assertThat(kpiEntry.meetsTarget()).isFalse();
    }

    @Test
    public void testNoTargetAlwaysMeetsTarget() {
        when(member.targetIsMinimum()).thenReturn(false);
        when(timeSeriesEntry.getBigDecimal(1)).thenReturn(null);

        KpiEntryImpl kpiEntry = new KpiEntryImpl(member, timeSeriesEntry);

        assertThat(kpiEntry.meetsTarget()).isTrue();
    }



}
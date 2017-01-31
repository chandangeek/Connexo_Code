/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KpiMissEventImplTest {

    public static final BigDecimal SCORE = BigDecimal.valueOf(5, 0);
    public static final BigDecimal TARGET = BigDecimal.valueOf(4, 0);
    @Mock
    private KpiEntry entry;
    @Mock
    private KpiMemberImpl member;
    @Mock
    private Kpi kpi;

    @Before
    public void setUp() {
        when(member.hasDynamicTarget()).thenReturn(true);
        when(member.targetIsMaximum()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return !((KpiMember) invocation.getMock()).targetIsMinimum();
            }
        });
        when(member.getKpi()).thenReturn(kpi);

        when(entry.getScore()).thenReturn(SCORE);
        when(entry.getTarget()).thenReturn(TARGET);
        when(entry.getTimestamp()).thenReturn(Instant.now());
    }

    @Test
    public void testEntry() {
        KpiMissEventImpl event = new KpiMissEventImpl(member, entry);

        assertThat(event.getEntry()).isEqualTo(entry);
    }

    @Test
    public void testMember() {
        KpiMissEventImpl event = new KpiMissEventImpl(member, entry);

        assertThat(event.getMember()).isEqualTo(member);
    }

    @Test
    public void testId() {
        KpiMissEventImpl event = new KpiMissEventImpl(member, entry);

        event.setId(5L);
        assertThat(event.getId()).isEqualTo(5L);
    }

    @Test
    public void testPosition() {
        KpiMissEventImpl event = new KpiMissEventImpl(member, entry);

        event.setPosition(1);
        assertThat(event.getPosition()).isEqualTo(1);
    }

    @Test
    public void testTimestamp() {
        KpiMissEventImpl event = new KpiMissEventImpl(member, entry);

        long now = Instant.now().toEpochMilli();
        event.setTimestamp(now);
        assertThat(event.getTimestamp()).isEqualTo(now);
    }

}
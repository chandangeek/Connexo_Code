/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.servicecall.DefaultState;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceCallImplTest {

    private static final ZonedDateTime TIME_BASE = ZonedDateTime.of(1990, 2, 27, 16, 12, 30, 0, TimeZoneNeutral.getMcMurdo());
    private int tickCounter = 0;

    private Clock clock;

    @Mock
    private DataModel dataModel;
    @Mock
    private IServiceCallType serviceCallType;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private IServiceCallLifeCycle serviceCallLifeCycle;
    @Mock
    private IServiceCallService serviceCallService;
    @Mock
    private State state;

    @Before
    public void setUp() {
        clock = new ProgrammableClock(TimeZoneNeutral.getMcMurdo(), () -> TIME_BASE.plusMinutes(tickCounter++)
                .toInstant());
        when(dataModel.getInstance(ServiceCallImpl.class)).thenAnswer(invocation -> new ServiceCallImpl(dataModel, serviceCallService, clock, customPropertySetService, null));
        when(serviceCallType.getServiceCallLifeCycle()).thenReturn(serviceCallLifeCycle);
        when(serviceCallLifeCycle.getState(DefaultState.CREATED)).thenReturn(Optional.of(state));
        when(state.getName()).thenReturn(DefaultState.CREATED.getKey());
    }

    @Test
    public void serviceCallNumberFormatIsSC_00000000() {
        ServiceCallImpl serviceCall = ServiceCallImpl.from(dataModel, serviceCallType);

        simulateSavedInDB(serviceCall);

        assertThat(serviceCall.getNumber()).isEqualTo("SC_00489216");

    }

    private void simulateSavedInDB(ServiceCallImpl serviceCall) {
        field("id").ofType(Long.TYPE).in(serviceCall).set(489216L); // simulate saved in db
        Instant now = clock.instant();
        field("createTime").ofType(Instant.class).in(serviceCall).set(now);
        field("modTime").ofType(Instant.class).in(serviceCall).set(now);
    }

    private void simulateUpdatedInDB(ServiceCallImpl serviceCall) {
        Instant now = clock.instant();
        field("modTime").ofType(Instant.class).in(serviceCall).set(now);
    }

    @Test
    public void serviceCallCreationTimeIsTimeAtConstruction() {
        ServiceCallImpl serviceCall = ServiceCallImpl.from(dataModel, serviceCallType);

        simulateSavedInDB(serviceCall);

        assertThat(serviceCall.getCreationTime()).isEqualTo(TIME_BASE.toInstant());
    }

    @Test
    public void serviceCallLastModificationDateAtConstructionIsTimeAtConstruction() {
        ServiceCallImpl serviceCall = ServiceCallImpl.from(dataModel, serviceCallType);

        simulateSavedInDB(serviceCall);

        assertThat(serviceCall.getLastModificationTime()).isEqualTo(TIME_BASE.toInstant());
    }

    @Test
    public void serviceCallLastModificationDateIsTimeOfLastUpdate() {
        ServiceCallImpl serviceCall = ServiceCallImpl.from(dataModel, serviceCallType);

        simulateSavedInDB(serviceCall);
        int numberOfUpdates = 3;
        IntStream.range(0, numberOfUpdates)
                .forEach(i -> simulateUpdatedInDB(serviceCall));

        assertThat(serviceCall.getLastModificationTime()).isEqualTo(TIME_BASE.plusMinutes(numberOfUpdates).toInstant());
    }

    @Test
    public void lastCompletedTimeIsEmptyOnConstruction() {
        ServiceCallImpl serviceCall = ServiceCallImpl.from(dataModel, serviceCallType);

        assertThat(serviceCall.getLastCompletedTime()).isEmpty();
    }

    @Test
    public void initialStateIsCreated() {
        ServiceCallImpl serviceCall = ServiceCallImpl.from(dataModel, serviceCallType);

        assertThat(serviceCall.getState()).isEqualTo(DefaultState.CREATED);
    }
}
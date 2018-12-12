/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.scheduling.SchedulingService;

import com.google.common.collect.ImmutableSet;

import java.time.Clock;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class ComScheduleEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    private SchedulingService schedulingService;
    @Mock
    private EventService eventService;
    @Mock
    private ComScheduleExceptionFactory comScheduleExceptionFactory;
    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Clock clock;

    private ComScheduleImpl comSchedule;

    @Override
    protected Object getInstanceA() {
        if (comSchedule == null) {
            comSchedule = new ComScheduleImpl(clock, schedulingService, eventService, dataModel, comScheduleExceptionFactory);
            setId(comSchedule, ID);
        }
        return comSchedule;
    }

    private void setId(ComScheduleImpl comSchedule, Long id) {
        field("id").ofType(Long.TYPE).in(comSchedule).set(id);
    }

    @Override
    protected Object getInstanceEqualToA() {
        ComScheduleImpl comSchedule = new ComScheduleImpl(clock, schedulingService, eventService, dataModel, comScheduleExceptionFactory);
        setId(comSchedule, ID);
        return comSchedule;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        ComScheduleImpl comSchedule = new ComScheduleImpl(clock, schedulingService, eventService, dataModel, comScheduleExceptionFactory);
        setId(comSchedule, OTHER_ID);
        return ImmutableSet.of(comSchedule);
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

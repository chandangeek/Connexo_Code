/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import com.google.common.collect.ImmutableSet;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class NextExecutionSpecsEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    private EventService eventService;
    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;

    private NextExecutionSpecsImpl nextExecutionSpecs;

    @Override
    protected Object getInstanceA() {
        if (nextExecutionSpecs == null) {
            nextExecutionSpecs = new NextExecutionSpecsImpl(dataModel, eventService, thesaurus);
            setId(nextExecutionSpecs, ID);
        }
        return nextExecutionSpecs;
    }

    private void setId(NextExecutionSpecsImpl nextExecutionSpecs, Long id) {
        field("id").ofType(Long.TYPE).in(nextExecutionSpecs).set(id);
    }

    @Override
    protected Object getInstanceEqualToA() {
        NextExecutionSpecsImpl nextExecutionSpecs = new NextExecutionSpecsImpl(dataModel, eventService, thesaurus);
        setId(nextExecutionSpecs, ID);
        return nextExecutionSpecs;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        NextExecutionSpecsImpl nextExecutionSpecs = new NextExecutionSpecsImpl(dataModel, eventService, thesaurus);
        setId(nextExecutionSpecs, OTHER_ID);
        return ImmutableSet.of(nextExecutionSpecs);
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

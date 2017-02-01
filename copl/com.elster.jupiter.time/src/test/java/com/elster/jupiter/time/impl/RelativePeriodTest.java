/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriod;

import java.util.Collections;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class RelativePeriodTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private Thesaurus thesaurus;

    private RelativePeriod relativePeriod;

    private void setId(Object entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
    }

    @Override
    protected Object getInstanceA() {
        if (relativePeriod == null) {
            relativePeriod = new RelativePeriodImpl(dataModel, eventService, thesaurus);
            setId(relativePeriod, ID);
        }
        return relativePeriod;
    }

    @Override
    protected Object getInstanceEqualToA() {
        RelativePeriod instanceB = new RelativePeriodImpl(dataModel, eventService, thesaurus);
        setId(instanceB, ID);
        return instanceB;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        RelativePeriodImpl relativePeriod = new RelativePeriodImpl(dataModel, eventService, thesaurus);
        setId(relativePeriod, OTHER_ID);
        return Collections.singletonList(relativePeriod);
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

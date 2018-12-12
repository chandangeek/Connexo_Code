/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.config.ComPortPoolMember;

import com.google.inject.Provider;

import java.util.Collections;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class OutboundComPortPoolEqualityTest extends EqualsContractTest {

    @Mock
    private DataModel dataModel;
    @Mock
    private Provider<ComPortPoolMember> comPortPoolMemberProvider;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private EventService eventService;
    private OutboundComPortPoolImpl outboundComPortPool;
    private int comPortPoolId = 12;

    OutboundComPortPoolImpl setId(OutboundComPortPoolImpl entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
        return entity;
    }

    @Override
    protected Object getInstanceA() {
        if (outboundComPortPool == null) {
            outboundComPortPool = setId(new OutboundComPortPoolImpl(dataModel, comPortPoolMemberProvider, thesaurus, eventService), comPortPoolId);
        }
        return outboundComPortPool;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return setId(new OutboundComPortPoolImpl(dataModel, comPortPoolMemberProvider, thesaurus, eventService), comPortPoolId);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Collections.singletonList(setId(new OutboundComPortPoolImpl(dataModel, comPortPoolMemberProvider, thesaurus, eventService), 1445654L));
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
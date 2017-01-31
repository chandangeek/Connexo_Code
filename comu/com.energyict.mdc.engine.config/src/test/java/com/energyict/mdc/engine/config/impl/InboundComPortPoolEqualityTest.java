/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.config.ComPortPoolMember;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.inject.Provider;

import java.util.Collections;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class InboundComPortPoolEqualityTest extends EqualsContractTest {

    @Mock
    private DataModel dataModel;
    @Mock
    private Provider<ComPortPoolMember> comPortPoolMemberProvider;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private EventService eventService;
    @Mock
    private EngineConfigurationService engineConfigurationService;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    private InboundComPortPoolImpl inboundComPortPoolImpl;
    private int comPortPoolId = 12;
    private InboundComPortPoolImpl inboundComPortPool;

    InboundComPortPoolImpl setId(InboundComPortPoolImpl entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
        return entity;
    }

    @Override
    protected Object getInstanceA() {
        if (inboundComPortPool == null) {
            inboundComPortPool = setId(new InboundComPortPoolImpl(dataModel, eventService, engineConfigurationService, thesaurus, protocolPluggableService), comPortPoolId);
        }
        return inboundComPortPool;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return setId(new InboundComPortPoolImpl(dataModel, eventService, engineConfigurationService, thesaurus, protocolPluggableService), comPortPoolId);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Collections.singletonList(setId(new InboundComPortPoolImpl(dataModel, eventService, engineConfigurationService, thesaurus, protocolPluggableService), 1443568465));
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

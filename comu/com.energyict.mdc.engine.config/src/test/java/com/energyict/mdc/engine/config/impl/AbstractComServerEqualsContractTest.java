/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;

import com.google.inject.Provider;

import org.mockito.Mock;

import static org.fest.reflect.core.Reflection.field;

public abstract class AbstractComServerEqualsContractTest  extends EqualsContractTest{

    @Mock
    EngineConfigurationService engineConfigurationService;
    @Mock
    DataModel dataModel;
    @Mock
    Provider<OutboundComPort> outboundComPortProvider;
    @Mock
    Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider;
    @Mock
    Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider;
    @Mock
    Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider;
    @Mock
    Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider;
    @Mock
    Thesaurus thesaurus;

    ComServerImpl setId(ComServerImpl entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
        return entity;
    }

}

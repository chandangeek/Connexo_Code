/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.servers;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.ModemBasedInboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.ServletBasedInboundComPort;
import com.energyict.mdc.common.comserver.TCPBasedInboundComPort;
import com.energyict.mdc.common.comserver.UDPBasedInboundComPort;
import com.energyict.mdc.engine.config.EngineConfigurationService;

import com.google.inject.Provider;

import org.mockito.Mock;

import static org.fest.reflect.core.Reflection.field;

public abstract class AbstractComServerEqualsContractTest extends EqualsContractTest {

    @Mock
    EngineConfigurationService engineConfigurationService;
    @Mock
    DataModel dataModel;
    @Mock
    Provider<OutboundComPort> outboundComPortProvider;
    @Mock
    Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider;
    @Mock
    Provider<CoapBasedInboundComPort> coapBasedInboundComPortProvider;
    @Mock
    Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider;
    @Mock
    Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider;
    @Mock
    Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider;
    @Mock
    Thesaurus thesaurus;

    ComServer setId(ComServer entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
        return entity;
    }

}

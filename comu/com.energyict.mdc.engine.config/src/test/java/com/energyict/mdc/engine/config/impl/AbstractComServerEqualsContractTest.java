package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.config.*;
import com.google.inject.Provider;
import org.mockito.Mock;

import static org.fest.reflect.core.Reflection.field;

/**
 * Copyrights EnergyICT
 * Date: 15.10.15
 * Time: 11:41
 */
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

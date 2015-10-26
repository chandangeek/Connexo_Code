package com.energyict.mdc.engine.config.impl;

import com.energyict.mdc.engine.config.ComServer;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

/**
 * Copyrights EnergyICT
 * Date: 15.10.15
 * Time: 10:51
 */
@RunWith(MockitoJUnitRunner.class)
public class OnlineComServerEqualsImplTest extends AbstractComServerEqualsContractTest {

    private ComServer comServerInstanceA;
    private int comserverInstanceAId = 1;

    @Override
    protected Object getInstanceA() {
        if (comServerInstanceA == null) {
            comServerInstanceA = setId(newOnlineComServer("InstanceA"), comserverInstanceAId);
        }
        return comServerInstanceA;
    }


    private OnlineComServerImpl newOnlineComServer(String name) {
        final OnlineComServerImpl onlineComServer = new OnlineComServerImpl(dataModel, engineConfigurationService, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
        onlineComServer.setName(name);
        return onlineComServer;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return setId(newOnlineComServer("InstanceA"), comserverInstanceAId);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Collections.singletonList(setId(newOnlineComServer("InstanceA"), 123456));
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
/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.servers;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.engine.config.impl.OnlineComServerImpl;

import java.util.Collections;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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


    private OnlineComServer newOnlineComServer(String name) {
        final OnlineComServer onlineComServer = new OnlineComServerImpl(dataModel, engineConfigurationService, outboundComPortProvider, servletBasedInboundComPortProvider, coapBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
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
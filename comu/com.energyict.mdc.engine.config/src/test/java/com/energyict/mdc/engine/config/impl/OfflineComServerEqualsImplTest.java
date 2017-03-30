/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.energyict.mdc.engine.config.ComServer;

import java.util.Collections;

public class OfflineComServerEqualsImplTest extends AbstractComServerEqualsContractTest {

    private ComServer comServerInstanceA;
    private int comserverInstanceAId = 1;

    @Override
    protected Object getInstanceA() {
        if(comServerInstanceA == null){
            comServerInstanceA = setId(newOfflineComServerImpl("InstanceA"), comserverInstanceAId);
        }
        return comServerInstanceA;
    }

    private OfflineComServerImpl newOfflineComServerImpl(String name) {
        final OfflineComServerImpl offlineComServer = new OfflineComServerImpl(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
        offlineComServer.setName(name);
        return offlineComServer;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return setId(newOfflineComServerImpl("InstanceA"), comserverInstanceAId);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Collections.singletonList(setId(newOfflineComServerImpl("InstanceA"), 123453));
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

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.energyict.mdc.engine.config.ComServer;

import java.util.Collections;

public class RemoteComServerEqualsImplTest extends AbstractComServerEqualsContractTest {

    private ComServer comServerInstanceA;
    private int comserverInstanceAId = 1;

    @Override
    protected Object getInstanceA() {
        if(comServerInstanceA == null) {
            comServerInstanceA = setId(newRemoteComServerImpl("MyInstance"), comserverInstanceAId);
        }
        return comServerInstanceA;
    }

    private RemoteComServerImpl newRemoteComServerImpl(String name) {
        final RemoteComServerImpl remoteComServer = new RemoteComServerImpl(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
        remoteComServer.setName(name);
        return remoteComServer;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return setId(newRemoteComServerImpl("MyInstance"), comserverInstanceAId);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Collections.singletonList(setId(newRemoteComServerImpl("MyInstance"), 44654));
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

/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.servers;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.RemoteComServer;
import com.energyict.mdc.engine.config.impl.RemoteComServerImpl;

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

    private RemoteComServer newRemoteComServerImpl(String name) {
        final RemoteComServer remoteComServer = new RemoteComServerImpl(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, coapBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
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

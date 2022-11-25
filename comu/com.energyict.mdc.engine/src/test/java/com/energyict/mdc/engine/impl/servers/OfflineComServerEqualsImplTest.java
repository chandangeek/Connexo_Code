/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.servers;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OfflineComServer;
import com.energyict.mdc.engine.config.impl.OfflineComServerImpl;

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

    private OfflineComServer newOfflineComServerImpl(String name) {
        final OfflineComServer offlineComServer = new OfflineComServerImpl(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, coapBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
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

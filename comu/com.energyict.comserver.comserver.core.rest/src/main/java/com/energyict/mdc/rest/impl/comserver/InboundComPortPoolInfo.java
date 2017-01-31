/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.util.stream.Collectors;

public class InboundComPortPoolInfo extends ComPortPoolInfo<InboundComPortPool> {

    public InboundComPortPoolInfo() {
    }

    public InboundComPortPoolInfo(InboundComPortPool comPortPool, ComPortInfoFactory comPortInfoFactory) {
        super(comPortPool);
        this.discoveryProtocolPluggableClassId = comPortPool.getDiscoveryProtocolPluggableClass().getId();
        this.inboundComPorts = comPortPool.getComPorts().stream().map(comPortInfoFactory::asInboundInfo).collect(Collectors.toList());
    }

    @Override
    protected InboundComPortPool writeTo(InboundComPortPool source, ProtocolPluggableService protocolPluggableService) {
        super.writeTo(source, protocolPluggableService);
        if (discoveryProtocolPluggableClassId != null) {
            protocolPluggableService
                    .findInboundDeviceProtocolPluggableClass(this.discoveryProtocolPluggableClassId)
                    .ifPresent(source::setDiscoveryProtocolPluggableClass);
        }
        return source;
    }

    @Override
    protected InboundComPortPool createNew(EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService) {
        InboundComPortPool inboundComPortPool = engineConfigurationService.newInboundComPortPool(
                this.name,
                this.comPortType != null ? this.comPortType.id : null,
                protocolPluggableService
                        .findInboundDeviceProtocolPluggableClass(this.discoveryProtocolPluggableClassId!=null?this.discoveryProtocolPluggableClassId:0)
                        .orElse(null));
        this.writeTo(inboundComPortPool);
        return inboundComPortPool;
    }

}
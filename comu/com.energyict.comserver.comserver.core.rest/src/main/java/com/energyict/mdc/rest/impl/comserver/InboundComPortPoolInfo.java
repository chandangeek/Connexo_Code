/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InboundComPortPoolInfo extends ComPortPoolInfo<InboundComPortPool> {

    public InboundComPortPoolInfo() {
    }

    public InboundComPortPoolInfo(InboundComPortPool comPortPool, ComPortInfoFactory comPortInfoFactory, MdcPropertyUtils mdcPropertyUtils) {
        super(comPortPool);
        this.discoveryProtocolPluggableClassId = comPortPool.getDiscoveryProtocolPluggableClass().getId();
        this.inboundComPorts = comPortPool.getComPorts().stream().map(comPortInfoFactory::asInboundInfo).collect(Collectors.toList());
        this.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(comPortPool.getDiscoveryProtocolPluggableClass().getInboundDeviceProtocol().getPropertySpecs() ,comPortPool.getTypedProperties());
    }

    @Override
    protected InboundComPortPool writeTo(InboundComPortPool source, ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils) {
        super.writeTo(source, protocolPluggableService, mdcPropertyUtils);
        if (discoveryProtocolPluggableClassId != null) {
            Optional<InboundDeviceProtocolPluggableClass> inboundDeviceProtocolPluggableClass = protocolPluggableService
                    .findInboundDeviceProtocolPluggableClass(this.discoveryProtocolPluggableClassId);
            inboundDeviceProtocolPluggableClass
                    .ifPresent(protocolPluggableClass -> {
                        source.setDiscoveryProtocolPluggableClass(protocolPluggableClass);
                        source.clearProperties();
                        getProperties(protocolPluggableClass, mdcPropertyUtils).entrySet().stream()
                                .forEach(entry -> source.setProperty(entry.getKey(), entry.getValue()));
                    });
        }
        return source;
    }

    @Override
    protected InboundComPortPool createNew(EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils) {
        InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass = protocolPluggableService
                .findInboundDeviceProtocolPluggableClass(this.discoveryProtocolPluggableClassId != null ? this.discoveryProtocolPluggableClassId : 0)
                .orElse(null);

        InboundComPortPool inboundComPortPool = engineConfigurationService.newInboundComPortPool(
                this.name,
                this.comPortType != null ? this.comPortType.id : null,
                inboundDeviceProtocolPluggableClass, getProperties(inboundDeviceProtocolPluggableClass, mdcPropertyUtils));
        this.writeTo(inboundComPortPool);
        return inboundComPortPool;
    }

    private Map<String, Object> getProperties(InboundDeviceProtocolPluggableClass discoveryProtocolPluggableClass, MdcPropertyUtils mdcPropertyUtils) {
        Map<String, Object> properties = new HashMap<>();
        if(this.properties!=null) {
            try {
                if(discoveryProtocolPluggableClass != null) {
                    discoveryProtocolPluggableClass.getInboundDeviceProtocol().getPropertySpecs()
                            .stream()
                            .forEach(spec -> {
                                Object propertyValue = mdcPropertyUtils.findPropertyValue(spec, this.properties);
                                if(propertyValue != null) {
                                    properties.put(spec.getName(), propertyValue);
                                }
                            });
                }
            } catch (LocalizedFieldValidationException e) {
                throw new LocalizedFieldValidationException(e.getMessageSeed(), "properties."+e.getViolatingProperty());
            }
        }
        return properties;
    }

}
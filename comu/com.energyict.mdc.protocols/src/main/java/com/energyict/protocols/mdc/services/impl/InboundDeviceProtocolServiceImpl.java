/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.common.pluggable.PluggableClass;
import com.energyict.mdc.common.protocol.InboundDeviceProtocol;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.UnableToCreateProtocolInstance;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLInboundDeviceProtocolAdapter;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component(name = "com.energyict.mdc.service.inbounddeviceprotocols", service = InboundDeviceProtocolService.class, immediate = true)
public class InboundDeviceProtocolServiceImpl implements InboundDeviceProtocolService {

    private static final Map<String, InstanceFactory> uplFactories = new ConcurrentHashMap<>();

    @Override
    public InboundDeviceProtocol createInboundDeviceProtocolFor(PluggableClass pluggableClass) {
        return this.createInboundDeviceProtocolFor(pluggableClass.getJavaClassName());
    }

    @Override
    public InboundDeviceProtocol createInboundDeviceProtocolFor(String className) {
        try {
            com.energyict.mdc.upl.InboundDeviceProtocol inboundDeviceProtocol = (com.energyict.mdc.upl.InboundDeviceProtocol) uplFactories
                    .computeIfAbsent(className, ConstructorBasedUplServiceInjection::from)
                    .newInstance();
            return new UPLInboundDeviceProtocolAdapter(inboundDeviceProtocol);
        } catch (UnableToCreateProtocolInstance e) {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, e, className);
        }
    }

    @Override
    public Collection<PluggableClassDefinition> getExistingInboundDeviceProtocolPluggableClasses() {
        return Arrays.asList((PluggableClassDefinition[]) InboundDeviceProtocolRule.values());
    }
}
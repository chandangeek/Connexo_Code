package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.UnableToCreateProtocolInstance;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLInboundDeviceProtocolAdapter;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyrights EnergyICT
 * Date: 15/11/13
 * Time: 12:03
 */
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
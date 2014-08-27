package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.protocols.mdc.InboundDeviceProtocolRule;
import java.util.Arrays;
import java.util.Collection;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Copyrights EnergyICT
 * Date: 15/11/13
 * Time: 12:03
 */
@Component(name = "com.energyict.mdc.service.inbounddeviceprotocols", service = InboundDeviceProtocolService.class, immediate = true)
public class InboundDeviceProtocolServiceImpl implements InboundDeviceProtocolService {

    private PropertySpecService propertySpecService;

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public InboundDeviceProtocol createInboundDeviceProtocolFor(PluggableClass pluggableClass) {
        try {
            InboundDeviceProtocol inboundDeviceProtocol = (InboundDeviceProtocol) (Class.forName(pluggableClass.getJavaClassName())).newInstance();
            inboundDeviceProtocol.setPropertySpecService(this.propertySpecService);
            return inboundDeviceProtocol;
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, e, pluggableClass.getJavaClassName());
        }
    }

    @Override
    public InboundDeviceProtocol createInboundDeviceProtocolFor(String javaClassName) {
        try {
            return (InboundDeviceProtocol) (Class.forName(javaClassName)).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, e, javaClassName);
        }
    }

    @Override
    public Collection<PluggableClassDefinition> getExistingInboundDeviceProtocolPluggableClasses() {
        return Arrays.asList((PluggableClassDefinition[])InboundDeviceProtocolRule.values());
    }
}

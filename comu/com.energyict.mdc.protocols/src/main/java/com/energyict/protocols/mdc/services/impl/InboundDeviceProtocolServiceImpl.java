package com.energyict.protocols.mdc.services.impl;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.protocol.PluggableClass;
import com.energyict.mdc.protocol.inbound.InboundDeviceProtocol;
import com.energyict.mdc.services.InboundDeviceProtocolService;
import org.osgi.service.component.annotations.Component;

/**
 * Copyrights EnergyICT
 * Date: 15/11/13
 * Time: 12:03
 */
@Component(name = "com.energyict.mdc.service.inbounddeviceprotocols", service = InboundDeviceProtocolService.class, immediate = true)
public class InboundDeviceProtocolServiceImpl extends AbstractPluggableClassServiceImpl implements InboundDeviceProtocolService {

    @Override
    public InboundDeviceProtocol createInboundDeviceProtocolFor(PluggableClass pluggableClass) {
        try {
            return (InboundDeviceProtocol) (Class.forName(pluggableClass.getJavaClassName())).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw CodingException.genericReflectionError(e, pluggableClass.getJavaClassName());
        }
    }

    @Override
    public InboundDeviceProtocol createInboundDeviceProtocolFor(String javaClassName) {
        try {
            return (InboundDeviceProtocol) (Class.forName(javaClassName)).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw CodingException.genericReflectionError(e, javaClassName);
        }
    }
}

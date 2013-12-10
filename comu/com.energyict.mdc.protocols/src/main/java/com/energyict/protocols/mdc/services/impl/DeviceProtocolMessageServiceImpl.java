package com.energyict.protocols.mdc.services.impl;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link DeviceProtocolMessageService} interface
 * and registers as a OSGi component.
 *
 * Copyrights EnergyICT
 * Date: 08/11/13
 * Time: 16:08
 */
@Component(name = "com.energyict.mdc.service.deviceprotocolmessage", service = DeviceProtocolMessageService.class)
public class DeviceProtocolMessageServiceImpl implements DeviceProtocolMessageService {

    @Override
    public Object createDeviceProtocolMessagesFor(String javaClassName) {
        try {
            return Class.forName(javaClassName).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw CodingException.genericReflectionError(e, javaClassName);
        }
    }

}
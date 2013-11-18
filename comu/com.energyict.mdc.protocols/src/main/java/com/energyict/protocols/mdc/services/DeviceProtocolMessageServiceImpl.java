package com.energyict.protocols.mdc.services;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.services.DeviceProtocolMessageService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Copyrights EnergyICT
 * Date: 08/11/13
 * Time: 16:08
 */
@Component(name = "com.energyict.mdc.service.deviceprotocolmessage", service = DeviceProtocolMessageService.class, immediate = true)
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

package com.energyict.protocols.mdc.services;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.services.DeviceProtocolSecurityService;
import org.osgi.service.component.annotations.Component;

/**
 * Copyrights EnergyICT
 * Date: 08/11/13
 * Time: 16:05
 */
@Component(name = "com.energyict.mdc.service.deviceprotocolsecurity", service = DeviceProtocolSecurityService.class, immediate = true)
public class DeviceProtocolSecurityServiceImpl implements DeviceProtocolSecurityService {

    @Override
    public Object createDeviceProtocolSecurityFor(String javaClassName) {
        try {
            return Class.forName(javaClassName).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw CodingException.genericReflectionError(e, javaClassName);
        }
    }
}

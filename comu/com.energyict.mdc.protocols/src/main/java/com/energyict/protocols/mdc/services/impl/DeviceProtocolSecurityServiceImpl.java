package com.energyict.protocols.mdc.services.impl;

import com.energyict.comserver.exceptions.CodingException;
import com.energyict.comserver.exceptions.DeviceProtocolAdapterCodingExceptions;
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
        } catch (InstantiationException | IllegalAccessException e) {
            throw CodingException.genericReflectionError(e, javaClassName);
        } catch (ClassNotFoundException e) {
            throw DeviceProtocolAdapterCodingExceptions.unKnownDeviceSecuritySupportClass(e, javaClassName);
        }
    }
}

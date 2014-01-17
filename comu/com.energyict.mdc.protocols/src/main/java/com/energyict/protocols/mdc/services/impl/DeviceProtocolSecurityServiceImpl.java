package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link DeviceProtocolSecurityService} interface
 * and registers as a OSGi component.
 *
 * Copyrights EnergyICT
 * Date: 08/11/13
 * Time: 16:05
 */
@Component(name = "com.energyict.mdc.service.deviceprotocolsecurity", service = DeviceProtocolSecurityService.class)
public class DeviceProtocolSecurityServiceImpl implements DeviceProtocolSecurityService {

    @Override
    public Object createDeviceProtocolSecurityFor(String javaClassName) {
        try {
            return Class.forName(javaClassName).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(e, javaClassName);
        } catch (ClassNotFoundException e) {
            throw DeviceProtocolAdapterCodingExceptions.unKnownDeviceSecuritySupportClass(e, javaClassName);
        }
    }
}

package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link DeviceProtocolSecurityService} interface
 * and registers as a OSGi component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 08/11/13
 * Time: 16:05
 */
@Component(name = "com.energyict.mdc.service.deviceprotocolsecurity", service = DeviceProtocolSecurityService.class)
public class DeviceProtocolSecurityServiceImpl implements DeviceProtocolSecurityService {

    private volatile PropertySpecService propertySpecService;

    public DeviceProtocolSecurityServiceImpl(){}

    @Inject
    public DeviceProtocolSecurityServiceImpl(PropertySpecService propertySpecService){
        this.setPropertySpecService(propertySpecService);
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public Object createDeviceProtocolSecurityFor(String javaClassName) {
        try {
            Object object = Class.forName(javaClassName).newInstance();
            if (object instanceof DeviceProtocolSecurityCapabilities) {
                DeviceProtocolSecurityCapabilities securityCapabilities = (DeviceProtocolSecurityCapabilities) object;
                securityCapabilities.setPropertySpecService(this.propertySpecService);
                return securityCapabilities;
            }
            else {
                return object;
            }
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, e, javaClassName);
        }
        catch (ClassNotFoundException e) {
            throw DeviceProtocolAdapterCodingExceptions.unKnownDeviceSecuritySupportClass(MessageSeeds.UNKNOWN_DEVICE_SECURITY_SUPPORT_CLASS, e, javaClassName);
        }
    }

}
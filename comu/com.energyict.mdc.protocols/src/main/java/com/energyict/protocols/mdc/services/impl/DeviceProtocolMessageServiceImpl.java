package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;

import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link DeviceProtocolMessageService} interface
 * and registers as a OSGi component.
 * <p/>
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
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, e, javaClassName);
        }
        catch (ClassNotFoundException e) {
            throw DeviceProtocolAdapterCodingExceptions.unKnownDeviceMessageConverterClass(MessageSeeds.UNKNOWN_DEVICE_MESSAGE_CONVERTER_CLASS, e, javaClassName);
        }
    }

}
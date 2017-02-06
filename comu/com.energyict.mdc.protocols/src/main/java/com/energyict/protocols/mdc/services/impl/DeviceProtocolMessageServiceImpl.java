package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.UnableToCreateProtocolInstance;
import org.osgi.service.component.annotations.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an implementation for the {@link DeviceProtocolMessageService} interface
 * and registers as a OSGi component.
 * <p>
 * Copyrights EnergyICT
 * Date: 08/11/13
 * Time: 16:08
 */
@Component(name = "com.energyict.mdc.service.deviceprotocolmessage", service = DeviceProtocolMessageService.class)
public class DeviceProtocolMessageServiceImpl implements DeviceProtocolMessageService {

    private static final Map<String, InstanceFactory> uplFactories = new ConcurrentHashMap<>();

    // For OSGi purposes
    public DeviceProtocolMessageServiceImpl() {
        super();
    }

    @Override
    public Object createDeviceProtocolMessagesFor(String className) {
        try {
            return uplFactories
                    .computeIfAbsent(className, ConstructorBasedUplServiceInjection::from)
                    .newInstance();
        } catch (UnableToCreateProtocolInstance e) {
            throw DeviceProtocolAdapterCodingExceptions
                    .deviceMessageConverterClassCreationFailure(MessageSeeds.DEVICE_MESSAGE_CONVERTER_CREATION_FAILURE, e, className);
        }
    }

}
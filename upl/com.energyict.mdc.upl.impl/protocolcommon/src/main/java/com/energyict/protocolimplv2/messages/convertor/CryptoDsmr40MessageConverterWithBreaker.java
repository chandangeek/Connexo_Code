package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.*;

import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeName;

/**
 * Extension of the Crypto DSMR2.3 message converter, that adds the breaker messages again
 *
 * @author khe
 */
public class CryptoDsmr40MessageConverterWithBreaker extends CryptoDsmr40MessageConverter {

    protected static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(CryptoDsmr40MessageConverter.registry);

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

    static {
        // contactor related
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new DisconnectLoadMessageEntry());
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, new DisconnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new ConnectLoadMessageEntry());
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE, new ConnectLoadWithActivationDateMessageEntry(contactorActivationDateAttributeName));
        registry.put(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE, new ConnectControlModeMessageEntry(contactorModeAttributeName));
    }

    public CryptoDsmr40MessageConverterWithBreaker() {
        super();
    }
}
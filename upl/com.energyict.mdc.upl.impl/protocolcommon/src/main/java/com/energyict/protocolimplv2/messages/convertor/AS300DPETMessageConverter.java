package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.Register;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceGroup;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleValueMessageEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a MessageConverter for the legacy IC AS300 protocol.
 *
 * @author sva
 * @since 28/10/13 - 14:22
 */

public class AS300DPETMessageConverter extends AS300MessageConverter {

    private static final String KEY = "Key";
    private static final ObisCode PUBLIC_KEYS_OBISCODE = ObisCode.fromString("0.128.0.2.0.2");

    public AS300DPETMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor);
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(super.getRegistry());
        // Alliander PET
        registry.put(messageSpec(SecurityMessage.GENERATE_NEW_PUBLIC_KEY), new SimpleTagMessageEntry("GenerateNewPublicKey"));
        registry.put(messageSpec(SecurityMessage.GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM), new MultipleAttributeMessageEntry("GenerateNewPublicKey", "Random 32 bytes (optional)"));
        registry.put(messageSpec(SecurityMessage.SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP), new SimpleValueMessageEntry("SetPublicKeysOfAggregationGroup"));
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.deviceGroupAttributeName:
                DeviceGroup deviceGroup = (DeviceGroup) messageAttribute;
                return encodeGroup(deviceGroup);    //Notice: this method requires DB access (which should be present at this moment)
            default:
                return super.format(propertySpec, messageAttribute);
        }
    }

    /**
     * Return an XML representation of the key pairs of all devices present in the group
     *
     * @param group the {@link DeviceGroup} containing all devices
     */
    private String encodeGroup(DeviceGroup group) {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        try {
            for (Object member : group.getMembers()) {
                Device device = (Device) member;
                Register register = device.getRegister(PUBLIC_KEYS_OBISCODE);
                if (register != null) {
                    List<RegisterReading> lastXReadings = register.getLastXReadings(1);
                    if (lastXReadings.size() > 0) {
                        String keyPair = lastXReadings.get(0).getText();
                        builder.append("<" + KEY + String.valueOf(index) + ">");
                        builder.append(keyPair);
                        builder.append("</" + KEY + String.valueOf(index) + ">");
                        index++;
                    } else {
                        ApplicationException e = new ApplicationException("Device with serial number " + device.getSerialNumber() + " doesn't have a value for the Public Key register (" + PUBLIC_KEYS_OBISCODE.toString() + ")!");
                        throw DataParseException.generalParseException(e);
                    }
                } else {
                    ApplicationException e = new ApplicationException("Rtu with serial number " + device.getSerialNumber() + " doesn't have the Public Key register (" + PUBLIC_KEYS_OBISCODE.toString() + ") defined!");
                    throw DataParseException.generalParseException(e);
                }
            }
        } catch (ClassCastException e) {
            throw DataParseException.generalParseException(e);
        }
        return builder.toString();
    }
}

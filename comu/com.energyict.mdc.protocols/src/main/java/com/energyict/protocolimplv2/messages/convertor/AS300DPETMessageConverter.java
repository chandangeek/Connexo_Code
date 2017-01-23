package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.Register;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleValueMessageEntry;

import javax.inject.Inject;
import java.util.ArrayList;
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
    private final IdentificationService identificationService;

    @Inject
    public AS300DPETMessageConverter(IdentificationService identificationService) {
        super();
        this.identificationService = identificationService;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.deviceListAttributeName:
                String deviceIdList = (String) messageAttribute;
                return encodeDevices(this.findDevices(this.parseIds(deviceIdList)));    //Notice: this method requires DB access (which should be present at this moment)
            default:
                return super.format(propertySpec, messageAttribute);
        }
    }

    private List<DeviceIdentifier> parseIds (String deviceIds) {
        try {
            String[] stringIds = deviceIds.split(",");
            List<DeviceIdentifier> ids = new ArrayList<>(stringIds.length);
            for (String stringId : stringIds) {
                ids.add(this.identificationService.createDeviceIdentifierByDatabaseId(Long.valueOf(stringId)));
            }
            return ids;
        }
        catch (NumberFormatException e) {
            throw new GeneralParseException(MessageSeeds.GENERAL_PARSE_ERROR, e);
        }
    }

    private List<Device> findDevices (List<DeviceIdentifier> deviceIds) {
        List<Device> devices = new ArrayList<>(deviceIds.size());
        for (DeviceIdentifier deviceId : deviceIds) {
            Device device = deviceId.findDevice();
            devices.add(device);
        }
        return devices;
    }

    @Override
    protected void initializeRegistry(Map<DeviceMessageId, MessageEntryCreator> registry) {
        super.initializeRegistry(registry);
        registry.put(DeviceMessageId.SECURITY_GENERATE_NEW_PUBLIC_KEY, new SimpleTagMessageEntry("GenerateNewPublicKey"));
        registry.put(DeviceMessageId.SECURITY_GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM, new MultipleAttributeMessageEntry("GenerateNewPublicKey", "Random 32 bytes (optional)"));
        registry.put(DeviceMessageId.SECURITY_SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP, new SimpleValueMessageEntry("SetPublicKeysOfAggregationGroup"));
    }

    /**
     * Returns an XML representation of the key pairs of all devices in the List.
     * @param devices The List of {@link com.energyict.mdc.upl.meterdata.Device}s
     */
    private String encodeDevices (List<Device> devices) {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        try {
            for (Device device : devices) {
                Register register = device.getRegisterWithDeviceObisCode(PUBLIC_KEYS_OBISCODE);
                if (register != null) {
                    // TODO need to find a proper way to get the readings of the registers in order for this message to properly work
//                    List<RegisterReading> lastXReadings = register.getLastTextRegisterReadings(1);
//                    if (!lastXReadings.isEmpty()) {
//                        String keyPair = lastXReadings.get(0).getText();
//                        builder.append("<" + KEY).append(String.valueOf(index)).append(">");
//                        builder.append(keyPair);
//                        builder.append("</" + KEY).append(String.valueOf(index)).append(">");
//                        index++;
//                    } else {
                    throw new GeneralParseException(
                            MessageSeeds.GENERAL_PARSE_ERROR,
                            new ApplicationException("Device with serial number " + device.getSerialNumber() + " doesn't have a value for the Public Key register (" + PUBLIC_KEYS_OBISCODE.toString() + ")!"));
//                    }
                } else {
                    throw new GeneralParseException(
                            MessageSeeds.GENERAL_PARSE_ERROR,
                            new ApplicationException("Rtu with serial number " + device.getSerialNumber() + " doesn't have the Public Key register (" + PUBLIC_KEYS_OBISCODE.toString() + ") defined!"));
                }
            }
        } catch (ClassCastException e) {
            throw new GeneralParseException(MessageSeeds.GENERAL_PARSE_ERROR, e);
        }
        return builder.toString();
    }
}

package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.meterdata.identifiers.CanFindDevice;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.dynamic.PropertySpec;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.amr.RegisterReading;
import com.energyict.mdw.core.Device;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleValueMessageEntry;

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

    static {
        // Alliander PET
        registry.put(SecurityMessage.GENERATE_NEW_PUBLIC_KEY, new SimpleTagMessageEntry("GenerateNewPublicKey"));
        registry.put(SecurityMessage.GENERATE_NEW_PUBLIC_KEY_FROM_RANDOM, new MultipleAttributeMessageEntry("GenerateNewPublicKey", "Random 32 bytes (optional)"));
        registry.put(SecurityMessage.SET_PUBLIC_KEYS_OF_AGGREGATION_GROUP, new SimpleValueMessageEntry("SetPublicKeysOfAggregationGroup"));
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public AS300DPETMessageConverter() {
        super();
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

    private List<CanFindDevice> parseIds (String deviceIds) {
        try {
            String[] stringIds = deviceIds.split(",");
            List<CanFindDevice> ids = new ArrayList<>(stringIds.length);
            for (String stringId : stringIds) {
                ids.add(new DeviceIdentifierById(stringId));
            }
            return ids;
        }
        catch (NumberFormatException e) {
            throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
        }
    }

    private List<Device> findDevices (List<CanFindDevice> deviceIds) {
        List<Device> devices = new ArrayList<>(deviceIds.size());
        for (CanFindDevice deviceId : deviceIds) {
            Device device = deviceId.findDevice();
            devices.add(device);
        }
        return devices;
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return super.getRegistry();
    }

    /**
     * Returns an XML representation of the key pairs of all devices in the List.
     * @param devices The List of {@link Device}s
     */
    private String encodeDevices (List<Device> devices) {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        try {
            for (Device device : devices) {
                Register register = device.getRegister(PUBLIC_KEYS_OBISCODE);
                if (register != null) {
                    List<RegisterReading> lastXReadings = register.getLastXReadings(1);
                    if (!lastXReadings.isEmpty()) {
                        String keyPair = lastXReadings.get(0).getText();
                        builder.append("<" + KEY).append(String.valueOf(index)).append(">");
                        builder.append(keyPair);
                        builder.append("</" + KEY).append(String.valueOf(index)).append(">");
                        index++;
                    } else {
                        ApplicationException e = new ApplicationException("Device with serial number " + device.getSerialNumber() + " doesn't have a value for the Public Key register (" + PUBLIC_KEYS_OBISCODE.toString() + ")!");
                        throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
                    }
                } else {
                    ApplicationException e = new ApplicationException("Rtu with serial number " + device.getSerialNumber() + " doesn't have the Public Key register (" + PUBLIC_KEYS_OBISCODE.toString() + ") defined!");
                    throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
                }
            }
        } catch (ClassCastException e) {
            throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
        }
        return builder.toString();
    }
}

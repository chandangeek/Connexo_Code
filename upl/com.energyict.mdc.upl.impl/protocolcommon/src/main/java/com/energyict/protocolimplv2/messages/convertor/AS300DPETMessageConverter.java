package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.ApplicationException;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.amr.RegisterReading;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.Group;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocolimplv2.MdcManager;
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

    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(AS300MessageConverter.registry);

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

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
            case DeviceMessageConstants.deviceGroupAttributeName:
                Group deviceGroup = (Group) messageAttribute;
                return encodeGroup(deviceGroup);    //Notice: this method requires DB access (which should be present at this moment)
            default:
                return super.format(propertySpec, messageAttribute);
        }
    }

    /**
     * Return an XML representation of the key pairs of all devices present in the group
     *
     * @param group the {@link Group} containing all devices
     */
    private String encodeGroup(Group group) {
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

package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.protocol.api.dynamic.PropertySpec;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.digitalOutputAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.relayNumberAttributeName;

/**
 * Provides a summary of all <i>Contactor</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:00
 */
public enum ContactorDeviceMessage implements DeviceMessageSpec {

    CONTACTOR_OPEN,
    CONTACTOR_OPEN_WITH_OUTPUT(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(digitalOutputAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2))
    ),
    CONTACTOR_OPEN_WITH_ACTIVATION_DATE(
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(contactorActivationDateAttributeName)),
    CONTACTOR_ARM,
    CONTACTOR_ARM_WITH_ACTIVATION_DATE(
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(contactorActivationDateAttributeName)),
    CONTACTOR_CLOSE,
    CONTACTOR_CLOSE_WITH_OUTPUT(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(digitalOutputAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2))
    ),
    CONTACTOR_CLOSE_WITH_ACTIVATION_DATE(
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(contactorActivationDateAttributeName)),
    CHANGE_CONNECT_CONTROL_MODE(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(contactorModeAttributeName,
                    new BigDecimal("0"), new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"),
                    new BigDecimal("4"), new BigDecimal("5"), new BigDecimal("6"))),
    CLOSE_RELAY(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(relayNumberAttributeName,
            new BigDecimal("1"), new BigDecimal("2"))),
    OPEN_RELAY(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(relayNumberAttributeName,
            new BigDecimal("1"), new BigDecimal("2")));

    private static final DeviceMessageCategory contactorCategory = DeviceMessageCategories.CONTACTOR;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private ContactorDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return contactorCategory;
    }

    private static String translate(final String key) {
        return UserEnvironment.getDefault().getTranslation(key);
    }

    @Override
    public String getName() {
        return translate(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return ContactorDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return deviceMessagePropertySpecs;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }
}

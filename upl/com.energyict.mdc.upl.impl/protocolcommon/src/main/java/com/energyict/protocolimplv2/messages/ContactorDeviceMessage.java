package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Provides a summary of all <i>Contactor</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:00
 */
public enum ContactorDeviceMessage implements DeviceMessageSpec {

    CONTACTOR_OPEN,
    CONTACTOR_OPEN_WITH_ACTIVATION_DATE(
            PropertySpecFactory.dateTimePropertySpec("ContactorDeviceMessage.activationdate")),
    CONTACTOR_ARM,
    CONTACTOR_ARM_WITH_ACTIVATION_DATE(
            PropertySpecFactory.dateTimePropertySpec("ContactorDeviceMessage.activationdate")),
    CONTACTOR_CLOSE,
    CONTACTOR_CLOSE_WITH_ACTIVATION_DATE(
            PropertySpecFactory.dateTimePropertySpec("ContactorDeviceMessage.activationdate")),
    CHANGE_CONNECT_CONTROL_MODE(
            PropertySpecFactory.bigDecimalPropertySpecWithValues("ContactorDeviceMessage.changemode.mode",
                    new BigDecimal("0"), new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"),
                    new BigDecimal("4"), new BigDecimal("5"), new BigDecimal("6")));

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

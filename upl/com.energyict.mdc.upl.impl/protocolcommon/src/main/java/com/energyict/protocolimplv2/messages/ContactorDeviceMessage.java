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

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Provides a summary of all <i>Contactor</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:00
 */
public enum ContactorDeviceMessage implements DeviceMessageSpec {

    CONTACTOR_OPEN(0),
    CONTACTOR_OPEN_WITH_OUTPUT(1,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(digitalOutputAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2))
    ),
    CONTACTOR_OPEN_WITH_ACTIVATION_DATE(2,
            PropertySpecFactory.dateTimePropertySpec(contactorActivationDateAttributeName)),
    CONTACTOR_ARM(3),
    CONTACTOR_ARM_WITH_ACTIVATION_DATE(4,
            PropertySpecFactory.dateTimePropertySpec(contactorActivationDateAttributeName)),
    CONTACTOR_CLOSE(5),
    CONTACTOR_CLOSE_WITH_OUTPUT(6,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(digitalOutputAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2))
    ),
    CONTACTOR_CLOSE_WITH_ACTIVATION_DATE(7,
            PropertySpecFactory.dateTimePropertySpec(contactorActivationDateAttributeName)),
    CHANGE_CONNECT_CONTROL_MODE(8,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(contactorModeAttributeName,
                    new BigDecimal("0"), new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"),
                    new BigDecimal("4"), new BigDecimal("5"), new BigDecimal("6"))),
    CLOSE_RELAY(9, PropertySpecFactory.bigDecimalPropertySpecWithValues(relayNumberAttributeName,
            new BigDecimal("1"), new BigDecimal("2"))),
    OPEN_RELAY(10, PropertySpecFactory.bigDecimalPropertySpecWithValues(relayNumberAttributeName,
            new BigDecimal("1"), new BigDecimal("2"))),
    SET_RELAY_CONTROL_MODE(11,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(relayNumberAttributeName,
                    new BigDecimal("1"), new BigDecimal("2")),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(contactorModeAttributeName,
                    new BigDecimal("0"), new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"),
                    new BigDecimal("4"), new BigDecimal("5"), new BigDecimal("6"))),
    CONTACTOR_OPEN_WITH_OUTPUT_AND_ACTIVATION_DATE(12,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(digitalOutputAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.dateTimePropertySpec(contactorActivationDateAttributeName)),
    CONTACTOR_CLOSE_WITH_OUTPUT_AND_ACTIVATION_DATE(13,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(digitalOutputAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.dateTimePropertySpec(contactorActivationDateAttributeName)),
    CONTACTOR_OPEN_WITH_DATA_PROTECTION(14),
    CONTACTOR_CLOSE_WITH_DATA_PROTECTION(15),
    ;
    private static final DeviceMessageCategory contactorCategory = DeviceMessageCategories.CONTACTOR;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private ContactorDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    private static String translate(final String key) {
        return UserEnvironment.getDefault().getTranslation(key);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return contactorCategory;
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

    @Override
    public int getMessageId() {
        return id;
    }
}

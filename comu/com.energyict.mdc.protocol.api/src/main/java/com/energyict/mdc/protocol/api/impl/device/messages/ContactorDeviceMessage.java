package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.contactorModeAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.digitalOutputAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.relayNumberAttributeName;

/**
 * Provides a summary of all <i>Contactor</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:00
 */
public enum ContactorDeviceMessage implements DeviceMessageSpecEnum {

    CONTACTOR_OPEN(DeviceMessageId.CONTACTOR_OPEN, "Contactor open"),
    CONTACTOR_OPEN_WITH_OUTPUT(DeviceMessageId.CONTACTOR_OPEN_WITH_OUTPUT, "Contactor open with output") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(digitalOutputAttributeName, true, BigDecimal.valueOf(1), BigDecimal.valueOf(2)));
        }
    },
    CONTACTOR_OPEN_WITH_ACTIVATION_DATE(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE, "Contactor open with activate date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(contactorActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    CONTACTOR_ARM(DeviceMessageId.CONTACTOR_ARM, "Contactor arm"),
    CONTACTOR_ARM_WITH_ACTIVATION_DATE(DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE, "Contactor arm with activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(contactorActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    CONTACTOR_CLOSE(DeviceMessageId.CONTACTOR_CLOSE, "Contactor close"),
    CONTACTOR_CLOSE_WITH_OUTPUT(DeviceMessageId.CONTACTOR_CLOSE_WITH_OUTPUT, "Contactor close with output") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(digitalOutputAttributeName, true, BigDecimal.ONE, BigDecimals.TWO));
        }
    },
    CONTACTOR_CLOSE_WITH_ACTIVATION_DATE(DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE, "Contactor close with activation date") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(contactorActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    CHANGE_CONNECT_CONTROL_MODE(DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE, "Change the connect control mode") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(
                    propertySpecService.
                            bigDecimalPropertySpecWithValues(
                                    contactorModeAttributeName, true,
                                    BigDecimal.ZERO,
                                    BigDecimal.ONE,
                                    BigDecimals.TWO,
                                    BigDecimals.THREE,
                                    BigDecimals.FOUR,
                                    BigDecimals.FIVE,
                                    BigDecimals.SIX));
        }
    },
    CLOSE_RELAY(DeviceMessageId.CONTACTOR_CLOSE_RELAY, "Close relay") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(relayNumberAttributeName, true, BigDecimal.ONE, BigDecimals.TWO));
        }
    },
    OPEN_RELAY(DeviceMessageId.CONTACTOR_OPEN_RELAY, "Open relay") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(relayNumberAttributeName, true, BigDecimal.ONE, BigDecimals.TWO));
        }
    }, SET_RELAY_CONTROL_MODE(DeviceMessageId.CONTACTOR_SET_RELAY_CONTROL_MODE, "Set relay control mode"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(relayNumberAttributeName, true, BigDecimal.ONE, BigDecimals.TWO));
            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(contactorModeAttributeName, true,
                    BigDecimal.ONE, BigDecimals.TWO, BigDecimals.THREE, BigDecimals.FOUR, BigDecimals.FIVE, BigDecimals.SIX));
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    ContactorDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    public String getKey() {
        return ContactorDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id = id;
    }

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    @Override
    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}

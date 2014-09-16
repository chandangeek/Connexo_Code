package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.alarmFilterAttributeName;

/**
 * Provides a summary of all messages related to configuring alarms
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 8:38
 */
public enum AlarmConfigurationMessage implements DeviceMessageSpecEnum {

    RESET_ALL_ALARM_BITS(DeviceMessageId.ALARM_CONFIGURATION_RESET_ALL_ALARM_BITS, "Reset all alarm bits"),
    WRITE_ALARM_FILTER(DeviceMessageId.ALARM_CONFIGURATION_WRITE_ALARM_FILTER, "Write alarm filter") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(alarmFilterAttributeName, true, new BigDecimalFactory()));
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    AlarmConfigurationMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getNameResourceKey() {
        return AlarmConfigurationMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String defaultTranslation() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id;
    }

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}
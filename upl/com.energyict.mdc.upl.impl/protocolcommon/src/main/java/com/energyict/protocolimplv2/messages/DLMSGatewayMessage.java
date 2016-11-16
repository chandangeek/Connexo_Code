package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.protocolimplv2.messages.enums.DLMSGatewayNotificationRelayType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


public enum DLMSGatewayMessage implements DeviceMessageSpec{
    MeterPushNotificationSettings(0,
            PropertySpecFactory.stringPropertySpecWithValues(
                    DeviceMessageConstants.RelayMeterNotifications,
                    DLMSGatewayNotificationRelayType.getOptionNames()),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.DecipherMeterNotifications),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.DropUnencryptedMeterNotifications)
            );

    private final int id;

    private static final DeviceMessageCategory category = DeviceMessageCategories.DLMS_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private DLMSGatewayMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    private static String translate(final String key) {
        return UserEnvironment.getDefault().getTranslation(key);
    }

    private String getNameResourceKey() {
        return DLMSGatewayMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return translate(this.getNameResourceKey());
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

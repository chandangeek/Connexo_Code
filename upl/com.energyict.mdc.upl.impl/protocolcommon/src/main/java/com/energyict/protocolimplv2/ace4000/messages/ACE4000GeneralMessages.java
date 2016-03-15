package com.energyict.protocolimplv2.ace4000.messages;


import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.protocolimplv2.messages.DeviceMessageCategories;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.util.Arrays;
import java.util.List;


public enum ACE4000GeneralMessages implements DeviceMessageSpec{

    FirmwareUpgrade(0,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.URL_PATH),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.JAR_FILE_SIZE),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.JAD_FILE_SIZE)),
    Connect(1, PropertySpecFactory.datePropertySpec(DeviceMessageConstants.OPTIONAL_DATE)),
    Disconnect(2, PropertySpecFactory.datePropertySpec(DeviceMessageConstants.OPTIONAL_DATE)
    );

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;
    private static final DeviceMessageCategory category = DeviceMessageCategories.ACE4000_GENERAL;

    ACE4000GeneralMessages(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.deviceMessagePropertySpecs;
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

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return ACE4000GeneralMessages.class.getSimpleName() + "." + this.toString();
    }
}

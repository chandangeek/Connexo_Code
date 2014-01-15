package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UserFileConfigAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.xmlConfigAttributeName;

/**
 * Provides a summary of all <i>Advanced Test</i> related messages
 *
 * Copyrights EnergyICT
 * Date: 2/05/13
 * Time: 9:52
 */
public enum AdvancedTestMessage implements DeviceMessageSpec {

    XML_CONFIG(RequiredPropertySpecFactory.newInstance().stringPropertySpec(xmlConfigAttributeName)),
    USERFILE_CONFIG(RequiredPropertySpecFactory.newInstance().referencePropertySpec(UserFileConfigAttributeName, (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(FactoryIds.USERFILE.id()))),
    LogObjectList();

    private static final DeviceMessageCategory advancedTestCategory = DeviceMessageCategories.ADVANCED_TEST;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private AdvancedTestMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return advancedTestCategory;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return AdvancedTestMessage.class.getSimpleName() + "." + this.toString();
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

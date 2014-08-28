package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import com.elster.jupiter.properties.PropertySpec;

import java.util.Arrays;
import java.util.Collections;
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

    XML_CONFIG {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.<PropertySpec>asList(RequiredPropertySpecFactory.newInstance().stringPropertySpec(xmlConfigAttributeName));
        }
    },
    USERFILE_CONFIG {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            PropertySpecService propertySpecService = PropertySpecService.INSTANCE.get();
            return Arrays.asList(propertySpecService.referencePropertySpec(UserFileConfigAttributeName, true, FactoryIds.USERFILE));
        }
    },
    LogObjectList {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    };

    private static final DeviceMessageCategory advancedTestCategory = DeviceMessageCategories.ADVANCED_TEST;

    @Override
    public DeviceMessageCategory getCategory() {
        return advancedTestCategory;
    }

    @Override
    public String getName() {
        return this.getNameResourceKey();
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

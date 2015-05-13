package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.UserFileConfigAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.xmlConfigAttributeName;

/**
 * Provides a summary of all <i>Advanced Test</i> related messages
 *
 * Copyrights EnergyICT
 * Date: 2/05/13
 * Time: 9:52
 */
public enum AdvancedTestMessage implements DeviceMessageSpecEnum {

    XML_CONFIG(DeviceMessageId.ADVANCED_TEST_XML_CONFIG, "XML configuration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(xmlConfigAttributeName, true, new StringFactory()));
        }
    },
    USERFILE_CONFIG(DeviceMessageId.ADVANCED_TEST_USERFILE_CONFIG, "User file configuration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(UserFileConfigAttributeName, true, new StringFactory()));
        }
    },
    LogObjectList(DeviceMessageId.ADVANCED_TEST_LOG_OBJECT_LIST, "Log object list");

    private DeviceMessageId id;
    private String defaultTranslation;

    AdvancedTestMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return AdvancedTestMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String getDefaultFormat() {
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
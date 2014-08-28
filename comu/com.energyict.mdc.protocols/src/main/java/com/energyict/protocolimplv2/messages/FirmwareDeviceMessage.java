package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.DateFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.protocols.mdc.services.impl.Bus;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateURLAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateVersionNumberAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.plcTypeFirmwareUpdateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.resumeFirmwareUpdateAttributeName;

/**
 * Provides a summary of all <i>Firmware</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum FirmwareDeviceMessage implements DeviceMessageSpec {

    UPGRADE_FIRMWARE_WITH_USER_FILE {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateUserFileAttributeName, true, FactoryIds.USERFILE));
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateUserFileAttributeName, true, FactoryIds.USERFILE));
            propertySpecs.add(propertySpecService.basicPropertySpec(resumeFirmwareUpdateAttributeName, true, new BooleanFactory()));
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateUserFileAttributeName, true, FactoryIds.USERFILE));
            propertySpecs.add(propertySpecService.basicPropertySpec(resumeFirmwareUpdateAttributeName, true, new BooleanFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(plcTypeFirmwareUpdateAttributeName, true, new BooleanFactory()));
        }
    },
    UPGRADE_FIRMWARE_ACTIVATE {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateActivationDateAttributeName, true, new DateFactory()));
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateUserFileAttributeName, true, FactoryIds.USERFILE));
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateActivationDateAttributeName, true, new DateFactory()));
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_VERSION_AND_ACTIVATE {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(firmwareUpdateUserFileAttributeName, true, FactoryIds.USERFILE));
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateActivationDateAttributeName, true, new DateFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateVersionNumberAttributeName, true, new StringFactory()));
        }
    },
    UPGRADE_FIRMWARE_URL {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateURLAttributeName, true, new StringFactory()));
        }
    },
    UPGRADE_FIRMWARE_URL_AND_ACTIVATE {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateURLAttributeName, true, new StringFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(firmwareUpdateActivationDateAttributeName, true, new DateFactory()));
        }
    };

    private static final DeviceMessageCategory firmwareCategory = DeviceMessageCategories.FIRMWARE;

    @Override
    public DeviceMessageCategory getCategory() {
        return firmwareCategory;
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
        return FirmwareDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, Bus.getPropertySpecService());
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

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

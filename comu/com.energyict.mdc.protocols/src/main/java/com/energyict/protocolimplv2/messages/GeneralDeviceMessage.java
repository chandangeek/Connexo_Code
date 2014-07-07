package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import com.energyict.protocols.mdc.services.impl.Bus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a summary of all messages that have no unique goal.
 * For example, this can be a message that writes a general value to a certain DLMS object, chosen by the user (obiscode)
 * <p/>
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 8:38
 */
public enum GeneralDeviceMessage implements DeviceMessageSpec {

    WRITE_RAW_IEC1107_CLASS {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.boundedDecimalPropertySpec(DeviceMessageConstants.IEC1107ClassIdAttributeName, true, BigDecimal.ZERO, BigDecimal.valueOf(9999)));
            propertySpecs.add(propertySpecService.boundedDecimalPropertySpec(DeviceMessageConstants.OffsetAttributeName, true, BigDecimal.ZERO, BigDecimal.valueOf(9999)));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.RawDataAttributeName, true, new HexStringFactory()));
        }
    },
    WRITE_FULL_CONFIGURATION {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(DeviceMessageConstants.configUserFileAttributeName, true, FactoryIds.USERFILE));
        }
    };

    private static final DeviceMessageCategory generalCategory = DeviceMessageCategories.GENERAL;

    @Override
    public DeviceMessageCategory getCategory() {
        return generalCategory;
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
        return GeneralDeviceMessage.class.getSimpleName() + "." + this.toString();
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

package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.mdc.services.impl.Bus;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a summary of all messages related to pricing
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 11:59
 */
public enum PricingInformationMessage implements DeviceMessageSpec {

    ReadPricingInformation {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            // No properties to add
        }
    },
    SetPricingInformation {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            propertySpecs.add(propertySpecService.referencePropertySpec(DeviceMessageConstants.PricingInformationUserFileAttributeName, true, FactoryIds.USERFILE));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.PricingInformationActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    SetStandingCharge {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.StandingChargeAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.PricingInformationActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    UpdatePricingInformation {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            propertySpecs.add(propertySpecService.referencePropertySpec(DeviceMessageConstants.PricingInformationUserFileAttributeName, true, FactoryIds.USERFILE));
        }
    };

    private static final DeviceMessageCategory category = DeviceMessageCategories.PRICING_INFORMATION;

    private static String translate(final String key) {
        return key;
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
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
        return PricingInformationMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, Bus.getPropertySpecService());
        return propertySpecs;
    }

    protected abstract void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService);

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

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.PricingInformationActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.PricingInformationUserFileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.StandingChargeAttributeName;

/**
 * Provides a summary of all messages related to pricing.
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 11:59
 */
public enum PricingInformationMessage implements DeviceMessageSpecEnum {

    ReadPricingInformation(DeviceMessageId.PRICING_GET_INFORMATION, "Read pricing information"),
    SetPricingInformation(DeviceMessageId.PRICING_SET_INFORMATION, "Set pricing information") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            propertySpecs.add(propertySpecService.referencePropertySpec(PricingInformationUserFileAttributeName, true, FactoryIds.USERFILE));
            propertySpecs.add(propertySpecService.basicPropertySpec(PricingInformationActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    SetStandingCharge(DeviceMessageId.PRICING_SET_STANDING_CHARGE, "Set standing charge") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            propertySpecs.add(propertySpecService.basicPropertySpec(StandingChargeAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(PricingInformationActivationDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    UpdatePricingInformation(DeviceMessageId.PRICING_UPDATE_INFORMATION, "Update pricing information") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            propertySpecs.add(propertySpecService.referencePropertySpec(PricingInformationUserFileAttributeName, true, FactoryIds.USERFILE));
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    PricingInformationMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return PricingInformationMessage.class.getSimpleName() + "." + this.toString();
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
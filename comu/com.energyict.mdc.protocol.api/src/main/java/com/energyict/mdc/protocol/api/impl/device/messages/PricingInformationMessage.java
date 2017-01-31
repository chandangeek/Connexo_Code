/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceMessageFile;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.List;

enum PricingInformationMessage implements DeviceMessageSpecEnum {

    ReadPricingInformation(DeviceMessageId.PRICING_GET_INFORMATION, "Read pricing information"),
    SetPricingInformation(DeviceMessageId.PRICING_SET_INFORMATION, "Set pricing information") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            propertySpecs.add(
                    propertySpecService
                            .referenceSpec(DeviceMessageFile.class)
                            .named(PricingInformationDeviceMessageAttributes.PricingInformationUserFileAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(PricingInformationDeviceMessageAttributes.PricingInformationActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetStandingCharge(DeviceMessageId.PRICING_SET_STANDING_CHARGE, "Set standing charge") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(PricingInformationDeviceMessageAttributes.StandingChargeAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(PricingInformationDeviceMessageAttributes.PricingInformationActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    UpdatePricingInformation(DeviceMessageId.PRICING_UPDATE_INFORMATION, "Update pricing information") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            propertySpecs.add(
                    propertySpecService
                            .referenceSpec(DeviceMessageFile.class)
                            .named(PricingInformationDeviceMessageAttributes.PricingInformationUserFileAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
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

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
        return propertySpecs;
    }

    protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        // Default behavior is not to add anything
    };

}
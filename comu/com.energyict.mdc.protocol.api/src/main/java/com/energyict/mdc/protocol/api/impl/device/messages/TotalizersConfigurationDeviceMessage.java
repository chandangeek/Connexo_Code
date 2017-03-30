/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.List;

enum TotalizersConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetSumMask(DeviceMessageId.TOTALIZER_CONFIGURATION_SET_SUM_MASK, "Set sum mask") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService.stringSpec().named(DeviceMessageAttributes.SetSumMaskAttributeName).fromThesaurus(thesaurus).markRequired().finish());
        }
    },
    SetSubstractMask(DeviceMessageId.TOTALIZER_CONFIGURATION_SET_SUBSTRACT_MASK, "Set subtract mask") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(propertySpecService.stringSpec().named(DeviceMessageAttributes.SetSubstractMaskAttributeName).fromThesaurus(thesaurus).markRequired().finish());
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    TotalizersConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }


    @Override
    public String getKey() {
        return TotalizersConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
        propertySpecs.add(
                propertySpecService
                        .bigDecimalSpec()
                        .named(DeviceMessageConstants.id, DeviceMessageAttributes.DeviceActionMessageId)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish());
    };

}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

enum ChannelConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetFunction(DeviceMessageId.CHANNEL_CONFIGURATION_SET_FUNCTION, "Set function") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(propertySpecService, thesaurus, ChannelConfigurationDeviceMessageAttributes.SetFunctionAttributeName));
        }
    },
    SetParameters(DeviceMessageId.CHANNEL_CONFIGURATION_SET_PARAMETERS, "Set parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(propertySpecService, thesaurus, ChannelConfigurationDeviceMessageAttributes.SetParametersAttributeName));
        }
    },
    SetName(DeviceMessageId.CHANNEL_CONFIGURATION_SET_NAME, "Set name") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(propertySpecService, thesaurus, ChannelConfigurationDeviceMessageAttributes.SetNameAttributeName));
        }
    },
    SetUnit(DeviceMessageId.CHANNEL_CONFIGURATION_SET_UNIT, "Set unit") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(propertySpecService, thesaurus, ChannelConfigurationDeviceMessageAttributes.SetUnitAttributeName));
        }
    },
    SetLPDivisor(DeviceMessageId.CHANNEL_CONFIGURATION_SET_LP_DIVISOR, "Set loadprofile divisor") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(this.stringProperty(propertySpecService, thesaurus, ChannelConfigurationDeviceMessageAttributes.DivisorAttributeName));
        }
    };

    /**
     * Return range 1 - 32
     */
    private static BigDecimal[] getBigDecimalValues() {
        BigDecimal[] result = new BigDecimal[32];
        for (int index = 0; index < result.length; index++) {
            result[index] = BigDecimal.valueOf(index + 1);
        }
        return result;
    }

    private DeviceMessageId id;
    private String defaultTranslation;

    ChannelConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }


    @Override
    public String getKey() {
        return ChannelConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
                        .named(ChannelConfigurationDeviceMessageAttributes.ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .addValues(getBigDecimalValues())
                        .markExhaustive()
                        .finish());
    };

    protected PropertySpec stringProperty(PropertySpecService propertySpecService, Thesaurus thesaurus, ChannelConfigurationDeviceMessageAttributes name) {
        return propertySpecService.stringSpec().named(name).fromThesaurus(thesaurus).markRequired().finish();
    }

}
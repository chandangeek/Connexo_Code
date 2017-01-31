/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.List;

enum MBusConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetMBusEvery(DeviceMessageId.MBUS_CONFIGURATION_SET_EVERY, "Set MBus every") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(ConfigurationChangeDeviceMessageAttributes.SetMBusEveryAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetMBusInterFrameTime(DeviceMessageId.MBUS_CONFIGURATION_SET_INTER_FRAME_TIME, "Set MBus inter frame time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(ConfigurationChangeDeviceMessageAttributes.SetMBusInterFrameTimeAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetMBusConfig(DeviceMessageId.MBUS_CONFIGURATION_SET_CONFIG, "Set MBus configuration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(ConfigurationChangeDeviceMessageAttributes.SetMBusConfigAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SetMBusVIF(DeviceMessageId.MBUS_CONFIGURATION_SET_VIF, "Set MBus VIF") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .hexStringSpec()
                            .named(ConfigurationChangeDeviceMessageAttributes.SetMBusVIFAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    MBusConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return MBusConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
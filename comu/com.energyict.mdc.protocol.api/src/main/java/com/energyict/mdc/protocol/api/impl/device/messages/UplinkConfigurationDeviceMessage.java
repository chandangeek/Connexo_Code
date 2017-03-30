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

enum UplinkConfigurationDeviceMessage implements DeviceMessageSpecEnum {
    EnableUplinkPing(DeviceMessageId.UPLINK_CONFIGURATION_ENABLE_PING, "Enable uplink ping"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(UplinkDeviceMessageAttributes.enableUplinkPing)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    WriteUplinkPingDestinationAddress(DeviceMessageId.UPLINK_CONFIGURATION_WRITE_UPLINK_PING_DESTINATION_ADDRESS, "Write uplink ping destination address"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(UplinkDeviceMessageAttributes.uplinkPingDestinationAddress)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue("")
                            .finish());
        }
    },
    WriteUplinkPingInterval(DeviceMessageId.UPLINK_CONFIGURATION_WRITE_UPLINK_PING_INTERVAL, "Write uplink ping interval"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(UplinkDeviceMessageAttributes.uplinkPingInterval)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    },
    WriteUplinkPingTimeout(DeviceMessageId.UPLINK_CONFIGURATION_WRITE_UPLINK_PING_TIMEOUT, "Write uplink ping timeout"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(UplinkDeviceMessageAttributes.uplinkPingTimeout)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(BigDecimal.ZERO)
                            .finish());
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    UplinkConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    public String getKey() {
        return UplinkConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
    }

}
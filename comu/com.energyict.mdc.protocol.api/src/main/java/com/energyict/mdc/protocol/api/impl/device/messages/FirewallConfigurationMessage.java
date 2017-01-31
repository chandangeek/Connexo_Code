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
import java.util.stream.Stream;

enum FirewallConfigurationMessage implements DeviceMessageSpecEnum {

    ActivateFirewall(DeviceMessageId.FIREWALL_ACTIVATE_FIREWALL, "Activate the firewall"),
    DeactivateFirewall(DeviceMessageId.FIREWALL_DEACTIVATE_FIREWALL, "Deactivate the firewall"),
    ConfigureFWWAN(DeviceMessageId.FIREWALL_CONFIGURE_FW_WAN, "Configure the WAN firewall") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(FirewallDeviceMessageAttributes.EnableDLMS, FirewallDeviceMessageAttributes.EnableHTTP, FirewallDeviceMessageAttributes.EnableSSH)
                .map(name -> propertySpecService
                        .booleanSpec()
                        .named(name)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish())
                .forEach(propertySpecs::add);
        }
    },
    ConfigureFWLAN(DeviceMessageId.FIREWALL_CONFIGURE_FW_LAN, "Configure the LAN firewall") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(FirewallDeviceMessageAttributes.EnableDLMS, FirewallDeviceMessageAttributes.EnableHTTP, FirewallDeviceMessageAttributes.EnableSSH)
                    .map(name -> propertySpecService
                            .booleanSpec()
                            .named(name)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish())
                    .forEach(propertySpecs::add);
        }
    },
    ConfigureFWGPRS(DeviceMessageId.FIREWALL_CONFIGURE_FW_GPRS, "Configure the GPRS firewall") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(FirewallDeviceMessageAttributes.EnableDLMS, FirewallDeviceMessageAttributes.EnableHTTP, FirewallDeviceMessageAttributes.EnableSSH)
                    .map(name -> propertySpecService
                            .booleanSpec()
                            .named(name)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish())
                    .forEach(propertySpecs::add);
        }
    },
    SetFWDefaultState(DeviceMessageId.FIREWALL_SET_FW_DEFAULT_STATE, "Set default") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(FirewallDeviceMessageAttributes.defaultEnabled)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    FirewallConfigurationMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    public String getKey() {
        return FirewallConfigurationMessage.class.getSimpleName() + "." + this.toString();
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
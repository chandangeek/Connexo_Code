/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.LoadProfileMode;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.loadProfileAttributeName;

enum LoadProfileMessage implements DeviceMessageSpecEnum {

    PARTIAL_LOAD_PROFILE_REQUEST(DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST, "Partial load profile request") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .referenceSpec(BaseLoadProfile.class)
                            .named(loadProfileAttributeName, LoadProfileDeviceMessageAttributes.loadProfileAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageConstants.fromDateAttributeName, LoadProfileDeviceMessageAttributes.LoadProfileMessageFromDate)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageConstants.toDateAttributeName, LoadProfileDeviceMessageAttributes.LoadProfileMessageToDate)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ResetActiveImportLP(DeviceMessageId.LOAD_PROFILE_RESET_ACTIVE_IMPORT, "Reset active import load profile"),
    ResetActiveExportLP(DeviceMessageId.LOAD_PROFILE_RESET_ACTIVE_EXPORT, "Reset active export load profile"),
    ResetDailyProfile(DeviceMessageId.LOAD_PROFILE_RESET_DAILY, "Reset daily laod provile"),
    ResetMonthlyProfile(DeviceMessageId.LOAD_PROFILE_RESET_MONTHLY, "Reset monthly load profile"),
    WRITE_CAPTURE_PERIOD_LP1(DeviceMessageId.LOAD_PROFILE_WRITE_CAPTURE_PERIOD_LP1, "Write capture period of load profile 1") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(DeviceMessageAttributes.capturePeriodAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    WRITE_CAPTURE_PERIOD_LP2(DeviceMessageId.LOAD_PROFILE_WRITE_CAPTURE_PERIOD_LP2, "Write capture period of load profile 2") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(DeviceMessageAttributes.capturePeriodAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    WriteConsumerProducerMode(DeviceMessageId.LOAD_PROFILE_WRITE_CONSUMER_PRODUCER_MODE, "Write consumer producer mode") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                        .stringSpec()
                        .named(DeviceMessageAttributes.consumerProducerModeAttributeName)
                        .fromThesaurus(thesaurus)
                        .addValues(LoadProfileMode.getAllDescriptions())
                        .markExhaustive()
                        .markRequired()
                        .finish());
        }
    },
    LOAD_PROFILE_REGISTER_REQUEST(DeviceMessageId.LOAD_PROFILE_REGISTER_REQUEST, "Load profile register request") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .referenceSpec(BaseLoadProfile.class)
                            .named(loadProfileAttributeName, LoadProfileDeviceMessageAttributes.loadProfileAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageConstants.fromDateAttributeName, LoadProfileDeviceMessageAttributes.LoadProfileMessageFromDate)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    LoadProfileMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return LoadProfileMessage.class.getSimpleName() + "." + this.toString();
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
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.LoadControlActions;
import com.energyict.mdc.protocol.api.device.messages.MonitoredValue;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

enum LoadBalanceDeviceMessage implements DeviceMessageSpecEnum {

    WriteControlThresholds(DeviceMessageId.LOAD_BALANCING_WRITE_CONTROL_THRESHOLDS, "Write control thresholds") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream
                    .of(DeviceMessageAttributes.controlThreshold1dAttributeName, DeviceMessageAttributes.controlThreshold2dAttributeName, DeviceMessageAttributes.controlThreshold3dAttributeName, DeviceMessageAttributes.controlThreshold4dAttributeName, DeviceMessageAttributes.controlThreshold5dAttributeName, DeviceMessageAttributes.controlThreshold6dAttributeName)
                    .map(attributeName -> propertySpecService
                            .bigDecimalSpec()
                            .named(attributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish())
                    .forEach(propertySpecs::add);
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageAttributes.activationDatedAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired().finish());
        }
    },
    SetDemandCloseToContractPowerThreshold(DeviceMessageId.LOAD_BALANCING_SET_DEMAND_CLOSE_TO_CONTRACT_POWER_THRESHOLD, "Set treshold for demand close to contract power") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageAttributes.DemandCloseToContractPowerThresholdAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    CONFIGURE_LOAD_LIMIT_PARAMETERS(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS, "Configure the load limit parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.normalThresholdAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.emergencyThresholdAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(DeviceMessageAttributes.overThresholdDurationAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            Stream.of(DeviceMessageAttributes.emergencyProfileIdAttributeName, DeviceMessageAttributes.emergencyProfileActivationDateAttributeName, DeviceMessageAttributes.emergencyProfileDurationAttributeName)
                    .map(attributeName -> propertySpecService
                            .bigDecimalSpec()
                            .named(attributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish())
                    .forEach(propertySpecs::add);
        }
    },
    CONFIGURE_LOAD_LIMIT_PARAMETERS_Z3(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS_Z3, "Configure load limit parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(DeviceMessageAttributes.readFrequencyInMinutesAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.normalThresholdAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(DeviceMessageAttributes.overThresholdDurationAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
            Stream.of(DeviceMessageAttributes.invertDigitalOutput1AttributeName, DeviceMessageAttributes.invertDigitalOutput2AttributeName)
                    .map(attributeName -> propertySpecService
                            .booleanSpec()
                            .named(attributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish())
                    .forEach(propertySpecs::add);
            propertySpecs.add(
                    propertySpecService
                            .booleanSpec()
                            .named(DeviceMessageAttributes.activateNowAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS(DeviceMessageId.LOAD_BALANCING_CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS, "Configure all load limit parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.monitoredValueAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(MonitoredValue.TotalInstantCurrent.getDescription())
                            .addValues(MonitoredValue.getAllDescriptions())
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.normalThresholdAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.emergencyThresholdAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            Stream.of(DeviceMessageAttributes.overThresholdDurationAttributeName, DeviceMessageAttributes.underThresholdDurationAttributeName)
                    .map(attributeName -> propertySpecService
                            .timeDurationSpec()
                            .named(attributeName)
                            .fromThesaurus(thesaurus)
                            .finish())
                    .forEach(propertySpecs::add);
            Stream.of(DeviceMessageAttributes.emergencyProfileIdAttributeName, DeviceMessageAttributes.emergencyProfileActivationDateAttributeName, DeviceMessageAttributes.emergencyProfileDurationAttributeName, DeviceMessageAttributes.emergencyProfileGroupIdListAttributeName)
                    .map(attributeName -> propertySpecService
                            .bigDecimalSpec()
                            .named(attributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish())
                    .forEach(propertySpecs::add);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.emergencyProfileIdAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(DeviceMessageAttributes.emergencyProfileActivationDateAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(DeviceMessageAttributes.emergencyProfileDurationAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.emergencyProfileGroupIdListAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.actionWhenUnderThresholdAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .setDefaultValue(LoadControlActions.Nothing.getDescription())
                            .addValues(LoadControlActions.getAllDescriptions())
                            .finish());
        }
    },
    CONFIGURE_LOAD_LIMIT_PARAMETERS_FOR_GROUP(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS_FOR_GROUP, "Configure the load limit parameters for group") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            Stream.of(DeviceMessageAttributes.loadLimitGroupIDAttributeName, DeviceMessageAttributes.powerLimitThresholdAttributeName, DeviceMessageAttributes.contractualPowerLimitAttributeName)
                    .map(attributeName -> propertySpecService
                            .bigDecimalSpec()
                            .named(attributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish())
                    .forEach(propertySpecs::add);
        }
    },
    SET_EMERGENCY_PROFILE_GROUP_IDS(DeviceMessageId.LOAD_BALANCING_SET_EMERGENCY_PROFILE_GROUP_IDS, "Set the load limit emergency profiles") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.emergencyProfileGroupIdListAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CLEAR_LOAD_LIMIT_CONFIGURATION(DeviceMessageId.LOAD_BALANCING_CLEAR_LOAD_LIMIT_CONFIGURATION, "Clear the load limit configuration"),
    CLEAR_LOAD_LIMIT_CONFIGURATION_FOR_GROUP(DeviceMessageId.LOAD_BALANCING_CLEAR_LOAD_LIMIT_CONFIGURATION_FOR_GROUP, "Clear the load limit configuration for group") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.loadLimitGroupIDAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    ENABLE_LOAD_LIMITING(DeviceMessageId.LOAD_BALANCING_ENABLE_LOAD_LIMITING, "Enable load limiting"),
    ENABLE_LOAD_LIMITING_FOR_GROUP(DeviceMessageId.LOAD_BALANCING_ENABLE_LOAD_LIMITING_FOR_GROUP, "Enable load limiting for group") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.loadLimitGroupIDAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            Stream.of(DeviceMessageAttributes.loadLimitStartDateAttributeName, DeviceMessageAttributes.loadLimitEndDateAttributeName)
                    .map(attributeName -> propertySpecService
                            .specForValuesOf(new DateAndTimeFactory())
                            .named(attributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish())
                    .forEach(propertySpecs::add);
        }
    },
    DISABLE_LOAD_LIMITING(DeviceMessageId.LOAD_BALANCING_DISABLE_LOAD_LIMITING, "Disable load limiting"),
    CONFIGURE_SUPERVISION_MONITOR(DeviceMessageId.LOAD_BALANCING_CONFIGURE_SUPERVISION_MONITOR, "Configure supervision monitor") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.phaseAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .addValues(
                                    BigDecimal.ONE,
                                    BigDecimal.valueOf(2),
                                    BigDecimal.valueOf(3))
                            .markExhaustive()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.thresholdInAmpereAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SET_LOAD_LIMIT_DURATION(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_DURATION, "Set load limit duration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(DeviceMessageAttributes.overThresholdDurationAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SET_LOAD_LIMIT_THRESHOLD(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD, "Set load limit threshold") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.normalThresholdAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.unitAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SET_LOAD_LIMIT_THRESHOLD_WITH_TARIFFS(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD_WITH_TARIFFS, "Set load limit threshold with tariffs") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.normalThresholdAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.unitAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.tariffsAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    SET_LOAD_LIMIT_MEASUREMENT_VALUE(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_MEASUREMENT_READING_TYPE, "Set load limit measurement reading type") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .referenceSpec(ReadingType.class)
                            .named(DeviceMessageAttributes.readingTypeAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
        }
    },
    CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION, "Configure the load limit threshold and duration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.normalThresholdAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.unitAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(DeviceMessageAttributes.overThresholdDurationAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    },
    CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION_WITH_TARIFFS(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION_WITH_TARIFFS, "Configure the load limit threshold and duration with tariffs") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService, Thesaurus thesaurus) {
            super.addPropertySpecs(propertySpecs, propertySpecService, thesaurus);
            propertySpecs.add(
                    propertySpecService
                            .bigDecimalSpec()
                            .named(DeviceMessageAttributes.normalThresholdAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.unitAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .stringSpec()
                            .named(DeviceMessageAttributes.tariffsAttributeName)
                            .fromThesaurus(thesaurus)
                            .markRequired()
                            .finish());
            propertySpecs.add(
                    propertySpecService
                            .timeDurationSpec()
                            .named(DeviceMessageAttributes.overThresholdDurationAttributeName)
                            .fromThesaurus(thesaurus)
                            .finish());
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    LoadBalanceDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return LoadBalanceDeviceMessage.class.getSimpleName() + "." + this.toString();
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
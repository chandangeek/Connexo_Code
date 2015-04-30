package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.LoadControlActions;
import com.energyict.mdc.protocol.api.device.messages.MonitoredValue;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.actionWhenUnderThresholdAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activateNowAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activationDatedAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.contractualPowerLimitAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.controlThreshold1dAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.controlThreshold2dAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.controlThreshold3dAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.controlThreshold4dAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.controlThreshold5dAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.controlThreshold6dAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.emergencyProfileDurationAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.emergencyProfileGroupIdListAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.emergencyProfileIdAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.emergencyThresholdAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.invertDigitalOutput1AttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.invertDigitalOutput2AttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.loadLimitEndDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.loadLimitGroupIDAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.loadLimitStartDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.monitoredValueAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.normalThresholdAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.phaseAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.powerLimitThresholdAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.readFrequencyInMinutesAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.thresholdInAmpereAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.underThresholdDurationAttributeName;

/**
 * Provides a summary of all messages related to
 * <ul>
 * <li>Load limiting</li>
 * <li>Energy balance</li>
 * <li>Grid stability</li>
 * </ul>
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 9:43
 */
public enum LoadBalanceDeviceMessage implements DeviceMessageSpecEnum {

    WriteControlThresholds(DeviceMessageId.LOAD_BALANCING_WRITE_CONTROL_THRESHOLDS, "Write control thresholds") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(controlThreshold1dAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(controlThreshold2dAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(controlThreshold3dAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(controlThreshold4dAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(controlThreshold5dAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(controlThreshold6dAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(activationDatedAttributeName, true, new DateAndTimeFactory()));
        }
    },
    SetDemandCloseToContractPowerThreshold(DeviceMessageId.LOAD_BALANCING_SET_DEMAND_CLOSE_TO_CONTRACT_POWER_THRESHOLD, "Set treshold for demand close to contract power") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.DemandCloseToContractPowerThresholdAttributeName, true, new DateAndTimeFactory()));
        }
    },
    CONFIGURE_LOAD_LIMIT_PARAMETERS(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS, "Configure the load limit parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(normalThresholdAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(emergencyThresholdAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(overThresholdDurationAttributeName, true, new TimeDurationValueFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(emergencyProfileIdAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(emergencyProfileActivationDateAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(emergencyProfileDurationAttributeName, true, new BigDecimalFactory()));
        }
    },
    CONFIGURE_LOAD_LIMIT_PARAMETERS_Z3(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS_Z3, "Configure load limit parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(readFrequencyInMinutesAttributeName, true, new TimeDurationValueFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(normalThresholdAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(overThresholdDurationAttributeName, true, new TimeDurationValueFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(invertDigitalOutput1AttributeName, true, new BooleanFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(invertDigitalOutput2AttributeName, true, new BooleanFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(activateNowAttributeName, true, new BooleanFactory()));
        }
    },
    CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS(DeviceMessageId.LOAD_BALANCING_CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS, "Configure all load limit parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(
                    propertySpecService.
                            stringPropertySpecWithValuesAndDefaultValue(
                                    monitoredValueAttributeName,
                                    true,
                                    MonitoredValue.TotalInstantCurrent.getDescription(),
                                    MonitoredValue.getAllDescriptions())
            );
            propertySpecs.add(propertySpecService.basicPropertySpec(normalThresholdAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(emergencyThresholdAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(overThresholdDurationAttributeName, true, new TimeDurationValueFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(underThresholdDurationAttributeName, true, new TimeDurationValueFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(emergencyProfileIdAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(emergencyProfileActivationDateAttributeName, true, new DateAndTimeFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(emergencyProfileDurationAttributeName, true, new TimeDurationValueFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(emergencyProfileGroupIdListAttributeName, true, new StringFactory()));
            propertySpecs.add(
                    propertySpecService.
                            stringPropertySpecWithValuesAndDefaultValue(
                                    actionWhenUnderThresholdAttributeName,
                                    true,
                                    LoadControlActions.Nothing.getDescription(),
                                    LoadControlActions.getAllDescriptions()));
        }
    },
    CONFIGURE_LOAD_LIMIT_PARAMETERS_FOR_GROUP(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS_FOR_GROUP, "Configure the load limit parameters for group") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(loadLimitGroupIDAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(powerLimitThresholdAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(contractualPowerLimitAttributeName, true, new BigDecimalFactory()));
        }
    },
    SET_EMERGENCY_PROFILE_GROUP_IDS(DeviceMessageId.LOAD_BALANCING_SET_EMERGENCY_PROFILE_GROUP_IDS, "Set the load limit emergency profiles") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(emergencyProfileGroupIdListAttributeName, true, FactoryIds.LOGBOOK));
        }
    },
    CLEAR_LOAD_LIMIT_CONFIGURATION(DeviceMessageId.LOAD_BALANCING_CLEAR_LOAD_LIMIT_CONFIGURATION, "Clear the load limit configuration"),
    CLEAR_LOAD_LIMIT_CONFIGURATION_FOR_GROUP(DeviceMessageId.LOAD_BALANCING_CLEAR_LOAD_LIMIT_CONFIGURATION_FOR_GROUP, "Clear the load limit configuration for group") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(loadLimitGroupIDAttributeName, true, new BigDecimalFactory()));
        }
    },
    ENABLE_LOAD_LIMITING(DeviceMessageId.LOAD_BALANCING_ENABLE_LOAD_LIMITING, "Enable load limiting"),
    ENABLE_LOAD_LIMITING_FOR_GROUP(DeviceMessageId.LOAD_BALANCING_ENABLE_LOAD_LIMITING_FOR_GROUP, "Enable load limiting for group") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(loadLimitGroupIDAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(loadLimitStartDateAttributeName, true, new DateAndTimeFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(loadLimitEndDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    DISABLE_LOAD_LIMITING(DeviceMessageId.LOAD_BALANCING_DISABLE_LOAD_LIMITING, "Disable load limiting"),
    CONFIGURE_SUPERVISION_MONITOR(DeviceMessageId.LOAD_BALANCING_CONFIGURE_SUPERVISION_MONITOR, "Configure supervision monitor") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(
                    propertySpecService.
                            bigDecimalPropertySpecWithValues(
                                    phaseAttributeName,
                                    true,
                                    BigDecimal.ONE,
                                    BigDecimal.valueOf(2),
                                    BigDecimal.valueOf(3)));
            propertySpecs.add(propertySpecService.basicPropertySpec(thresholdInAmpereAttributeName, true, new BigDecimalFactory()));
        }
    },
    SET_LOAD_LIMIT_DURATION(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_DURATION, "Set load limit duration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(overThresholdDurationAttributeName, true, new TimeDurationValueFactory()));
        }
    },
    SET_LOAD_LIMIT_THRESHOLD(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD, "Set load limit threshold") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(normalThresholdAttributeName, true, new BigDecimalFactory()));
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
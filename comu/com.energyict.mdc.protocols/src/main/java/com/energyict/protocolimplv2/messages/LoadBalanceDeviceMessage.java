package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.dynamic.BigDecimalFactory;
import com.energyict.mdc.dynamic.BooleanFactory;
import com.energyict.mdc.dynamic.DateAndTimeFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.StringFactory;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

import com.energyict.protocolimplv2.messages.enums.LoadControlActions;
import com.energyict.protocolimplv2.messages.enums.MonitoredValue;
import com.energyict.protocols.mdc.services.impl.Bus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.actionWhenUnderThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activateNowAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activationDatedAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contractualPowerLimitAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold1dAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold2dAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold3dAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold4dAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold5dAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.controlThreshold6dAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileGroupIdListAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyProfileIdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.emergencyThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.invertDigitalOutput1AttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.invertDigitalOutput2AttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadLimitEndDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadLimitGroupIDAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadLimitStartDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.monitoredValueAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.normalThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.overThresholdDurationAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.phaseAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.powerLimitThresholdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.readFrequencyInMinutesAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.thresholdInAmpereAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.underThresholdDurationAttributeName;

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
public enum LoadBalanceDeviceMessage implements DeviceMessageSpec {

    WriteControlThresholds {
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
    SetDemandCloseToContractPowerThreshold {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.DemandCloseToContractPowerThresholdAttributeName, true, new DateAndTimeFactory()));
        }
    },
    CONFIGURE_LOAD_LIMIT_PARAMETERS {
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
    CONFIGURE_LOAD_LIMIT_PARAMETERS_Z3 {
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
    CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS {
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
    CONFIGURE_LOAD_LIMIT_PARAMETERS_FOR_GROUP {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(loadLimitGroupIDAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(powerLimitThresholdAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(contractualPowerLimitAttributeName, true, new BigDecimalFactory()));
        }
    },
    SET_EMERGENCY_PROFILE_GROUP_IDS {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.referencePropertySpec(emergencyProfileGroupIdListAttributeName, true, FactoryIds.LOGBOOK));
        }
    },
    CLEAR_LOAD_LIMIT_CONFIGURATION,
    CLEAR_LOAD_LIMIT_CONFIGURATION_FOR_GROUP {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(loadLimitGroupIDAttributeName, true, new BigDecimalFactory()));
        }
    },
    ENABLE_LOAD_LIMITING,
    ENABLE_LOAD_LIMITING_FOR_GROUP {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(loadLimitGroupIDAttributeName, true, new BigDecimalFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(loadLimitStartDateAttributeName, true, new DateAndTimeFactory()));
            propertySpecs.add(propertySpecService.basicPropertySpec(loadLimitEndDateAttributeName, true, new DateAndTimeFactory()));
        }
    },
    DISABLE_LOAD_LIMITING,
    CONFIGURE_SUPERVISION_MONITOR {
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
    SET_LOAD_LIMIT_DURATION {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(overThresholdDurationAttributeName, true, new TimeDurationValueFactory()));
        }
    },
    SET_LOAD_LIMIT_THRESHOLD {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(normalThresholdAttributeName, true, new BigDecimalFactory()));
        }
    };

    private static final DeviceMessageCategory LOAD_BALANCE_CATEGORY = DeviceMessageCategories.LOAD_BALANCE;

    @Override
    public DeviceMessageCategory getCategory() {
        return LOAD_BALANCE_CATEGORY;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return LoadBalanceDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, Bus.getPropertySpecService());
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }
}

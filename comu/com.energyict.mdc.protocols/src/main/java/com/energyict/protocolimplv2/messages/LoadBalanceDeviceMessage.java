package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;
import com.energyict.protocolimplv2.messages.enums.LoadControlActions;
import com.energyict.protocolimplv2.messages.enums.MonitoredValue;

import java.math.BigDecimal;
import java.util.Arrays;
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

    WriteControlThresholds(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(controlThreshold1dAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(controlThreshold2dAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(controlThreshold3dAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(controlThreshold4dAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(controlThreshold5dAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(controlThreshold6dAttributeName),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(activationDatedAttributeName)),
    SetDemandCloseToContractPowerThreshold(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.DemandCloseToContractPowerThresholdAttributeName)),
    CONFIGURE_LOAD_LIMIT_PARAMETERS(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(normalThresholdAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(emergencyThresholdAttributeName),
            RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(overThresholdDurationAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(emergencyProfileIdAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(emergencyProfileActivationDateAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(emergencyProfileDurationAttributeName)
    ),
    CONFIGURE_LOAD_LIMIT_PARAMETERS_Z3(
            RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(readFrequencyInMinutesAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(normalThresholdAttributeName),
            RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(overThresholdDurationAttributeName),
            RequiredPropertySpecFactory.newInstance().notNullableBooleanPropertySpec(invertDigitalOutput1AttributeName),
            RequiredPropertySpecFactory.newInstance().notNullableBooleanPropertySpec(invertDigitalOutput2AttributeName),
            RequiredPropertySpecFactory.newInstance().notNullableBooleanPropertySpec(activateNowAttributeName)
    ),
    CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS(
            RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValuesAndDefaultValue(monitoredValueAttributeName, MonitoredValue.TotalInstantCurrent.getDescription(), MonitoredValue.getAllDescriptions()),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(normalThresholdAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(emergencyThresholdAttributeName),
            RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(overThresholdDurationAttributeName),
            RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(underThresholdDurationAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(emergencyProfileIdAttributeName),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(emergencyProfileActivationDateAttributeName),
            RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(emergencyProfileDurationAttributeName),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(emergencyProfileGroupIdListAttributeName),      //List of values, comma separated
            RequiredPropertySpecFactory.newInstance().stringPropertySpecWithValuesAndDefaultValue(actionWhenUnderThresholdAttributeName, LoadControlActions.Nothing.getDescription(), LoadControlActions.getAllDescriptions())
    ),
    CONFIGURE_LOAD_LIMIT_PARAMETERS_FOR_GROUP(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(loadLimitGroupIDAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(powerLimitThresholdAttributeName),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(contractualPowerLimitAttributeName)
    ),
    SET_EMERGENCY_PROFILE_GROUP_IDS(RequiredPropertySpecFactory.newInstance().lookupPropertySpec(emergencyProfileGroupIdListAttributeName)),
    CLEAR_LOAD_LIMIT_CONFIGURATION(),
    CLEAR_LOAD_LIMIT_CONFIGURATION_FOR_GROUP(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(DeviceMessageConstants.loadLimitGroupIDAttributeName)),
    ENABLE_LOAD_LIMITING(),
    ENABLE_LOAD_LIMITING_FOR_GROUP(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(loadLimitGroupIDAttributeName),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(loadLimitStartDateAttributeName),
            RequiredPropertySpecFactory.newInstance().dateTimePropertySpec(loadLimitEndDateAttributeName)
    ),
    DISABLE_LOAD_LIMITING(),
    CONFIGURE_SUPERVISION_MONITOR(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(phaseAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3)),
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(thresholdInAmpereAttributeName)
    ),
    SET_LOAD_LIMIT_DURATION(RequiredPropertySpecFactory.newInstance().timeDurationPropertySpec(overThresholdDurationAttributeName)),
    SET_LOAD_LIMIT_THRESHOLD(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(normalThresholdAttributeName));

    private static final DeviceMessageCategory LOAD_BALANCE_CATEGORY = DeviceMessageCategories.LOAD_BALANCE;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private LoadBalanceDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

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
        return this.deviceMessagePropertySpecs;
    }

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

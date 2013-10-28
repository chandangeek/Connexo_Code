package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.protocolimplv2.messages.enums.LoadControlActions;
import com.energyict.protocolimplv2.messages.enums.MonitoredValue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

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

    CONFIGURE_LOAD_LIMIT_PARAMETERS(
            PropertySpecFactory.bigDecimalPropertySpec(normalThresholdAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(emergencyThresholdAttributeName),
            PropertySpecFactory.timeDurationPropertySpec(overThresholdDurationAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(emergencyProfileIdAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(emergencyProfileActivationDateAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(emergencyProfileDurationAttributeName)),
    CONFIGURE_LOAD_LIMIT_PARAMETERS_Z3(
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(readFrequencyInMinutesAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(normalThresholdAttributeName),
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(overThresholdDurationAttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(invertDigitalOutput1AttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(invertDigitalOutput2AttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(activateNowAttributeName)),
    CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS(
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(monitoredValueAttributeName, MonitoredValue.TotalInstantCurrent.getDescription(), MonitoredValue.getAllDescriptions()),
            PropertySpecFactory.bigDecimalPropertySpec(normalThresholdAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(emergencyThresholdAttributeName),
            PropertySpecFactory.timeDurationPropertySpec(overThresholdDurationAttributeName),
            PropertySpecFactory.timeDurationPropertySpec(underThresholdDurationAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(emergencyProfileIdAttributeName),
            PropertySpecFactory.dateTimePropertySpec(emergencyProfileActivationDateAttributeName),
            PropertySpecFactory.timeDurationPropertySpec(emergencyProfileDurationAttributeName),
            PropertySpecFactory.stringPropertySpec(emergencyProfileGroupIdListAttributeName),      //List of values, comma separated
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(actionWhenUnderThresholdAttributeName, LoadControlActions.Nothing.getDescription(), LoadControlActions.getAllDescriptions())),
    SET_EMERGENCY_PROFILE_GROUP_IDS(PropertySpecFactory.lookupPropertySpec(emergencyProfileGroupIdListAttributeName)),
    CLEAR_LOAD_LIMIT_CONFIGURATION(),
    ENABLE_LOAD_LIMITING(),
    DISABLE_LOAD_LIMITING(),
    CONFIGURE_SUPERVISION_MONITOR(
            PropertySpecFactory.bigDecimalPropertySpecWithValues(phaseAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3)),
            PropertySpecFactory.bigDecimalPropertySpec(thresholdInAmpereAttributeName)
    ),
    SET_LOAD_LIMIT_DURATION(PropertySpecFactory.timeDurationPropertySpec(overThresholdDurationAttributeName)),
    SET_LOAD_LIMIT_THRESHOLD(PropertySpecFactory.bigDecimalPropertySpec(normalThresholdAttributeName));

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

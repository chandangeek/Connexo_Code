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

    WriteControlThresholds(0,
            PropertySpecFactory.bigDecimalPropertySpec(controlThreshold1dAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(controlThreshold2dAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(controlThreshold3dAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(controlThreshold4dAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(controlThreshold5dAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(controlThreshold6dAttributeName),
            PropertySpecFactory.dateTimePropertySpec(activationDatedAttributeName)),
    SetDemandCloseToContractPowerThreshold(1, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.DemandCloseToContractPowerThresholdAttributeName)),
    CONFIGURE_LOAD_LIMIT_PARAMETERS(2,
            PropertySpecFactory.bigDecimalPropertySpec(normalThresholdAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(emergencyThresholdAttributeName),
            PropertySpecFactory.timeDurationPropertySpec(overThresholdDurationAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(emergencyProfileIdAttributeName),
            PropertySpecFactory.dateTimePropertySpec(emergencyProfileActivationDateAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(emergencyProfileDurationAttributeName)
    ),
    CONFIGURE_LOAD_LIMIT_PARAMETERS_Z3(3,
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(readFrequencyInMinutesAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(normalThresholdAttributeName),
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(overThresholdDurationAttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(invertDigitalOutput1AttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(invertDigitalOutput2AttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(activateNowAttributeName)
    ),
    CONFIGURE_ALL_LOAD_LIMIT_PARAMETERS(4,
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(monitoredValueAttributeName, MonitoredValue.TotalInstantCurrent.getDescription(), MonitoredValue.getAllDescriptions()),
            PropertySpecFactory.bigDecimalPropertySpec(normalThresholdAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(emergencyThresholdAttributeName),
            PropertySpecFactory.timeDurationPropertySpec(overThresholdDurationAttributeName),
            PropertySpecFactory.timeDurationPropertySpec(underThresholdDurationAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(emergencyProfileIdAttributeName),
            PropertySpecFactory.dateTimePropertySpec(emergencyProfileActivationDateAttributeName),
            PropertySpecFactory.timeDurationPropertySpec(emergencyProfileDurationAttributeName),
            PropertySpecFactory.stringPropertySpec(emergencyProfileGroupIdListAttributeName),      //List of values, comma separated
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(actionWhenUnderThresholdAttributeName, LoadControlActions.Nothing.getDescription(), LoadControlActions.getAllDescriptions())
    ),
    CONFIGURE_LOAD_LIMIT_PARAMETERS_FOR_GROUP(5,
            PropertySpecFactory.bigDecimalPropertySpec(loadLimitGroupIDAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(powerLimitThresholdAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(contractualPowerLimitAttributeName)
    ),
    SET_EMERGENCY_PROFILE_GROUP_IDS(6, PropertySpecFactory.lookupPropertySpec(emergencyProfileGroupIdListAttributeName)),
    CLEAR_LOAD_LIMIT_CONFIGURATION(7),
    CLEAR_LOAD_LIMIT_CONFIGURATION_FOR_GROUP(8, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.loadLimitGroupIDAttributeName)),
    ENABLE_LOAD_LIMITING(9),
    ENABLE_LOAD_LIMITING_FOR_GROUP(10,
            PropertySpecFactory.bigDecimalPropertySpec(loadLimitGroupIDAttributeName),
            PropertySpecFactory.dateTimePropertySpec(loadLimitStartDateAttributeName),
            PropertySpecFactory.dateTimePropertySpec(loadLimitEndDateAttributeName)
    ),
    DISABLE_LOAD_LIMITING(11),
    CONFIGURE_SUPERVISION_MONITOR(12,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(phaseAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3)),
            PropertySpecFactory.bigDecimalPropertySpec(thresholdInAmpereAttributeName)
    ),
    SET_LOAD_LIMIT_DURATION(13, PropertySpecFactory.timeDurationPropertySpec(overThresholdDurationAttributeName)),
    SET_LOAD_LIMIT_THRESHOLD(14, PropertySpecFactory.bigDecimalPropertySpec(normalThresholdAttributeName)),
    UPDATE_SUPERVISION_MONITOR(15,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(monitorInstanceAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3)),
            PropertySpecFactory.bigDecimalPropertySpec(thresholdInAmpereAttributeName)
    ),
    CONFIGURE_SUPERVISION_MONITOR_FOR_IMPORT_EXPORT(16,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(phaseAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3)),
            PropertySpecFactory.bigDecimalPropertySpec(positiveThresholdInAmpereAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(negativeThresholdInAmpereAttributeName)
    ),
    CONFIGURE_LOAD_LIMIT_PARAMETERS_EXCEPT_EMERGENCY_ONES(17,
            PropertySpecFactory.stringPropertySpec(monitoredValueAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(normalThresholdAttributeName),
            PropertySpecFactory.timeDurationPropertySpec(overThresholdDurationAttributeName),
            PropertySpecFactory.timeDurationPropertySpec(underThresholdDurationAttributeName),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(actionWhenUnderThresholdAttributeName, LoadControlActions.Nothing.getDescription(), LoadControlActions.getAllDescriptions()),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(actionWhenOverThresholdAttributeName, LoadControlActions.Nothing.getDescription(), LoadControlActions.getAllDescriptions())
    ),
    CONFIGURE_LOAD_LIMIT_PARAMETERS_ATTRIBUTES_4TO9(18,
            PropertySpecFactory.bigDecimalPropertySpec(normalThresholdAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(emergencyThresholdAttributeName),
            PropertySpecFactory.timeDurationPropertySpec(overThresholdDurationAttributeName),
            PropertySpecFactory.timeDurationPropertySpec(underThresholdDurationAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(emergencyProfileIdAttributeName),
            PropertySpecFactory.stringPropertySpec(emergencyProfileGroupIdListAttributeName),
            PropertySpecFactory.dateTimePropertySpec(emergencyProfileActivationDateAttributeName),
            PropertySpecFactory.timeDurationPropertySpec(emergencyProfileDurationAttributeName)
    ),
    ;


    private static final DeviceMessageCategory LOAD_BALANCE_CATEGORY = DeviceMessageCategories.LOAD_BALANCE;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private LoadBalanceDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
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

    @Override
    public int getMessageId() {
        return id;
    }
}

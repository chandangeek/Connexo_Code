package com.energyict.protocolimplv2.ace4000.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.protocolimplv2.messages.DeviceMessageCategories;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public enum ACE4000ConfigurationMessages implements DeviceMessageSpec  {
    SendShortDisplayMessage(0,
            PropertySpecFactory.fixedLengthStringPropertySpec(DeviceMessageConstants.SHORT_DISPLAY_MESSAGE, 8)),
    SendLongDisplayMessage(1,
            PropertySpecFactory.fixedLengthStringPropertySpec(DeviceMessageConstants.LONG_DISPLAY_MESSAGE, 1024)),
    DisplayMessage(2),
    ConfigureLCDDisplay(3,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.NUMBER_OF_DIGITS_BEFORE_COMMA,
                    new BigDecimal(5),
                    new BigDecimal(6),
                    new BigDecimal(7)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.NUMBER_OF_DIGITS_AFTER_COMMA,
                    new BigDecimal(0),
                    new BigDecimal(1),
                    new BigDecimal(2),
                    new BigDecimal(3)),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.DISPLAY_SEQUENCE),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.DISPLAY_CYCLE_TIME)
            ),
    ConfigureLoadProfileDataRecording(4,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.ENABLE_DISABLE,
                    new BigDecimal(0),
                    new BigDecimal(1)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.CONFIG_LOAD_PROFILE_INTERVAL,
                    new BigDecimal(1),
                    new BigDecimal(2),
                    new BigDecimal(3),
                    new BigDecimal(5),
                    new BigDecimal(6),
                    new BigDecimal(10),
                    new BigDecimal(12),
                    new BigDecimal(15),
                    new BigDecimal(20),
                    new BigDecimal(30),
                    new BigDecimal(60),
                    new BigDecimal(120),
                    new BigDecimal(240)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.MAX_NUMBER_RECORDS)
    ),

    ConfigureSpecialDataMode(5,
    PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DAYS),
    PropertySpecFactory.datePropertySpec(DeviceMessageConstants.SPECIAL_DATE_MODE_DURATION_DATE),
    PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING,
            new BigDecimal(0),
            new BigDecimal(1)),
    PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_INTERVAL,
            new BigDecimal(0),
            new BigDecimal(1),
            new BigDecimal(2)),
    PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.SPECIAL_BILLING_REGISTER_RECORDING_MAX_NUMBER_RECORDS, new BigDecimal(1), new BigDecimal(65535)),
    PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.SPECIAL_LOAD_PROFILE,
            new BigDecimal(0),
            new BigDecimal(1)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.SPECIAL_LOAD_PROFILE_INTERVAL,
                    new BigDecimal(1),
                    new BigDecimal(2),
                    new BigDecimal(3),
                    new BigDecimal(5),
                    new BigDecimal(6),
                    new BigDecimal(10),
                    new BigDecimal(12),
                    new BigDecimal(15),
                    new BigDecimal(20),
                    new BigDecimal(30),
                    new BigDecimal(60),
                    new BigDecimal(120),
                    new BigDecimal(240))
    ),
    ConfigureMaxDemandSettings(6,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.ACTIVE_REGISTERS_0_OR_REACTIVE_REGISTERS_1,
            new BigDecimal(0),
            new BigDecimal(1)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.NUMBER_OF_SUBINTERVALS,
                    new BigDecimal(0),
                    new BigDecimal(1),
                    new BigDecimal(2),
                    new BigDecimal(3),
                    new BigDecimal(4),
                    new BigDecimal(5),
                    new BigDecimal(10),
                    new BigDecimal(15)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.SUB_INTERVAL_DURATION,
                    new BigDecimal(30),
                    new BigDecimal(60),
                    new BigDecimal(300),
                    new BigDecimal(600),
                    new BigDecimal(900),
                    new BigDecimal(1200),
                    new BigDecimal(1800),
                    new BigDecimal(3600)),
            PropertySpecFactory.boundedDecimalPropertySpec(DeviceMessageConstants.SPECIAL_LOAD_PROFILE_MAX_NO, new BigDecimal(1), new BigDecimal(65535))
    ),
    ConfigureConsumptionLimitationsSettings(7,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.NUMBER_OF_SUBINTERVALS,
                    new BigDecimal(0),
                    new BigDecimal(1),
                    new BigDecimal(2),
                    new BigDecimal(3),
                    new BigDecimal(4),
                    new BigDecimal(5),
                    new BigDecimal(10),
                    new BigDecimal(15)),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.SUB_INTERVAL_DURATION,
                    new BigDecimal(30),
                    new BigDecimal(60),
                    new BigDecimal(300),
                    new BigDecimal(600),
                    new BigDecimal(900),
                    new BigDecimal(1200),
                    new BigDecimal(1800),
                    new BigDecimal(3600)),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.OVERRIDE_RATE),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.ALLOWED_EXCESS_TOLERANCE),
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.THRESHOLD_SELECTION,
                    new BigDecimal(0),
                    new BigDecimal(1)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE0),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE0),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.THRESHOLDS_MOMENTS),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE0),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SWITCHING_MOMENTS_DAILY_PROFILE1),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.THRESHOLDS_MOMENTS_DAILY_PROFILE1),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.THRESHOLDS_MOMENTS),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.ACTIONS_IN_HEX_DAILY_PROFILE1),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.DAY_PROFILES),
            PropertySpecFactory.datePropertySpec(DeviceMessageConstants.ACTIVATION_DATE)

            ),
    ConfigureEmergencyConsumptionLimitation(8,
    PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.DURATION_MINUTES),
    PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.TRESHOLD_VALUE) ,
    PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.TRESHOLD_UNIT,
            new BigDecimal(0),
            new BigDecimal(1)),
    PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.OVERRIDE_RATE)
    ),
    ConfigureTariffSettings(9,
    PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.UNIQUE_TARIFF_ID_NO),
    PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.NUMBER_OF_TARIFF_RATES),
    PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.CODE_TABLE_ID)
    );

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;
    private static final DeviceMessageCategory category = DeviceMessageCategories.ACE4000_CONFIGURATION;

    ACE4000ConfigurationMessages(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }



    @Override
    public DeviceMessageCategory getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
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

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return ACE4000ConfigurationMessages.class.getSimpleName() + "." + this.toString();
    }
}

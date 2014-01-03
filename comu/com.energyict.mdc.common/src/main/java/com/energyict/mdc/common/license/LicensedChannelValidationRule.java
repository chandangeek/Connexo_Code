package com.energyict.mdc.common.license;

import com.energyict.mdc.common.TranslatorProvider;

/**
 * User: gde
 * Date: 15/07/13
 */
public enum LicensedChannelValidationRule {

    INTERVAL_STATE(1, "com.energyict.mdw.validationimpl.IntervalStateValidator", "INTERVALSTATE"),
    CONSECUTIVE_ZEROS(2, "com.energyict.mdw.validationimpl.ConsecutiveZeroValidator", "CONSECUTIVEZERO"),
    PROFILE_CONSECUTIVE_ZEROS(3, "com.energyict.mdw.validationimpl.ProfileConsecutiveZeroValidator", "PROFILECONSECUTIVEZERO"),
    MAIN_CHECK(4, "com.energyict.mdw.validationimpl.MainCheckValidator", "MAINCHECK"),
    MIN_MAX(5, "com.energyict.mdw.validationimpl.MinMaxValidator", "MINMAX"),
    REFERENCE_MIN_MAX(6, "com.energyict.mdw.validationimpl.ReferenceMinMaxValidator", "REFERENCEMINMAX"),
    METER_ADVANCE(7, "com.energyict.mdw.validationimpl.MeterAdvanceValidator", "METERADVANCE"),
    METER_ADVANCE_TOU(8, "com.energyict.mdw.validationimpl.MeterAdvanceTouValidator", "METERADVANCETOU"),
    TUNNEL_LOWER_LIMIT(9, "com.energyict.mdw.validationimpl.TunnelLowerLimitValidator", "TUNNELLOWERLIMIT"),
    TUNNEL_UPPER_LIMIT(10, "com.energyict.mdw.validationimpl.TunnelUpperLimitValidator", "TUNNELUPPERLIMIT"),
    UNRELIABLE_CHANGING_VALUE(11, "com.energyict.mdw.validationimpl.UnreliableChangeValidator", "UNRELIABLECHANGINGVALUE"),
    UNRELIABLE_CONSTANT_VALUE(12, "com.energyict.mdw.validationimpl.ConstantValuesValidator", "UNRELIABLECONSTANTVALUE"),
    DAILY_CONSUMPTION(13, "com.energyict.mdw.validationimpl.DayValidator", "DAYCONSUMPTION");


    private int code;
    private String className;
    private String translationKey;

    LicensedChannelValidationRule(int code, String className, String translationKey) {
        this.code = code;
        this.className = className;
        this.translationKey = translationKey;
    }

    public int getCode() {
        return this.code;
    }

    public String getClassName() {
        return this.className;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public static LicensedChannelValidationRule fromCode(int code) {
        for (LicensedChannelValidationRule each : LicensedChannelValidationRule.values()) {
            if (each.getCode()==code) {
                return each;
            }
        }
        return null;
    }

    // We can't work with Utils.translateEnum()
    // The names of the 'standard' ValidatorFactory's are already saved in the dataabse.
    // So we keep using these names (should match)
    public static String getLocalizedName(LicensedChannelValidationRule rule) {
        if (rule == null) {
            return "?";
        }
        else {
            return TranslatorProvider.instance.get().getTranslator().getTranslation(rule.getTranslationKey());
        }
    }

}

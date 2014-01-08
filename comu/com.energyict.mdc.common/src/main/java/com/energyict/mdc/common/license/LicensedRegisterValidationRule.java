package com.energyict.mdc.common.license;

import com.energyict.mdc.common.TranslationSupport;

/**
 * User: gde
 * Date: 15/07/13
 */
public enum LicensedRegisterValidationRule {

    READING_FLAGS(1, "com.energyict.validation.ReadingFlagsValidator"),
    REGISTER_ADVANCE(2, "com.energyict.validation.RegisterAdvanceValidator"),
    REFERENCE_REGISTER(3, "com.energyict.validation.MainCheckValidator"),
    MIN_MAX(4, "com.energyict.validation.MinMaxValidator");


    private int code;
    private String className;

    LicensedRegisterValidationRule(int code, String className) {
        this.code = code;
        this.className = className;
    }

    public int getCode() {
        return this.code;
    }

    public String getClassName() {
        return this.className;
    }

    public static LicensedRegisterValidationRule fromCode(int code) {
        for (LicensedRegisterValidationRule each : LicensedRegisterValidationRule.values()) {
            if (each.getCode()==code) {
                return each;
            }
        }
        return null;
    }

    public static String getLocalizedName(LicensedRegisterValidationRule rule) {
        return TranslationSupport.translateEnum(rule, true);
    }

}
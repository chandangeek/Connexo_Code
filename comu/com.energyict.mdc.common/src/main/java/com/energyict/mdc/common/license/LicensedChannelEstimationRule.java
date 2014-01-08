package com.energyict.mdc.common.license;

import com.energyict.mdc.common.TranslationSupport;

/**
 * User: gde
 * Date: 16/07/13
 */
public enum LicensedChannelEstimationRule {

    SHORT_PERIOD_ADVANCE(1, "com.energyict.estimate.ShortPeriodEstimator"),
    SHORT_PERIOD_ADVANCE_TOU(2,"com.energyict.estimate.ShortPeriodTouEstimator"),
    INTERPOLATION(3, "com.energyict.estimate.InterpolationEstimator"),
    SINGLE_PERIOD_WITHOUT_ADVANCE(4, "com.energyict.estimate.SinglePeriodWithoutAdvanceEstimator"),
    SINGLE_PERIOD_WITH_ADVANCE(5, "com.energyict.estimate.SinglePeriodWithAdvanceEstimator"),
    NEAREST_AVG_WITHOUT_ADVANCE(6, "com.energyict.estimate.NearestAvgWithoutAdvanceEstimator"),
    NEAREST_AVG_WITH_ADVANCE(7, "com.energyict.estimate.NearestAvgWithAdvanceEstimator"),
    NEAREST_AVG_WITH_ADVANCE_TOU(8, "com.energyict.estimate.NearestAvgWithAdvanceTouEstimator"),
    CUSTOM_PERIOD_WITHOUT_ADVANCE(9, "com.energyict.estimate.CustomPeriodWithoutAdvanceEstimator"),
    CUSTOM_PERIOD_WITH_ADVANCE(10, "com.energyict.estimate.CustomPeriodWithAdvanceEstimator"),
    CUSTOM_PERIOD_WITH_ADVANCE_TOU(11, "com.energyict.estimate.CustomPeriodWithAdvanceTouEstimator"),
    AVERAGE_WITHOUT_ADVANCE(12, "com.energyict.estimate.AverageWithoutAdvanceEstimator"),
    AVERAGE_WITH_ADVANCE(13, "com.energyict.estimate.AverageWithAdvanceEstimator"),
    AVERAGE_BY_CODE_WITHOUT_ADVANCE(14, "com.energyict.estimate.AvgByCodeWithoutAdvanceEstimator"),
    AVERAGE_BY_CODE_WITH_ADVANCE(15, "com.energyict.estimate.AvgByCodeWithAdvanceEstimator"),
    AVERAGE_BY_CODE_WITH_ADVANCE_TOU(16, "com.energyict.estimate.AvgByCodeWithAdvanceTouEstimator"),
    PEAK_DISTRIBUTION(17, "com.energyict.estimate.PeakDistributionEstimator"),
    ZERO_ESTIMATOR(18, "com.energyict.estimate.ZeroEstimator"),
    MAIN_CHECK(19, "com.energyict.estimate.MainCheckEstimator"),
    INTERPOLATION_WITH_CHANNEL_ADVANCE(20, "com.energyict.estimate.RawValueEstimator");


    private int code;
    private String className;

    LicensedChannelEstimationRule(int code, String className) {
        this.code = code;
        this.className = className;
    }

    public int getCode() {
        return this.code;
    }

    public String getClassName() {
        return this.className;
    }

    public static LicensedChannelEstimationRule fromCode(int code) {
        for (LicensedChannelEstimationRule each : LicensedChannelEstimationRule.values()) {
            if (each.getCode()==code) {
                return each;
            }
        }
        return null;
    }

    public static LicensedChannelEstimationRule fromClassName(String className) {
        for (LicensedChannelEstimationRule each : LicensedChannelEstimationRule.values()) {
            if (each.getClassName().compareToIgnoreCase(className)==0) {
                return each;
            }
        }
        return null;
    }

    public static String getLocalizedName(LicensedChannelEstimationRule rule) {
        return TranslationSupport.translateEnum(rule, true);
    }

}
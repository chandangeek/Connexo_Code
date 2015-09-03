package com.elster.jupiter.cbo.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    DATAVALID("ReadingQualityDataValid", "Data Valid"),
    VALIDATED("ReadingQualityValidated", "Validated"),
    DIAGNOSTICSFLAG("ReadingQualityDiagnosticsFlag", "Diagnostics Flag"),
    BATTERYLOW("ReadingQualityBatteryLow", "Battery Low"),
    SENSORFAILURE("ReadingQualitySensorFailure", "Sensor Failure"),
    WATCHDOGFLAG("ReadingQualityWatchdogFlag", "Watchdog Flag"),
    PARITYERROR("ReadingQualityParityError", "Parity Error"),
    CRCERROR("ReadingQualityCRCError", "CRC Error"),
    RAMCHECKSUMERROR("ReadingQualityRAMChecksumError", "RAM Checksum Error"),
    ROMCHECKSUMERROR("ReadingQualityROMChecksumError", "ROM Checksum Error"),
    CLOCKERROR("ReadingQualityClockError", "Clock Error"),
    POWERQUALITYFLAG("ReadingQualityPowerQualityFlag", "Power Quality Flag"),
    EXCESSIVEOUTAGECOUNT("ReadingQualityExcessiveOutageCount", "Excessive OutageCount"),
    PQCOUNTER("ReadingQualityPqCounter", "Pq Counter"),
    SERVICEDISCONNECTSWITCHING("ReadingQualityServiceDisconnectSwitching", "Service Disconnect Switching"),
    POWERFAIL("ReadingQualityPowerFail", "Power Fail"),
    REVENUEPROTECTION("ReadingQualityRevenueProtection", "Revenue Protection"),
    COVEROPENED("ReadingQualityCoverOpened", "Cover Opened"),
    LOGICALDISCONNECT("ReadingQualityLogicalDisconnect", "Logical Disconnect"),
    REVENUEPROTECTIONSUSPECT("ReadingQualityRevenueProtectionSuspect", "Revenue Protection Suspect"),
    REVERSEROTATION("ReadingQualityReverseRotation", "Reverse Rotation"),
    STATICDATAFLAG("ReadingQualityStaticDataFlag", "Static Data Flag"),
    ALARMFLAG("ReadingQualityAlarmFlag", "Alarm Flag"),
    OVERFLOWCONDITIONDETECTED("ReadingQualityOverflowConditionDetected", "Overflow Condition Detected"),
    PARTIALINTERVAL("ReadingQualityPartialInterval", "Partial Interval"),
    LONGINTERVAL("ReadingQualityLongInterval", "Long Interval"),
    SKIPPEDINTERVAL("ReadingQualitySkippedInterval", "Skipped Interval"),
    TESTDATA("ReadingQualityTestData", "Test Data"),
    CONFIGURATIONCHANGED("ReadingQualityConfigurationChanged", "Configuration Changed"),
    NOTRECORDING("ReadingQualityNotRecording", "Not Recording"),
    RESETOCCURED("ReadingQualityResetOccurred", "Reset Occurred"),
    CLOCKCHANGED("ReadingQualityClockChanged", "Clock Changed"),
    LOADCONTROLOCCUREDD("ReadingQualityLoadControlOccurred", "Load Control Occurred"),
    DSTINEFFECT("ReadingQualityDstInEffect", "Dst In Effect"),
    CLOCKSETFORWARD("ReadingQualityClockSetForward", "Clock Set Forward"),
    CLOCKSETBACKWARD("ReadingQualityClockSetBackward", "Clock Set Backward"),
    FAILEDPROBEATTEMPT("ReadingQualityFailedProbeAttempt", "Failed Probe Attempt"),
    CUSTOMERRAD("ReadingQualityCustomerRead", "Customer Read"),
    MANUALREAD("ReadingQualityManualRead", "Manual Read"),
    DSTCHANGEOCCURED("ReadingQualityDstChangeOccurred", "Dst Change Occurred"),
    DATAOUTSIDEEXPECTEDRANGE("ReadingQualityDataOutsideExpectedRange", "Data Outside Expected Range"),
    ERRORCODE("ReadingQualityErrorCode", "Error Code"),
    SUSPECT("ReadingQualitySuspect", "Suspect"),
    KNOWNMISSINGREAD("ReadingQualityKnownMissingRead", "Known Missing Read"),
    VALIDATIONGENERIC("ReadingQualityValidationGeneric", "Failed validation - Generic"),
    ZEROUSAGE("ReadingQualityZeroUsage", "Failed validation - Zero Usages"),
    USAGEONINACTIVEMETER("ReadingQualityUsageOnInactiveMeter", "Failed validation - Usage on Inactive Meter"),
    USAGEABOVE("ReadingQualityUsageAbove", "Failed validation - Usage above Maximum"),
    USAGEBELOW("ReadingQualityUsageBelow", "Failed validation - Usage below Minimum"),
    USAGEABOVEPERCENT("ReadingQualityUsageAbovePercent", "Failed validation - Usage above Maximum Percentage"),
    USAGEBELOWPERCENT("ReadingQualityUsageBelowPercent", "Failed validation - Usage below Minimum Percentage"),
    TOUSUMCHECK("ReadingQualityTouSumCheck", "Failed validation - TOU Sum Check Failure"),
    EDITGENERIC("ReadingQualityEditGeneric", "Manually Edited - Generic"),
    ADDED("ReadingQualityAdded", "Manually Added"),
    REJECTED("ReadingQualityRejected", "Manually Rejected"),
    ESTIMATEGENERIC("ReadingQualityEstimateGeneric", "Estimated - Generic"),
    INDETERMINATE("ReadingQualityIndeterminate", "Indeterminate"),
    ACCEPTED("ReadingQualityAccepted", "Manually Accepted"),
    DETERMINISTIC("ReadingQualityDeterministic", "Derived - Deterministic"),
    INFERRED("ReadingQualityInferred", "Derived - Inferred"),
    PROJECTEDGENERIC("ReadingQualityProjectedGeneric", "Projected - Generic"),
    ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}

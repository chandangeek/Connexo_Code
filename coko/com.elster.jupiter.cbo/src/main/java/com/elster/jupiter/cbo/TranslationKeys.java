/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    DATAVALID("ReadingQualityDataValid", "Data valid"),
    VALIDATED("ReadingQualityValidated", "Validated"),
    DIAGNOSTICSFLAG("ReadingQualityDiagnosticsFlag", "Diagnostics flag"),
    BATTERYLOW("ReadingQualityBatteryLow", "Battery low"),
    SENSORFAILURE("ReadingQualitySensorFailure", "Sensor failure"),
    WATCHDOGFLAG("ReadingQualityWatchdogFlag", "Watchdog flag"),
    PARITYERROR("ReadingQualityParityError", "Parity error"),
    CRCERROR("ReadingQualityCRCError", "CRC error"),
    RAMCHECKSUMERROR("ReadingQualityRAMChecksumError", "RAM checksum error"),
    ROMCHECKSUMERROR("ReadingQualityROMChecksumError", "ROM checksum error"),
    CLOCKERROR("ReadingQualityClockError", "Clock error"),
    POWERQUALITYFLAG("ReadingQualityPowerQualityFlag", "Power quality flag"),
    EXCESSIVEOUTAGECOUNT("ReadingQualityExcessiveOutageCount", "Excessive outage count"),
    PQCOUNTER("ReadingQualityPqCounter", "PQ counter"),
    SERVICEDISCONNECTSWITCHING("ReadingQualityServiceDisconnectSwitching", "Service disconnect switching"),
    POWERFAIL("ReadingQualityPowerFail", "Power fail"),
    POWERDOWN("ReadingQualityPowerDown", "Power down"),
    POWERUP("ReadingQualityPowerUp", "Power up"),
    PHASEFAILURE("ReadingQualityPhaseFailure", "Phase failure"),
    REVENUEPROTECTION("ReadingQualityRevenueProtection", "Revenue protection"),
    COVEROPENED("ReadingQualityCoverOpened", "Cover opened"),
    LOGICALDISCONNECT("ReadingQualityLogicalDisconnect", "Logical disconnect"),
    REVENUEPROTECTIONSUSPECT("ReadingQualityRevenueProtectionSuspect", "Revenue protection suspect"),
    REVERSEROTATION("ReadingQualityReverseRotation", "Reverse rotation"),
    STATICDATAFLAG("ReadingQualityStaticDataFlag", "Static data flag"),
    ALARMFLAG("ReadingQualityAlarmFlag", "Alarm flag"),
    OVERFLOWCONDITIONDETECTED("ReadingQualityOverflowConditionDetected", "Overflow condition detected"),
    PARTIALINTERVAL("ReadingQualityPartialInterval", "Partial interval"),
    LONGINTERVAL("ReadingQualityLongInterval", "Long interval"),
    SKIPPEDINTERVAL("ReadingQualitySkippedInterval", "Skipped interval"),
    TESTDATA("ReadingQualityTestData", "Test data"),
    CONFIGURATIONCHANGED("ReadingQualityConfigurationChanged", "Configuration changed"),
    NOTRECORDING("ReadingQualityNotRecording", "Not recording"),
    RESETOCCURED("ReadingQualityResetOccurred", "Reset occurred"),
    CLOCKCHANGED("ReadingQualityClockChanged", "Clock changed"),
    LOADCONTROLOCCUREDD("ReadingQualityLoadControlOccurred", "Load control occurred"),
    DSTINEFFECT("ReadingQualityDstInEffect", "DST in effect"),
    CLOCKSETFORWARD("ReadingQualityClockSetForward", "Clock set forward"),
    CLOCKSETBACKWARD("ReadingQualityClockSetBackward", "Clock set backward"),
    FAILEDPROBEATTEMPT("ReadingQualityFailedProbeAttempt", "Failed probe attempt"),
    CUSTOMERRAD("ReadingQualityCustomerRead", "Customer read"),
    MANUALREAD("ReadingQualityManualRead", "Manual read"),
    DSTCHANGEOCCURED("ReadingQualityDstChangeOccurred", "DST change occurred"),
    CUSTOM_OTHER("ReadingQualityOther", "Other"),
    CUSTOM_SHORTLONG("ReadingQualityShortLong", "Short long"),
    DATAOUTSIDEEXPECTEDRANGE("ReadingQualityDataOutsideExpectedRange", "Data outside expected range"),
    ERRORCODE("ReadingQualityErrorCode", "Error code"),
    SUSPECT("ReadingQualitySuspect", "Suspect"),
    KNOWNMISSINGREAD("ReadingQualityKnownMissingRead", "Known missing read"),
    VALIDATIONGENERIC("ReadingQualityValidationGeneric", "Validated by specific rule"),
    ZEROUSAGE("ReadingQualityZeroUsage", "Failed validation - zero usages"),
    USAGEONINACTIVEMETER("ReadingQualityUsageOnInactiveMeter", "Failed validation - usage on inactive meter"),
    USAGEABOVE("ReadingQualityUsageAbove", "Failed validation - usage above maximum"),
    USAGEBELOW("ReadingQualityUsageBelow", "Failed validation - usage below minimum"),
    USAGEABOVEPERCENT("ReadingQualityUsageAbovePercent", "Failed validation - usage above maximum percentage"),
    USAGEBELOWPERCENT("ReadingQualityUsageBelowPercent", "Failed validation - usage below minimum percentage"),
    TOUSUMCHECK("ReadingQualityTouSumCheck", "Failed validation - TOU sum check failure"),
    EDITGENERIC("ReadingQualityEditGeneric", "Manually edited - generic"),
    ADDED("ReadingQualityAdded", "Manually added"),
    REJECTED("ReadingQualityRejected", "Manually rejected"),
    ESTIMATEGENERIC("ReadingQualityEstimateGeneric", "Estimated with specific rule"),
    INDETERMINATE("ReadingQualityIndeterminate", "Indeterminate"),
    ACCEPTED("ReadingQualityAccepted", "Manually accepted"),
    DETERMINISTIC("ReadingQualityDeterministic", "Derived - deterministic"),
    INFERRED("ReadingQualityInferred", "Derived - inferred"),
    PROJECTEDGENERIC("ReadingQualityProjectedGeneric", "Projected - generic"),

    CATEGORY_VALID("ReadingQualityCategory_VALID", "Valid"),
    CATEGORY_DIAGNOSTICS("ReadingQualityCategory_DIAGNOSTICS", "Diagnostics"),
    CATEGORY_POWERQUALITY("ReadingQualityCategory_POWERQUALITY", "Power quality"),
    CATEGORY_TAMPER("ReadingQualityCategory_TAMPER", "Tamper"),
    CATEGORY_DATACOLLECTION("ReadingQualityCategory_DATACOLLECTION", "Data collection"),
    CATEGORY_REASONABILITY("ReadingQualityCategory_REASONABILITY", "Reasonability"),
    CATEGORY_VALIDATION("ReadingQualityCategory_VALIDATION", "Validation"),
    CATEGORY_EDITED("ReadingQualityCategory_EDITED", "Edited"),
    CATEGORY_ESTIMATED("ReadingQualityCategory_ESTIMATED", "Estimated"),
    CATEGORY_OBSOLETE_OSCILLATORY("ReadingQualityCategory_OBSOLETE_OSCILLATORY", "Oscillatory"),
    CATEGORY_QUESTIONABLE("ReadingQualityCategory_QUESTIONABLE", "Questionable"),
    CATEGORY_DERIVED("ReadingQualityCategory_DERIVED", "Derived"),
    CATEGORY_PROJECTED("ReadingQualityCategory_PROJECTED", "Projected"),

    SYSTEM_NOTAPPLICABLE("ReadingQualitySystem_NOTAPPLICABLE", "Not applicable"),
    SYSTEM_ENDDEVICE("ReadingQualitySystem_ENDDEVICE", "Device"),
    SYSTEM_MDC("ReadingQualitySystem_MDC", "MDC"),
    SYSTEM_MDM("ReadingQualitySystem_MDM", "MDM"),
    SYSTEM_OTHER("ReadingQualitySystem_OTHER", "Other system (third party)"),
    SYSTEM_EXTERNAL("ReadingQualitySystem_EXTERNAL", "Externally specified (third party)");

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

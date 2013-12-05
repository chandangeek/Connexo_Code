package com.energyict.mdc.common.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.UserEnvironment;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ObisCodeAnalyzer {

    public static String REGISTER_ACTIVE_POWER_PLUS = UserEnvironment.getDefault().getTranslation("obisWizard.activePowerPlus");
    private static String REGISTER_ACTIVE_POWER_MINUS = UserEnvironment.getDefault().getTranslation("obisWizard.activePowerMinus");
    private static String REGISTER_REACTIVE_POWER_PLUS = UserEnvironment.getDefault().getTranslation("obisWizard.reactivePowerPlus");
    private static String REGISTER_REACTIVE_POWER_MINUS = UserEnvironment.getDefault().getTranslation("obisWizard.reactivePowerMinus");
    private static String REGISTER_REACTIVE_POWER_QI = UserEnvironment.getDefault().getTranslation("obisWizard.reactivePowerQI");
    private static String REGISTER_REACTIVE_POWER_QII = UserEnvironment.getDefault().getTranslation("obisWizard.reactivePowerQII");
    private static String REGISTER_REACTIVE_POWER_QIII = UserEnvironment.getDefault().getTranslation("obisWizard.reactivePowerQIII");
    private static String REGISTER_REACTIVE_POWER_QIV = UserEnvironment.getDefault().getTranslation("obisWizard.reactivePowerQIV");
    private static String REGISTER_APPARENT_POWER_PLUS = UserEnvironment.getDefault().getTranslation("obisWizard.apparentPowerPlus");
    private static String REGISTER_APPARENT_POWER_MINUS = UserEnvironment.getDefault().getTranslation("obisWizard.apparentPowerMinus");
    private static String REGISTER_CURRENT = UserEnvironment.getDefault().getTranslation("obisWizard.current");
    private static String REGISTER_VOLTAGE = UserEnvironment.getDefault().getTranslation("obisWizard.voltage");
    private static String REGISTER_POWER_FACTOR = UserEnvironment.getDefault().getTranslation("obisWizard.powerFactor");
    private static String REGISTER_FREQUENCY = UserEnvironment.getDefault().getTranslation("obisWizard.frequency");
    private static String REGISTER_ACTIVE_POWER_Q1423 = UserEnvironment.getDefault().getTranslation("obisWizard.activePowerQ1423");
    private static String REGISTER_ACTIVE_POWER_Q14_MINUS_Q23 = UserEnvironment.getDefault().getTranslation("obisWizard.activePowerQ14MinusQ23");
    private static String REGISTER_ACTIVE_POWER_QI = UserEnvironment.getDefault().getTranslation("obisWizard.activePowerQI");
    private static String REGISTER_ACTIVE_POWER_QII = UserEnvironment.getDefault().getTranslation("obisWizard.activePowerQII");
    private static String REGISTER_ACTIVE_POWER_QIII = UserEnvironment.getDefault().getTranslation("obisWizard.activePowerQIII");
    private static String REGISTER_ACTIVE_POWER_QIV = UserEnvironment.getDefault().getTranslation("obisWizard.activePowerQIV");

    public static String ALL_PHASES = UserEnvironment.getDefault().getTranslation("obisWizard.allPhases");
    private static String PHASE_1 = UserEnvironment.getDefault().getTranslation("obisWizard.phase1");
    private static String PHASE_2 = UserEnvironment.getDefault().getTranslation("obisWizard.phase2");
    private static String PHASE_3 = UserEnvironment.getDefault().getTranslation("obisWizard.phase3");

    private static String CHOICE_A_ABSTRACT_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.abstractObisCode");
    private static String CHOICE_A_ELECTRICITY_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.electricityRelatedObisCode");
    private static String CHOICE_A_HEAT_COST_ALLOCATOR_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.heatCostAllocatorRelatedObisCode");
    private static String CHOICE_A_COOLING_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.coolingRelatedObisCode");
    private static String CHOICE_A_HEAT_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.heatRelatedObisCode");
    private static String CHOICE_A_GAS_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.gasRelatedObisCode");
    private static String CHOICE_A_COLD_WATER_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.coldWaterRelatedObisCode");
    private static String CHOICE_A_HOT_WATER_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.hotWaterRelatedObisCode");

    private static String CHOICE_C_GENERAL_SERVICE_ENTRIES = UserEnvironment.getDefault().getTranslation("obisWizard.generalServiceEntries");
    private static String CHOICE_C_GENERAL_PURPOSE = UserEnvironment.getDefault().getTranslation("obisWizard.generalPurpose");
    private static String CHOICE_C_UNITLESS_QUANTITY = UserEnvironment.getDefault().getTranslation("obisWizard.unitlessQuantity");
    private static String CHOICE_C_L0_CURRENT = UserEnvironment.getDefault().getTranslation("obisWizard.l0Current");
    private static String CHOICE_C_L0_VOLTAGE = UserEnvironment.getDefault().getTranslation("obisWizard.l0Voltage");

    private static String CHOICE_D_PARAMETER_CHANGES_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.parameterChangesRelatedObisCode");
    private static String CHOICE_D_IO_CONTROL_SIGNALS_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.IOControlSignalsRelatedObisCode");
    private static String CHOICE_D_INTERNAL_CONTROL_SIGNALS_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.internalControlSignalsRelatedObisCode");
    private static String CHOICE_D_INTERNAL_STATUS_SIGNALS_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.internalOperatingStatusRelatedObisCode");
    private static String CHOICE_D_BATTERY_ENTRIES_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.batteryEntriesRelatedObisCode");
    private static String CHOICE_D_POWER_FAILURES_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.powerFailuresRelatedObisCode");
    private static String CHOICE_D_OPERATING_TIME_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.operatingTimeRelatedObisCode");
    private static String CHOICE_D_ENVIRONMENTAL_OBIS_CODE = UserEnvironment.getDefault().getTranslation("obisWizard.environmentalRelatedObisCode");
    private static String CHOICE_D_BILLING_PERIOD_COUNTER_ENTRIES = UserEnvironment.getDefault().getTranslation("obisWizard.billingPeriodCounterEntries");
    private static String CHOICE_D_PROGRAM_ENTRIES = UserEnvironment.getDefault().getTranslation("obisWizard.programEntries");
    private static String CHOICE_D_OUTPUT_PULSE_CONSTANTS = UserEnvironment.getDefault().getTranslation("obisWizard.outputPulseConstants");
    private static String CHOICE_D_RATIOS = UserEnvironment.getDefault().getTranslation("obisWizard.ratios");
    private static String CHOICE_D_NOMINAL_VALUES = UserEnvironment.getDefault().getTranslation("obisWizard.nominalValues");
    private static String CHOICE_D_INPUT_PULSE_CONSTANTS = UserEnvironment.getDefault().getTranslation("obisWizard.inputPulseConstants");
    private static String CHOICE_D_MEASUREMENT_PERIODS = UserEnvironment.getDefault().getTranslation("obisWizard.measurementPeriods");
    private static String CHOICE_D_TIME_ENTRIES = UserEnvironment.getDefault().getTranslation("obisWizard.timeEntries");
    private static String CHOICE_D_COEFFICIENTS = UserEnvironment.getDefault().getTranslation("obisWizard.coefficients");
    private static String CHOICE_D_MEASUREMENT_METHODS = UserEnvironment.getDefault().getTranslation("obisWizard.measurementMethods");
    private static String CHOICE_D_BILLING_PERIOD_AVG = UserEnvironment.getDefault().getTranslation("obisWizard.billingPeriodAverage");
    private static String CHOICE_D_CUMULATIVE_MIN_USING_MEASUREMENT_PERIOD1 = UserEnvironment.getDefault().getTranslation("obisWizard.cumulativeMinimumUsingMeasurementPeriod1");
    private static String CHOICE_D_CUMULATIVE_MAX_USING_MEASUREMENT_PERIOD1 = UserEnvironment.getDefault().getTranslation("obisWizard.cumulativeMaximumUsingMeasurementPeriod1");
    private static String CHOICE_D_MIN_USING_MEASUREMENT_PERIOD1 = UserEnvironment.getDefault().getTranslation("obisWizard.minimumUsingMeasurementPeriod1");
    private static String CHOICE_D_CURRENT_AVG_USING_MEASUREMENT_PERIOD1 = UserEnvironment.getDefault().getTranslation("obisWizard.currentAverageUsingMeasurementPeriod1");
    private static String CHOICE_D_LAST_AVG_USING_MEASUREMENT_PERIOD1 = UserEnvironment.getDefault().getTranslation("obisWizard.lastAverageUsingMeasurementPeriod1");
    private static String CHOICE_D_MAX_USING_MEASUREMENT_PERIOD1 = UserEnvironment.getDefault().getTranslation("obisWizard.maximumUsingMeasurementPeriod1");
    private static String CHOICE_D_INSTANTANEOUS_VALUE = UserEnvironment.getDefault().getTranslation("obisWizard.instantaneousValue");
    private static String CHOICE_D_CUMULATIVE_MIN_USING_MEASUREMENT_PERIOD2 = UserEnvironment.getDefault().getTranslation("obisWizard.cumulativeMinimumUsingMeasurementPeriod2");
    private static String CHOICE_D_CUMULATIVE_MAX_USING_MEASUREMENT_PERIOD2 = UserEnvironment.getDefault().getTranslation("obisWizard.cumulativeMaximumUsingMeasurementPeriod2");
    private static String CHOICE_D_MIN_USING_MEASUREMENT_PERIOD2 = UserEnvironment.getDefault().getTranslation("obisWizard.minimumUsingMeasurementPeriod2");
    private static String CHOICE_D_CURRENT_AVG_USING_MEASUREMENT_PERIOD2 = UserEnvironment.getDefault().getTranslation("obisWizard.currentAverageUsingMeasurementPeriod2");
    private static String CHOICE_D_LAST_AVG_USING_MEASUREMENT_PERIOD2 = UserEnvironment.getDefault().getTranslation("obisWizard.lastAverageUsingMeasurementPeriod2");
    private static String CHOICE_D_MAX_USING_MEASUREMENT_PERIOD2 = UserEnvironment.getDefault().getTranslation("obisWizard.maximumUsingMeasurementPeriod2");
    private static String CHOICE_D_CUMULATIVE_MIN_USING_MEASUREMENT_PERIOD3 = UserEnvironment.getDefault().getTranslation("obisWizard.cumulativeMinimumUsingMeasurementPeriod3");
    private static String CHOICE_D_CUMULATIVE_MAX_USING_MEASUREMENT_PERIOD3 = UserEnvironment.getDefault().getTranslation("obisWizard.cumulativeMaximumUsingMeasurementPeriod3");
    private static String CHOICE_D_MIN_USING_MEASUREMENT_PERIOD3 = UserEnvironment.getDefault().getTranslation("obisWizard.minimumUsingMeasurementPeriod3");
    private static String CHOICE_D_CURRENT_AVG_USING_MEASUREMENT_PERIOD3 = UserEnvironment.getDefault().getTranslation("obisWizard.currentAverageUsingMeasurementPeriod3");
    private static String CHOICE_D_LAST_AVG_USING_MEASUREMENT_PERIOD3 = UserEnvironment.getDefault().getTranslation("obisWizard.lastAverageUsingMeasurementPeriod3");
    private static String CHOICE_D_MAX_USING_MEASUREMENT_PERIOD3 = UserEnvironment.getDefault().getTranslation("obisWizard.maximumUsingMeasurementPeriod3");
    private static String CHOICE_D_CURRENT_AVG_USING_RECORDING_INTERVAL1 = UserEnvironment.getDefault().getTranslation("obisWizard.currentAverageUsingRecordingInterval1");
    private static String CHOICE_D_CURRENT_AVG_USING_RECORDING_INTERVAL2 = UserEnvironment.getDefault().getTranslation("obisWizard.currentAverageUsingRecordingInterval2");
    private static String CHOICE_D_UNDER_LIMIT_THRESHOLD = UserEnvironment.getDefault().getTranslation("obisWizard.underLimitThreshold");
    private static String CHOICE_D_UNDER_LIMIT_OCCURRENCE_COUNTER = UserEnvironment.getDefault().getTranslation("obisWizard.underLimitOccurrenceCounter");
    private static String CHOICE_D_UNDER_LIMIT_DURATION = UserEnvironment.getDefault().getTranslation("obisWizard.underLimitDuration");
    private static String CHOICE_D_UNDER_LIMIT_MAGNITUDE = UserEnvironment.getDefault().getTranslation("obisWizard.underLimitMagnitude");
    private static String CHOICE_D_OVER_LIMIT_THRESHOLD = UserEnvironment.getDefault().getTranslation("obisWizard.overLimitThreshold");
    private static String CHOICE_D_OVER_LIMIT_OCCURRENCE_COUNTER = UserEnvironment.getDefault().getTranslation("obisWizard.overLimitOccurrenceCounter");
    private static String CHOICE_D_OVER_LIMIT_DURATION = UserEnvironment.getDefault().getTranslation("obisWizard.overLimitDuration");
    private static String CHOICE_D_OVER_LIMIT_MAGNITUDE = UserEnvironment.getDefault().getTranslation("obisWizard.overLimitMagnitude");
    private static String CHOICE_D_MISSING_THRESHOLD = UserEnvironment.getDefault().getTranslation("obisWizard.missingThreshold");
    private static String CHOICE_D_MISSING_OCCURRENCE_COUNTER = UserEnvironment.getDefault().getTranslation("obisWizard.missingOccurrenceCounter");
    private static String CHOICE_D_MISSING_DURATION = UserEnvironment.getDefault().getTranslation("obisWizard.missingDuration");
    private static String CHOICE_D_MISSING_MAGNITUDE = UserEnvironment.getDefault().getTranslation("obisWizard.missingMagnitude");
    private static String CHOICE_D_TEST_AVG = UserEnvironment.getDefault().getTranslation("obisWizard.testAverage");
    private static String CHOICE_D_TIME_INTEGRAL_TO_BILLING_POINT_X = UserEnvironment.getDefault().getTranslation("obisWizard.timeIntegralFromStartOfMeasurementToBillingPointX");
    private static String CHOICE_D_TIME_INTEGRAL_TO_NOW = UserEnvironment.getDefault().getTranslation("obisWizard.timeIntegralFromStartOfMeasurementToNow");
    private static String CHOICE_D_TIME_INTEGRAL_OVER_BILLING_PERIOD_X = UserEnvironment.getDefault().getTranslation("obisWizard.timeIntegralOverBillingPeriodX");
    private static String CHOICE_D_TIME_INTEGRAL_FROM_LAST_BILLING_POINT_TO_NOW = UserEnvironment.getDefault().getTranslation("obisWizard.timeIntegralFromLastBillingPointToNow");
    private static String CHOICE_D_TIME_INTEGRAL_POSITIVE_DIFFERENCE = UserEnvironment.getDefault().getTranslation("obisWizard.timeIntegralOfPositiveDifference");
    private static String CHOICE_D_TIME_INTEGRAL_OVER_DEVICE_SPECIFIC_TIME = UserEnvironment.getDefault().getTranslation("obisWizard.timeIntegralOverDeviceSpecificTime");
    private static String CHOICE_D_TIME_INTEGRAL_TO_NOW_USING_RECORDING_PERIOD1 = UserEnvironment.getDefault().getTranslation("obisWizard.timeIntegralFromStartOfCurrentRecordingIntervalToNowUsingRecordingPeriod1");
    private static String CHOICE_D_TIME_INTEGRAL_TO_NOW_USING_RECORDING_PERIOD2 = UserEnvironment.getDefault().getTranslation("obisWizard.timeIntegralFromStartOfCurrentRecordingIntervalToNowUsingRecordingPeriod2");

    private static String CHOICE_E_NR_OF_CFG_PROGRAM_CHANGES = UserEnvironment.getDefault().getTranslation("obisWizard.nrOfConfigProgramChanges");
    private static String CHOICE_E_DATE_OF_LAST_CFG_PROGRAM_CHANGE = UserEnvironment.getDefault().getTranslation("obisWizard.dateOfLastConfigProgramChange");
    private static String CHOICE_E_DATE_OF_LAST_TIME_SWITCH_PROGRAM_CHANGE = UserEnvironment.getDefault().getTranslation("obisWizard.dateOfLastTimeSwitchProgramChange");
    private static String CHOICE_E_DATE_OF_LAST_RIPPLE_CONTROL_RECEIVER_PROGRAM_CHANGE = UserEnvironment.getDefault().getTranslation("obisWizard.dateOfLastRippleControlReceiverProgramChange");
    private static String CHOICE_E_STATUS_OF_SECURITY_SWITCHES = UserEnvironment.getDefault().getTranslation("obisWizard.statusOfSecuritySwitches");
    private static String CHOICE_E_DATE_OF_LAST_CALIBRATION = UserEnvironment.getDefault().getTranslation("obisWizard.dateOfLastCalibration");
    private static String CHOICE_E_DATE_OF_NEXT_CFG_PROGRAM_CHANGE = UserEnvironment.getDefault().getTranslation("obisWizard.dateOfNextConfigProgramChange");
    private static String CHOICE_E_NR_OF_PROTECTED_CFG_PROGRAM_CHANGES = UserEnvironment.getDefault().getTranslation("obisWizard.nrOfProtectedConfigProgramChanges");
    private static String CHOICE_E_DATE_OF_LAST_PROTECTED_CFG_PROGRAM_CHANGE = UserEnvironment.getDefault().getTranslation("obisWizard.dateOfLastProtectedConfigProgramChange");
    private static String CHOICE_E_STATE_INPUT_CONTROL_SIGNALS = UserEnvironment.getDefault().getTranslation("obisWizard.stateOfInputControlSignals");
    private static String CHOICE_E_STATE_OUTPUT_CONTROL_SIGNALS = UserEnvironment.getDefault().getTranslation("obisWizard.stateOfOutputControlSignals");
    private static String CHOICE_E_STATE_OF_INTERNAL_CONTROL_SIGNALS = UserEnvironment.getDefault().getTranslation("obisWizard.stateOfInternalControlSignals");
    private static String CHOICE_E_INTERNAL_OPERATING_STATUS = UserEnvironment.getDefault().getTranslation("obisWizard.internalOperatingStatus");
    private static String CHOICE_E_BATTERY_USE_TIME_COUNTER = UserEnvironment.getDefault().getTranslation("obisWizard.batteryUseTimeCounter");
    private static String CHOICE_E_BATTERY_CHARGE_DISPLAY = UserEnvironment.getDefault().getTranslation("obisWizard.batteryChargeDisplay");
    private static String CHOICE_E_DATE_OF_NEXT_CHANGE = UserEnvironment.getDefault().getTranslation("obisWizard.dateOfNextChange");
    private static String CHOICE_E_BATTERY_VOLTAGE = UserEnvironment.getDefault().getTranslation("obisWizard.batteryVoltage");
    private static String CHOICE_E_TOTAL_FAILURE_3_PHASES = UserEnvironment.getDefault().getTranslation("obisWizard.totalFailureOfAllThreePhases");
    private static String CHOICE_E_PHASE_1_FAILURE = UserEnvironment.getDefault().getTranslation("obisWizard.phase1Failure");
    private static String CHOICE_E_PHASE_2_FAILURE = UserEnvironment.getDefault().getTranslation("obisWizard.phase2Failure");
    private static String CHOICE_E_PHASE_3_FAILURE = UserEnvironment.getDefault().getTranslation("obisWizard.phase3Failure");
    private static String CHOICE_E_TIME_OF_OPERATION = UserEnvironment.getDefault().getTranslation("obisWizard.timeOfOperation");
    private static String CHOICE_E_TIME_OF_REGISTRATION_RATE_X = UserEnvironment.getDefault().getTranslation("obisWizard.timeOfRegistrationRateX");
    private static String CHOICE_E_AMBIENT_TEMPERATURE = UserEnvironment.getDefault().getTranslation("obisWizard.ambientTemperature");
    private static String CHOICE_E_HARMONIC_X = UserEnvironment.getDefault().getTranslation("obisWizard.harmonicX");
    private static String CHOICE_E_RATE_X = UserEnvironment.getDefault().getTranslation("obisWizard.rateX");
    private static String CHOICE_E_BILLING_PERIOD_COUNTER = UserEnvironment.getDefault().getTranslation("obisWizard.billingPeriodCounter");
    private static String CHOICE_E_NR_OF_AVAILABLE_BILLING_PERIODS = UserEnvironment.getDefault().getTranslation("obisWizard.nrOfAvailableBillingPeriods");
    private static String CHOICE_E_TIMESTAMP_OF_BILLING_POINT_IN_CODE_F = UserEnvironment.getDefault().getTranslation("obisWizard.timestampOfBillingPointInCodeF");
    private static String CHOICE_E_TIMESTAMP_OF_BILLING_POINT_X = UserEnvironment.getDefault().getTranslation("obisWizard.timestampOfBillingPointX");
    private static String CHOICE_E_CFG_PROGRAM_VERSION_NUMBER = UserEnvironment.getDefault().getTranslation("obisWizard.configurationProgramVersionNumber");
    private static String CHOICE_E_PARAMETER_RECORD_NUMBER = UserEnvironment.getDefault().getTranslation("obisWizard.parameterRecordNumber");
    private static String CHOICE_E_TIME_SWITCH_PROGRAM_NUMBER = UserEnvironment.getDefault().getTranslation("obisWizard.timeSwitchProgramNumber");
    private static String CHOICE_E_RCR_PROGRAM_NUMBER = UserEnvironment.getDefault().getTranslation("obisWizard.RCRProgramNumber");
    private static String CHOICE_E_MASTER_CONNECTION_DIAGRAM_ID = UserEnvironment.getDefault().getTranslation("obisWizard.masterConnectionDiagramId");
    private static String CHOICE_E_RLW_ACTIVE_ENERGY = UserEnvironment.getDefault().getTranslation("obisWizard.RLWActiveEnergy");
    private static String CHOICE_E_RLB_REACTIVE_ENERGY = UserEnvironment.getDefault().getTranslation("obisWizard.RLBReactiveEnergy");
    private static String CHOICE_E_RLS_APPARENT_ENERGY = UserEnvironment.getDefault().getTranslation("obisWizard.RLSApparentEnergy");
    private static String CHOICE_E_RAW_ACTIVE_ENERGY = UserEnvironment.getDefault().getTranslation("obisWizard.RAWActiveEnergy");
    private static String CHOICE_E_RAB_REACTIVE_ENERGY = UserEnvironment.getDefault().getTranslation("obisWizard.RABReactiveEnergy");
    private static String CHOICE_E_RAS_APPARENT_ENERGY = UserEnvironment.getDefault().getTranslation("obisWizard.RASApparentEnergy");
    private static String CHOICE_E_TRANSFORMER_RATIO_CURRENT_NUMERATOR = UserEnvironment.getDefault().getTranslation("obisWizard.transformerRatioCurrentNumerator");
    private static String CHOICE_E_TRANSFORMER_RATIO_VOLTAGE_NUMERATOR = UserEnvironment.getDefault().getTranslation("obisWizard.transformerRatioVoltageNumerator");
    private static String CHOICE_E_OVERALL_TRANSFORMER_RATIO_NUMERATOR = UserEnvironment.getDefault().getTranslation("obisWizard.overallTransformerRatioNumerator");
    private static String CHOICE_E_TRANSFORMER_RATIO_CURRENT_DENOMINATOR = UserEnvironment.getDefault().getTranslation("obisWizard.transformerRatioCurrentDenominator");
    private static String CHOICE_E_TRANSFORMER_RATIO_VOLTAGE_DENOMINATOR = UserEnvironment.getDefault().getTranslation("obisWizard.transformerRatioVoltageDenominator");
    private static String CHOICE_E_OVERALL_TRANSFORMER_RATIO_DENOMINATOR = UserEnvironment.getDefault().getTranslation("obisWizard.overallTransformerRatioDenominator");
    private static String CHOICE_E_READING_FACTOR_FOR_POWER = UserEnvironment.getDefault().getTranslation("obisWizard.readingFactorForPower");
    private static String CHOICE_E_READING_FACTOR_FOR_ENERGY = UserEnvironment.getDefault().getTranslation("obisWizard.readingFactorForEnergy");
    private static String CHOICE_E_REFERENCE_VOLTAGE_FOR_POWER_QUALITY = UserEnvironment.getDefault().getTranslation("obisWizard.referenceVoltageForPowerQualityMeasurement");
    private static String CHOICE_E_NOMINAL_VOLTAGE = UserEnvironment.getDefault().getTranslation("obisWizard.nominalVoltage");
    private static String CHOICE_E_NOMINAL_CURRENT = UserEnvironment.getDefault().getTranslation("obisWizard.nominalCurrent");
    private static String CHOICE_E_NOMINAL_FREQUENCY = UserEnvironment.getDefault().getTranslation("obisWizard.nominalFrequency");
    private static String CHOICE_E_MAXIMUM_CURRENT = UserEnvironment.getDefault().getTranslation("obisWizard.maximumCurrent");
    private static String CHOICE_E_REW_ACTIVE_ENERGY = UserEnvironment.getDefault().getTranslation("obisWizard.REWActiveEnergy");
    private static String CHOICE_E_REB_REACTIVE_ENERGY = UserEnvironment.getDefault().getTranslation("obisWizard.REBReactiveEnergy");
    private static String CHOICE_E_RES_APPARENT_ENERGY = UserEnvironment.getDefault().getTranslation("obisWizard.RESApparentEnergy");
    private static String CHOICE_E_MEASUREMENT_PERIOD_1_FOR_AVG_1 = UserEnvironment.getDefault().getTranslation("obisWizard.measurementPeriod1ForAvgValue1");
    private static String CHOICE_E_MEASUREMENT_PERIOD_2_FOR_AVG_2 = UserEnvironment.getDefault().getTranslation("obisWizard.measurementPeriod2ForAvgValue2");
    private static String CHOICE_E_MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUE = UserEnvironment.getDefault().getTranslation("obisWizard.measurementPeriod3ForInstantaneousValue");
    private static String CHOICE_E_RECORDING_INTERVAL_1_FOR_LOAD_PROFILE = UserEnvironment.getDefault().getTranslation("obisWizard.recordingInterval1ForLoadProfile");
    private static String CHOICE_E_RECORDING_INTERVAL_2_FOR_LOAD_PROFILE = UserEnvironment.getDefault().getTranslation("obisWizard.recordingInterval2ForLoadProfile");
    private static String CHOICE_E_BILLING_PERIOD = UserEnvironment.getDefault().getTranslation("obisWizard.billingPeriod");
    private static String CHOICE_E_MEASUREMENT_PERIOD_4_FOR_TEST_VALUE = UserEnvironment.getDefault().getTranslation("obisWizard.measurementPeriod4ForTestValue");
    private static String CHOICE_E_TIME_EXPIRED_SINCE_LAST_BILLING_POINT = UserEnvironment.getDefault().getTranslation("obisWizard.timeExpiredSinceLastBillingPoint");
    private static String CHOICE_E_LOCAL_TIME = UserEnvironment.getDefault().getTranslation("obisWizard.localTime");
    private static String CHOICE_E_LOCAL_DATE = UserEnvironment.getDefault().getTranslation("obisWizard.localDate");
    private static String CHOICE_E_WEEK_DAY = UserEnvironment.getDefault().getTranslation("obisWizard.weekDay");
    private static String CHOICE_E_TIME_OF_LAST_RESET = UserEnvironment.getDefault().getTranslation("obisWizard.timeOfLastReset");
    private static String CHOICE_E_DATE_OF_LAST_RESET = UserEnvironment.getDefault().getTranslation("obisWizard.dateOfLastReset");
    private static String CHOICE_E_OUTPUT_PULSE_DURATION = UserEnvironment.getDefault().getTranslation("obisWizard.outputPulseDuration");
    private static String CHOICE_E_CLOCK_SYNC_WINDOW = UserEnvironment.getDefault().getTranslation("obisWizard.clockSyncWindow");
    private static String CHOICE_E_CLOCK_SYNC_METHOD = UserEnvironment.getDefault().getTranslation("obisWizard.clockSyncMethod");
    private static String CHOICE_E_TRANSFORMER_MAGNETIC_LOSSES = UserEnvironment.getDefault().getTranslation("obisWizard.transformerMagneticLosses");
    private static String CHOICE_E_TRANSFORMER_THERMA_LOSSES = UserEnvironment.getDefault().getTranslation("obisWizard.transformerThermaLosses");
    private static String CHOICE_E_LINE_RESISTANCE_LOSSES = UserEnvironment.getDefault().getTranslation("obisWizard.lineResistanceLosses");
    private static String CHOICE_E_LINE_REACTANCE_LOSSES = UserEnvironment.getDefault().getTranslation("obisWizard.lineReactanceLosses");
    private static String CHOICE_E_ALGORITHM_ACTIVE_POWER_MANAGEMENT = UserEnvironment.getDefault().getTranslation("obisWizard.algorithmForActivePowerMeasurement");
    private static String CHOICE_E_ALGORITHM_ACTIVE_ENERGY_MANAGEMENT = UserEnvironment.getDefault().getTranslation("obisWizard.algorithmForActiveEnergyMeasurement");
    private static String CHOICE_E_ALGORITHM_REACTIVE_POWER_MANAGEMENT = UserEnvironment.getDefault().getTranslation("obisWizard.algorithmForReactivePowerMeasurement");
    private static String CHOICE_E_ALGORITHM_REACTIVE_ENERGY_MANAGEMENT = UserEnvironment.getDefault().getTranslation("obisWizard.algorithmForReactiveEnergyMeasurement");
    private static String CHOICE_E_ALGORITHM_APPARENT_POWER_MANAGEMENT = UserEnvironment.getDefault().getTranslation("obisWizard.algorithmForApparentPowerMeasurement");
    private static String CHOICE_E_ALGORITHM_APPARENT_ENERGY_MANAGEMENT = UserEnvironment.getDefault().getTranslation("obisWizard.algorithmForApparentEnergyMeasurement");
    private static String CHOICE_E_ALGORITHM_POWER_FACTOR_CALCULATION = UserEnvironment.getDefault().getTranslation("obisWizard.algorithmForPowerFactorCalculation");

    private static String IN_CURRENT_ACTIVE_BILLING_PERIOD = UserEnvironment.getDefault().getTranslation("obisWizard.inCurrentActiveBillingPeriod");
    private static String IN_BILLING_PERIOD_X = UserEnvironment.getDefault().getTranslation("obisWizard.inBillingPeriodX");

    private static final String[] eDemandRegisters = {
            REGISTER_ACTIVE_POWER_PLUS,
            REGISTER_ACTIVE_POWER_MINUS,
            REGISTER_REACTIVE_POWER_PLUS,
            REGISTER_REACTIVE_POWER_MINUS,
            REGISTER_REACTIVE_POWER_QI,
            REGISTER_REACTIVE_POWER_QII,
            REGISTER_REACTIVE_POWER_QIII,
            REGISTER_REACTIVE_POWER_QIV,
            REGISTER_APPARENT_POWER_PLUS,
            REGISTER_APPARENT_POWER_MINUS,
            REGISTER_CURRENT,
            REGISTER_VOLTAGE,
            REGISTER_POWER_FACTOR,
            REGISTER_FREQUENCY,
            REGISTER_ACTIVE_POWER_Q1423,
            REGISTER_ACTIVE_POWER_Q14_MINUS_Q23,
            REGISTER_ACTIVE_POWER_QI,
            REGISTER_ACTIVE_POWER_QII,
            REGISTER_ACTIVE_POWER_QIII,
            REGISTER_ACTIVE_POWER_QIV,
    };

    private static final String[] phaseStrings = {
            ALL_PHASES,
            PHASE_1,
            PHASE_2,
            PHASE_3
    };

    ObisCode code;
    StringBuilder descriptionBuilder;
    List<ObisCodeFieldValue> choicesA;
    List<ObisCodeFieldValue> choicesB;
    List<ObisCodeFieldValue> choicesC;
    List<ObisCodeFieldValue> choicesD;
    List<ObisCodeFieldValue> choicesE;
    List<ObisCodeFieldValue> choicesF;

    /**
     * Creates a new instance of ObisCodeBuilderWizard
     */
    public ObisCodeAnalyzer(ObisCode code) {
        this.code = code;
    }

    public String getDescription() {
        descriptionBuilder = new StringBuilder();
        buildDescription();
        return descriptionBuilder.toString();
    }

    public List getChoices(int page) {
        switch (page) {
            case 0:
                return choicesA;
            case 1:
                return choicesB;
            case 2:
                return choicesC;
            case 3:
                return choicesD;
            case 4:
                return choicesE;
            case 5:
                return choicesF;
            default:
                throw new RuntimeException("ObisCodeBuilderWizard, wrong page id");
        }
    }

    public void buildDescriptionChoices() {
        choicesA = choicesB = choicesC = choicesD = choicesE = choicesF = null;
        choicesF = new ArrayList<>(); // to enable billing choice dialog panel
        choicesA = new ArrayList<>();
        choicesA.add(new ObisCodeFieldValue(0, CHOICE_A_ABSTRACT_OBIS_CODE));
        choicesA.add(new ObisCodeFieldValue(1, CHOICE_A_ELECTRICITY_OBIS_CODE));
        choicesA.add(new ObisCodeFieldValue(4, CHOICE_A_HEAT_COST_ALLOCATOR_OBIS_CODE));
        choicesA.add(new ObisCodeFieldValue(5, CHOICE_A_COOLING_OBIS_CODE));
        choicesA.add(new ObisCodeFieldValue(6, CHOICE_A_HEAT_OBIS_CODE));
        choicesA.add(new ObisCodeFieldValue(7, CHOICE_A_GAS_OBIS_CODE));
        choicesA.add(new ObisCodeFieldValue(8, CHOICE_A_COLD_WATER_OBIS_CODE));
        choicesA.add(new ObisCodeFieldValue(9, CHOICE_A_HOT_WATER_OBIS_CODE));
    }

    public void buildDescription() {
        buildDescriptionChoices();

        switch (code.getA()) {
            case 0:
                doAbstract();
                break;
            case 1:
                doElectricityRelatedChoices();
                break;
            case 4:
                doHeatCostAllocatorRelated();
                break;
            case 5:
                doCoolingRelated();
                break;
            case 6:
                doHeatRelated();
                break;
            case 7:
                doGasRelated();
                break;
            case 8:
                doColdWaterRelated();
                break;
            case 9:
                doHotWaterRelated();
                break;
            default:
                doReservedACode();
        }
    }

    protected void doReservedACode() {
        doDefault();
    }

    protected void doDefault() {
        descriptionBuilder.append(code);
    }

    protected void doAbstractChoices() {
        choicesC = new ArrayList<>();
        choicesC.add(new ObisCodeFieldValue(96, CHOICE_C_GENERAL_SERVICE_ENTRIES));
    }

    private void buildAbstractChoices() {
        choicesD = new ArrayList<>();
        choicesD.add(new ObisCodeFieldValue(2, CHOICE_D_PARAMETER_CHANGES_OBIS_CODE));
        choicesD.add(new ObisCodeFieldValue(3, CHOICE_D_IO_CONTROL_SIGNALS_OBIS_CODE));
        choicesD.add(new ObisCodeFieldValue(4, CHOICE_D_INTERNAL_CONTROL_SIGNALS_OBIS_CODE));
        choicesD.add(new ObisCodeFieldValue(5, CHOICE_D_INTERNAL_STATUS_SIGNALS_OBIS_CODE));
        choicesD.add(new ObisCodeFieldValue(6, CHOICE_D_BATTERY_ENTRIES_OBIS_CODE));
        choicesD.add(new ObisCodeFieldValue(7, CHOICE_D_POWER_FAILURES_OBIS_CODE));
        choicesD.add(new ObisCodeFieldValue(8, CHOICE_D_OPERATING_TIME_OBIS_CODE));
        choicesD.add(new ObisCodeFieldValue(9, CHOICE_D_ENVIRONMENTAL_OBIS_CODE));
    }

    protected void doAbstract() {

        doAbstractChoices();

        if (code.getC() != 96) {
            choicesD = null;
            choicesF = null;
            doDefault();
            return;
        } else {
            choicesF = null;
        }

        buildAbstractChoices();

        switch (code.getD()) {
            case 2:
                doParameterChanges();
                break;
            case 3:
                doInputOutputControlSignals();
                break;
            case 4:
                doInternalControlSignals();
                break;
            case 5:
                doInternalOperatingStatus();
                break;
            case 6:
                doBatteryEntries();
                break;
            case 7:
                doNumberOfPowerFailures();
                break;
            case 8:
                doOperatingTime();
                break;
            case 9:
                doEnvironmental();
                break;
            default:
                doDefault();
        }

    }

    protected void doParameterChanges() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_NR_OF_CFG_PROGRAM_CHANGES));
        choicesE.add(new ObisCodeFieldValue(1, CHOICE_E_DATE_OF_LAST_CFG_PROGRAM_CHANGE));
        choicesE.add(new ObisCodeFieldValue(2, CHOICE_E_DATE_OF_LAST_TIME_SWITCH_PROGRAM_CHANGE));
        choicesE.add(new ObisCodeFieldValue(3, CHOICE_E_DATE_OF_LAST_RIPPLE_CONTROL_RECEIVER_PROGRAM_CHANGE));
        choicesE.add(new ObisCodeFieldValue(4, CHOICE_E_STATUS_OF_SECURITY_SWITCHES));
        choicesE.add(new ObisCodeFieldValue(5, CHOICE_E_DATE_OF_LAST_CALIBRATION));
        choicesE.add(new ObisCodeFieldValue(6, CHOICE_E_DATE_OF_NEXT_CFG_PROGRAM_CHANGE));
        choicesE.add(new ObisCodeFieldValue(10, CHOICE_E_NR_OF_PROTECTED_CFG_PROGRAM_CHANGES));
        choicesE.add(new ObisCodeFieldValue(11, CHOICE_E_DATE_OF_LAST_PROTECTED_CFG_PROGRAM_CHANGE));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doInputOutputControlSignals() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(1, CHOICE_E_STATE_INPUT_CONTROL_SIGNALS));
        choicesE.add(new ObisCodeFieldValue(2, CHOICE_E_STATE_OUTPUT_CONTROL_SIGNALS));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doInternalControlSignals() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_STATE_OF_INTERNAL_CONTROL_SIGNALS));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doInternalOperatingStatus() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_INTERNAL_OPERATING_STATUS));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doBatteryEntries() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_BATTERY_USE_TIME_COUNTER));
        choicesE.add(new ObisCodeFieldValue(1, CHOICE_E_BATTERY_CHARGE_DISPLAY));
        choicesE.add(new ObisCodeFieldValue(2, CHOICE_E_DATE_OF_NEXT_CHANGE));
        choicesE.add(new ObisCodeFieldValue(3, CHOICE_E_BATTERY_VOLTAGE));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doNumberOfPowerFailures() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_TOTAL_FAILURE_3_PHASES));
        choicesE.add(new ObisCodeFieldValue(1, CHOICE_E_PHASE_1_FAILURE));
        choicesE.add(new ObisCodeFieldValue(2, CHOICE_E_PHASE_2_FAILURE));
        choicesE.add(new ObisCodeFieldValue(3, CHOICE_E_PHASE_3_FAILURE));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doOperatingTimechoices() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_TIME_OF_OPERATION));
        for (int codeE = 1; codeE < 64; codeE++) {
            choicesE.add(new ObisCodeFieldValue(codeE, this.format(CHOICE_E_TIME_OF_REGISTRATION_RATE_X, new Object[]{codeE})));
        }
    }

    protected void doOperatingTime() {
        doOperatingTimechoices();
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doEnvironmental() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_AMBIENT_TEMPERATURE));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }


    protected void doElectricityRelatedChoices() {
        choicesC = new ArrayList<>();
        choicesC.add(new ObisCodeFieldValue(0, CHOICE_C_GENERAL_PURPOSE));
        doDemandRegisterChoices();

        switch (code.getC()) {
            case 0:
                doGeneralPurpose();
                break;
            default:
                if (code.getC() < 96) {
                    doTimePeriod();
                }
        }
    }


    protected void doDemandRegisterChoices() {
        choicesC.add(new ObisCodeFieldValue(82, CHOICE_C_UNITLESS_QUANTITY));
        choicesC.add(new ObisCodeFieldValue(91, CHOICE_C_L0_CURRENT));
        choicesC.add(new ObisCodeFieldValue(92, CHOICE_C_L0_VOLTAGE));
        for (int codeC = 1; codeC <= 80; codeC++) {
            StringBuilder builder = new StringBuilder();
            int phenoTypeId = codeC % 20;
            int phaseId = (codeC - 1) / 20;
            if (phenoTypeId == 0) {
                phenoTypeId = 20;
            }
            builder.append(eDemandRegisters[phenoTypeId - 1]);
            space(builder);
            builder.append(phaseStrings[phaseId]);
            space(builder);
            switch (code.getC()) {
                case 31:
                case 51:
                case 71:
                case 32:
                case 52:
                case 72:
                    doHarmonics(builder);
                    break;
                default:
                    if (code.getD() == 7) {
                        doHarmonics(builder);
                    } else {
                        doTariff(builder);
                    }
            }
            choicesC.add(new ObisCodeFieldValue(codeC, builder.toString()));
        }

        descriptionBuilder.append(findString(choicesC, code.getC()));

    }

    protected void doHarmonics(StringBuilder builder) {
        if (code.getE() > 0 && code.getE() < 128) {
            choicesE = null;
            builder.append(this.format(CHOICE_E_HARMONIC_X, new Object[]{code.getE()}));
            space(builder);
        }
    }

    protected void doTariff(StringBuilder builder) {
        if (code.getE() > 0 && code.getE() < 64) {
            choicesE = null;
            builder.append(this.format(CHOICE_E_RATE_X, new Object[]{code.getE()}));
            space(builder);
        }
    }

    private void doGeneralPurposeChoices() {
        choicesD = new ArrayList<>();
        choicesD.add(new ObisCodeFieldValue(1, CHOICE_D_BILLING_PERIOD_COUNTER_ENTRIES));
        choicesD.add(new ObisCodeFieldValue(2, CHOICE_D_PROGRAM_ENTRIES));
        choicesD.add(new ObisCodeFieldValue(3, CHOICE_D_OUTPUT_PULSE_CONSTANTS));
        choicesD.add(new ObisCodeFieldValue(4, CHOICE_D_RATIOS));
        choicesD.add(new ObisCodeFieldValue(6, CHOICE_D_NOMINAL_VALUES));
        choicesD.add(new ObisCodeFieldValue(7, CHOICE_D_INPUT_PULSE_CONSTANTS));
        choicesD.add(new ObisCodeFieldValue(8, CHOICE_D_MEASUREMENT_PERIODS));
        choicesD.add(new ObisCodeFieldValue(9, CHOICE_D_TIME_ENTRIES));
        choicesD.add(new ObisCodeFieldValue(10, CHOICE_D_COEFFICIENTS));
        choicesD.add(new ObisCodeFieldValue(11, CHOICE_D_MEASUREMENT_METHODS));
    }

    protected void doGeneralPurpose() {

        doGeneralPurposeChoices();

        switch (code.getD()) {
            case 1:
                doBillingPeriodCounterEntries();
                break;
            case 2:
                doProgramEntries();
                break;
            case 3:
                doOutputPulseConstants();
                break;
            case 4:
                doRatios();
                break;
            case 6:
                doNominalValues();
                break;
            case 7:
                doInputPulseConstants();
                break;
            case 8:
                doMeasurementsPeriods();
                break;
            case 9:
                doTimeEntries();
                break;
            case 10:
                doCoefficients();
                break;
            case 11:
                doMeasurementMethods();
                break;
            default:
                doDefault();
        }
    }

    protected void doBillingPeriodCounterEntries() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_BILLING_PERIOD_COUNTER));
        choicesE.add(new ObisCodeFieldValue(1, CHOICE_E_NR_OF_AVAILABLE_BILLING_PERIODS));
        if (code.getF() == 255) {
            choicesE.add(new ObisCodeFieldValue(2, CHOICE_E_TIMESTAMP_OF_BILLING_POINT_IN_CODE_F));
        } else {
            choicesE.add(new ObisCodeFieldValue(2, this.format(CHOICE_E_TIMESTAMP_OF_BILLING_POINT_X, new Object[]{code.getF()})));
        }
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doProgramEntries() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_CFG_PROGRAM_VERSION_NUMBER));
        choicesE.add(new ObisCodeFieldValue(1, CHOICE_E_PARAMETER_RECORD_NUMBER));
        choicesE.add(new ObisCodeFieldValue(2, CHOICE_E_TIME_SWITCH_PROGRAM_NUMBER));
        choicesE.add(new ObisCodeFieldValue(3, CHOICE_E_RCR_PROGRAM_NUMBER));
        choicesE.add(new ObisCodeFieldValue(4, CHOICE_E_MASTER_CONNECTION_DIAGRAM_ID));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doOutputPulseConstants() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_RLW_ACTIVE_ENERGY));
        choicesE.add(new ObisCodeFieldValue(1, CHOICE_E_RLB_REACTIVE_ENERGY));
        choicesE.add(new ObisCodeFieldValue(2, CHOICE_E_RLS_APPARENT_ENERGY));
        choicesE.add(new ObisCodeFieldValue(3, CHOICE_E_RAW_ACTIVE_ENERGY));
        choicesE.add(new ObisCodeFieldValue(4, CHOICE_E_RAB_REACTIVE_ENERGY));
        choicesE.add(new ObisCodeFieldValue(5, CHOICE_E_RAS_APPARENT_ENERGY));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doRatios() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(2, CHOICE_E_TRANSFORMER_RATIO_CURRENT_NUMERATOR));
        choicesE.add(new ObisCodeFieldValue(3, CHOICE_E_TRANSFORMER_RATIO_VOLTAGE_NUMERATOR));
        choicesE.add(new ObisCodeFieldValue(4, CHOICE_E_OVERALL_TRANSFORMER_RATIO_NUMERATOR));
        choicesE.add(new ObisCodeFieldValue(5, CHOICE_E_TRANSFORMER_RATIO_CURRENT_DENOMINATOR));
        choicesE.add(new ObisCodeFieldValue(6, CHOICE_E_TRANSFORMER_RATIO_VOLTAGE_DENOMINATOR));
        choicesE.add(new ObisCodeFieldValue(7, CHOICE_E_OVERALL_TRANSFORMER_RATIO_DENOMINATOR));
        doInBillingPeriodChoices(choicesE);
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_READING_FACTOR_FOR_POWER));
        choicesE.add(new ObisCodeFieldValue(1, CHOICE_E_READING_FACTOR_FOR_ENERGY));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doNominalValues() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(4, CHOICE_E_REFERENCE_VOLTAGE_FOR_POWER_QUALITY));
        doInBillingPeriodChoices(choicesE);
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_NOMINAL_VOLTAGE));
        choicesE.add(new ObisCodeFieldValue(1, CHOICE_E_NOMINAL_CURRENT));
        choicesE.add(new ObisCodeFieldValue(2, CHOICE_E_NOMINAL_FREQUENCY));
        choicesE.add(new ObisCodeFieldValue(3, CHOICE_E_MAXIMUM_CURRENT));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doInputPulseConstants() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_REW_ACTIVE_ENERGY));
        choicesE.add(new ObisCodeFieldValue(1, CHOICE_E_REB_REACTIVE_ENERGY));
        choicesE.add(new ObisCodeFieldValue(2, CHOICE_E_RES_APPARENT_ENERGY));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doMeasurementsPeriods() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_MEASUREMENT_PERIOD_1_FOR_AVG_1));
        choicesE.add(new ObisCodeFieldValue(1, CHOICE_E_MEASUREMENT_PERIOD_2_FOR_AVG_2));
        choicesE.add(new ObisCodeFieldValue(2, CHOICE_E_MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUE));
        choicesE.add(new ObisCodeFieldValue(4, CHOICE_E_RECORDING_INTERVAL_1_FOR_LOAD_PROFILE));
        choicesE.add(new ObisCodeFieldValue(5, CHOICE_E_RECORDING_INTERVAL_2_FOR_LOAD_PROFILE));
        choicesE.add(new ObisCodeFieldValue(6, CHOICE_E_BILLING_PERIOD));
        doInBillingPeriodChoices(choicesE);
        choicesE.add(new ObisCodeFieldValue(3, CHOICE_E_MEASUREMENT_PERIOD_4_FOR_TEST_VALUE));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doTimeEntries() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_TIME_EXPIRED_SINCE_LAST_BILLING_POINT));
        choicesE.add(new ObisCodeFieldValue(1, CHOICE_E_LOCAL_TIME));
        choicesE.add(new ObisCodeFieldValue(2, CHOICE_E_LOCAL_DATE));
        choicesE.add(new ObisCodeFieldValue(5, CHOICE_E_WEEK_DAY));
        choicesE.add(new ObisCodeFieldValue(6, CHOICE_E_TIME_OF_LAST_RESET));
        choicesE.add(new ObisCodeFieldValue(7, CHOICE_E_DATE_OF_LAST_RESET));
        choicesE.add(new ObisCodeFieldValue(8, CHOICE_E_OUTPUT_PULSE_DURATION));
        choicesE.add(new ObisCodeFieldValue(9, CHOICE_E_CLOCK_SYNC_WINDOW));
        choicesE.add(new ObisCodeFieldValue(10, CHOICE_E_CLOCK_SYNC_METHOD));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doCoefficients() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, CHOICE_E_TRANSFORMER_MAGNETIC_LOSSES));
        choicesE.add(new ObisCodeFieldValue(1, CHOICE_E_TRANSFORMER_THERMA_LOSSES));
        choicesE.add(new ObisCodeFieldValue(2, CHOICE_E_LINE_RESISTANCE_LOSSES));
        choicesE.add(new ObisCodeFieldValue(3, CHOICE_E_LINE_REACTANCE_LOSSES));
        doInBillingPeriodChoices(choicesE);
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doMeasurementMethods() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(1, CHOICE_E_ALGORITHM_ACTIVE_POWER_MANAGEMENT));
        choicesE.add(new ObisCodeFieldValue(2, CHOICE_E_ALGORITHM_ACTIVE_ENERGY_MANAGEMENT));
        choicesE.add(new ObisCodeFieldValue(3, CHOICE_E_ALGORITHM_REACTIVE_POWER_MANAGEMENT));
        choicesE.add(new ObisCodeFieldValue(4, CHOICE_E_ALGORITHM_REACTIVE_ENERGY_MANAGEMENT));
        choicesE.add(new ObisCodeFieldValue(5, CHOICE_E_ALGORITHM_APPARENT_POWER_MANAGEMENT));
        choicesE.add(new ObisCodeFieldValue(6, CHOICE_E_ALGORITHM_APPARENT_ENERGY_MANAGEMENT));
        choicesE.add(new ObisCodeFieldValue(7, CHOICE_E_ALGORITHM_POWER_FACTOR_CALCULATION));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    protected void doTimePeriodChoices() {
        choicesD = new ArrayList<>();
        choicesD.add(new ObisCodeFieldValue(0, CHOICE_D_BILLING_PERIOD_AVG));
        choicesD.add(new ObisCodeFieldValue(1, CHOICE_D_CUMULATIVE_MIN_USING_MEASUREMENT_PERIOD1));
        choicesD.add(new ObisCodeFieldValue(2, CHOICE_D_CUMULATIVE_MAX_USING_MEASUREMENT_PERIOD1));
        choicesD.add(new ObisCodeFieldValue(3, CHOICE_D_MIN_USING_MEASUREMENT_PERIOD1));
        choicesD.add(new ObisCodeFieldValue(4, CHOICE_D_CURRENT_AVG_USING_MEASUREMENT_PERIOD1));
        choicesD.add(new ObisCodeFieldValue(5, CHOICE_D_LAST_AVG_USING_MEASUREMENT_PERIOD1));
        choicesD.add(new ObisCodeFieldValue(6, CHOICE_D_MAX_USING_MEASUREMENT_PERIOD1));
        choicesD.add(new ObisCodeFieldValue(7, CHOICE_D_INSTANTANEOUS_VALUE));
        doTimeIntegralChoices();
        choicesD.add(new ObisCodeFieldValue(11, CHOICE_D_CUMULATIVE_MIN_USING_MEASUREMENT_PERIOD2));
        choicesD.add(new ObisCodeFieldValue(12, CHOICE_D_CUMULATIVE_MAX_USING_MEASUREMENT_PERIOD2));
        choicesD.add(new ObisCodeFieldValue(13, CHOICE_D_MIN_USING_MEASUREMENT_PERIOD2));
        choicesD.add(new ObisCodeFieldValue(14, CHOICE_D_CURRENT_AVG_USING_MEASUREMENT_PERIOD2));
        choicesD.add(new ObisCodeFieldValue(15, CHOICE_D_LAST_AVG_USING_MEASUREMENT_PERIOD2));
        choicesD.add(new ObisCodeFieldValue(16, CHOICE_D_MAX_USING_MEASUREMENT_PERIOD2));
        choicesD.add(new ObisCodeFieldValue(21, CHOICE_D_CUMULATIVE_MIN_USING_MEASUREMENT_PERIOD3));
        choicesD.add(new ObisCodeFieldValue(22, CHOICE_D_CUMULATIVE_MAX_USING_MEASUREMENT_PERIOD3));
        choicesD.add(new ObisCodeFieldValue(23, CHOICE_D_MIN_USING_MEASUREMENT_PERIOD3));
        choicesD.add(new ObisCodeFieldValue(24, CHOICE_D_CURRENT_AVG_USING_MEASUREMENT_PERIOD3));
        choicesD.add(new ObisCodeFieldValue(25, CHOICE_D_LAST_AVG_USING_MEASUREMENT_PERIOD3));
        choicesD.add(new ObisCodeFieldValue(26, CHOICE_D_MAX_USING_MEASUREMENT_PERIOD3));
        choicesD.add(new ObisCodeFieldValue(27, CHOICE_D_CURRENT_AVG_USING_RECORDING_INTERVAL1));
        choicesD.add(new ObisCodeFieldValue(28, CHOICE_D_CURRENT_AVG_USING_RECORDING_INTERVAL2));
        choicesD.add(new ObisCodeFieldValue(31, CHOICE_D_UNDER_LIMIT_THRESHOLD));
        choicesD.add(new ObisCodeFieldValue(32, CHOICE_D_UNDER_LIMIT_OCCURRENCE_COUNTER));
        choicesD.add(new ObisCodeFieldValue(33, CHOICE_D_UNDER_LIMIT_DURATION));
        choicesD.add(new ObisCodeFieldValue(34, CHOICE_D_UNDER_LIMIT_MAGNITUDE));
        choicesD.add(new ObisCodeFieldValue(35, CHOICE_D_OVER_LIMIT_THRESHOLD));
        choicesD.add(new ObisCodeFieldValue(36, CHOICE_D_OVER_LIMIT_OCCURRENCE_COUNTER));
        choicesD.add(new ObisCodeFieldValue(37, CHOICE_D_OVER_LIMIT_DURATION));
        choicesD.add(new ObisCodeFieldValue(38, CHOICE_D_OVER_LIMIT_MAGNITUDE));
        choicesD.add(new ObisCodeFieldValue(39, CHOICE_D_MISSING_THRESHOLD));
        choicesD.add(new ObisCodeFieldValue(40, CHOICE_D_MISSING_OCCURRENCE_COUNTER));
        choicesD.add(new ObisCodeFieldValue(41, CHOICE_D_MISSING_DURATION));
        choicesD.add(new ObisCodeFieldValue(42, CHOICE_D_MISSING_MAGNITUDE));
        choicesD.add(new ObisCodeFieldValue(55, CHOICE_D_TEST_AVG));
        doInBillingPeriodChoices(choicesD);
    }

    protected void doTimePeriod() {
        doTimePeriodChoices();
        descriptionBuilder.append(findString(choicesD, code.getD()));
    }

    protected void doTimeIntegralChoices() {
        StringBuilder builder = new StringBuilder();
        if (code.hasBillingPeriod()) {
            builder.append(this.format(CHOICE_D_TIME_INTEGRAL_TO_BILLING_POINT_X, new Object[]{code.getF()}));
        } else {
            builder.append(CHOICE_D_TIME_INTEGRAL_TO_NOW);
        }
        choicesD.add(new ObisCodeFieldValue(8, builder.toString()));

        builder = new StringBuilder();
        if (code.hasBillingPeriod()) {
            builder.append(this.format(CHOICE_D_TIME_INTEGRAL_OVER_BILLING_PERIOD_X, new Object[]{code.getF()}));
        } else {
            builder.append(CHOICE_D_TIME_INTEGRAL_FROM_LAST_BILLING_POINT_TO_NOW);
        }
        choicesD.add(new ObisCodeFieldValue(9, builder.toString()));
        choicesD.add(new ObisCodeFieldValue(10, CHOICE_D_TIME_INTEGRAL_POSITIVE_DIFFERENCE));
        choicesD.add(new ObisCodeFieldValue(58, CHOICE_D_TIME_INTEGRAL_OVER_DEVICE_SPECIFIC_TIME));
        choicesD.add(new ObisCodeFieldValue(29, CHOICE_D_TIME_INTEGRAL_TO_NOW_USING_RECORDING_PERIOD1));
        choicesD.add(new ObisCodeFieldValue(30, CHOICE_D_TIME_INTEGRAL_TO_NOW_USING_RECORDING_PERIOD2));
    }

    protected void doHeatCostAllocatorRelated() {
        doDefault();
    }

    protected void doCoolingRelated() {
        doDefault();
    }

    protected void doHeatRelated() {
        doDefault();
    }

    protected void doGasRelated() {
        doDefault();
    }

    protected void doColdWaterRelated() {
        doDefault();
    }

    protected void doHotWaterRelated() {
        doDefault();
    }

    protected void space(StringBuilder builder) {
        builder.append(" ");
    }

    protected void doInBillingPeriodChoices(List<ObisCodeFieldValue> list) {
        for (ObisCodeFieldValue ocf : list) {
            if (code.hasBillingPeriod()) {
                if (code.isCurrentBillingPeriod()) {
                    ocf.add2Description(" ");
                    ocf.add2Description(IN_CURRENT_ACTIVE_BILLING_PERIOD);
                }
                else {
                    ocf.add2Description(" ");
                    ocf.add2Description(this.format(IN_BILLING_PERIOD_X, new Object[]{code.getF()}));
                }
            }
        }
    }

    private String findString(List<ObisCodeFieldValue> list, int val) {
        for (ObisCodeFieldValue ocfv : list) {
            if (ocfv.getCode() == val) {
                return ocfv.getDescription();
            }
        }
        return (code.toString());
    }

    // Since MessageFormat.format() interprets a single quote (')
    // as the start of a quoted string, a pattern string like
    // "It's recommended to change ... within {0,number{ day(s)"
    // isn't processed as expected: the {0, number} part is seen as part
    // of a quoted (no to touch) string and is NOT replaced by the 1st argument
    // eg. The result of
    // 1) MessageFormat.format("It's recommended to change ... within {0,number} day(s)", 10)
    // 2) MessageFormat.format("It's recommended to change '{0} to {0,number}' within {0,number} day(s)", 10)
    // 3) MessageFormat.format("It''s recommended to change '{0} to {0,number}' within {0,number} day(s)", 10)
    // is
    // 1) Its recommended to change ... within {0,number} day(s)         -- No argument replacement done
    // 2) Its recommended to change 10 to 10 within {0,number} day(s)    -- Wrong argument replacement done
    // 3) It's recommended to change {0} to {0,number} within 10 day(s)  -- Correct/As expected
    // Therefor in the pattern, we first replace each single quote
    // by two single quotes (indicating that quote is NOT the start of a quoted string)
    private String format(String pattern, Object[] arguments) {
        return MessageFormat.format(pattern.replaceAll("'", "''"), arguments);
    }

}
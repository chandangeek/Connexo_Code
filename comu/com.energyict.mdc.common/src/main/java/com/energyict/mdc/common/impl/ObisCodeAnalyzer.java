/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.impl;

import com.energyict.mdc.common.ObisCode;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ObisCodeAnalyzer {

    public static String REGISTER_ACTIVE_POWER_PLUS = "obisWizard.activePowerPlus";
    private static String REGISTER_ACTIVE_POWER_MINUS = "obisWizard.activePowerMinus";
    private static String REGISTER_REACTIVE_POWER_PLUS = "obisWizard.reactivePowerPlus";
    private static String REGISTER_REACTIVE_POWER_MINUS = "obisWizard.reactivePowerMinus";
    private static String REGISTER_REACTIVE_POWER_QI = "obisWizard.reactivePowerQI";
    private static String REGISTER_REACTIVE_POWER_QII = "obisWizard.reactivePowerQII";
    private static String REGISTER_REACTIVE_POWER_QIII = "obisWizard.reactivePowerQIII";
    private static String REGISTER_REACTIVE_POWER_QIV = "obisWizard.reactivePowerQIV";
    private static String REGISTER_APPARENT_POWER_PLUS = "obisWizard.apparentPowerPlus";
    private static String REGISTER_APPARENT_POWER_MINUS = "obisWizard.apparentPowerMinus";
    private static String REGISTER_CURRENT = "obisWizard.current";
    private static String REGISTER_VOLTAGE = "obisWizard.voltage";
    private static String REGISTER_POWER_FACTOR = "obisWizard.powerFactor";
    private static String REGISTER_FREQUENCY = "obisWizard.frequency";
    private static String REGISTER_ACTIVE_POWER_Q1423 = "obisWizard.activePowerQ1423";
    private static String REGISTER_ACTIVE_POWER_Q14_MINUS_Q23 = "obisWizard.activePowerQ14MinusQ23";
    private static String REGISTER_ACTIVE_POWER_QI = "obisWizard.activePowerQI";
    private static String REGISTER_ACTIVE_POWER_QII = "obisWizard.activePowerQII";
    private static String REGISTER_ACTIVE_POWER_QIII = "obisWizard.activePowerQIII";
    private static String REGISTER_ACTIVE_POWER_QIV = "obisWizard.activePowerQIV";

    public static String ALL_PHASES = "obisWizard.allPhases";
    private static String PHASE_1 = "obisWizard.phase1";
    private static String PHASE_2 = "obisWizard.phase2";
    private static String PHASE_3 = "obisWizard.phase3";

    private static String CHOICE_A_ABSTRACT_OBIS_CODE = "obisWizard.abstractObisCode";
    private static String CHOICE_A_ELECTRICITY_OBIS_CODE = "obisWizard.electricityRelatedObisCode";
    private static String CHOICE_A_HEAT_COST_ALLOCATOR_OBIS_CODE = "obisWizard.heatCostAllocatorRelatedObisCode";
    private static String CHOICE_A_COOLING_OBIS_CODE = "obisWizard.coolingRelatedObisCode";
    private static String CHOICE_A_HEAT_OBIS_CODE = "obisWizard.heatRelatedObisCode";
    private static String CHOICE_A_GAS_OBIS_CODE = "obisWizard.gasRelatedObisCode";
    private static String CHOICE_A_COLD_WATER_OBIS_CODE = "obisWizard.coldWaterRelatedObisCode";
    private static String CHOICE_A_HOT_WATER_OBIS_CODE = "obisWizard.hotWaterRelatedObisCode";

    private static String CHOICE_C_GENERAL_SERVICE_ENTRIES = "obisWizard.generalServiceEntries";
    private static String CHOICE_C_GENERAL_PURPOSE = "obisWizard.generalPurpose";
    private static String CHOICE_C_UNITLESS_QUANTITY = "obisWizard.unitlessQuantity";
    private static String CHOICE_C_L0_CURRENT = "obisWizard.l0Current";
    private static String CHOICE_C_L0_VOLTAGE = "obisWizard.l0Voltage";

    private static String CHOICE_D_PARAMETER_CHANGES_OBIS_CODE = "obisWizard.parameterChangesRelatedObisCode";
    private static String CHOICE_D_IO_CONTROL_SIGNALS_OBIS_CODE = "obisWizard.IOControlSignalsRelatedObisCode";
    private static String CHOICE_D_INTERNAL_CONTROL_SIGNALS_OBIS_CODE = "obisWizard.internalControlSignalsRelatedObisCode";
    private static String CHOICE_D_INTERNAL_STATUS_SIGNALS_OBIS_CODE = "obisWizard.internalOperatingStatusRelatedObisCode";
    private static String CHOICE_D_BATTERY_ENTRIES_OBIS_CODE = "obisWizard.batteryEntriesRelatedObisCode";
    private static String CHOICE_D_POWER_FAILURES_OBIS_CODE = "obisWizard.powerFailuresRelatedObisCode";
    private static String CHOICE_D_OPERATING_TIME_OBIS_CODE = "obisWizard.operatingTimeRelatedObisCode";
    private static String CHOICE_D_ENVIRONMENTAL_OBIS_CODE = "obisWizard.environmentalRelatedObisCode";
    private static String CHOICE_D_BILLING_PERIOD_COUNTER_ENTRIES = "obisWizard.billingPeriodCounterEntries";
    private static String CHOICE_D_PROGRAM_ENTRIES = "obisWizard.programEntries";
    private static String CHOICE_D_OUTPUT_PULSE_CONSTANTS = "obisWizard.outputPulseConstants";
    private static String CHOICE_D_RATIOS = "obisWizard.ratios";
    private static String CHOICE_D_NOMINAL_VALUES = "obisWizard.nominalValues";
    private static String CHOICE_D_INPUT_PULSE_CONSTANTS = "obisWizard.inputPulseConstants";
    private static String CHOICE_D_MEASUREMENT_PERIODS = "obisWizard.measurementPeriods";
    private static String CHOICE_D_TIME_ENTRIES = "obisWizard.timeEntries";
    private static String CHOICE_D_COEFFICIENTS = "obisWizard.coefficients";
    private static String CHOICE_D_MEASUREMENT_METHODS = "obisWizard.measurementMethods";
    private static String CHOICE_D_BILLING_PERIOD_AVG = "obisWizard.billingPeriodAverage";
    private static String CHOICE_D_CUMULATIVE_MIN_USING_MEASUREMENT_PERIOD1 = "obisWizard.cumulativeMinimumUsingMeasurementPeriod1";
    private static String CHOICE_D_CUMULATIVE_MAX_USING_MEASUREMENT_PERIOD1 = "obisWizard.cumulativeMaximumUsingMeasurementPeriod1";
    private static String CHOICE_D_MIN_USING_MEASUREMENT_PERIOD1 = "obisWizard.minimumUsingMeasurementPeriod1";
    private static String CHOICE_D_CURRENT_AVG_USING_MEASUREMENT_PERIOD1 = "obisWizard.currentAverageUsingMeasurementPeriod1";
    private static String CHOICE_D_LAST_AVG_USING_MEASUREMENT_PERIOD1 = "obisWizard.lastAverageUsingMeasurementPeriod1";
    private static String CHOICE_D_MAX_USING_MEASUREMENT_PERIOD1 = "obisWizard.maximumUsingMeasurementPeriod1";
    private static String CHOICE_D_INSTANTANEOUS_VALUE = "obisWizard.instantaneousValue";
    private static String CHOICE_D_CUMULATIVE_MIN_USING_MEASUREMENT_PERIOD2 = "obisWizard.cumulativeMinimumUsingMeasurementPeriod2";
    private static String CHOICE_D_CUMULATIVE_MAX_USING_MEASUREMENT_PERIOD2 = "obisWizard.cumulativeMaximumUsingMeasurementPeriod2";
    private static String CHOICE_D_MIN_USING_MEASUREMENT_PERIOD2 = "obisWizard.minimumUsingMeasurementPeriod2";
    private static String CHOICE_D_CURRENT_AVG_USING_MEASUREMENT_PERIOD2 = "obisWizard.currentAverageUsingMeasurementPeriod2";
    private static String CHOICE_D_LAST_AVG_USING_MEASUREMENT_PERIOD2 = "obisWizard.lastAverageUsingMeasurementPeriod2";
    private static String CHOICE_D_MAX_USING_MEASUREMENT_PERIOD2 = "obisWizard.maximumUsingMeasurementPeriod2";
    private static String CHOICE_D_CUMULATIVE_MIN_USING_MEASUREMENT_PERIOD3 = "obisWizard.cumulativeMinimumUsingMeasurementPeriod3";
    private static String CHOICE_D_CUMULATIVE_MAX_USING_MEASUREMENT_PERIOD3 = "obisWizard.cumulativeMaximumUsingMeasurementPeriod3";
    private static String CHOICE_D_MIN_USING_MEASUREMENT_PERIOD3 = "obisWizard.minimumUsingMeasurementPeriod3";
    private static String CHOICE_D_CURRENT_AVG_USING_MEASUREMENT_PERIOD3 = "obisWizard.currentAverageUsingMeasurementPeriod3";
    private static String CHOICE_D_LAST_AVG_USING_MEASUREMENT_PERIOD3 = "obisWizard.lastAverageUsingMeasurementPeriod3";
    private static String CHOICE_D_MAX_USING_MEASUREMENT_PERIOD3 = "obisWizard.maximumUsingMeasurementPeriod3";
    private static String CHOICE_D_CURRENT_AVG_USING_RECORDING_INTERVAL1 = "obisWizard.currentAverageUsingRecordingInterval1";
    private static String CHOICE_D_CURRENT_AVG_USING_RECORDING_INTERVAL2 = "obisWizard.currentAverageUsingRecordingInterval2";
    private static String CHOICE_D_UNDER_LIMIT_THRESHOLD = "obisWizard.underLimitThreshold";
    private static String CHOICE_D_UNDER_LIMIT_OCCURRENCE_COUNTER = "obisWizard.underLimitOccurrenceCounter";
    private static String CHOICE_D_UNDER_LIMIT_DURATION = "obisWizard.underLimitDuration";
    private static String CHOICE_D_UNDER_LIMIT_MAGNITUDE = "obisWizard.underLimitMagnitude";
    private static String CHOICE_D_OVER_LIMIT_THRESHOLD = "obisWizard.overLimitThreshold";
    private static String CHOICE_D_OVER_LIMIT_OCCURRENCE_COUNTER = "obisWizard.overLimitOccurrenceCounter";
    private static String CHOICE_D_OVER_LIMIT_DURATION = "obisWizard.overLimitDuration";
    private static String CHOICE_D_OVER_LIMIT_MAGNITUDE = "obisWizard.overLimitMagnitude";
    private static String CHOICE_D_MISSING_THRESHOLD = "obisWizard.missingThreshold";
    private static String CHOICE_D_MISSING_OCCURRENCE_COUNTER = "obisWizard.missingOccurrenceCounter";
    private static String CHOICE_D_MISSING_DURATION = "obisWizard.missingDuration";
    private static String CHOICE_D_MISSING_MAGNITUDE = "obisWizard.missingMagnitude";
    private static String CHOICE_D_TEST_AVG = "obisWizard.testAverage";
    private static String CHOICE_D_TIME_INTEGRAL_TO_BILLING_POINT_X = "obisWizard.timeIntegralFromStartOfMeasurementToBillingPointX";
    private static String CHOICE_D_TIME_INTEGRAL_TO_NOW = "obisWizard.timeIntegralFromStartOfMeasurementToNow";
    private static String CHOICE_D_TIME_INTEGRAL_OVER_BILLING_PERIOD_X = "obisWizard.timeIntegralOverBillingPeriodX";
    private static String CHOICE_D_TIME_INTEGRAL_FROM_LAST_BILLING_POINT_TO_NOW = "obisWizard.timeIntegralFromLastBillingPointToNow";
    private static String CHOICE_D_TIME_INTEGRAL_POSITIVE_DIFFERENCE = "obisWizard.timeIntegralOfPositiveDifference";
    private static String CHOICE_D_TIME_INTEGRAL_OVER_DEVICE_SPECIFIC_TIME = "obisWizard.timeIntegralOverDeviceSpecificTime";
    private static String CHOICE_D_TIME_INTEGRAL_TO_NOW_USING_RECORDING_PERIOD1 = "obisWizard.timeIntegralFromStartOfCurrentRecordingIntervalToNowUsingRecordingPeriod1";
    private static String CHOICE_D_TIME_INTEGRAL_TO_NOW_USING_RECORDING_PERIOD2 = "obisWizard.timeIntegralFromStartOfCurrentRecordingIntervalToNowUsingRecordingPeriod2";

    private static String CHOICE_E_NR_OF_CFG_PROGRAM_CHANGES = "obisWizard.nrOfConfigProgramChanges";
    private static String CHOICE_E_DATE_OF_LAST_CFG_PROGRAM_CHANGE = "obisWizard.dateOfLastConfigProgramChange";
    private static String CHOICE_E_DATE_OF_LAST_TIME_SWITCH_PROGRAM_CHANGE = "obisWizard.dateOfLastTimeSwitchProgramChange";
    private static String CHOICE_E_DATE_OF_LAST_RIPPLE_CONTROL_RECEIVER_PROGRAM_CHANGE = "obisWizard.dateOfLastRippleControlReceiverProgramChange";
    private static String CHOICE_E_STATUS_OF_SECURITY_SWITCHES = "obisWizard.statusOfSecuritySwitches";
    private static String CHOICE_E_DATE_OF_LAST_CALIBRATION = "obisWizard.dateOfLastCalibration";
    private static String CHOICE_E_DATE_OF_NEXT_CFG_PROGRAM_CHANGE = "obisWizard.dateOfNextConfigProgramChange";
    private static String CHOICE_E_NR_OF_PROTECTED_CFG_PROGRAM_CHANGES = "obisWizard.nrOfProtectedConfigProgramChanges";
    private static String CHOICE_E_DATE_OF_LAST_PROTECTED_CFG_PROGRAM_CHANGE = "obisWizard.dateOfLastProtectedConfigProgramChange";
    private static String CHOICE_E_STATE_INPUT_CONTROL_SIGNALS = "obisWizard.stateOfInputControlSignals";
    private static String CHOICE_E_STATE_OUTPUT_CONTROL_SIGNALS = "obisWizard.stateOfOutputControlSignals";
    private static String CHOICE_E_STATE_OF_INTERNAL_CONTROL_SIGNALS = "obisWizard.stateOfInternalControlSignals";
    private static String CHOICE_E_INTERNAL_OPERATING_STATUS = "obisWizard.internalOperatingStatus";
    private static String CHOICE_E_BATTERY_USE_TIME_COUNTER = "obisWizard.batteryUseTimeCounter";
    private static String CHOICE_E_BATTERY_CHARGE_DISPLAY = "obisWizard.batteryChargeDisplay";
    private static String CHOICE_E_DATE_OF_NEXT_CHANGE = "obisWizard.dateOfNextChange";
    private static String CHOICE_E_BATTERY_VOLTAGE = "obisWizard.batteryVoltage";
    private static String CHOICE_E_TOTAL_FAILURE_3_PHASES = "obisWizard.totalFailureOfAllThreePhases";
    private static String CHOICE_E_PHASE_1_FAILURE = "obisWizard.phase1Failure";
    private static String CHOICE_E_PHASE_2_FAILURE = "obisWizard.phase2Failure";
    private static String CHOICE_E_PHASE_3_FAILURE = "obisWizard.phase3Failure";
    private static String CHOICE_E_TIME_OF_OPERATION = "obisWizard.timeOfOperation";
    private static String CHOICE_E_TIME_OF_REGISTRATION_RATE_X = "obisWizard.timeOfRegistrationRateX";
    private static String CHOICE_E_AMBIENT_TEMPERATURE = "obisWizard.ambientTemperature";
    private static String CHOICE_E_HARMONIC_X = "obisWizard.harmonicX";
    private static String CHOICE_E_RATE_X = "obisWizard.rateX";
    private static String CHOICE_E_BILLING_PERIOD_COUNTER = "obisWizard.billingPeriodCounter";
    private static String CHOICE_E_NR_OF_AVAILABLE_BILLING_PERIODS = "obisWizard.nrOfAvailableBillingPeriods";
    private static String CHOICE_E_TIMESTAMP_OF_BILLING_POINT_IN_CODE_F = "obisWizard.timestampOfBillingPointInCodeF";
    private static String CHOICE_E_TIMESTAMP_OF_BILLING_POINT_X = "obisWizard.timestampOfBillingPointX";
    private static String CHOICE_E_CFG_PROGRAM_VERSION_NUMBER = "obisWizard.configurationProgramVersionNumber";
    private static String CHOICE_E_PARAMETER_RECORD_NUMBER = "obisWizard.parameterRecordNumber";
    private static String CHOICE_E_TIME_SWITCH_PROGRAM_NUMBER = "obisWizard.timeSwitchProgramNumber";
    private static String CHOICE_E_RCR_PROGRAM_NUMBER = "obisWizard.RCRProgramNumber";
    private static String CHOICE_E_MASTER_CONNECTION_DIAGRAM_ID = "obisWizard.masterConnectionDiagramId";
    private static String CHOICE_E_RLW_ACTIVE_ENERGY = "obisWizard.RLWActiveEnergy";
    private static String CHOICE_E_RLB_REACTIVE_ENERGY = "obisWizard.RLBReactiveEnergy";
    private static String CHOICE_E_RLS_APPARENT_ENERGY = "obisWizard.RLSApparentEnergy";
    private static String CHOICE_E_RAW_ACTIVE_ENERGY = "obisWizard.RAWActiveEnergy";
    private static String CHOICE_E_RAB_REACTIVE_ENERGY = "obisWizard.RABReactiveEnergy";
    private static String CHOICE_E_RAS_APPARENT_ENERGY = "obisWizard.RASApparentEnergy";
    private static String CHOICE_E_TRANSFORMER_RATIO_CURRENT_NUMERATOR = "obisWizard.transformerRatioCurrentNumerator";
    private static String CHOICE_E_TRANSFORMER_RATIO_VOLTAGE_NUMERATOR = "obisWizard.transformerRatioVoltageNumerator";
    private static String CHOICE_E_OVERALL_TRANSFORMER_RATIO_NUMERATOR = "obisWizard.overallTransformerRatioNumerator";
    private static String CHOICE_E_TRANSFORMER_RATIO_CURRENT_DENOMINATOR = "obisWizard.transformerRatioCurrentDenominator";
    private static String CHOICE_E_TRANSFORMER_RATIO_VOLTAGE_DENOMINATOR = "obisWizard.transformerRatioVoltageDenominator";
    private static String CHOICE_E_OVERALL_TRANSFORMER_RATIO_DENOMINATOR = "obisWizard.overallTransformerRatioDenominator";
    private static String CHOICE_E_READING_FACTOR_FOR_POWER = "obisWizard.readingFactorForPower";
    private static String CHOICE_E_READING_FACTOR_FOR_ENERGY = "obisWizard.readingFactorForEnergy";
    private static String CHOICE_E_REFERENCE_VOLTAGE_FOR_POWER_QUALITY = "obisWizard.referenceVoltageForPowerQualityMeasurement";
    private static String CHOICE_E_NOMINAL_VOLTAGE = "obisWizard.nominalVoltage";
    private static String CHOICE_E_NOMINAL_CURRENT = "obisWizard.nominalCurrent";
    private static String CHOICE_E_NOMINAL_FREQUENCY = "obisWizard.nominalFrequency";
    private static String CHOICE_E_MAXIMUM_CURRENT = "obisWizard.maximumCurrent";
    private static String CHOICE_E_REW_ACTIVE_ENERGY = "obisWizard.REWActiveEnergy";
    private static String CHOICE_E_REB_REACTIVE_ENERGY = "obisWizard.REBReactiveEnergy";
    private static String CHOICE_E_RES_APPARENT_ENERGY = "obisWizard.RESApparentEnergy";
    private static String CHOICE_E_MEASUREMENT_PERIOD_1_FOR_AVG_1 = "obisWizard.measurementPeriod1ForAvgValue1";
    private static String CHOICE_E_MEASUREMENT_PERIOD_2_FOR_AVG_2 = "obisWizard.measurementPeriod2ForAvgValue2";
    private static String CHOICE_E_MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUE = "obisWizard.measurementPeriod3ForInstantaneousValue";
    private static String CHOICE_E_RECORDING_INTERVAL_1_FOR_LOAD_PROFILE = "obisWizard.recordingInterval1ForLoadProfile";
    private static String CHOICE_E_RECORDING_INTERVAL_2_FOR_LOAD_PROFILE = "obisWizard.recordingInterval2ForLoadProfile";
    private static String CHOICE_E_BILLING_PERIOD = "obisWizard.billingPeriod";
    private static String CHOICE_E_MEASUREMENT_PERIOD_4_FOR_TEST_VALUE = "obisWizard.measurementPeriod4ForTestValue";
    private static String CHOICE_E_TIME_EXPIRED_SINCE_LAST_BILLING_POINT = "obisWizard.timeExpiredSinceLastBillingPoint";
    private static String CHOICE_E_LOCAL_TIME = "obisWizard.localTime";
    private static String CHOICE_E_LOCAL_DATE = "obisWizard.localDate";
    private static String CHOICE_E_WEEK_DAY = "obisWizard.weekDay";
    private static String CHOICE_E_TIME_OF_LAST_RESET = "obisWizard.timeOfLastReset";
    private static String CHOICE_E_DATE_OF_LAST_RESET = "obisWizard.dateOfLastReset";
    private static String CHOICE_E_OUTPUT_PULSE_DURATION = "obisWizard.outputPulseDuration";
    private static String CHOICE_E_CLOCK_SYNC_WINDOW = "obisWizard.clockSyncWindow";
    private static String CHOICE_E_CLOCK_SYNC_METHOD = "obisWizard.clockSyncMethod";
    private static String CHOICE_E_TRANSFORMER_MAGNETIC_LOSSES = "obisWizard.transformerMagneticLosses";
    private static String CHOICE_E_TRANSFORMER_THERMA_LOSSES = "obisWizard.transformerThermaLosses";
    private static String CHOICE_E_LINE_RESISTANCE_LOSSES = "obisWizard.lineResistanceLosses";
    private static String CHOICE_E_LINE_REACTANCE_LOSSES = "obisWizard.lineReactanceLosses";
    private static String CHOICE_E_ALGORITHM_ACTIVE_POWER_MANAGEMENT = "obisWizard.algorithmForActivePowerMeasurement";
    private static String CHOICE_E_ALGORITHM_ACTIVE_ENERGY_MANAGEMENT = "obisWizard.algorithmForActiveEnergyMeasurement";
    private static String CHOICE_E_ALGORITHM_REACTIVE_POWER_MANAGEMENT = "obisWizard.algorithmForReactivePowerMeasurement";
    private static String CHOICE_E_ALGORITHM_REACTIVE_ENERGY_MANAGEMENT = "obisWizard.algorithmForReactiveEnergyMeasurement";
    private static String CHOICE_E_ALGORITHM_APPARENT_POWER_MANAGEMENT = "obisWizard.algorithmForApparentPowerMeasurement";
    private static String CHOICE_E_ALGORITHM_APPARENT_ENERGY_MANAGEMENT = "obisWizard.algorithmForApparentEnergyMeasurement";
    private static String CHOICE_E_ALGORITHM_POWER_FACTOR_CALCULATION = "obisWizard.algorithmForPowerFactorCalculation";

    private static String IN_CURRENT_ACTIVE_BILLING_PERIOD = "obisWizard.inCurrentActiveBillingPeriod";
    private static String IN_BILLING_PERIOD_X = "obisWizard.inBillingPeriodX";

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
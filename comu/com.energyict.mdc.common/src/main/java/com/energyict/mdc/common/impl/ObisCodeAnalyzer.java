package com.energyict.mdc.common.impl;

import com.elster.jupiter.nls.Thesaurus;

import com.energyict.obis.ObisCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ObisCodeAnalyzer {

    private static final ObisCodeTranslationKeys[] eDemandRegisters = {
            ObisCodeTranslationKeys.REGISTER_ACTIVE_POWER_PLUS,
            ObisCodeTranslationKeys.REGISTER_ACTIVE_POWER_MINUS,
            ObisCodeTranslationKeys.REGISTER_REACTIVE_POWER_PLUS,
            ObisCodeTranslationKeys.REGISTER_REACTIVE_POWER_MINUS,
            ObisCodeTranslationKeys.REGISTER_REACTIVE_POWER_QI,
            ObisCodeTranslationKeys.REGISTER_REACTIVE_POWER_QII,
            ObisCodeTranslationKeys.REGISTER_REACTIVE_POWER_QIII,
            ObisCodeTranslationKeys.REGISTER_REACTIVE_POWER_QIV,
            ObisCodeTranslationKeys.REGISTER_APPARENT_POWER_PLUS,
            ObisCodeTranslationKeys.REGISTER_APPARENT_POWER_MINUS,
            ObisCodeTranslationKeys.REGISTER_CURRENT,
            ObisCodeTranslationKeys.REGISTER_VOLTAGE,
            ObisCodeTranslationKeys.REGISTER_POWER_FACTOR,
            ObisCodeTranslationKeys.REGISTER_FREQUENCY,
            ObisCodeTranslationKeys.REGISTER_ACTIVE_POWER_Q1423,
            ObisCodeTranslationKeys.REGISTER_ACTIVE_POWER_Q14_MINUS_Q23,
            ObisCodeTranslationKeys.REGISTER_ACTIVE_POWER_QI,
            ObisCodeTranslationKeys.REGISTER_ACTIVE_POWER_QII,
            ObisCodeTranslationKeys.REGISTER_ACTIVE_POWER_QIII,
            ObisCodeTranslationKeys.REGISTER_ACTIVE_POWER_QIV,
    };

    private static final ObisCodeTranslationKeys[] phaseStrings = {
            ObisCodeTranslationKeys.ALL_PHASES,
            ObisCodeTranslationKeys.PHASE_1,
            ObisCodeTranslationKeys.PHASE_2,
            ObisCodeTranslationKeys.PHASE_3
    };

    private final Thesaurus thesaurus;
    private final ObisCode code;

    StringBuilder descriptionBuilder;
    List<ObisCodeFieldValue> choicesA;
    List<ObisCodeFieldValue> choicesB;
    List<ObisCodeFieldValue> choicesC;
    List<ObisCodeFieldValue> choicesD;
    List<ObisCodeFieldValue> choicesE;
    List<ObisCodeFieldValue> choicesF;

    ObisCodeAnalyzer(ObisCode code, Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        this.code = code;
    }

    String getDescription() {
        descriptionBuilder = new StringBuilder();
        buildDescription();
        return descriptionBuilder.toString();
    }

    private void buildDescriptionChoices() {
        choicesA = choicesB = choicesC = choicesD = choicesE = choicesF = null;
        choicesF = new ArrayList<>(); // to enable billing choice dialog panel
        choicesA = new ArrayList<>();
        choicesA.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_A_ABSTRACT_OBIS_CODE, thesaurus));
        choicesA.add(new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_A_ELECTRICITY_OBIS_CODE, thesaurus));
        choicesA.add(new ObisCodeFieldValue(4, ObisCodeTranslationKeys.CHOICE_A_HEAT_COST_ALLOCATOR_OBIS_CODE, thesaurus));
        choicesA.add(new ObisCodeFieldValue(5, ObisCodeTranslationKeys.CHOICE_A_COOLING_OBIS_CODE, thesaurus));
        choicesA.add(new ObisCodeFieldValue(6, ObisCodeTranslationKeys.CHOICE_A_HEAT_OBIS_CODE, thesaurus));
        choicesA.add(new ObisCodeFieldValue(7, ObisCodeTranslationKeys.CHOICE_A_GAS_OBIS_CODE, thesaurus));
        choicesA.add(new ObisCodeFieldValue(8, ObisCodeTranslationKeys.CHOICE_A_COLD_WATER_OBIS_CODE, thesaurus));
        choicesA.add(new ObisCodeFieldValue(9, ObisCodeTranslationKeys.CHOICE_A_HOT_WATER_OBIS_CODE, thesaurus));
    }

    private void buildDescription() {
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

    private void doReservedACode() {
        doDefault();
    }

    private void doDefault() {
        descriptionBuilder.append(code);
    }

    private void doAbstractChoices() {
        choicesC = new ArrayList<>();
        choicesC.add(new ObisCodeFieldValue(96, ObisCodeTranslationKeys.CHOICE_C_GENERAL_SERVICE_ENTRIES, this.thesaurus));
    }

    private void buildAbstractChoices() {
        choicesD = new ArrayList<>();
        choicesD.add(new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_D_PARAMETER_CHANGES_OBIS_CODE, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(3, ObisCodeTranslationKeys.CHOICE_D_IO_CONTROL_SIGNALS_OBIS_CODE, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(4, ObisCodeTranslationKeys.CHOICE_D_INTERNAL_CONTROL_SIGNALS_OBIS_CODE, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(5, ObisCodeTranslationKeys.CHOICE_D_INTERNAL_STATUS_SIGNALS_OBIS_CODE, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(6, ObisCodeTranslationKeys.CHOICE_D_BATTERY_ENTRIES_OBIS_CODE, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(7, ObisCodeTranslationKeys.CHOICE_D_POWER_FAILURES_OBIS_CODE, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(8, ObisCodeTranslationKeys.CHOICE_D_OPERATING_TIME_OBIS_CODE, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(9, ObisCodeTranslationKeys.CHOICE_D_ENVIRONMENTAL_OBIS_CODE, this.thesaurus));
    }

    private void doAbstract() {
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

    private void doParameterChanges() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_NR_OF_CFG_PROGRAM_CHANGES, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_E_DATE_OF_LAST_CFG_PROGRAM_CHANGE, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_E_DATE_OF_LAST_TIME_SWITCH_PROGRAM_CHANGE, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(3, ObisCodeTranslationKeys.CHOICE_E_DATE_OF_LAST_RIPPLE_CONTROL_RECEIVER_PROGRAM_CHANGE, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(4, ObisCodeTranslationKeys.CHOICE_E_STATUS_OF_SECURITY_SWITCHES, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(5, ObisCodeTranslationKeys.CHOICE_E_DATE_OF_LAST_CALIBRATION, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(6, ObisCodeTranslationKeys.CHOICE_E_DATE_OF_NEXT_CFG_PROGRAM_CHANGE, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(10, ObisCodeTranslationKeys.CHOICE_E_NR_OF_PROTECTED_CFG_PROGRAM_CHANGES, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(11, ObisCodeTranslationKeys.CHOICE_E_DATE_OF_LAST_PROTECTED_CFG_PROGRAM_CHANGE, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doInputOutputControlSignals() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_E_STATE_INPUT_CONTROL_SIGNALS, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_E_STATE_OUTPUT_CONTROL_SIGNALS, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doInternalControlSignals() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_STATE_OF_INTERNAL_CONTROL_SIGNALS, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doInternalOperatingStatus() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_INTERNAL_OPERATING_STATUS, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doBatteryEntries() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_BATTERY_USE_TIME_COUNTER, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_E_BATTERY_CHARGE_DISPLAY, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_E_DATE_OF_NEXT_CHANGE, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(3, ObisCodeTranslationKeys.CHOICE_E_BATTERY_VOLTAGE, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doNumberOfPowerFailures() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_TOTAL_FAILURE_3_PHASES, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_E_PHASE_1_FAILURE, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_E_PHASE_2_FAILURE, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(3, ObisCodeTranslationKeys.CHOICE_E_PHASE_3_FAILURE, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doOperatingTimechoices() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_TIME_OF_OPERATION, this.thesaurus));
        for (int codeE = 1; codeE < 64; codeE++) {
            choicesE.add(new ObisCodeFieldValue(codeE, ObisCodeTranslationKeys.CHOICE_E_TIME_OF_REGISTRATION_RATE_X, this.thesaurus));
        }
    }

    private void doOperatingTime() {
        doOperatingTimechoices();
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doEnvironmental() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_AMBIENT_TEMPERATURE, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }


    private void doElectricityRelatedChoices() {
        choicesC = new ArrayList<>();
        choicesC.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_C_GENERAL_PURPOSE, this.thesaurus));
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


    private void doDemandRegisterChoices() {
        choicesC.add(new ObisCodeFieldValue(82, ObisCodeTranslationKeys.CHOICE_C_UNITLESS_QUANTITY, this.thesaurus));
        choicesC.add(new ObisCodeFieldValue(91, ObisCodeTranslationKeys.CHOICE_C_L0_CURRENT, this.thesaurus));
        choicesC.add(new ObisCodeFieldValue(92, ObisCodeTranslationKeys.CHOICE_C_L0_VOLTAGE, this.thesaurus));
        for (int codeC = 1; codeC <= 80; codeC++) {
            StringBuilder builder = new StringBuilder();
            int phenoTypeId = codeC % 20;
            int phaseId = (codeC - 1) / 20;
            if (phenoTypeId == 0) {
                phenoTypeId = 20;
            }
            builder.append(this.thesaurus.getFormat(eDemandRegisters[phenoTypeId - 1]).format());
            space(builder);
            builder.append(this.thesaurus.getFormat(phaseStrings[phaseId]).format());
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
            choicesC.add(new ObisCodeFieldValue(codeC, builder.toString(), thesaurus));
        }

        descriptionBuilder.append(findString(choicesC, code.getC()));

    }

    private void doHarmonics(StringBuilder builder) {
        if (code.getE() > 0 && code.getE() < 128) {
            choicesE = null;
            builder.append(this.thesaurus.getFormat(ObisCodeTranslationKeys.CHOICE_E_HARMONIC_X).format(code.getE()));
            space(builder);
        }
    }

    private void doTariff(StringBuilder builder) {
        if (code.getE() > 0 && code.getE() < 64) {
            choicesE = null;
            builder.append(this.thesaurus.getFormat(ObisCodeTranslationKeys.CHOICE_E_RATE_X).format(code.getE()));
            space(builder);
        }
    }

    private void doGeneralPurposeChoices() {
        choicesD = Arrays.asList(
                        new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_D_BILLING_PERIOD_COUNTER_ENTRIES, this.thesaurus),
                        new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_D_PROGRAM_ENTRIES, this.thesaurus),
                        new ObisCodeFieldValue(3, ObisCodeTranslationKeys.CHOICE_D_OUTPUT_PULSE_CONSTANTS, this.thesaurus),
                        new ObisCodeFieldValue(4, ObisCodeTranslationKeys.CHOICE_D_RATIOS, this.thesaurus),
                        new ObisCodeFieldValue(6, ObisCodeTranslationKeys.CHOICE_D_NOMINAL_VALUES, this.thesaurus),
                        new ObisCodeFieldValue(7, ObisCodeTranslationKeys.CHOICE_D_INPUT_PULSE_CONSTANTS, this.thesaurus),
                        new ObisCodeFieldValue(8, ObisCodeTranslationKeys.CHOICE_D_MEASUREMENT_PERIODS, this.thesaurus),
                        new ObisCodeFieldValue(9, ObisCodeTranslationKeys.CHOICE_D_TIME_ENTRIES, this.thesaurus),
                        new ObisCodeFieldValue(10, ObisCodeTranslationKeys.CHOICE_D_COEFFICIENTS, this.thesaurus),
                        new ObisCodeFieldValue(11, ObisCodeTranslationKeys.CHOICE_D_MEASUREMENT_METHODS, this.thesaurus));
    }

    private void doGeneralPurpose() {

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

    private void doBillingPeriodCounterEntries() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_BILLING_PERIOD_COUNTER, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_E_NR_OF_AVAILABLE_BILLING_PERIODS, this.thesaurus));
        if (code.getF() == 255) {
            choicesE.add(new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_E_TIMESTAMP_OF_BILLING_POINT_IN_CODE_F, this.thesaurus));
        } else {
            choicesE.add(new ObisCodeFieldValue(2, this.thesaurus.getFormat(ObisCodeTranslationKeys.CHOICE_E_TIMESTAMP_OF_BILLING_POINT_X).format(code.getF()), thesaurus));
        }
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doProgramEntries() {
        choicesE = Arrays.asList(
                        new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_CFG_PROGRAM_VERSION_NUMBER, this.thesaurus),
                        new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_E_PARAMETER_RECORD_NUMBER, this.thesaurus),
                        new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_E_TIME_SWITCH_PROGRAM_NUMBER, this.thesaurus),
                        new ObisCodeFieldValue(3, ObisCodeTranslationKeys.CHOICE_E_RCR_PROGRAM_NUMBER, this.thesaurus),
                        new ObisCodeFieldValue(4, ObisCodeTranslationKeys.CHOICE_E_MASTER_CONNECTION_DIAGRAM_ID, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doOutputPulseConstants() {
        choicesE = Arrays.asList(
                        new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_RLW_ACTIVE_ENERGY, this.thesaurus),
                        new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_E_RLB_REACTIVE_ENERGY, this.thesaurus),
                        new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_E_RLS_APPARENT_ENERGY, this.thesaurus),
                        new ObisCodeFieldValue(3, ObisCodeTranslationKeys.CHOICE_E_RAW_ACTIVE_ENERGY, this.thesaurus),
                        new ObisCodeFieldValue(4, ObisCodeTranslationKeys.CHOICE_E_RAB_REACTIVE_ENERGY, this.thesaurus),
                        new ObisCodeFieldValue(5, ObisCodeTranslationKeys.CHOICE_E_RAS_APPARENT_ENERGY, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doRatios() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_E_TRANSFORMER_RATIO_CURRENT_NUMERATOR, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(3, ObisCodeTranslationKeys.CHOICE_E_TRANSFORMER_RATIO_VOLTAGE_NUMERATOR, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(4, ObisCodeTranslationKeys.CHOICE_E_OVERALL_TRANSFORMER_RATIO_NUMERATOR, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(5, ObisCodeTranslationKeys.CHOICE_E_TRANSFORMER_RATIO_CURRENT_DENOMINATOR, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(6, ObisCodeTranslationKeys.CHOICE_E_TRANSFORMER_RATIO_VOLTAGE_DENOMINATOR, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(7, ObisCodeTranslationKeys.CHOICE_E_OVERALL_TRANSFORMER_RATIO_DENOMINATOR, this.thesaurus));
        doInBillingPeriodChoices(choicesE);
        choicesE.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_READING_FACTOR_FOR_POWER, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_E_READING_FACTOR_FOR_ENERGY, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doNominalValues() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(4, ObisCodeTranslationKeys.CHOICE_E_REFERENCE_VOLTAGE_FOR_POWER_QUALITY, this.thesaurus));
        doInBillingPeriodChoices(choicesE);
        choicesE.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_NOMINAL_VOLTAGE, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_E_NOMINAL_CURRENT, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_E_NOMINAL_FREQUENCY, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(3, ObisCodeTranslationKeys.CHOICE_E_MAXIMUM_CURRENT, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doInputPulseConstants() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_REW_ACTIVE_ENERGY, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_E_REB_REACTIVE_ENERGY, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_E_RES_APPARENT_ENERGY, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doMeasurementsPeriods() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_MEASUREMENT_PERIOD_1_FOR_AVG_1, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_E_MEASUREMENT_PERIOD_2_FOR_AVG_2, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_E_MEASUREMENT_PERIOD_3_FOR_INSTANTANEOUS_VALUE, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(4, ObisCodeTranslationKeys.CHOICE_E_RECORDING_INTERVAL_1_FOR_LOAD_PROFILE, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(5, ObisCodeTranslationKeys.CHOICE_E_RECORDING_INTERVAL_2_FOR_LOAD_PROFILE, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(6, ObisCodeTranslationKeys.CHOICE_E_BILLING_PERIOD, this.thesaurus));
        doInBillingPeriodChoices(choicesE);
        choicesE.add(new ObisCodeFieldValue(3, ObisCodeTranslationKeys.CHOICE_E_MEASUREMENT_PERIOD_4_FOR_TEST_VALUE, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doTimeEntries() {
        choicesE = Arrays.asList(
                        new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_TIME_EXPIRED_SINCE_LAST_BILLING_POINT, this.thesaurus),
                        new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_E_LOCAL_TIME, this.thesaurus),
                        new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_E_LOCAL_DATE, this.thesaurus),
                        new ObisCodeFieldValue(5, ObisCodeTranslationKeys.CHOICE_E_WEEK_DAY, this.thesaurus),
                        new ObisCodeFieldValue(6, ObisCodeTranslationKeys.CHOICE_E_TIME_OF_LAST_RESET, this.thesaurus),
                        new ObisCodeFieldValue(7, ObisCodeTranslationKeys.CHOICE_E_DATE_OF_LAST_RESET, this.thesaurus),
                        new ObisCodeFieldValue(8, ObisCodeTranslationKeys.CHOICE_E_OUTPUT_PULSE_DURATION, this.thesaurus),
                        new ObisCodeFieldValue(9, ObisCodeTranslationKeys.CHOICE_E_CLOCK_SYNC_WINDOW, this.thesaurus),
                        new ObisCodeFieldValue(10, ObisCodeTranslationKeys.CHOICE_E_CLOCK_SYNC_METHOD, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doCoefficients() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_E_TRANSFORMER_MAGNETIC_LOSSES, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_E_TRANSFORMER_THERMA_LOSSES, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_E_LINE_RESISTANCE_LOSSES, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(3, ObisCodeTranslationKeys.CHOICE_E_LINE_REACTANCE_LOSSES, this.thesaurus));
        doInBillingPeriodChoices(choicesE);
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doMeasurementMethods() {
        choicesE = new ArrayList<>();
        choicesE.add(new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_E_ALGORITHM_ACTIVE_POWER_MANAGEMENT, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_E_ALGORITHM_ACTIVE_ENERGY_MANAGEMENT, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(3, ObisCodeTranslationKeys.CHOICE_E_ALGORITHM_REACTIVE_POWER_MANAGEMENT, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(4, ObisCodeTranslationKeys.CHOICE_E_ALGORITHM_REACTIVE_ENERGY_MANAGEMENT, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(5, ObisCodeTranslationKeys.CHOICE_E_ALGORITHM_APPARENT_POWER_MANAGEMENT, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(6, ObisCodeTranslationKeys.CHOICE_E_ALGORITHM_APPARENT_ENERGY_MANAGEMENT, this.thesaurus));
        choicesE.add(new ObisCodeFieldValue(7, ObisCodeTranslationKeys.CHOICE_E_ALGORITHM_POWER_FACTOR_CALCULATION, this.thesaurus));
        descriptionBuilder.append(findString(choicesE, code.getE()));
    }

    private void doTimePeriodChoices() {
        choicesD = new ArrayList<>();
        choicesD.add(new ObisCodeFieldValue(0, ObisCodeTranslationKeys.CHOICE_D_BILLING_PERIOD_AVG, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(1, ObisCodeTranslationKeys.CHOICE_D_CUMULATIVE_MIN_USING_MEASUREMENT_PERIOD1, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(2, ObisCodeTranslationKeys.CHOICE_D_CUMULATIVE_MAX_USING_MEASUREMENT_PERIOD1, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(3, ObisCodeTranslationKeys.CHOICE_D_MIN_USING_MEASUREMENT_PERIOD1, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(4, ObisCodeTranslationKeys.CHOICE_D_CURRENT_AVG_USING_MEASUREMENT_PERIOD1, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(5, ObisCodeTranslationKeys.CHOICE_D_LAST_AVG_USING_MEASUREMENT_PERIOD1, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(6, ObisCodeTranslationKeys.CHOICE_D_MAX_USING_MEASUREMENT_PERIOD1, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(7, ObisCodeTranslationKeys.CHOICE_D_INSTANTANEOUS_VALUE, this.thesaurus));
        doTimeIntegralChoices();
        choicesD.add(new ObisCodeFieldValue(11, ObisCodeTranslationKeys.CHOICE_D_CUMULATIVE_MIN_USING_MEASUREMENT_PERIOD2, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(12, ObisCodeTranslationKeys.CHOICE_D_CUMULATIVE_MAX_USING_MEASUREMENT_PERIOD2, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(13, ObisCodeTranslationKeys.CHOICE_D_MIN_USING_MEASUREMENT_PERIOD2, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(14, ObisCodeTranslationKeys.CHOICE_D_CURRENT_AVG_USING_MEASUREMENT_PERIOD2, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(15, ObisCodeTranslationKeys.CHOICE_D_LAST_AVG_USING_MEASUREMENT_PERIOD2, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(16, ObisCodeTranslationKeys.CHOICE_D_MAX_USING_MEASUREMENT_PERIOD2, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(21, ObisCodeTranslationKeys.CHOICE_D_CUMULATIVE_MIN_USING_MEASUREMENT_PERIOD3, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(22, ObisCodeTranslationKeys.CHOICE_D_CUMULATIVE_MAX_USING_MEASUREMENT_PERIOD3, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(23, ObisCodeTranslationKeys.CHOICE_D_MIN_USING_MEASUREMENT_PERIOD3, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(24, ObisCodeTranslationKeys.CHOICE_D_CURRENT_AVG_USING_MEASUREMENT_PERIOD3, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(25, ObisCodeTranslationKeys.CHOICE_D_LAST_AVG_USING_MEASUREMENT_PERIOD3, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(26, ObisCodeTranslationKeys.CHOICE_D_MAX_USING_MEASUREMENT_PERIOD3, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(27, ObisCodeTranslationKeys.CHOICE_D_CURRENT_AVG_USING_RECORDING_INTERVAL1, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(28, ObisCodeTranslationKeys.CHOICE_D_CURRENT_AVG_USING_RECORDING_INTERVAL2, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(31, ObisCodeTranslationKeys.CHOICE_D_UNDER_LIMIT_THRESHOLD, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(32, ObisCodeTranslationKeys.CHOICE_D_UNDER_LIMIT_OCCURRENCE_COUNTER, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(33, ObisCodeTranslationKeys.CHOICE_D_UNDER_LIMIT_DURATION, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(34, ObisCodeTranslationKeys.CHOICE_D_UNDER_LIMIT_MAGNITUDE, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(35, ObisCodeTranslationKeys.CHOICE_D_OVER_LIMIT_THRESHOLD, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(36, ObisCodeTranslationKeys.CHOICE_D_OVER_LIMIT_OCCURRENCE_COUNTER, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(37, ObisCodeTranslationKeys.CHOICE_D_OVER_LIMIT_DURATION, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(38, ObisCodeTranslationKeys.CHOICE_D_OVER_LIMIT_MAGNITUDE, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(39, ObisCodeTranslationKeys.CHOICE_D_MISSING_THRESHOLD, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(40, ObisCodeTranslationKeys.CHOICE_D_MISSING_OCCURRENCE_COUNTER, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(41, ObisCodeTranslationKeys.CHOICE_D_MISSING_DURATION, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(42, ObisCodeTranslationKeys.CHOICE_D_MISSING_MAGNITUDE, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(55, ObisCodeTranslationKeys.CHOICE_D_TEST_AVG, this.thesaurus));
        doInBillingPeriodChoices(choicesD);
    }

    private void doTimePeriod() {
        doTimePeriodChoices();
        descriptionBuilder.append(findString(choicesD, code.getD()));
    }

    private void doTimeIntegralChoices() {
        StringBuilder builder = new StringBuilder();
        if (code.hasBillingPeriod()) {
            builder.append(this.thesaurus.getFormat(ObisCodeTranslationKeys.CHOICE_D_TIME_INTEGRAL_TO_BILLING_POINT_X).format(code.getF()));
        } else {
            builder.append(this.thesaurus.getFormat(ObisCodeTranslationKeys.CHOICE_D_TIME_INTEGRAL_TO_NOW).format());
        }
        choicesD.add(new ObisCodeFieldValue(8, builder.toString(), thesaurus));

        builder = new StringBuilder();
        if (code.hasBillingPeriod()) {
            builder.append(this.thesaurus.getFormat(ObisCodeTranslationKeys.CHOICE_D_TIME_INTEGRAL_OVER_BILLING_PERIOD_X).format(code.getF()));
        } else {
            builder.append(this.thesaurus.getFormat(ObisCodeTranslationKeys.CHOICE_D_TIME_INTEGRAL_FROM_LAST_BILLING_POINT_TO_NOW).format());
        }
        choicesD.add(new ObisCodeFieldValue(9, builder.toString(), thesaurus));
        choicesD.add(new ObisCodeFieldValue(10, ObisCodeTranslationKeys.CHOICE_D_TIME_INTEGRAL_POSITIVE_DIFFERENCE, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(58, ObisCodeTranslationKeys.CHOICE_D_TIME_INTEGRAL_OVER_DEVICE_SPECIFIC_TIME, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(29, ObisCodeTranslationKeys.CHOICE_D_TIME_INTEGRAL_TO_NOW_USING_RECORDING_PERIOD1, this.thesaurus));
        choicesD.add(new ObisCodeFieldValue(30, ObisCodeTranslationKeys.CHOICE_D_TIME_INTEGRAL_TO_NOW_USING_RECORDING_PERIOD2, this.thesaurus));
    }

    private void doHeatCostAllocatorRelated() {
        doDefault();
    }

    private void doCoolingRelated() {
        doDefault();
    }

    private void doHeatRelated() {
        doDefault();
    }

    private void doGasRelated() {
        doDefault();
    }

    private void doColdWaterRelated() {
        doDefault();
    }

    private void doHotWaterRelated() {
        doDefault();
    }

    private void space(StringBuilder builder) {
        builder.append(" ");
    }

    private void doInBillingPeriodChoices(List<ObisCodeFieldValue> list) {
        for (ObisCodeFieldValue ocf : list) {
            if (code.hasBillingPeriod()) {
                if (code.isCurrentBillingPeriod()) {
                    ocf.addSpaceDescription();
                    ocf.add2Description(ObisCodeTranslationKeys.IN_CURRENT_ACTIVE_BILLING_PERIOD);
                }
                else {
                    ocf.addSpaceDescription();
                    ocf.add2Description(ObisCodeTranslationKeys.IN_BILLING_PERIOD_X, code.getF());
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

}
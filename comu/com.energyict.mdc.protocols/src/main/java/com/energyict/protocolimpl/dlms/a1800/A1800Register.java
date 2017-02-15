/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.a1800;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.UniversalObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * All readable Registers of the A1800
 * <p/>
 * Created by heuckeg on 03.07.2014.
 */
public enum A1800Register {
    // Current billing objects
    kW_Del_cumulative_current_billing_period("1.1.1.2.0.255", 3),
    kW_Del_cumulative_rate_1_current_billing_period("1.1.1.2.1.255", 3),
    kW_Del_cumulative_rate_2_current_billing_period("1.1.1.2.2.255", 3),
    kW_Del_cumulative_rate_3_current_billing_period("1.1.1.2.3.255", 3),
    kW_Del_cumulative_rate_4_current_billing_period("1.1.1.2.4.255", 3),
    kW_Rec_cumulative_current_billing_period("1.1.2.2.0.255", 3),
    kW_Rec_cumulative_rate_1_current_billing_period("1.1.2.2.1.255", 3),
    kW_Rec_cumulative_rate_2_current_billing_period("1.1.2.2.2.255", 3),
    kW_Rec_cumulative_rate_3_current_billing_period("1.1.2.2.3.255", 3),
    kW_Rec_cumulative_rate_4_current_billing_period("1.1.2.2.4.255", 3),
    kvar_Del_cumulative_current_billing_period("1.1.3.2.0.255", 3),
    kvar_Del_cumulative_rate_1_current_billing_period("1.1.3.2.1.255", 3),
    kvar_Del_cumulative_rate_2_current_billing_period("1.1.3.2.2.255", 3),
    kvar_Del_cumulative_rate_3_current_billing_period("1.1.3.2.3.255", 3),
    kvar_Del_cumulative_rate_4_current_billing_period("1.1.3.2.4.255", 3),
    kvar_Rec_cumulative_current_billing_period("1.1.4.2.0.255", 3),
    kvar_Rec_cumulative_rate_1_current_billing_period("1.1.4.2.1.255", 3),
    kvar_Rec_cumulative_rate_2_current_billing_period("1.1.4.2.2.255", 3),
    kvar_Rec_cumulative_rate_3_current_billing_period("1.1.4.2.3.255", 3),
    kvar_Rec_cumulative_rate_4_current_billing_period("1.1.4.2.4.255", 3),
    kvar_Q1_cumulative_current_billing_period("1.1.5.2.0.255", 3),
    kvar_Q1_cumulative_rate_1_current_billing_period("1.1.5.2.1.255", 3),
    kvar_Q1_cumulative_rate_2_current_billing_period("1.1.5.2.2.255", 3),
    kvar_Q1_cumulative_rate_3_current_billing_period("1.1.5.2.3.255", 3),
    kvar_Q1_cumulative_rate_4_current_billing_period("1.1.5.2.4.255", 3),
    kvar_Q2_cumulative_current_billing_period("1.1.6.2.0.255", 3),
    kvar_Q2_cumulative_rate_1_current_billing_period("1.1.6.2.1.255", 3),
    kvar_Q2_cumulative_rate_2_current_billing_period("1.1.6.2.2.255", 3),
    kvar_Q2_cumulative_rate_3_current_billing_period("1.1.6.2.3.255", 3),
    kvar_Q2_cumulative_rate_4_current_billing_period("1.1.6.2.4.255", 3),
    kvar_Q3_cumulative_current_billing_period("1.1.7.2.0.255", 3),
    kvar_Q3_cumulative_rate_1_current_billing_period("1.1.7.2.1.255", 3),
    kvar_Q3_cumulative_rate_2_current_billing_period("1.1.7.2.2.255", 3),
    kvar_Q3_cumulative_rate_3_current_billing_period("1.1.7.2.3.255", 3),
    kvar_Q3_cumulative_rate_4_current_billing_period("1.1.7.2.4.255", 3),
    kvar_Q4_cumulative_current_billing_period("1.1.8.2.0.255", 3),
    kvar_Q4_cumulative_rate_1_current_billing_period("1.1.8.2.1.255", 3),
    kvar_Q4_cumulative_rate_2_current_billing_period("1.1.8.2.2.255", 3),
    kvar_Q4_cumulative_rate_3_current_billing_period("1.1.8.2.3.255", 3),
    kvar_Q4_cumulative_rate_4_current_billing_period("1.1.8.2.4.255", 3),
    kW_Del_max_current_billing_period("1.1.1.6.0.255", 4),
    kW_Del_max_rate_1_current_billing_period("1.1.1.6.1.255", 4),
    kW_Del_max_rate_2_current_billing_period("1.1.1.6.2.255", 4),
    kW_Del_max_rate_3_current_billing_period("1.1.1.6.3.255", 4),
    kW_Del_max_rate_4_current_billing_period("1.1.1.6.4.255", 4),
    kW_Rec_max_current_billing_period("1.1.2.6.0.255", 4),
    kW_Rec_max_rate_1_current_billing_period("1.1.2.6.1.255", 4),
    kW_Rec_max_rate_2_current_billing_period("1.1.2.6.2.255", 4),
    kW_Rec_max_rate_3_current_billing_period("1.1.2.6.3.255", 4),
    kW_Rec_max_rate_4_current_billing_period("1.1.2.6.4.255", 4),
    kvar_Del_max_current_billing_period("1.1.3.6.0.255", 4),
    kvar_Del_max_rate_1_current_billing_period("1.1.3.6.1.255", 4),
    kvar_Del_max_rate_2_current_billing_period("1.1.3.6.2.255", 4),
    kvar_Del_max_rate_3_current_billing_period("1.1.3.6.3.255", 4),
    kvar_Del_max_rate_4_current_billing_period("1.1.3.6.4.255", 4),
    kvar_Rec_max_current_billing_period("1.1.4.6.0.255", 4),
    kvar_Rec_max_rate_1_current_billing_period("1.1.4.6.1.255", 4),
    kvar_Rec_max_rate_2_current_billing_period("1.1.4.6.2.255", 4),
    kvar_Rec_max_rate_3_current_billing_period("1.1.4.6.3.255", 4),
    kvar_Rec_max_rate_4_current_billing_period("1.1.4.6.4.255", 4),
    kvar_Q1_max_current_billing_period("1.1.5.6.0.255", 4),
    kvar_Q1_max_rate_1_current_billing_period("1.1.5.6.1.255", 4),
    kvar_Q1_max_rate_2_current_billing_period("1.1.5.6.2.255", 4),
    kvar_Q1_max_rate_3_current_billing_period("1.1.5.6.3.255", 4),
    kvar_Q1_max_rate_4_current_billing_period("1.1.5.6.4.255", 4),
    kvar_Q2_max_current_billing_period("1.1.6.6.0.255", 4),
    kvar_Q2_max_rate_1_current_billing_period("1.1.6.6.1.255", 4),
    kvar_Q2_max_rate_2_current_billing_period("1.1.6.6.2.255", 4),
    kvar_Q2_max_rate_3_current_billing_period("1.1.6.6.3.255", 4),
    kvar_Q2_max_rate_4_current_billing_period("1.1.6.6.4.255", 4),
    kvar_Q3_max_current_billing_period("1.1.7.6.0.255", 4),
    kvar_Q3_max_rate_1_current_billing_period("1.1.7.6.1.255", 4),
    kvar_Q3_max_rate_2_current_billing_period("1.1.7.6.2.255", 4),
    kvar_Q3_max_rate_3_current_billing_period("1.1.7.6.3.255", 4),
    kvar_Q3_max_rate_4_current_billing_period("1.1.7.6.4.255", 4),
    kvar_Q4_max_current_billing_period("1.1.8.6.0.255", 4),
    kvar_Q4_max_rate_1_current_billing_period("1.1.8.6.1.255", 4),
    kvar_Q4_max_rate_2_current_billing_period("1.1.8.6.2.255", 4),
    kvar_Q4_max_rate_3_current_billing_period("1.1.8.6.3.255", 4),
    kvar_Q4_max_rate_4_current_billing_period("1.1.8.6.4.255", 4),

    // Load profile in pulses
    kW_Del_total_current_billing_period("1.1.1.8.0.255", 3),
    kW_Del_rate_1_current_billing_period("1.1.1.8.1.255", 3),
    kW_Del_rate_2_current_billing_period("1.1.1.8.2.255", 3),
    kW_Del_rate_3_current_billing_period("1.1.1.8.3.255", 3),
    kW_Del_rate_4_current_billing_period("1.1.1.8.4.255", 3),

    // Load profile in engineering units
    kW_Rec_total_current_billing_period("1.1.2.8.0.255", 3),
    kW_Rec_rate_1_current_billing_period("1.1.2.8.1.255", 3),
    kW_Rec_rate_2_current_billing_period("1.1.2.8.2.255", 3),
    kW_Rec_rate_3_current_billing_period("1.1.2.8.3.255", 3),
    kW_Rec_rate_4_current_billing_period("1.1.2.8.4.255", 3),
    kvar_Del_total_current_billing_period("1.1.3.8.0.255", 3),
    kvar_Del_rate_1_current_billing_period("1.1.3.8.1.255", 3),
    kvar_Del_rate_2_current_billing_period("1.1.3.8.2.255", 3),
    kvar_Del_rate_3_current_billing_period("1.1.3.8.3.255", 3),
    kvar_Del_rate_4_current_billing_period("1.1.3.8.4.255", 3),
    kvar_Rec_total_current_billing_period("1.1.4.8.0.255", 3),
    kvar_Rec_rate_1_current_billing_period("1.1.4.8.1.255", 3),
    kvar_Rec_rate_2_current_billing_period("1.1.4.8.2.255", 3),
    kvar_Rec_rate_3_current_billing_period("1.1.4.8.3.255", 3),
    kvar_Rec_rate_4_current_billing_period("1.1.4.8.4.255", 3),
    kvar_Q1_total_current_billing_period("1.1.5.8.0.255", 3),
    kvar_Q1_rate_1_current_billing_period("1.1.5.8.1.255", 3),
    kvar_Q1_rate_2_current_billing_period("1.1.5.8.2.255", 3),
    kvar_Q1_rate_3_current_billing_period("1.1.5.8.3.255", 3),
    kvar_Q1_rate_4_current_billing_period("1.1.5.8.4.255", 3),
    kvar_Q2_total_current_billing_period("1.1.6.8.0.255", 3),
    kvar_Q2_rate_1_current_billing_period("1.1.6.8.1.255", 3),
    kvar_Q2_rate_2_current_billing_period("1.1.6.8.2.255", 3),
    kvar_Q2_rate_3_current_billing_period("1.1.6.8.3.255", 3),
    kvar_Q2_rate_4_current_billing_period("1.1.6.8.4.255", 3),
    kvar_Q3_total_current_billing_period("1.1.7.8.0.255", 3),
    kvar_Q3_rate_1_current_billing_period("1.1.7.8.1.255", 3),
    kvar_Q3_rate_2_current_billing_period("1.1.7.8.2.255", 3),
    kvar_Q3_rate_3_current_billing_period("1.1.7.8.3.255", 3),
    kvar_Q3_rate_4_current_billing_period("1.1.7.8.4.255", 3),
    kvar_Q4_total_current_billing_period("1.1.8.8.0.255", 3),
    kvar_Q4_rate_1_current_billing_period("1.1.8.8.1.255", 3),
    kvar_Q4_rate_2_current_billing_period("1.1.8.8.2.255", 3),
    kvar_Q4_rate_3_current_billing_period("1.1.8.8.3.255", 3),
    kvar_Q4_rate_4_current_billing_period("1.1.8.8.4.255", 3),

    // Instrumentation profile
    kW_System_active_power_p("1.1.1.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    kW_System_active_power_m("1.1.2.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVAR_System_reactive_power_p("1.1.3.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVAR_System_reactive_power_m("1.1.4.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVA_System_apparent_power_vectorial_p("1.1.9.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVA_System_apparent_power_vectorial_m("1.1.10.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    Power_factor_System("1.1.13.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Line_frequency("1.1.14.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    kW_Line_1_active_power_p("1.1.21.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    kW_Line_1_active_power_m("1.1.22.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVAR_Line_1_reactive_power_p("1.1.23.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVAR_Line_1_reactive_power_m("1.1.24.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVA_Line_1_apparent_power_p("1.1.29.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVA_Line_1_apparent_power_m("1.1.30.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    Current_Line_1("1.1.31.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _1st_harmonic_current_Line_1("1.1.31.7.1.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _2nd_harmonic_current_Line_1("1.1.31.7.2.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _3rd_harmonic_current_Line_1("1.1.31.7.3.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _4th_harmonic_current_Line_1("1.1.31.7.4.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _5th_harmonic_current_Line_1("1.1.31.7.5.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _6th_harmonic_current_Line_1("1.1.31.7.6.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _7th_harmonic_current_Line_1("1.1.31.7.7.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _8th_harmonic_current_Line_1("1.1.31.7.8.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _9th_harmonic_current_Line_1("1.1.31.7.9.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _10th_harmonic_current_Line_1("1.1.31.7.10.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _11th_harmonic_current_Line_1("1.1.31.7.11.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _12th_harmonic_current_Line_1("1.1.31.7.12.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _13th_harmonic_current_Line_1("1.1.31.7.13.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _14th_harmonic_current_Line_1("1.1.31.7.14.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _15th_harmonic_current_Line_1("1.1.31.7.15.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    Current_THD_Line_1("1.1.31.7.124.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Total_demand_distortion_Line_1("1.1.31.7.125.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Voltage_Line_1("1.1.32.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _1st_harmonic_voltage_Line_1("1.1.32.7.1.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _2nd_harmonic_voltage_Line_1("1.1.32.7.2.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _3rd_harmonic_voltage_Line_1("1.1.32.7.3.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _4th_harmonic_voltage_Line_1("1.1.32.7.4.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _5th_harmonic_voltage_Line_1("1.1.32.7.5.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _6th_harmonic_voltage_Line_1("1.1.32.7.6.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _7th_harmonic_voltage_Line_1("1.1.32.7.7.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _8th_harmonic_voltage_Line_1("1.1.32.7.8.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _9th_harmonic_voltage_Line_1("1.1.32.7.9.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _10th_harmonic_voltage_Line_1("1.1.32.7.10.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _11th_harmonic_voltage_Line_1("1.1.32.7.11.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _12th_harmonic_voltage_Line_1("1.1.32.7.12.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _13th_harmonic_voltage_Line_1("1.1.32.7.13.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _14th_harmonic_voltage_Line_1("1.1.32.7.14.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _15th_harmonic_voltage_Line_1("1.1.32.7.15.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    Voltage_THD_Line_1("1.1.32.7.124.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Power_factor_Line_1("1.1.33.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    kW_Line_2_active_power_p("1.1.41.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    kW_Line_2_active_power_m("1.1.42.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVAR_Line_2_reactive_power_p("1.1.43.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVAR_Line_2_reactive_power_m("1.1.44.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVA_Line_2_apparent_power_p("1.1.49.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVA_Line_2_apparent_power_m("1.1.50.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    Current_Line_2("1.1.51.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _1st_harmonic_current_Line_2("1.1.51.7.1.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _2nd_harmonic_current_Line_2("1.1.51.7.2.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _3rd_harmonic_current_Line_2("1.1.51.7.3.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _4th_harmonic_current_Line_2("1.1.51.7.4.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _5th_harmonic_current_Line_2("1.1.51.7.5.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _6th_harmonic_current_Line_2("1.1.51.7.6.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _7th_harmonic_current_Line_2("1.1.51.7.7.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _8th_harmonic_current_Line_2("1.1.51.7.8.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _9th_harmonic_current_Line_2("1.1.51.7.9.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _10th_harmonic_current_Line_2("1.1.51.7.10.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _11th_harmonic_current_Line_2("1.1.51.7.11.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _12th_harmonic_current_Line_2("1.1.51.7.12.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _13th_harmonic_current_Line_2("1.1.51.7.13.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _14th_harmonic_current_Line_2("1.1.51.7.14.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _15th_harmonic_current_Line_2("1.1.51.7.15.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    Current_THD_Line_2("1.1.51.7.124.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Total_demand_distortion_Line_2("1.1.51.7.125.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Voltage_Line_2("1.1.52.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _1st_harmonic_voltage_Line_2("1.1.52.7.1.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    _2nd_harmonic_voltage_Line_2("1.1.52.7.2.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _3rd_harmonic_voltage_Line_2("1.1.52.7.3.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _4th_harmonic_voltage_Line_2("1.1.52.7.4.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _5th_harmonic_voltage_Line_2("1.1.52.7.5.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _6th_harmonic_voltage_Line_2("1.1.52.7.6.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _7th_harmonic_voltage_Line_2("1.1.52.7.7.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _8th_harmonic_voltage_Line_2("1.1.52.7.8.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _9th_harmonic_voltage_Line_2("1.1.52.7.9.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _10th_harmonic_voltage_Line_2("1.1.52.7.10.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _11th_harmonic_voltage_Line_2("1.1.52.7.11.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _12th_harmonic_voltage_Line_2("1.1.52.7.12.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _13th_harmonic_voltage_Line_2("1.1.52.7.13.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _14th_harmonic_voltage_Line_2("1.1.52.7.14.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _15th_harmonic_voltage_Line_2("1.1.52.7.15.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    Voltage_THD_Line_2("1.1.52.7.124.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Power_factor_Line_2("1.1.53.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    kW_Line_3_active_power_p("1.1.61.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    kW_Line_3_active_power_m("1.1.62.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVAR_Line_3_reactive_power_p("1.1.63.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVAR_Line_3_reactive_power_m("1.1.64.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVA_Line_3_apparent_power_p("1.1.69.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVA_Line_3_apparent_power_m("1.1.70.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    Current_Line_3("1.1.71.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _1st_harmonic_current_Line_3("1.1.71.7.1.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    _2nd_harmonic_current_Line_3("1.1.71.7.2.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _3rd_harmonic_current_Line_3("1.1.71.7.3.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _4th_harmonic_current_Line_3("1.1.71.7.4.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _5th_harmonic_current_Line_3("1.1.71.7.5.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _6th_harmonic_current_Line_3("1.1.71.7.6.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _7th_harmonic_current_Line_3("1.1.71.7.7.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _8th_harmonic_current_Line_3("1.1.71.7.8.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _9th_harmonic_current_Line_3("1.1.71.7.9.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _10th_harmonic_current_Line_3("1.1.71.7.10.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _11th_harmonic_current_Line_3("1.1.71.7.11.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _12th_harmonic_current_Line_3("1.1.71.7.12.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _13th_harmonic_current_Line_3("1.1.71.7.13.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _14th_harmonic_current_Line_3("1.1.71.7.14.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    _15th_harmonic_current_Line_3("1.1.71.7.15.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS),
    Current_THD_Line_3("1.1.71.7.124.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Total_demand_distortion_Line_3("1.1.71.7.125.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Voltage_Line_3("1.1.72.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _1st_harmonic_voltage_Line_3("1.1.72.7.1.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _2nd_harmonic_voltage_Line_3("1.1.72.7.2.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _3rd_harmonic_voltage_Line_3("1.1.72.7.3.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _4th_harmonic_voltage_Line_3("1.1.72.7.4.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _5th_harmonic_voltage_Line_3("1.1.72.7.5.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _6th_harmonic_voltage_Line_3("1.1.72.7.6.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _7th_harmonic_voltage_Line_3("1.1.72.7.7.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _8th_harmonic_voltage_Line_3("1.1.72.7.8.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _9th_harmonic_voltage_Line_3("1.1.72.7.9.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _10th_harmonic_voltage_Line_3("1.1.72.7.10.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _11th_harmonic_voltage_Line_3("1.1.72.7.11.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _12th_harmonic_voltage_Line_3("1.1.72.7.12.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _13th_harmonic_voltage_Line_3("1.1.72.7.13.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _14th_harmonic_voltage_Line_3("1.1.72.7.14.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    _15th_harmonic_voltage_Line_3("1.1.72.7.15.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.VT_RATIO_OBIS),
    Voltage_THD_Line_3("1.1.72.7.124.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Power_factor_Line_3("1.1.73.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Voltage_phase_angle_Line_1_Va_to_Va("1.1.81.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Power_factor_angle_Line_1("1.1.81.7.4.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Voltage_phase_angle_Line_2_Vb_to_Va("1.1.81.7.10.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Power_factor_angle_Line_2("1.1.81.7.15.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Voltage_phase_angle_Line_3_Vc_to_Va("1.1.81.7.20.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Power_factor_angle_Line_3("1.1.81.7.26.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Current_phase_angle_Line_1_Ia_to_Va("1.1.81.7.40.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Current_phase_angle_Line_2_Ib_to_Va("1.1.81.7.50.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    Current_phase_angle_Line_3_Ic_to_Va("1.1.81.7.60.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    KVA_System_apparent_power_arithmetic_p("1.1.140.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    Power_factor_system_arithmetic("1.1.143.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),
    KVAR_vectorial_system_reactive_power_p("1.1.144.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    KVAR_signed_vectorial_system_reactive_power("1.1.146.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS, Constants.CT_RATIO_OBIS, Constants.VT_RATIO_OBIS),
    Vectorial_power_factor_angle_System("1.1.147.7.0.255", 3, Constants.INSTRUMENTATION_MULTIPLIER_OBIS),

    //Other objects
    Block_demand_interval("1.1.0.8.0.255", 3),
    Sliding_demand_interval("1.1.0.8.1.255", 3),
    Detected_connection_diagram("1.1.0.2.4.255", 1),
    CT_ratio(Constants.CT_RATIO_OBIS, 1),
    VT_ratio(Constants.VT_RATIO_OBIS, 1),
    Factory_serial_number("1.0.96.1.0.255", 1),
    Meter_ID("1.0.96.1.1.255", 1),
    Utility_serial_number("1.0.96.1.2.255", 1),
    Account("1.0.96.1.3.255", 1),
    Program_ID("1.0.96.1.4.255", 1),
    Firmware_version("1.0.96.1.5.255", 1),

    DisplayedErrorByte1("0.0.97.97.0.255", 1),
    DisplayedErrorByte2("0.0.97.97.1.255", 1),
    DisplayedErrorByte3("0.0.97.97.2.255", 1),
    DisplayedWarningByte1("0.0.97.97.3.255", 1),
    DisplayedWarningByte2("0.0.97.97.4.255", 1),
    DisplayedWarningByte3("0.0.97.97.5.255", 1),

    InstrumentationMultiplier(Constants.INSTRUMENTATION_MULTIPLIER_OBIS, 1),
    InstrumentationFactor(Constants.INSTRUMENTATION_SCALE_FACTOR_OBIS, 1),
    NonInstrumentationMultiplier(Constants.NON_INSTRUMENTATION_MULTIPLIER_OBIS, 1),
    NonInstrumentationFactor(Constants.NON_INSTRUMENTATION_FACTOR_OBIS, 1);

    private final UniversalObject universalObject;
    private final String multiplierObisCode;
    private final List<String> transformerObisCodes;

    private A1800Register(String obisCode, int classId) {
        this.multiplierObisCode = null;
        this.transformerObisCodes = new ArrayList<String>(0);
        this.universalObject = new UniversalObject(ObisCode.fromString(obisCode).getLN(), classId, -1);
    }

    private A1800Register(String obisCode, int classId, String multiplierObisCode) {
        this.multiplierObisCode = multiplierObisCode;
        this.transformerObisCodes = new ArrayList<String>(0);
        this.universalObject = new UniversalObject(ObisCode.fromString(obisCode).getLN(), classId, -1);
    }

    A1800Register(String obisCode, int classId, String multiplierObisCode, String... transformerObisCodes) {
        this.multiplierObisCode = multiplierObisCode;
        this.transformerObisCodes = Arrays.asList(transformerObisCodes);
        this.universalObject = new UniversalObject(ObisCode.fromString(obisCode).getLN(), classId, -1);
    }

    public ObisCode getObisCode() {
        return universalObject.getObisCode();
    }

    public String getMultiplierObisCode() {
        return multiplierObisCode;
    }

    public List<String> getTransformerObisCodes() {
        return transformerObisCodes;
    }

    @SuppressWarnings("unused")
    public UniversalObject getUniversalObject() {
        return universalObject;
    }

    public int getClassId() {
        return universalObject.getClassID();
    }

    static A1800Register find(ObisCode obisCode) {
        for (A1800Register reg : A1800Register.values()) {
            if (reg.getObisCode().equals(obisCode)) {
                return reg;
            }
        }
        return null;
    }

    public static class Constants {
        public static final String CT_RATIO_OBIS = "1.1.0.4.2.255";
        public static final String VT_RATIO_OBIS = "1.1.0.4.3.255";
        public static final String INSTRUMENTATION_MULTIPLIER_OBIS = "1.1.96.131.1.255";
        public static final String INSTRUMENTATION_SCALE_FACTOR_OBIS = "1.1.96.131.2.255";
        private static final String NON_INSTRUMENTATION_MULTIPLIER_OBIS = "1.1.96.132.1.255";
        private static final String NON_INSTRUMENTATION_FACTOR_OBIS = "1.1.96.132.2.255";
    }
}
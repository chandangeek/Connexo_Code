package com.elster.protocolimpl.lis200.registers;

/**
 * User: heuckeg
 * Date: 05.04.11
 * Time: 15:04
 */
@SuppressWarnings({"unused"})
public enum Lis200ObisCode {

    MOMENTARY_STATUS_TOTAL("7.0.96.5.0.255", "Momentary status, total"),
    REMAINING_BATTERY_LIFE("0.0.96.6.6.255", "Remaining battery service life"),
    GSM_RECEPTION_LEVEL("0.0.96.12.5.255", "GSM reception level"),
    SOFTWARE_VERSION("7.0.0.2.2.255", "Software version"),
    SERIAL_NUMBER_METER("7.0.0.2.14.255", "Serial number of meter"),
    SERIAL_NUMBER_PRESSURE_SENS("7.0.0.2.11.255", "Serial number of pressure sensor"),
    SERIAL_NUMBER_TEMP_SENS("7.0.0.2.12.255", "Serial number of temperature sensor"),
    MAIN_COUNTER_CURR("7.0.23.2.0.255", "Main counter"),
    ADJUSTABLE_COUNTER_CURR("7.128.23.2.0.255", "Adjustable counter"),
    HIGH_TARIFF_COUNTER_CURR("7.0.23.2.1.255", "Main/high tariff counter"),
    LOW_TARIFF_COUNTER_CURR("7.0.23.2.2.255", "Low tariff counter"),
    VOLUME_MEAS_COND_CORR_CURR("7.0.11.0.0.255", "Volume at measurement conditions"),
    VOLUME_BASE_COND_CORR_CURR("7.0.11.2.0.255", "Volume at base conditions"),
    VOLUME_MEAS_COND_TOTAL_CURR("7.0.13.0.0.255", "Volume at measurement conditions total"),
    VOLUME_BASE_COND_TOTAL_CURR("7.0.13.2.0.255", "Volume at base conditions total"),
    VOLUME_MEAS_COND_ORG_CURR("7.1.13.0.0.255", "Original volume"),
    ENERGY_TOTAL_CURR("7.0.33.2.0.255", "Energy total"),
    PRESSURE_CURR("7.0.42.0.0.255", "Current pressure"),
    TEMPERATURE_CURR("7.0.41.0.0.255", "Current temperature"),
    CONVERSION_FACTOR_CURR("7.0.52.0.0.255", "Conversion factor"),
    COMPRESSIBILITY_CURR("7.0.53.0.0.255", "Compressibility, Z"),

    FLOW_RATE("7.0.43.0.255.255", "Flow rate"),
    FLOW_RATE_MEAS_COND_CURR("7.0.43.0.255.255", "flow rate at measurement conditions"),
    FLOW_RATE_BASE_COND_CURR("7.0.43.2.255.255", "flow rate at base conditions"),

    MAX_INTERVAL_VALUE("7.0.23.56.0.101", "Maximum measurement period counter - last month"),
    MAX_DAY_VALUE("7.0.23.62.0.101", "Maximum daily counter - last month"),

    MAX_INT_VAL_MEAS_COND("7.0.13.54.0.101", "Maximum measurement period Vm - last month"),
    MAX_INT_VAL_BASE_COND("7.0.13.56.0.101", "Maximum measurement period Vb - last month"),
    MAX_DAY_VAL_MEAS_COND("7.0.13.60.0.101", "Maximum daily Vm - last month"),
    MAX_DAY_VAL_BASE_COND("7.0.13.62.0.101", "Maximum daily Vb - last month"),

    /* all historic counter codes follow the scheme a.b.c.d.e.1 - 15 */
    MAIN_COUNTER_HIST("(7\\.0\\.23\\.2\\.0\\.)(([1-9])|([1][0-5]))","Main counter (historic)"),
    ADJUSTABLE_COUNTER_HIST("(7\\.128\\.23\\.2\\.0\\.)(([1-9])|([1][0-5]))","Adjustable counter (historic)"),
    MAX_INTERVAL_VALUE_HIST("(7\\.0\\.23\\.56\\.0\\.)(([1-9])|([1][0-5]))", "Maximum measurement period counter - last month"),
    MAX_DAY_VALUE_HIST("(7\\.0\\.23\\.62\\.0\\.)(([1-9])|([1][0-5]))", "Maximum daily counter - last month"),

    VOLUME_BASE_COND_TOTAL_HIST("(7\\.0\\.13\\.2\\.0\\.)(([1-9])|([1][0-5]))", "Volume at base conditions total (historic)"),
    VOLUME_BASE_COND_CORR_HIST("(7\\.0\\.11\\.2\\.0\\.)(([1-9])|([1][0-5]))", "Corrected volume at base conditions (historic)"),

    VOLUME_MEAS_COND_ORG_HIST("(7\\.1\\.13\\.0\\.0\\.)(([1-9])|([1][0-5]))", "Original volume at measurement conditions (historic)"),
    VOLUME_MEAS_COND_TOTAL_HIST("(7\\.0\\.13\\.0\\.0\\.)(([1-9])|([1][0-5]))", "Volume at measurement conditions total (historic)"),
    VOLUME_MEAS_COND_CORR_HIST("(7\\.0\\.11\\.0\\.0\\.)(([1-9])|([1][0-5]))", "Corrected volume at measurement conditions (historic)"),

    MAX_INT_VAL_MEAS_COND_HIST("(7\\.0\\.13\\.54\\.0\\.)(([1-9])|([1][0-5]))", "Maximum measurement period Vm (historic)"),
    MAX_DAY_VAL_MEAS_COND_HIST("(7\\.0\\.13\\.60\\.0\\.)(([1-9])|([1][0-5]))", "Maximum daily Vm (historic)"),
    MAX_INT_VAL_BASE_COND_HIST("(7\\.0\\.13\\.56\\.0\\.)(([1-9])|([1][0-5]))", "Maximum measurement period Vb (historic)"),
    MAX_DAY_VAL_BASE_COND_HIST("(7\\.0\\.11\\.62\\.0\\.)(([1-9])|([1][0-5]))", "Maximum daily Vb (historic)"),

    MAX_INT_FR_BASE_COND_HIST("(7\\.0\\.43\\.57\\.255\\.)(([1-9])|([1][0-5]))", "Maximum interval flow rate at base conditions (historic)"),
    MIN_INT_FR_BASE_COND_HIST("(7\\.128\\.43\\.22\\.255\\.)(([1-9])|([1][0-5]))", "Minumum interval flow rate as base conditions (historic)"),
    MAX_INT_FR_MEAS_COND_HIST("(7\\.0\\.43\\.55\\.255\\.)(([1-9])|([1][0-5]))", "Maximum interval flow rate as meas. conditions (historic)"),
    MIN_INT_FR_MEAS_COND_HIST("(7\\.128\\.43\\.20\\.255\\.)(([1-9])|([1][0-5]))", "Minimum interval flow rate as meas. conditions (historic)"),

    MEAN_PREASURE_HIST("(7\\.0\\.42\\.42\\.0\\.)(([1-9])|([1][0-5]))", "mean pressure (historic)"),
    MAX_INT_PREASURE_HIST("(7\\.0\\.42\\.57\\.0\\.)(([1-9])|([1][0-5]))", "Maximum pressure (historic)"),
    MIN_INT_PREASURE_HIST("(7\\.0\\.42\\.54\\.0\\.)(([1-9])|([1][0-5]))", "Minimum pressure (historic)"),

    MEAN_TEMPERATURE_HIST("(7\\.0\\.41\\.42\\.0\\.)(([1-9])|([1][0-5]))", "mean temperature (historic)"),
    MAX_INT_TEMPERATURE_HIST("(7\\.0\\.41\\.57\\.0\\.)(([1-9])|([1][0-5]))", "Maximum temperature (historic)"),
    MIN_INT_TEMPERATURE_HIST("(7\\.0\\.41\\.54\\.0\\.)(([1-9])|([1][0-5]))", "Minimum temperature (historic)"),

    MEAN_CONVERSION_FACTOR_HIST("(7\\.0\\.52\\.42\\.0\\.)(([1-9])|([1][0-5]))", "mean conversion factor (historic)"),
    MEAN_COMPRESS_FACTOR_HIST("(7\\.0\\.53\\.42\\.0\\.)(([1-9])|([1][0-5]))", "mean compressibility factor (historic)"),

    //EK280
    MOMENTARY_STATUS_TOTAL_2("7.129.96.5.1.255", "Momentary status, total"),
    SERIAL_NUMBER_PRESSURE_SENS_2("7.0.0.15.1.255", "Serial number of pressure sensor"),
    SERIAL_NUMBER_TEMP_SENS_2("7.0.0.15.2.255", "Serial number of temperature sensor"),
    GSM_RECEPTION_LEVEL_int("0.2.96.12.5.255", "GSM reception level"),
    GSM_RECEPTION_LEVEL_ext("0.3.96.12.5.255", "GSM reception level"),
    VOLUME_MEAS_COND_ORG_CURR_2("7.0.3.0.0.255", "Original volume"),

    MAX_INT_FR_BASE_COND_HIST_2("(7\\.0\\.43\\.206\\.20\\.)(([1-9])|([1][0-5]))", "Maximum interval flow rate at base conditions (historic)"),
    MIN_INT_FR_BASE_COND_HIST_2("(7\\.0\\.43\\.216\\.20\\.)(([1-9])|([1][0-5]))", "Minumum interval flow rate as base conditions (historic)"),
    MAX_INT_FR_MEAS_COND_HIST_2("(7\\.0\\.43\\.206\\.0\\.)(([1-9])|([1][0-5]))", "Maximum interval flow rate as meas. conditions (historic)"),
    MIN_INT_FR_MEAS_COND_HIST_2("(7\\.0\\.43\\.216\\.0\\.)(([1-9])|([1][0-5]))", "Minimum interval flow rate as meas. conditions (historic)"),

    MEAN_PREASURE_HIST_2("(7\\.0\\.42\\.78\\.0\\.)(([1-9])|([1][0-5]))", "mean pressure (historic)"),
    MAX_INT_PREASURE_HIST_2("(7\\.0\\.42\\.84\\.0\\.)(([1-9])|([1][0-5]))", "Maximum pressure (historic)"),
    MIN_INT_PREASURE_HIST_2("(7\\.0\\.42\\.81\\.0\\.)(([1-9])|([1][0-5]))", "Minimum pressure (historic)"),

    MEAN_TEMPERATURE_HIST_2("(7\\.0\\.41\\.78\\.0\\.)(([1-9])|([1][0-5]))", "mean temperature (historic)"),
    MAX_INT_TEMPERATURE_HIST_2("(7\\.0\\.41\\.84\\.0\\.)(([1-9])|([1][0-5]))", "Maximum temperature (historic)"),
    MIN_INT_TEMPERATURE_HIST_2("(7\\.0\\.41\\.81\\.0\\.)(([1-9])|([1][0-5]))", "Minimum temperature (historic)"),

    MEAN_CONVERSION_FACTOR_HIST_2("(7\\.0\\.52\\.0\\.20\\.)(([1-9])|([1][0-5]))", "mean conversion factor (historic)"),
    MEAN_COMPRESS_FACTOR_HIST_2("(7\\.0\\.55\\.0\\.20\\.)(([1-9])|([1][0-5]))", "mean compressibility factor (historic)"),

    FLOW_RATE_MEAS_COND_CURR_2("7.0.43.0.0.255", "flow rate at measurement conditions"),
    FLOW_RATE_BASE_COND_CURR_2("7.0.43.2.0.255", "flow rate at base conditions");

    private final String obisCode;
    private final String desc;

    private Lis200ObisCode(String code, String desc) {
        this.obisCode = code;
        this.desc = desc;
    }

    public String getObisCode() {
        return obisCode;
    }

    public String getDesc() {
        return desc;
    }

    public boolean matches(String code) {
        return code.matches(this.obisCode);
    }
}

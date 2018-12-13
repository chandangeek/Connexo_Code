package com.elster.protocolimpl.dlms.registers;

/**
 * User: heuckeg
 * Date: 03.01.13
 * Time: 13:51
 */
public enum HistoricalObisCode
{
    /* all historic counter codes follow the scheme a.b.c.d.e.1 - 15 */
    VOLUME_BASE_COND_TOTAL_HIST("(7\\.0\\.13\\.2\\.0\\.)(([1-9])|([1][0-5]))", "Volume at base conditions total (historic)"),
    VOLUME_BASE_COND_CORR_HIST("(7\\.0\\.11\\.2\\.0\\.)(([1-9])|([1][0-5]))", "Corrected volume at base conditions (historic)"),

    VOLUME_MEAS_COND_TOTAL_HIST("(7\\.0\\.13\\.0\\.0\\.)(([1-9])|([1][0-5]))", "Volume at measurement conditions total (historic)"),
    VOLUME_MEAS_COND_CORR_HIST("(7\\.0\\.11\\.0\\.0\\.)(([1-9])|([1][0-5]))", "Corrected volume at measurement conditions (historic)"),

    MAX_INT_VAL_MEAS_COND_HIST("(7\\.0\\.13\\.54\\.0\\.)(([1-9])|([1][0-5]))", "Maximum measurement period Vm (historic)"),
    MAX_DAY_VAL_MEAS_COND_HIST("(7\\.0\\.13\\.60\\.0\\.)(([1-9])|([1][0-5]))", "Maximum daily Vm (historic)"),
    MAX_INT_VAL_BASE_COND_HIST("(7\\.0\\.13\\.56\\.0\\.)(([1-9])|([1][0-5]))", "Maximum measurement period Vb (historic)"),
    MAX_DAY_VAL_BASE_COND_HIST("(7\\.0\\.11\\.62\\.0\\.)(([1-9])|([1][0-5]))", "Maximum daily Vb (historic)"),

    MAX_INT_FR_BASE_COND_HIST("(7\\.0\\.43\\.206\\.20\\.)(([1-9])|([1][0-5]))", "Maximum interval flow rate at base conditions (historic)"),
    MIN_INT_FR_BASE_COND_HIST("(7\\.0\\.43\\.216\\.20\\.)(([1-9])|([1][0-5]))", "Minumum interval flow rate as base conditions (historic)"),
    MAX_INT_FR_MEAS_COND_HIST("(7\\.0\\.43\\.206\\.0\\.)(([1-9])|([1][0-5]))", "Maximum interval flow rate as meas. conditions (historic)"),
    MIN_INT_FR_MEAS_COND_HIST("(7\\.0\\.43\\.216\\.0\\.)(([1-9])|([1][0-5]))", "Minimum interval flow rate as meas. conditions (historic)"),

    MEAN_PREASURE_HIST("(7\\.0\\.42\\.78\\.0\\.)(([1-9])|([1][0-5]))", "mean pressure (historic)"),
    MAX_INT_PREASURE_HIST("(7\\.0\\.42\\.84\\.0\\.)(([1-9])|([1][0-5]))", "Maximum pressure (historic)"),
    MIN_INT_PREASURE_HIST("(7\\.0\\.42\\.81\\.0\\.)(([1-9])|([1][0-5]))", "Minimum pressure (historic)"),

    MEAN_TEMPERATURE_HIST("(7\\.0\\.41\\.78\\.0\\.)(([1-9])|([1][0-5]))", "mean temperature (historic)"),
    MAX_INT_TEMPERATURE_HIST("(7\\.0\\.41\\.84\\.0\\.)(([1-9])|([1][0-5]))", "Maximum temperature (historic)"),
    MIN_INT_TEMPERATURE_HIST("(7\\.0\\.41\\.81\\.0\\.)(([1-9])|([1][0-5]))", "Minimum temperature (historic)"),

    MEAN_CONVERSION_FACTOR_HIST("(7\\.0\\.52\\.0\\.20\\.)(([1-9])|([1][0-5]))", "mean conversion factor (historic)"),
    MEAN_COMPRESS_FACTOR_HIST("(7\\.0\\.55\\.0\\.20\\.)(([1-9])|([1][0-5]))", "mean compressibility factor (historic)");

    private final String obisCode;
    private final String desc;

    private HistoricalObisCode(String code, String desc)
    {
        this.obisCode = code;
        this.desc = desc;
    }

    public String getObisCode()
    {
        return obisCode;
    }

    public String getDesc()
    {
        return desc;
    }
}

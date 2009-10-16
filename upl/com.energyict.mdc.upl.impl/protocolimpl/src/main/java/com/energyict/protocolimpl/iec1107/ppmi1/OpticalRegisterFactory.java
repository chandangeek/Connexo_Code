package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.protocol.MeterExceptionInfo;

/**
 * There is 1 register per actual "Meter Register".
 * But a register can also be composed out of several "Meter Registers"
 * Or a register may need another meter register to interpret the value.
 * 
 * @author jme
 */
public class OpticalRegisterFactory extends AbstractRegisterFactory {

	/** Creates a new instance of RegisterFactory */
	public OpticalRegisterFactory(PPM ppm, MeterExceptionInfo meterExceptionInfo ) {
		super(ppm, meterExceptionInfo);
	}

	{
		add("850", R_TIME_ADJUSTMENT_RS232, Register.DATE, 0, -1, Register.WRITEABLE, Register.NOT_CACHED, false);
		add("860", R_TIME_DATE_RS232, Register.DATE, 0, -1, Register.WRITEABLE, Register.NOT_CACHED);
		add("861", R_TIME_DATE_OPTICAL, Register.DATE, 0, -1, Register.WRITEABLE, Register.NOT_CACHED, false);
		add("878", R_INTEGRATION_PERIOD, Register.INTEGER, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("878", R_SUBINTERVAL_PERIOD, Register.INTEGER, 1, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("878", R_NUMBER_OF_SUBINTERVALS, Register.INTEGER, 2, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("795", R_SCHEME_ID, Register.STRING, 0, 8, Register.NOT_WRITEABLE, Register.CACHED);
		add("798", R_SERIAL_NUMBER, Register.STRING, 0, 16, Register.NOT_WRITEABLE, Register.CACHED);
		add("704", R_SCALING_FACTOR, Register.SCALINGFACTOR, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("774", R_LOAD_PROFILE_DEFININTION, Register.LOADPROFILEDEF, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("501", R_TOTAL_IMPORT_WH, Register.REGISTER, 0, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("501", R_TOTAL_EXPORT_WH, Register.REGISTER, 5, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("501", R_TOTAL_IMPORT_VARH, Register.REGISTER, 10, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("501", R_TOTAL_EXPORT_VARH, Register.REGISTER, 15, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("501", R_TOTAL_VAH, Register.REGISTER, 20, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_1, Register.REGISTER, 25, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_2, Register.REGISTER, 30, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_3, Register.REGISTER, 35, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_4, Register.REGISTER, 40, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_5, Register.REGISTER, 45, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_6, Register.REGISTER, 50, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_7, Register.REGISTER, 55, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_8, Register.REGISTER, 60, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("503", R_MAXIMUM_DEMAND_1, Register.MD, 0, 32, Register.NOT_WRITEABLE, Register.CACHED);
		add("503", R_MAXIMUM_DEMAND_2, Register.MD, 32, 32, Register.NOT_WRITEABLE, Register.CACHED);
		add("503", R_MAXIMUM_DEMAND_3, Register.MD, 64, 32, Register.NOT_WRITEABLE, Register.CACHED);
		add("503", R_MAXIMUM_DEMAND_4, Register.MD, 96, 32, Register.NOT_WRITEABLE, Register.CACHED);
		add("504", R_CUMULATIVE_MAXIMUM_DEMAND1, Register.REGISTER, 0, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("504", R_CUMULATIVE_MAXIMUM_DEMAND2, Register.REGISTER, 5, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("504", R_CUMULATIVE_MAXIMUM_DEMAND3, Register.REGISTER, 16, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("504", R_CUMULATIVE_MAXIMUM_DEMAND4, Register.REGISTER, 21, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("741", R_TOU_ALLOC_IMPORT_KWH, Register.BYTEARRAY, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("742", R_TOU_ALLOC_EXPORT_KWH, Register.BYTEARRAY, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("743", R_TOU_ALLOC_IMPORT_KVARH, Register.BYTEARRAY, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("744", R_TOU_ALLOC_EXPORT_KVARH, Register.BYTEARRAY, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("745", R_TOU_ALLOC_TOTAL_KVAH, Register.BYTEARRAY, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("751", R_MD_TOU_ALLOC_IMPORT_KWH, Register.BYTEARRAY, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("752", R_MD_TOU_ALLOC_EXPORT_KWH, Register.BYTEARRAY, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("753", R_MD_TOU_ALLOC_IMPORT_KVARH, Register.BYTEARRAY, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("754", R_MD_TOU_ALLOC_EXPORT_KVARH, Register.BYTEARRAY, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("755", R_MD_TOU_ALLOC_TOTAL_KVAH, Register.BYTEARRAY, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("540", R_HISTORICAL_DATA, Register.HISTORICAL, 0, 1024, Register.NOT_WRITEABLE, Register.CACHED);
		add("550", R_LOAD_PROFILE, Register.BYTEARRAY, 0, -1, Register.NOT_WRITEABLE, Register.CACHED);
	}

}
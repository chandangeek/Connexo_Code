package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.protocol.MeterExceptionInfo;

/**
 * There is 1 register per actual "Meter Register".
 * But a register can also be composed out of several "Meter Registers"
 * Or a register may need another meter register to interpret the value.
 * 
 * @author fbo
 */

public class OpusRegisterFactory extends AbstractRegisterFactory {

	/** Creates a new instance of RegisterFactory */
	public OpusRegisterFactory(PPM ppm, MeterExceptionInfo meterExceptionInfo) {
		super(ppm, meterExceptionInfo);
	}

	{
		add("850", R_TIME_ADJUSTMENT_RS232, Register.DATE, 0, -1, Register.WRITEABLE, Register.NOT_CACHED, false);
		add("860", R_TIME_DATE_RS232, Register.DATE, 0, -1, Register.WRITEABLE, Register.NOT_CACHED);
		add("861", R_TIME_DATE_OPTICAL, Register.DATE, 0, -1, Register.WRITEABLE, Register.NOT_CACHED, false);
		add("870", R_INTEGRATION_PERIOD, Register.INTEGER, 4, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("870", R_SUBINTERVAL_PERIOD, Register.INTEGER, 5, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("870", R_NUMBER_OF_SUBINTERVALS, Register.INTEGER, 6, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("790", R_SCHEME_ID, Register.STRING, 0, 8, Register.NOT_WRITEABLE, Register.CACHED);
		add("790", R_SERIAL_NUMBER, Register.STRING, 13, 16, Register.NOT_WRITEABLE, Register.CACHED);
		add("700", R_SCALING_FACTOR, Register.SCALINGFACTOR, 16, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("770", R_LOAD_PROFILE_DEFININTION, Register.LOADPROFILEDEF, 4, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_TOTAL_IMPORT_WH, Register.REGISTER, 0, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_TOTAL_EXPORT_WH, Register.REGISTER, 5, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_TOTAL_IMPORT_VARH, Register.REGISTER, 10, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_TOTAL_EXPORT_VARH, Register.REGISTER, 15, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_TOTAL_VAH, Register.REGISTER, 20, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_TIME_OF_USE_1, Register.REGISTER, 25, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_TIME_OF_USE_2, Register.REGISTER, 30, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_TIME_OF_USE_3, Register.REGISTER, 35, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_TIME_OF_USE_4, Register.REGISTER, 40, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_TIME_OF_USE_5, Register.REGISTER, 45, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_TIME_OF_USE_6, Register.REGISTER, 50, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_TIME_OF_USE_7, Register.REGISTER, 55, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_TIME_OF_USE_8, Register.REGISTER, 60, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_MAXIMUM_DEMAND_1, Register.MD, 65, 32, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_MAXIMUM_DEMAND_2, Register.MD, 97, 32, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_MAXIMUM_DEMAND_3, Register.MD, 129, 32, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_MAXIMUM_DEMAND_4, Register.MD, 161, 32, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_CUMULATIVE_MAXIMUM_DEMAND1, Register.REGISTER, 193, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_CUMULATIVE_MAXIMUM_DEMAND2, Register.REGISTER, 198, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_CUMULATIVE_MAXIMUM_DEMAND3, Register.REGISTER, 209, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("500", R_CUMULATIVE_MAXIMUM_DEMAND4, Register.REGISTER, 214, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("740", R_TOU_ALLOC_IMPORT_KWH, Register.BYTEARRAY, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("740", R_TOU_ALLOC_EXPORT_KWH, Register.BYTEARRAY, 1, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("740", R_TOU_ALLOC_IMPORT_KVARH, Register.BYTEARRAY, 2, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("740", R_TOU_ALLOC_EXPORT_KVARH, Register.BYTEARRAY, 3, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("740", R_TOU_ALLOC_TOTAL_KVAH, Register.BYTEARRAY, 4, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("750", R_MD_TOU_ALLOC_IMPORT_KWH, Register.BYTEARRAY, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("750", R_MD_TOU_ALLOC_EXPORT_KWH, Register.BYTEARRAY, 1, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("750", R_MD_TOU_ALLOC_IMPORT_KVARH, Register.BYTEARRAY, 2, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("750", R_MD_TOU_ALLOC_EXPORT_KVARH, Register.BYTEARRAY, 3, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("750", R_MD_TOU_ALLOC_TOTAL_KVAH, Register.BYTEARRAY, 4, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("540", R_HISTORICAL_DATA, Register.HISTORICAL, 0, 1024, Register.NOT_WRITEABLE, Register.CACHED);
		add("550", R_LOAD_PROFILE, Register.BYTEARRAY, 0, -1, Register.NOT_WRITEABLE, Register.CACHED);

	}

}
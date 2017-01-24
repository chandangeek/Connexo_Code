package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;

/**
 * There is 1 register per actual "Meter Register".
 * But a register can also be composed out of several "Meter Registers"
 * Or a register may need another meter register to interpret the value.
 *
 * @author jme
 */

public class OpusRegisterFactory extends AbstractRegisterFactory {

	/** Creates a new instance of RegisterFactory */
	public OpusRegisterFactory(PPM ppm, MeterExceptionInfo meterExceptionInfo) {
		super(ppm, meterExceptionInfo);
	}

	{
		add("850", R_TIME_ADJUSTMENT_RS232, PPM1Register.DATE, 0, -1, PPM1Register.WRITEABLE, PPM1Register.NOT_CACHED, false);
		add("860", R_TIME_DATE_RS232, PPM1Register.DATE, 0, -1, PPM1Register.WRITEABLE, PPM1Register.NOT_CACHED);
		add("861", R_TIME_DATE_OPTICAL, PPM1Register.DATE, 0, -1, PPM1Register.WRITEABLE, PPM1Register.NOT_CACHED, false);
		add("870", R_INTEGRATION_PERIOD, PPM1Register.INTEGER, 4, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("870", R_SUBINTERVAL_PERIOD, PPM1Register.INTEGER, 5, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("870", R_NUMBER_OF_SUBINTERVALS, PPM1Register.INTEGER, 6, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("790", R_SCHEME_ID, PPM1Register.STRING, 0, 8, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("790", R_SERIAL_NUMBER, PPM1Register.STRING, 13, 16, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("700", R_SCALING_FACTOR, PPM1Register.SCALINGFACTOR, 16, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("770", R_LOAD_PROFILE_DEFININTION, PPM1Register.LOADPROFILEDEF, 4, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_TOTAL_IMPORT_WH, PPM1Register.REGISTER, 0, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_TOTAL_EXPORT_WH, PPM1Register.REGISTER, 5, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_TOTAL_IMPORT_VARH, PPM1Register.REGISTER, 10, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_TOTAL_EXPORT_VARH, PPM1Register.REGISTER, 15, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_TOTAL_VAH, PPM1Register.REGISTER, 20, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_TIME_OF_USE_1, PPM1Register.REGISTER, 25, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_TIME_OF_USE_2, PPM1Register.REGISTER, 30, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_TIME_OF_USE_3, PPM1Register.REGISTER, 35, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_TIME_OF_USE_4, PPM1Register.REGISTER, 40, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_TIME_OF_USE_5, PPM1Register.REGISTER, 45, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_TIME_OF_USE_6, PPM1Register.REGISTER, 50, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_TIME_OF_USE_7, PPM1Register.REGISTER, 55, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_TIME_OF_USE_8, PPM1Register.REGISTER, 60, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_MAXIMUM_DEMAND_1, PPM1Register.MD, 65, 32, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_MAXIMUM_DEMAND_2, PPM1Register.MD, 97, 32, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_MAXIMUM_DEMAND_3, PPM1Register.MD, 129, 32, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_MAXIMUM_DEMAND_4, PPM1Register.MD, 161, 32, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_CUMULATIVE_MAXIMUM_DEMAND1, PPM1Register.REGISTER, 193, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_CUMULATIVE_MAXIMUM_DEMAND2, PPM1Register.REGISTER, 198, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_CUMULATIVE_MAXIMUM_DEMAND3, PPM1Register.REGISTER, 209, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("500", R_CUMULATIVE_MAXIMUM_DEMAND4, PPM1Register.REGISTER, 214, 5, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("740", R_TOU_ALLOC_IMPORT_KWH, PPM1Register.BYTEARRAY, 0, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("740", R_TOU_ALLOC_EXPORT_KWH, PPM1Register.BYTEARRAY, 1, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("740", R_TOU_ALLOC_IMPORT_KVARH, PPM1Register.BYTEARRAY, 2, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("740", R_TOU_ALLOC_EXPORT_KVARH, PPM1Register.BYTEARRAY, 3, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("740", R_TOU_ALLOC_TOTAL_KVAH, PPM1Register.BYTEARRAY, 4, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("750", R_MD_TOU_ALLOC_IMPORT_KWH, PPM1Register.BYTEARRAY, 0, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("750", R_MD_TOU_ALLOC_EXPORT_KWH, PPM1Register.BYTEARRAY, 1, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("750", R_MD_TOU_ALLOC_IMPORT_KVARH, PPM1Register.BYTEARRAY, 2, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("750", R_MD_TOU_ALLOC_EXPORT_KVARH, PPM1Register.BYTEARRAY, 3, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("750", R_MD_TOU_ALLOC_TOTAL_KVAH, PPM1Register.BYTEARRAY, 4, 1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("540", R_HISTORICAL_DATA, PPM1Register.HISTORICAL, 0, 1024, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);
		add("550", R_LOAD_PROFILE, PPM1Register.BYTEARRAY, 0, -1, PPM1Register.NOT_WRITEABLE, PPM1Register.CACHED);

	}

}
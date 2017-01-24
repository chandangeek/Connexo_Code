package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.ppmi1.register.HistoricalDataSet;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileDefinition;
import com.energyict.protocolimpl.iec1107.ppmi1.register.MainRegister;
import com.energyict.protocolimpl.iec1107.ppmi1.register.MaximumDemand;
import com.energyict.protocolimpl.iec1107.ppmi1.register.RegisterInformation;
import com.energyict.protocolimpl.iec1107.ppmi1.register.ScalingFactor;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public interface RegisterFactory {

	/** 850 Time Adjustement via RS232 */
	String R_TIME_ADJUSTMENT_RS232 = "TimeDateAdjustmentRs232";

	/** 860 Time and date via RS232 */
	String R_TIME_DATE_RS232 = "TimeDateRs232";

	/** 861 Time and date via optical port */
	String R_TIME_DATE_OPTICAL = "TimeDateOptical";

	/** 878 Integration period */
	String R_INTEGRATION_PERIOD = "IntegrationPeriod";

	/** 798 Serial number */
	String R_SERIAL_NUMBER = "SerialNumber";

	/** 799 Opus password */
	String R_OPUS_PASSWORD = "OpusPassword";

	/** 795 Scheme id */
	String R_SCHEME_ID = "SchemeId";

	/** 878 Subinterval period */
	String R_SUBINTERVAL_PERIOD = "SubintervalPeriod";

	/** 878 Number subintervals */
	String R_NUMBER_OF_SUBINTERVALS = "NumberOfSubintervals";

	/** 704 Scaling factor */
	String R_SCALING_FACTOR = "ScalingFactor";

	/** 774 Load profile defenition */
	String R_LOAD_PROFILE_DEFININTION = "LoadProfileDefinition";

	/** 501 Total Registers: Import kWh */
	String R_TOTAL_IMPORT_WH = "TotalImportKwh";

	/** 501 Total Registers: Export kWh */
	String R_TOTAL_EXPORT_WH = "TotalExportKwh";

	/** 501 Total Registers: Import kvarh */
	String R_TOTAL_IMPORT_VARH = "TotalImportKvarh";

	/** 501 Total Registers: Export kvarh */
	String R_TOTAL_EXPORT_VARH = "TotalExportKvarh";

	/** 501 Total Registers: Total kVAh */
	String R_TOTAL_VAH = "TotalKvah";

	/** 502 Time Of Use Registers: TOU 1 */
	String R_TIME_OF_USE_1 = "TimeOfUse1";

	/** 502 Time Of Use Registers: TOU 2 */
	String R_TIME_OF_USE_2 = "TimeOfUse2";

	/** 502 Time Of Use Registers: TOU 3 */
	String R_TIME_OF_USE_3 = "TimeOfUse3";

	/** 502 Time Of Use Registers: TOU 4 */
	String R_TIME_OF_USE_4 = "TimeOfUse4";

	/** 502 Time Of Use Registers: TOU 5 */
	String R_TIME_OF_USE_5 = "TimeOfUse5";

	/** 502 Time Of Use Registers: TOU 6 */
	String R_TIME_OF_USE_6 = "TimeOfUse6";

	/** 502 Time Of Use Registers: TOU 7 */
	String R_TIME_OF_USE_7 = "TimeOfUse7";

	/** 502 Time Of Use Registers: TOU 8 */
	String R_TIME_OF_USE_8 = "TimeOfUse8";

	/** 503 Maximum Demand 1 */
	String R_MAXIMUM_DEMAND_1 = "MaximumDemand1";

	/** 503 Maximum Demand 2 */
	String R_MAXIMUM_DEMAND_2 = "MaximumDemand2";

	/** 503 Maximum Demand 3 */
	String R_MAXIMUM_DEMAND_3 = "MaximumDemand3";

	/** 503 Maximum Demand 4 */
	String R_MAXIMUM_DEMAND_4 = "MaximumDemand4";

	/** 504 Cumulative Maximum Demand 1 */
	String R_CUMULATIVE_MAXIMUM_DEMAND1 = "CumMaximumDemand1";

	/** 504 Cumulative Maximum Demand 1 */
	String R_CUMULATIVE_MAXIMUM_DEMAND2 = "CumMaximumDemand2";

	/** 504 Cumulative Maximum Demand 1 */
	String R_CUMULATIVE_MAXIMUM_DEMAND3 = "CumMaximumDemand3";

	/** 504 Cumulative Maximum Demand 1 */
	String R_CUMULATIVE_MAXIMUM_DEMAND4 = "CumMaximumDemand4";

	/** 741 Time Of Use Register Allocation from Import kWh */
	String R_TOU_ALLOC_IMPORT_KWH = "TouAllocImportKWH";

	/** 742 Time Of Use Register Allocation from Export kWh */
	String R_TOU_ALLOC_EXPORT_KWH = "TouAllocExportKWH";

	/** 743 Time Of Use Register Allocation from Import kvarh */
	String R_TOU_ALLOC_IMPORT_KVARH = "TouAllocImportKvarh";

	/** 744 Time Of Use Register Allocation from Export kvarh */
	String R_TOU_ALLOC_EXPORT_KVARH = "TouAllocExportKvarh";

	/** 745 Time Of Use Register Allocation from Total kVAh */
	String R_TOU_ALLOC_TOTAL_KVAH = "TouAllocImportKVAh";

	/** 751 Maximum Demand Time Of Use Register Allocation From Import kW */
	String R_MD_TOU_ALLOC_IMPORT_KWH = "MdTouAllocImportKW";

	/** 752 Maximum Demand Time Of Use Register Allocation from Export kW */
	String R_MD_TOU_ALLOC_EXPORT_KWH = "MdTouAllocExportKW";

	/** 753 Maximum Demand Time Of Use Register Allocation from Import kvar */
	String R_MD_TOU_ALLOC_IMPORT_KVARH = "MdTouAllocImportKvar";

	/** 754 Maximum Demand Time Of Use Register Allocation from Export kvar */
	String R_MD_TOU_ALLOC_EXPORT_KVARH = "MdTouAllocImportKvarh";

	/** 755 Maximum Demand Time Of Use Register Allocation from Total kVA */
	String R_MD_TOU_ALLOC_TOTAL_KVAH = "MdTouAllocImportKVAh";

	/** 540 Historical Data */
	String R_HISTORICAL_DATA = "HistoricalData";

	/** Register keys */

	String R_LOAD_PROFILE = "LoadProfile";

	String	R_TIME_ADJUSTMENT_OPTICAL	= "TimeAdjustmentOptical";

	Map getRegisters();

	void setRegister(String name, String value) throws IOException;

	void setRegister(String name, Object object) throws IOException;

	Object getRegister(String name) throws IOException;

	RegisterValue readRegister(ObisCode obisCode) throws IOException;

	byte[] getRegisterRawData(String name, int dataLength) throws IOException;

	// search the map for the register info
	PPM1Register findRegister(String name) throws IOException;

	RegisterInfo getRegisterInfo(String name);

	/** 861 Time and date via optical port/ 860 Time and date via RS232 */
	Date getTimeDate() throws IOException;

	/** 878 Integration period */
	Integer getIntegrationPeriod() throws IOException;

	/** 798 Serial number */
	String getSerialNumber() throws IOException;

	/** 795 Scheme id */
	String getSchemeId() throws IOException;

	/** 878 Subinterval period */
	Integer getSubIntervalPeriod() throws IOException;

	/** 878 Number subintervals */
	Integer getNumberOfSubintervals() throws IOException;

	/** 704 Scaling factor */
	ScalingFactor getScalingFactor() throws IOException;

	/** 501 Total Registers: Import kWh */
	MainRegister getTotalImportKWh() throws IOException;

	/** 501 Total Registers: Export kWh */
	MainRegister getTotalExportKWh() throws IOException;

	/** 501 Total Registers: Import kvarh */
	MainRegister getTotalImportKvarh() throws IOException;

	/** 501 Total Registers: Export kvarh */
	MainRegister getTotalExportKvarh() throws IOException;

	/** 501 Total Registers: Total kVAh */
	MainRegister getTotalTotalKVAh() throws IOException;

	/** 502 Time Of Use Registers: TOU 1 */
	MainRegister getTimeOfUse1() throws IOException;

	/** 502 Time Of Use Registers: TOU 2 */
	MainRegister getTimeOfUse2() throws IOException;

	/** 502 Time Of Use Registers: TOU 3 */
	MainRegister getTimeOfUse3() throws IOException;

	/** 502 Time Of Use Registers: TOU 4 */
	MainRegister getTimeOfUse4() throws IOException;

	/** 502 Time Of Use Registers: TOU 5 */
	MainRegister getTimeOfUse5() throws IOException;

	/** 502 Time Of Use Registers: TOU 6 */
	MainRegister getTimeOfUse6() throws IOException;

	/** 502 Time Of Use Registers: TOU 7 */
	MainRegister getTimeOfUse7() throws IOException;

	/** 502 Time Of Use Registers: TOU 8 */
	MainRegister getTimeOfUse8() throws IOException;

	/** 503 Maximum Demand 1 */
	MaximumDemand getMaximumDemand1() throws IOException;

	/** 503 Maximum Demand 2 */
	MaximumDemand getMaximumDemand2() throws IOException;

	/** 503 Maximum Demand 3 */
	MaximumDemand getMaximumDemand3() throws IOException;

	/** 503 Maximum Demand 4 */
	MaximumDemand getMaximumDemand4() throws IOException;

	/** 504 Cumulative Maximum Demand 1 */
	MainRegister getCumulativeMaximumDemand1() throws IOException;

	/** 504 Cumulative Maximum Demand 2 */
	MainRegister getCumulativeMaximumDemand2() throws IOException;

	/** 504 Cumulative Maximum Demand 3 */
	MainRegister getCumulativeMaximumDemand3() throws IOException;

	/** 504 Cumulative Maximum Demand 4 */
	MainRegister getCumulativeMaximumDemand4() throws IOException;

	/**
	 * A DataIndentity can contain multiple registers. But what if a Register is
	 * best composed out of several other registers ?
	 *
	 * I will cache this in RegisterFactory, it could be done better .... 741 -
	 * 745, 751 - 755 Register Allocation
	 */
	RegisterInformation getRegisterInformation() throws IOException;

	/** 540 Historical Data */
	HistoricalDataSet getHistoricalData() throws IOException;

	/** 774 Load Profile Definition */
	LoadProfileDefinition getLoadProfileDefinition() throws IOException;

	PPM getPpm();

	DataIdentityFactory getDataIdentityFactory();

}
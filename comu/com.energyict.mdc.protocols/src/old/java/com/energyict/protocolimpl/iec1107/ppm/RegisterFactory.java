package com.energyict.protocolimpl.iec1107.ppm;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ppm.parser.RegisterInformationParser;
import com.energyict.protocolimpl.iec1107.ppm.register.HistoricalData;
import com.energyict.protocolimpl.iec1107.ppm.register.HistoricalDataSet;
import com.energyict.protocolimpl.iec1107.ppm.register.LoadProfileDefinition;
import com.energyict.protocolimpl.iec1107.ppm.register.MainRegister;
import com.energyict.protocolimpl.iec1107.ppm.register.MaximumDemand;
import com.energyict.protocolimpl.iec1107.ppm.register.RegisterInformation;
import com.energyict.protocolimpl.iec1107.ppm.register.ScalingFactor;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * There is 1 register per actual "Meter Register".
 * But a register can also be composed out of several "Meter Registers"
 * Or a register may need another meter register to iterprete the value.
 * @author fbo
 */

public class RegisterFactory {

	static final int MAX_CMD_REGS = 8;
	static final int MAX_MD_REGS = 24;

	/** Register keys */

	/** 850 Time Adjustement via RS232 */
	final static String R_TIME_ADJUSTMENT_RS232 = "TimeDateAdjustmentRs232";
	/** 860 Time and date via RS232 */
	final static String R_TIME_DATE_RS232 = "TimeDateRs232";
	/** 861 Time and date via optical port */
	final static String R_TIME_DATE_OPTICAL = "TimeDateOptical";
	/** 862 Time Adjustement via optical port */
	final static String	R_TIME_ADJUSTMENT_OPTICAL	= "TimeAdjustmentOptical";

	/** 878 Integration period */
	final static String R_INTEGRATION_PERIOD = "IntegrationPeriod";
	/** 798 Serial number */
	final static String R_SERIAL_NUMBER = "SerialNumber";
	/** 795 Scheme id */
	final static String R_SCHEME_ID = "SchemeId";
	/** 878 Subinterval period */
	final static String R_SUBINTERVAL_PERIOD = "SubintervalPeriod";
	/** 878 Number subintervals */
	final static String R_NUMBER_OF_SUBINTERVALS = "NumberOfSubintervals";
	/** 704 Scaling factor */
	final static String R_SCALING_FACTOR = "ScalingFactor";
	/** 774 Load profile defenition */
	final static String R_LOAD_PROFILE_DEFININTION = "LoadProfileDefinition";

	/** 501 Total Registers: Import kWh */
	public final static String R_TOTAL_IMPORT_WH = "TotalImportKwh";
	/** 501 Total Registers: Export kWh */
	public final static String R_TOTAL_EXPORT_WH = "TotalExportKwh";
	/** 501 Total Registers: Import kvarh */
	public final static String R_TOTAL_IMPORT_VARH = "TotalImportKvarh";
	/** 501 Total Registers: Export kvarh */
	public final static String R_TOTAL_EXPORT_VARH = "TotalExportKvarh";
	/** 501 Total Registers: Total kVAh */
	public final static String R_TOTAL_VAH = "TotalKvah";

	/** 502 Time Of Use Registers: TOU 1 */
	public final static String R_TIME_OF_USE_1 = "TimeOfUse1";
	/** 502 Time Of Use Registers: TOU 2 */
	public final static String R_TIME_OF_USE_2 = "TimeOfUse2";
	/** 502 Time Of Use Registers: TOU 3 */
	public final static String R_TIME_OF_USE_3 = "TimeOfUse3";
	/** 502 Time Of Use Registers: TOU 4 */
	public final static String R_TIME_OF_USE_4 = "TimeOfUse4";
	/** 502 Time Of Use Registers: TOU 5 */
	public final static String R_TIME_OF_USE_5 = "TimeOfUse5";
	/** 502 Time Of Use Registers: TOU 6 */
	public final static String R_TIME_OF_USE_6 = "TimeOfUse6";
	/** 502 Time Of Use Registers: TOU 7 */
	public final static String R_TIME_OF_USE_7 = "TimeOfUse7";
	/** 502 Time Of Use Registers: TOU 8 */
	public final static String R_TIME_OF_USE_8 = "TimeOfUse8";

	/** 503 Maximum Demand 1 */
	public final static String R_MAXIMUM_DEMAND_1 = "MaximumDemand1";
	/** 503 Maximum Demand 2 */
	public final static String R_MAXIMUM_DEMAND_2 = "MaximumDemand2";
	/** 503 Maximum Demand 3 */
	public final static String R_MAXIMUM_DEMAND_3 = "MaximumDemand3";
	/** 503 Maximum Demand 4 */
	public final static String R_MAXIMUM_DEMAND_4 = "MaximumDemand4";

	/** 504 Cumulative Maximum Demand 1 */
	public final static String R_CUMULATIVE_MAXIMUM_DEMAND1 = "CumMaximumDemand1";
	/** 504 Cumulative Maximum Demand 1 */
	public final static String R_CUMULATIVE_MAXIMUM_DEMAND2 = "CumMaximumDemand2";
	/** 504 Cumulative Maximum Demand 1 */
	public final static String R_CUMULATIVE_MAXIMUM_DEMAND3 = "CumMaximumDemand3";
	/** 504 Cumulative Maximum Demand 1 */
	public final static String R_CUMULATIVE_MAXIMUM_DEMAND4 = "CumMaximumDemand4";

	/** 741 Time Of Use Register Allocation from Import kWh */
	public final static String R_TOU_ALLOC_IMPORT_KWH = "TouAllocImportKWH";
	/** 742 Time Of Use Register Allocation from Export kWh */
	public final static String R_TOU_ALLOC_EXPORT_KWH = "TouAllocExportKWH";
	/** 743 Time Of Use Register Allocation from Import kvarh */
	public final static String R_TOU_ALLOC_IMPORT_KVARH = "TouAllocImportKvarh";
	/** 744 Time Of Use Register Allocation from Export kvarh */
	public final static String R_TOU_ALLOC_EXPORT_KVARH = "TouAllocExportKvarh";
	/** 745 Time Of Use Register Allocation from Total kVAh */
	public final static String R_TOU_ALLOC_TOTAL_KVAH = "TouAllocImportKVAh";

	/** 751 Maximum Demand Time Of Use Register Allocation From Import kW */
	public final static String R_MD_TOU_ALLOC_IMPORT_KWH = "MdTouAllocImportKW";
	/** 752 Maximum Demand Time Of Use Register Allocation from Export kW */
	public final static String R_MD_TOU_ALLOC_EXPORT_KWH = "MdTouAllocExportKW";
	/** 753 Maximum Demand Time Of Use Register Allocation from Import kvar */
	public final static String R_MD_TOU_ALLOC_IMPORT_KVARH = "MdTouAllocImportKvar";
	/** 754 Maximum Demand Time Of Use Register Allocation from Export kvar */
	public final static String R_MD_TOU_ALLOC_EXPORT_KVARH = "MdTouAllocImportKvarh";
	/** 755 Maximum Demand Time Of Use Register Allocation from Total kVA */
	public final static String R_MD_TOU_ALLOC_TOTAL_KVAH = "MdTouAllocImportKVAh";
	/** 540 Historical Data */
	final static String R_HISTORICAL_DATA = "HistoricalData";
	/** 541 Historical Data: Last Billing Period data */
	final static String R_LAST_BILLING = "LastBillingPeriod";

	final static String R_LOAD_PROFILE = "LoadProfile";
	static final String R_DEVICE_STATUS = "DeviceStatus";

	{
		this.registers = new TreeMap();

		add("850", R_TIME_ADJUSTMENT_RS232, Register.DATE, 0, -1, Register.WRITEABLE, Register.NOT_CACHED);
		add("862", R_TIME_ADJUSTMENT_OPTICAL, Register.HEX, 0, 1, Register.WRITEABLE, Register.NOT_CACHED);

		add("860", R_TIME_DATE_RS232, Register.DATE, 0, -1, Register.WRITEABLE, Register.NOT_CACHED);
		add("861", R_TIME_DATE_OPTICAL, Register.DATE, 0, -1, Register.WRITEABLE, Register.NOT_CACHED);
		add("878", R_INTEGRATION_PERIOD, Register.INTEGER, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("798", R_SERIAL_NUMBER, Register.STRING, 0, -1, Register.NOT_WRITEABLE, Register.CACHED);
		add("795", R_SCHEME_ID, Register.STRING, 0, -1, Register.NOT_WRITEABLE, Register.CACHED);
		add("878", R_SUBINTERVAL_PERIOD, Register.INTEGER, 1, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("878", R_NUMBER_OF_SUBINTERVALS, Register.INTEGER, 2, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("704", R_SCALING_FACTOR, Register.SCALINGFACTOR, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("774", R_LOAD_PROFILE_DEFININTION, Register.LOADPROFILEDEF, 0, 1, Register.NOT_WRITEABLE, Register.CACHED);
		add("501", R_TOTAL_IMPORT_WH, Register.REGISTER, 0, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("501", R_TOTAL_EXPORT_WH, Register.REGISTER, 5, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("501", R_TOTAL_IMPORT_VARH, Register.REGISTER, 10, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("501", R_TOTAL_EXPORT_VARH, Register.REGISTER, 15, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("501", R_TOTAL_VAH, Register.REGISTER, 20, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_1, Register.REGISTER, 0, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_2, Register.REGISTER, 5, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_3, Register.REGISTER, 10, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_4, Register.REGISTER, 15, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_5, Register.REGISTER, 20, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_6, Register.REGISTER, 25, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_7, Register.REGISTER, 30, 5, Register.NOT_WRITEABLE, Register.CACHED);
		add("502", R_TIME_OF_USE_8, Register.REGISTER, 35, 5, Register.NOT_WRITEABLE, Register.CACHED);
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
		add("541", R_LAST_BILLING, Register.LAST_BILLING, 0, 256, Register.NOT_WRITEABLE, Register.CACHED);
		add("550", R_LOAD_PROFILE, Register.BYTEARRAY, 0, -1, Register.NOT_WRITEABLE, Register.CACHED);
		add("561", R_DEVICE_STATUS, Register.BYTEARRAY, 0, 9, Register.NOT_WRITEABLE, Register.CACHED);

	}

	PPMMeterType meterType;
	private Map registers;
	private PPM ppm = null;
	private MeterExceptionInfo meterExceptionInfo = null;
	DataIdentityFactory dataItendityFactory = null;

	private RegisterInformation registerInformation = null;

	/** Creates a new instance of RegisterFactory */
	public RegisterFactory(PPM ppm, MeterExceptionInfo meterExceptionInfo, PPMMeterType abba1700MeterType) {
		this.ppm = ppm;
		this.setMeterExceptionInfo(meterExceptionInfo);
		meterType = abba1700MeterType;
		dataItendityFactory = new DataIdentityFactory(ppm, meterExceptionInfo, meterType);
	}

	protected DataIdentityFactory getDataIdentityFactory() {
		return dataItendityFactory;
	}

	protected PPM getPpm() {
		return ppm;
	}

	public Map getRegisters() {
		return registers;
	}

	private void addRegister(Register register) {
		this.registers.put(register.getName(), register);
	}

	private void add(String dataId, String name, int type, int offset,
			int length, boolean writeable, boolean cached) {

		Register r = new Register(dataId, name, type, offset, length,
				writeable, cached);
		r.setRegisterFactory(this);
		this.registers.put(name, r);
	}

	public void setRegister(String name, String value) throws IOException {
		try {
			Register register = findRegister(name);
			if (register.isWriteable()) {
				register.writeRegister(value);
			} else {
				throw new IOException(
				"ABBA1700, setRegister, register not writeable");
			}

		} catch (FlagIEC1107ConnectionException e) {
			throw new IOException("ABBA1700, setRegister, " + e.getMessage());
		}
	}

	public void setRegister(String name, Object object) throws IOException {
		try {
			Register register = findRegister(name);
			if (register.isWriteable()) {
				register.writeRegister(object);
			} else {
				throw new IOException(
				"ABBA1700, setRegister, register not writeable");
			}

		} catch (FlagIEC1107ConnectionException e) {
			throw new IOException("ABBA1700, setRegister, " + e.getMessage());
		}
	}

	public Object getRegister(String name) throws IOException {
		return getRegister(name, -1);
	}

	/*
	 * Read a register in the meter, from the current set or a billing set...
	 * @return object the register read @param billingPoint -1 = current, 0 =
	 * last billing point, 1 = 2-throws last billing point, ...
	 */
	private Object getRegister(String name, int billingPoint) throws IOException {
		Register register2Retrieve = findRegister(name);
		register2Retrieve = findRegister(name);
		return register2Retrieve.getValue();
	}

	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		ObisCodeMapper ocm = new ObisCodeMapper(this);
		return ocm.getRegisterValue(obisCode);
	}

	public byte[] getRegisterRawData(String name, int dataLength)
	throws IOException {
		try {
			Register register = findRegister(name);
			return (register.readRegister(register.isCached(), dataLength, 0));
		} catch (FlagIEC1107ConnectionException e) {
			throw new IOException("ABBA1700, getRegisterRawData, "
					+ e.getMessage());
		}
	}

	// search the map for the register info
	public Register findRegister(String name) throws IOException {
		Register register = (Register) registers.get(name);
		if (register == null) {
			throw new IOException("RegisterFactory, findRegister, " + name
					+ " does not exist!");
		} else {
			return register;
		}
	}

	public RegisterInfo getRegisterInfo(String name) {
		return null;
	}

	/** 861 Time and date via optical port */
	public Date getTimeDate() throws IOException {
		if( ppm.isOpus() ) {
			return (Date) getRegister(R_TIME_DATE_RS232);
		} else {
			return (Date) getRegister(R_TIME_DATE_OPTICAL);
		}
	}

	/** 878 Integration period */
	public Integer getIntegrationPeriod() throws IOException {
		return (Integer) getRegister(R_INTEGRATION_PERIOD);
	}

	/** 798 Serial number */
	public String getSerialNumber() throws IOException {
		return (String) getRegister(R_SERIAL_NUMBER);
	}

	/** 795 Scheme id */
	String getSchemeId() throws IOException {
		return (String) getRegister(R_SCHEME_ID);
	}

	/** 878 Subinterval period */
	public Integer getSubIntervalPeriod() throws IOException {
		return (Integer) getRegister(R_SUBINTERVAL_PERIOD);
	}

	/** 878 Number subintervals */
	public Integer getNumberOfSubintervals() throws IOException {
		return (Integer) getRegister(R_NUMBER_OF_SUBINTERVALS);
	}

	/** 704 Scaling factor */
	public ScalingFactor getScalingFactor() throws IOException {
		return (ScalingFactor) getRegister(R_SCALING_FACTOR);
	}

	/** 501 Total Registers: Import kWh */
	MainRegister getTotalImportKWh() throws IOException {
		return (MainRegister) getRegister(R_TOTAL_IMPORT_WH);
	}

	/** 501 Total Registers: Export kWh */
	MainRegister getTotalExportKWh() throws IOException {
		return (MainRegister) getRegister(R_TOTAL_EXPORT_WH);
	}

	/** 501 Total Registers: Import kvarh */
	MainRegister getTotalImportKvarh() throws IOException {
		return (MainRegister) getRegister(R_TOTAL_IMPORT_VARH);
	}
	/** 501 Total Registers: Export kvarh */
	MainRegister getTotalExportKvarh() throws IOException {
		return (MainRegister) getRegister(R_TOTAL_EXPORT_VARH);
	}

	/** 501 Total Registers: Total kVAh */
	MainRegister getTotalTotalKVAh() throws IOException {
		return (MainRegister) getRegister(R_TOTAL_VAH);
	}

	/** 502 Time Of Use Registers: TOU 1 */
	MainRegister getTimeOfUse1() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_1);
	}

	/** 502 Time Of Use Registers: TOU 2 */
	MainRegister getTimeOfUse2() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_2);
	}

	/** 502 Time Of Use Registers: TOU 3 */
	MainRegister getTimeOfUse3() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_3);
	}

	/** 502 Time Of Use Registers: TOU 4 */
	MainRegister getTimeOfUse4() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_4);
	}

	/** 502 Time Of Use Registers: TOU 5 */
	MainRegister getTimeOfUse5() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_5);
	}

	/** 502 Time Of Use Registers: TOU 6 */
	MainRegister getTimeOfUse6() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_6);
	}

	/** 502 Time Of Use Registers: TOU 7 */
	MainRegister getTimeOfUse7() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_7);
	}

	/** 502 Time Of Use Registers: TOU 8 */
	MainRegister getTimeOfUse8() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_8);
	}

	/** 503 Maximum Demand 1 */
	MaximumDemand getMaximumDemand1() throws IOException {
		return (MaximumDemand) getRegister(R_MAXIMUM_DEMAND_1);
	}

	/** 503 Maximum Demand 2 */
	MaximumDemand getMaximumDemand2() throws IOException {
		return (MaximumDemand) getRegister(R_MAXIMUM_DEMAND_2);
	}

	/** 503 Maximum Demand 3 */
	MaximumDemand getMaximumDemand3() throws IOException {
		return (MaximumDemand) getRegister(R_MAXIMUM_DEMAND_3);
	}

	/** 503 Maximum Demand 4 */
	MaximumDemand getMaximumDemand4() throws IOException {
		return (MaximumDemand) getRegister(R_MAXIMUM_DEMAND_4);
	}

	/** 504 Cumulative Maximum Demand 1 */
	MainRegister getCumulativeMaximumDemand1() throws IOException {
		return (MainRegister) getRegister(R_CUMULATIVE_MAXIMUM_DEMAND1);
	}

	/** 504 Cumulative Maximum Demand 2 */
	MainRegister getCumulativeMaximumDemand2() throws IOException {
		return (MainRegister) getRegister(R_CUMULATIVE_MAXIMUM_DEMAND2);
	}

	/** 504 Cumulative Maximum Demand 3 */
	MainRegister getCumulativeMaximumDemand3() throws IOException {
		return (MainRegister) getRegister(R_CUMULATIVE_MAXIMUM_DEMAND3);
	}

	/** 504 Cumulative Maximum Demand 4 */
	MainRegister getCumulativeMaximumDemand4() throws IOException {
		return (MainRegister) getRegister(R_CUMULATIVE_MAXIMUM_DEMAND4);
	}

	/**
	 * A DataIndentity can contain multiple registers. But what if a Register is
	 * best composed out of several other registers ?
	 *
	 * I will cache this in RegisterFactory, it could be done better .... 741 -
	 * 745, 751 - 755 Register Allocation
	 */
	public RegisterInformation getRegisterInformation() throws IOException {
		if (registerInformation == null) {
			byte b741 = ((byte[]) getRegister(R_TOU_ALLOC_IMPORT_KWH))[0];
			byte b742 = ((byte[]) getRegister(R_TOU_ALLOC_EXPORT_KWH))[0];
			byte b743 = ((byte[]) getRegister(R_TOU_ALLOC_IMPORT_KVARH))[0];
			byte b744 = ((byte[]) getRegister(R_TOU_ALLOC_EXPORT_KVARH))[0];
			byte b745 = ((byte[]) getRegister(R_TOU_ALLOC_TOTAL_KVAH))[0];

			byte b751 = ((byte[]) getRegister(R_MD_TOU_ALLOC_IMPORT_KWH))[0];
			byte b752 = ((byte[]) getRegister(R_MD_TOU_ALLOC_EXPORT_KWH))[0];
			byte b753 = ((byte[]) getRegister(R_MD_TOU_ALLOC_IMPORT_KVARH))[0];
			byte b754 = ((byte[]) getRegister(R_MD_TOU_ALLOC_EXPORT_KVARH))[0];
			byte b755 = ((byte[]) getRegister(R_MD_TOU_ALLOC_TOTAL_KVAH))[0];

			RegisterInformationParser ap = new RegisterInformationParser();
			ap.set(b741, b742, b743, b744, b745, b751, b752, b753, b754, b755);

			registerInformation = ap.match();

			registerInformation.setScalingFactor(getScalingFactor());
		}
		return registerInformation;
	}

	/** 540 Historical Data */
	HistoricalDataSet getHistoricalData() throws IOException {
		return (HistoricalDataSet) getRegister(R_HISTORICAL_DATA);
	}

	/** 541 Last Billing */
	HistoricalData getLastBilling() throws IOException {
		return (HistoricalData) getRegister( R_LAST_BILLING );
	}

	/** 774 Load Profile Definition */
	public LoadProfileDefinition getLoadProfileDefinition() throws IOException {
		return (LoadProfileDefinition) getRegister(R_LOAD_PROFILE_DEFININTION);
	}

	public MeterExceptionInfo getMeterExceptionInfo() {
		return meterExceptionInfo;
	}

	public void setMeterExceptionInfo(MeterExceptionInfo meterExceptionInfo) {
		this.meterExceptionInfo = meterExceptionInfo;
	}

}
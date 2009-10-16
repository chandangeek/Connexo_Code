package com.energyict.protocolimpl.iec1107.ppmi1;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ppmi1.parser.RegisterInformationParser;
import com.energyict.protocolimpl.iec1107.ppmi1.register.HistoricalDataSet;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileDefinition;
import com.energyict.protocolimpl.iec1107.ppmi1.register.MainRegister;
import com.energyict.protocolimpl.iec1107.ppmi1.register.MaximumDemand;
import com.energyict.protocolimpl.iec1107.ppmi1.register.RegisterInformation;
import com.energyict.protocolimpl.iec1107.ppmi1.register.ScalingFactor;

/**
 * There is 1 register per actual "Meter Register".
 * 
 * But a register can also be composed out of several "Meter Registers"
 * 
 * Or a register may need another meter register to iterprete the value.
 * 
 * @author fbo
 */

public class OpusRegisterFactory implements RegisterFactory {

	static final int MAX_CMD_REGS = 8;
	static final int MAX_MD_REGS = 24;

	private Map registers = new TreeMap();
	private PPM ppm = null;
	DataIdentityFactory dataItendityFactory = null;

	private RegisterInformation registerInformation = null;


	/** Creates a new instance of RegisterFactory */
	public OpusRegisterFactory(PPM ppm, MeterExceptionInfo meterExceptionInfo ) {

		this.ppm = ppm;
		dataItendityFactory = new DataIdentityFactory(ppm);

	}

	{

		this.registers = new TreeMap();

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

	public DataIdentityFactory getDataIdentityFactory() {
		return dataItendityFactory;
	}

	public PPM getPpm() {
		return ppm;
	}

	public Map getRegisters() {
		return registers;
	}

	private void addRegister(Register register) {
		this.registers.put(register.getName(), register);
	}

	private void add(String dataId, String name, int type, int offset, int length, boolean writeable, boolean cached) {
		Register r = new Register(dataId, name, type, offset, length, writeable, cached);
		r.setRegisterFactory(this);
		this.registers.put(name, r);
	}

	private void add(String dataId, String name, int type, int offset, int length, boolean writeable, boolean cached, boolean readable) {
		Register r = new Register(dataId, name, type, offset, length, writeable, cached, readable);
		r.setRegisterFactory(this);
		this.registers.put(name, r);
		System.out.println(
				"Adding register to map: " +
				dataId + " " + name + " length=" + length
		);
	}

	public void setRegister(String name, String value) throws IOException {
		try {
			Register register = findRegister(name);
			if (register.isWriteable()) {
				register.writeRegister(value);
			} else {
				throw new IOException("ABBA1700, setRegister, register not writeable");
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
				throw new IOException("ABBA1700, setRegister, register not writeable");
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
		return register2Retrieve.getValue();
	}

	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		ObisCodeMapper ocm = new ObisCodeMapper(this);
		return ocm.getRegisterValue(obisCode);
	}

	public byte[] getRegisterRawData(String name, int dataLength) throws IOException {
		try {
			Register register = findRegister(name);
			return (register.readRegister(register.isCached(), dataLength, 0));
		} catch (FlagIEC1107ConnectionException e) {
			throw new IOException("ABBA1700, getRegisterRawData, " + e.getMessage());
		}
	}

	// search the map for the register info
	public Register findRegister(String name) throws IOException {
		Register register = (Register) registers.get(name);
		if (register == null) {
			throw new IOException("RegisterFactory, findRegister, " + name + " does not exist!");
		} else {
			return register;
		}
	}

	public RegisterInfo getRegisterInfo(String name) {
		return null;
	}

	/** 861 Time and date via optical port/ 860 Time and date via RS232 */
	public Date getTimeDate() throws IOException {
		if (ppm.isOpus()) {
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
	public String getSchemeId() throws IOException {
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
	public MainRegister getTotalImportKWh() throws IOException {
		return (MainRegister) getRegister(R_TOTAL_IMPORT_WH);
	}

	/** 501 Total Registers: Export kWh */
	public MainRegister getTotalExportKWh() throws IOException {
		return (MainRegister) getRegister(R_TOTAL_EXPORT_WH);
	}

	/** 501 Total Registers: Import kvarh */
	public MainRegister getTotalImportKvarh() throws IOException {
		return (MainRegister) getRegister(R_TOTAL_IMPORT_VARH);
	}
	/** 501 Total Registers: Export kvarh */
	public MainRegister getTotalExportKvarh() throws IOException {
		return (MainRegister) getRegister(R_TOTAL_EXPORT_VARH);
	}

	/** 501 Total Registers: Total kVAh */
	public MainRegister getTotalTotalKVAh() throws IOException {
		return (MainRegister) getRegister(R_TOTAL_VAH);
	}

	/** 502 Time Of Use Registers: TOU 1 */
	public MainRegister getTimeOfUse1() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_1);
	}

	/** 502 Time Of Use Registers: TOU 2 */
	public MainRegister getTimeOfUse2() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_2);
	}

	/** 502 Time Of Use Registers: TOU 3 */
	public MainRegister getTimeOfUse3() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_3);
	}

	/** 502 Time Of Use Registers: TOU 4 */
	public MainRegister getTimeOfUse4() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_4);
	}

	/** 502 Time Of Use Registers: TOU 5 */
	public MainRegister getTimeOfUse5() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_5);
	}

	/** 502 Time Of Use Registers: TOU 6 */
	public MainRegister getTimeOfUse6() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_6);
	}

	/** 502 Time Of Use Registers: TOU 7 */
	public MainRegister getTimeOfUse7() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_7);
	}

	/** 502 Time Of Use Registers: TOU 8 */
	public MainRegister getTimeOfUse8() throws IOException {
		return (MainRegister) getRegister(R_TIME_OF_USE_8);
	}

	/** 503 Maximum Demand 1 */
	public MaximumDemand getMaximumDemand1() throws IOException {
		return (MaximumDemand) getRegister(R_MAXIMUM_DEMAND_1);
	}

	/** 503 Maximum Demand 2 */
	public MaximumDemand getMaximumDemand2() throws IOException {
		return (MaximumDemand) getRegister(R_MAXIMUM_DEMAND_2);
	}

	/** 503 Maximum Demand 3 */
	public MaximumDemand getMaximumDemand3() throws IOException {
		return (MaximumDemand) getRegister(R_MAXIMUM_DEMAND_3);
	}

	/** 503 Maximum Demand 4 */
	public MaximumDemand getMaximumDemand4() throws IOException {
		return (MaximumDemand) getRegister(R_MAXIMUM_DEMAND_4);
	}

	/** 504 Cumulative Maximum Demand 1 */
	public MainRegister getCumulativeMaximumDemand1() throws IOException {
		return (MainRegister) getRegister(R_CUMULATIVE_MAXIMUM_DEMAND1);
	}

	/** 504 Cumulative Maximum Demand 2 */
	public MainRegister getCumulativeMaximumDemand2() throws IOException {
		return (MainRegister) getRegister(R_CUMULATIVE_MAXIMUM_DEMAND2);
	}

	/** 504 Cumulative Maximum Demand 3 */
	public MainRegister getCumulativeMaximumDemand3() throws IOException {
		return (MainRegister) getRegister(R_CUMULATIVE_MAXIMUM_DEMAND3);
	}

	/** 504 Cumulative Maximum Demand 4 */
	public MainRegister getCumulativeMaximumDemand4() throws IOException {
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
	public HistoricalDataSet getHistoricalData() throws IOException {
		return (HistoricalDataSet) getRegister(R_HISTORICAL_DATA);
	}

	/** 774 Load Profile Definition */
	public LoadProfileDefinition getLoadProfileDefinition() throws IOException {
		return (LoadProfileDefinition) getRegister(R_LOAD_PROFILE_DEFININTION);
	}

}
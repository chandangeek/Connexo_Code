package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ppmi1.parser.RegisterInformationParser;
import com.energyict.protocolimpl.iec1107.ppmi1.register.HistoricalDataSet;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileDefinition;
import com.energyict.protocolimpl.iec1107.ppmi1.register.MainRegister;
import com.energyict.protocolimpl.iec1107.ppmi1.register.MaximumDemand;
import com.energyict.protocolimpl.iec1107.ppmi1.register.RegisterInformation;
import com.energyict.protocolimpl.iec1107.ppmi1.register.ScalingFactor;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Abstract RegisterFactory class, containing all the common code of the OPUS and IEC1107 protocol
 *
 * @author jme
 */
public abstract class AbstractRegisterFactory implements RegisterFactory {

	private Map registers = new TreeMap();
	private PPM ppm = null;
	private DataIdentityFactory dataItendityFactory = null;
	private RegisterInformation registerInformation = null;

	public AbstractRegisterFactory(PPM ppm, MeterExceptionInfo meterExceptionInfo) {
		this.ppm = ppm;
		dataItendityFactory = new DataIdentityFactory(ppm);
	}

	public DataIdentityFactory getDataIdentityFactory() {
		return dataItendityFactory;
	}

	public PPM getPpm() {
		return ppm;
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

	public Map getRegisters() {
		return registers;
	}

	protected void addRegister(PPM1Register register) {
		this.registers.put(register.getName(), register);
	}

	protected void add(String dataId, String name, int type, int offset, int length, boolean writeable, boolean cached) {
		PPM1Register r = new PPM1Register(dataId, name, type, offset, length, writeable, cached);
		r.setRegisterFactory(this);
		this.registers.put(name, r);
	}

	protected void add(String dataId, String name, int type, int offset, int length, boolean writeable, boolean cached, boolean readable) {
		PPM1Register r = new PPM1Register(dataId, name, type, offset, length, writeable, cached, readable);
		r.setRegisterFactory(this);
		this.registers.put(name, r);
	}

	// search the map for the register info
	public PPM1Register findRegister(String name) throws IOException {
		PPM1Register register = (PPM1Register) registers.get(name);
		if (register == null) {
			throw new IOException("RegisterFactory, findRegister, " + name + " does not exist!");
		} else {
			return register;
		}
	}

	public void setRegister(String name, String value) throws IOException {
		try {
			PPM1Register register = findRegister(name);
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
			PPM1Register register = findRegister(name);
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
		PPM1Register register2Retrieve = findRegister(name);
		return register2Retrieve.getValue();
	}

	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		ObisCodeMapper ocm = new ObisCodeMapper(this);
		return ocm.getRegisterValue(obisCode);
	}

	public byte[] getRegisterRawData(String name, int dataLength) throws IOException {
		try {
			PPM1Register register = findRegister(name);
			return (register.readRegister(register.isCached(), dataLength, 0));
		} catch (FlagIEC1107ConnectionException e) {
			throw new IOException("ABBA1700, getRegisterRawData, " + e.getMessage());
		}
	}

	public RegisterInfo getRegisterInfo(String name) {
		return null;
	}

	/** 861 Time and date via optical port/ 860 Time and date via RS232 */
	public Date getTimeDate() throws IOException {
		if (getPpm().isOpus()) {
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


	/** 540 Historical Data */
	public HistoricalDataSet getHistoricalData() throws IOException {
		return (HistoricalDataSet) getRegister(R_HISTORICAL_DATA);
	}

	/** 774 Load Profile Definition */
	public LoadProfileDefinition getLoadProfileDefinition() throws IOException {
		return (LoadProfileDefinition) getRegister(R_LOAD_PROFILE_DEFININTION);
	}

}

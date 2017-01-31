/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.ppmi1.MetaRegister;
import com.energyict.protocolimpl.iec1107.ppmi1.PPM;
import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.RegisterFactory;
import com.energyict.protocolimpl.iec1107.ppmi1.register.HistoricalData;
import com.energyict.protocolimpl.iec1107.ppmi1.register.HistoricalDataSet;
import com.energyict.protocolimpl.iec1107.ppmi1.register.MainRegister;
import com.energyict.protocolimpl.iec1107.ppmi1.register.MaximumDemand;
import com.energyict.protocolimpl.iec1107.ppmi1.register.RegisterInformation;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.TimeZone;

/**
 * @author fbo
 *
 */
public class HistoricalDataParser {

	private ByteAssembly bAss = null;

	private HistoricalDataSet historicalDataSet;
	private HistoricalData hd;
	private RegisterFactory rf;
	private TimeZone timeZone;

	/**
	 * @param ppm
	 * @param rf
	 * @throws IOException
	 */
	public HistoricalDataParser(PPM ppm, RegisterFactory rf) throws IOException {
		this.rf = rf;
		this.timeZone = ppm.getTimeZone();
	}

	/**
	 * @param input
	 */
	public void setInput(byte[] input) {
		this.bAss = new ByteAssembly();
		this.bAss.setInput(input);
	}

	/**
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public HistoricalDataSet match() throws NumberFormatException, IOException {
		this.historicalDataSet = new HistoricalDataSet();
		for (int i = 0; i < PPM.NR_HISTORICAL_DATA; i++) {
			this.hd = new HistoricalData();
			matchCumulative();
			matchTimeOfUse();
			matchMaximumDemands();
			matchCumulativeMaximumDemands();
			this.bAss.addToIndex(16);
			this.historicalDataSet.add(this.hd);
		}
		return this.historicalDataSet;
	}

	/**
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	void matchCumulative() throws NumberFormatException, IOException {

		this.hd.setImportKWh(createMR(RegisterFactory.R_TOTAL_IMPORT_WH));
		this.hd.setExportKWh(createMR(RegisterFactory.R_TOTAL_EXPORT_WH));
		this.hd.setImportKvarh(createMR(RegisterFactory.R_TOTAL_IMPORT_VARH ));
		this.bAss.addToIndex(1); // unused byte

		this.hd.setExportKvarh(createMR(RegisterFactory.R_TOTAL_EXPORT_VARH));
		this.hd.setTotalKVAh(createMR(RegisterFactory.R_TOTAL_VAH ));

		int bc = Integer.parseInt(Integer.toHexString(ProtocolUtils.getInt(this.bAss.getInput(), this.bAss.getIndex(), 2)));
		//PPMUtils.parseInteger(bAss.input, bAss.index, 2).intValue();
		this.hd.setBillingCount(bc);
		this.bAss.addToIndex(2); // billing reset counter

		this.hd.setDate(PPMUtils.parseTimeStamp(this.bAss.getInput(), this.bAss.getIndex(), this.timeZone));
		this.bAss.addToIndex(4);
	}

	/**
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	void matchTimeOfUse() throws NumberFormatException, IOException {

		this.hd.setTimeOfUse1(createMR(RegisterFactory.R_TIME_OF_USE_1));
		this.hd.setTimeOfUse2(createMR(RegisterFactory.R_TIME_OF_USE_2));
		this.hd.setTimeOfUse3(createMR(RegisterFactory.R_TIME_OF_USE_3));
		this.bAss.incIndex(); // unused byte

		this.hd.setTimeOfUse4(createMR(RegisterFactory.R_TIME_OF_USE_4));
		this.hd.setTimeOfUse5(createMR(RegisterFactory.R_TIME_OF_USE_5));
		this.hd.setTimeOfUse6(createMR(RegisterFactory.R_TIME_OF_USE_6));
		this.bAss.incIndex();

		this.hd.setTimeOfUse7(createMR(RegisterFactory.R_TIME_OF_USE_7));
		this.hd.setTimeOfUse8(createMR(RegisterFactory.R_TIME_OF_USE_8));
		this.bAss.addToIndex(6); // unused byte

	}

	/**
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public void matchMaximumDemands() throws NumberFormatException, IOException {

		RegisterInformation ri = this.rf.getRegisterInformation();
		BigDecimal scaleFactor = null;
		Unit unit = null;

		//1
		byte[] d = ProtocolUtils.getSubArray2(this.bAss.getInput(), this.bAss.getIndex(), 32);
		//MeterUnit meterUnit = rf.getRegisterInformation().mdTou1.getMeterUnit();

		scaleFactor = ri.getMdTou1().getRegisterScaleFactor();
		unit = ri.getMdTou1().getUnit();

		MaximumDemand md = new MaximumDemand(unit, d, scaleFactor, this.timeZone);
		this.hd.setMaxDemand1(md);
		this.bAss.addToIndex(32);

		//2
		d = ProtocolUtils.getSubArray2(this.bAss.getInput(), this.bAss.getIndex(), 32);
		//meterUnit = rf.getRegisterInformation().mdTou2.getMeterUnit();

		scaleFactor = ri.getMdTou2().getRegisterScaleFactor();
		unit = ri.getMdTou2().getUnit();

		md = new MaximumDemand(unit, d, scaleFactor, this.timeZone);
		this.hd.setMaxDemand2(md);
		this.bAss.addToIndex(32);

		//3
		d = ProtocolUtils.getSubArray2(this.bAss.getInput(), this.bAss.getIndex(), 32);
		//meterUnit = rf.getRegisterInformation().mdTou3.getMeterUnit();

		scaleFactor = ri.getMdTou3().getRegisterScaleFactor();
		unit = ri.getMdTou3().getUnit();

		md = new MaximumDemand(unit, d, scaleFactor, this.timeZone);
		this.hd.setMaxDemand3(md);
		this.bAss.addToIndex(32);

		//4
		d = ProtocolUtils.getSubArray2(this.bAss.getInput(), this.bAss.getIndex(), 32);

		scaleFactor = ri.getMdTou4().getRegisterScaleFactor();
		unit = ri.getMdTou4().getUnit();

		md = new MaximumDemand(unit, d, scaleFactor, this.timeZone);
		this.hd.setMaxDemand4(md);
		this.bAss.addToIndex(32);

	}

	/**
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	void matchCumulativeMaximumDemands() throws NumberFormatException, IOException {
		this.hd.setCumulativeMaxDemand1(createMR(RegisterFactory.R_MAXIMUM_DEMAND_1));
		this.hd.setCumulativeMaxDemand2(createMR(RegisterFactory.R_MAXIMUM_DEMAND_2));
		this.bAss.addToIndex(6);
		this.hd.setCumulativeMaxDemand3(createMR(RegisterFactory.R_MAXIMUM_DEMAND_3));
		this.hd.setCumulativeMaxDemand4(createMR(RegisterFactory.R_MAXIMUM_DEMAND_4));
		this.bAss.addToIndex(6);
	}

	/**
	 * @param RegisterId
	 * @return
	 * @throws IOException
	 */
	private MainRegister createMR(String RegisterId) throws IOException {
		MainRegister m = null;
		MetaRegister metaRegister = this.rf.getRegisterInformation().get(RegisterId);
		Unit unit = null;
		BigDecimal scaleFactor = null;
		if (metaRegister != null) {
			unit = metaRegister.getUnit();
			scaleFactor = metaRegister.getRegisterScaleFactor();
		}
		Quantity q = PPMUtils.parseQuantity(this.bAss.getInput(), this.bAss.getIndex(), 5, scaleFactor, unit);
		m = new MainRegister(metaRegister, q);

		this.bAss.addToIndex(5);
		return m;
	}

	/**
	 * @param u
	 * @return
	 * @throws IOException
	 */
	public Quantity getQuantity(Unit u) throws IOException {
		Quantity q = PPMUtils.parseQuantity(this.bAss.getInput(), this.bAss.getIndex(), 5, this.rf
				.getScalingFactor().getRegisterScaleFactor(), u);
		this.bAss.addToIndex(5);
		return q;
	}

}
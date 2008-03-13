package com.energyict.protocolimpl.iec1107.ppmi1.parser;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.register.HistoricalData;
import com.energyict.protocolimpl.iec1107.ppmi1.register.HistoricalDataSet;
import com.energyict.protocolimpl.iec1107.ppmi1.register.MainRegister;
import com.energyict.protocolimpl.iec1107.ppmi1.register.MaximumDemand;
import com.energyict.protocolimpl.iec1107.ppmi1.register.RegisterInformation;
import com.energyict.protocolimpl.iec1107.ppmi1.MetaRegister;
import com.energyict.protocolimpl.iec1107.ppmi1.PPM;
import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.RegisterFactory;

/** @author fbo */

public class HistoricalDataParser {

	ByteAssembly bAss = null;

	HistoricalDataSet historicalDataSet;
	HistoricalData hd;

	//PPM ppm;// KV 22072005 unused code
	RegisterFactory rf;
	//Logger log;// KV 22072005 unused code

	TimeZone timeZone;

	//RegisterInformation registerInformation;// KV 22072005 unused code

	public HistoricalDataParser(PPM ppm, RegisterFactory rf) throws IOException {
		//log = ppm.getLogger(); // KV 22072005 unused code
		//this.ppm = ppm;// KV 22072005 unused code
		this.rf = rf;
		timeZone = ppm.getTimeZone();

	}

	public void setInput(byte[] input) {
		this.bAss = new ByteAssembly();
		this.bAss.setInput(input);
	}

	public HistoricalDataSet match() throws NumberFormatException, IOException {

		//registerInformation = rf.getRegisterInformation();// KV 22072005 unused code
		historicalDataSet = new HistoricalDataSet();

		for (int i = 0; i < PPM.NR_HISTORICAL_DATA; i++) {

			hd = new HistoricalData();

			matchCumulative();
			matchTimeOfUse();
			matchMaximumDemands();
			matchCumulativeMaximumDemands();
			bAss.index += 16;

			historicalDataSet.add(hd);

		}
                
		return historicalDataSet;
	}

	void matchCumulative() throws NumberFormatException, IOException {

		hd.setImportKWh(createMR(RegisterFactory.R_TOTAL_IMPORT_WH));
		hd.setExportKWh(createMR(RegisterFactory.R_TOTAL_EXPORT_WH));
		hd.setImportKvarh(createMR(RegisterFactory.R_TOTAL_IMPORT_VARH ));
		bAss.index += 1; // unused byte

		hd.setExportKvarh(createMR(RegisterFactory.R_TOTAL_EXPORT_VARH));
		hd.setTotalKVAh(createMR(RegisterFactory.R_TOTAL_VAH ));

		int bc = 
        Integer.parseInt(Integer.toHexString(ProtocolUtils.getInt(bAss.input, bAss.index, 2)));
        //PPMUtils.parseInteger(bAss.input, bAss.index, 2)
		//		.intValue();
		hd.setBillingCount(bc);
		bAss.index += 2; // billing reset counter

		hd.setDate(PPMUtils.parseTimeStamp(bAss.input, bAss.index, timeZone));
		bAss.index += 4;
	}

	void matchTimeOfUse() throws NumberFormatException, IOException {

		hd.setTimeOfUse1(createMR(RegisterFactory.R_TIME_OF_USE_1));
		hd.setTimeOfUse2(createMR(RegisterFactory.R_TIME_OF_USE_2));
		hd.setTimeOfUse3(createMR(RegisterFactory.R_TIME_OF_USE_3));
		bAss.index += 1; // unused byte

		hd.setTimeOfUse4(createMR(RegisterFactory.R_TIME_OF_USE_4));
		hd.setTimeOfUse5(createMR(RegisterFactory.R_TIME_OF_USE_5));
		hd.setTimeOfUse6(createMR(RegisterFactory.R_TIME_OF_USE_6));
		bAss.index += 1; // unused byte

		hd.setTimeOfUse7(createMR(RegisterFactory.R_TIME_OF_USE_7));
		hd.setTimeOfUse8(createMR(RegisterFactory.R_TIME_OF_USE_8));
		bAss.index += 6; // unused byte

	}

	public void matchMaximumDemands() throws NumberFormatException, IOException {

		RegisterInformation ri = rf.getRegisterInformation();
		BigDecimal scaleFactor = null;
		Unit unit = null;

		//1
		byte[] d = ProtocolUtils.getSubArray2(bAss.input, bAss.index, 32);
		//MeterUnit meterUnit = rf.getRegisterInformation().mdTou1.getMeterUnit();

		scaleFactor = ri.mdTou1.getRegisterScaleFactor();
		unit = ri.mdTou1.getUnit();

		MaximumDemand md = new MaximumDemand(unit, d, scaleFactor, timeZone);
		hd.setMaxDemand1(md);
		bAss.index += 32;

		//2
		d = ProtocolUtils.getSubArray2(bAss.input, bAss.index, 32);
		//meterUnit = rf.getRegisterInformation().mdTou2.getMeterUnit();

		scaleFactor = ri.mdTou2.getRegisterScaleFactor();
		unit = ri.mdTou2.getUnit();

		md = new MaximumDemand(unit, d, scaleFactor, timeZone);
		hd.setMaxDemand2(md);
		bAss.index += 32;

		//3
		d = ProtocolUtils.getSubArray2(bAss.input, bAss.index, 32);
		//meterUnit = rf.getRegisterInformation().mdTou3.getMeterUnit();

		scaleFactor = ri.mdTou3.getRegisterScaleFactor();
		unit = ri.mdTou3.getUnit();

		md = new MaximumDemand(unit, d, scaleFactor, timeZone);
		hd.setMaxDemand3(md);
		bAss.index += 32;

		//4
		d = ProtocolUtils.getSubArray2(bAss.input, bAss.index, 32);

		scaleFactor = ri.mdTou4.getRegisterScaleFactor();
		unit = ri.mdTou4.getUnit();

		md = new MaximumDemand(unit, d, scaleFactor, timeZone);
		hd.setMaxDemand4(md);
		bAss.index += 32;

	}

	void matchCumulativeMaximumDemands() throws NumberFormatException,
			IOException {

		hd.setCumulativeMaxDemand1(createMR(RegisterFactory.R_MAXIMUM_DEMAND_1));
		hd.setCumulativeMaxDemand2(createMR(RegisterFactory.R_MAXIMUM_DEMAND_2));
		bAss.index += 6;

		hd.setCumulativeMaxDemand3(createMR(RegisterFactory.R_MAXIMUM_DEMAND_3));
		hd.setCumulativeMaxDemand4(createMR(RegisterFactory.R_MAXIMUM_DEMAND_4));
		bAss.index += 6;

	}

	private MainRegister createMR(String RegisterId) throws IOException {
		MainRegister m = null;

		MetaRegister metaRegister = rf.getRegisterInformation().get(RegisterId);
		Unit unit = null;
		BigDecimal scaleFactor = null;
		if (metaRegister != null) {
			unit = metaRegister.getUnit();
			scaleFactor = metaRegister.getRegisterScaleFactor();
		}
		Quantity q = PPMUtils.parseQuantity(bAss.input, bAss.index, 5,
				scaleFactor, unit);
		m = new MainRegister(metaRegister, q);

		bAss.index += 5;
		return m;
	}

	public Quantity getQuantity(Unit u) throws IOException {
		Quantity q = PPMUtils.parseQuantity(bAss.input, bAss.index, 5, rf
				.getScalingFactor().getRegisterScaleFactor(), u);
		bAss.index += 5;
		return q;
	}

}
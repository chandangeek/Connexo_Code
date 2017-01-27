package com.energyict.protocolimpl.cm10;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

public class StatusTable {
	
	private CM10 cm10Protocol;
	
	public StatusTable(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}
	
	private int totals;
	private int demCnt;
	private int demLyr;
	private int demTyr;
	private int ramTop;
	private int sc1;
	private int sc2;
	private int ot;
	private int nt;
	private int tCnt;
	private int tnsCnt;
	private int om;
	private int nm;
	private int mCnt;
	private int mnsCnt;
	private int mtrs; // number of channels
	private int batLow;
	
	public int getNumberOfChannels() {
		return mtrs;
	}
	
	public void parse(byte[] data) throws IOException {
		//skip first 6 bytes (current time)
		totals = data[6];
		demCnt = ProtocolUtils.getIntLE(data, 7, 2);
		demLyr = ProtocolUtils.getIntLE(data, 9, 2);
		demTyr = ProtocolUtils.getIntLE(data, 11, 2);
		ramTop = ProtocolUtils.getIntLE(data, 13, 2);
		sc1 = ProtocolUtils.getIntLE(data, 15, 2);
		sc2 = ProtocolUtils.getIntLE(data, 17, 2);
		ot = ProtocolUtils.getIntLE(data, 19, 2);
		nt = ProtocolUtils.getIntLE(data, 21, 2);
		tCnt = ProtocolUtils.getIntLE(data, 23, 2);
		tnsCnt = ProtocolUtils.getIntLE(data, 25, 2);
		om = ProtocolUtils.getIntLE(data, 27, 2);
		nm = ProtocolUtils.getIntLE(data, 29, 2);
		mCnt = ProtocolUtils.getIntLE(data, 31, 2);
		mnsCnt = ProtocolUtils.getIntLE(data, 33, 2);
		mtrs = data[35];
		batLow = data[data.length - 12];
		//cm10Protocol.getLogger().info("bat low: " + batLow);
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer("");
		buf.append("demCnt: " + demCnt).append("\n");
		buf.append("demLyr: " + demLyr).append("\n");
		buf.append("demTyr: " + demTyr).append("\n");
		buf.append("ramTop: " + ramTop).append("\n");
		buf.append("demCnt: " + demCnt).append("\n");
		buf.append("sc1: " + sc1).append("\n");
		buf.append("sc2: " + sc2).append("\n");
		buf.append("ot: " + ot).append("\n");
		buf.append("nt: " + nt).append("\n");
		buf.append("tCnt: " + tCnt).append("\n");
		buf.append("tnsCnt: " + tnsCnt).append("\n");
		buf.append("om: " + om).append("\n");
		buf.append("nm: " + nm).append("\n");
		buf.append("mCnt: " + mCnt).append("\n");
		buf.append("mnsCnt: " + mnsCnt).append("\n");
		buf.append("mtrs: " + mtrs).append("\n");
		return buf.toString();
	}
	
	public boolean isBatteryLow() {
		return batLow > 0;
	}
	
	public int getMostHistoricDemandCount() {
		return om;
	}
	
	public int getMostRecentDemandCount() {
		return nm;
	}
	
	public int getNumberOfMeterDemandsStored() {
		return mCnt;
	}

	public int getNumberOfDemandPeriodsPreviousYear() {
		return demTyr;
	}
	
}

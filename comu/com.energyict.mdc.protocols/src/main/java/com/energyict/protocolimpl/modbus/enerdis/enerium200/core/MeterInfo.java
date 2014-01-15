package com.energyict.protocolimpl.modbus.enerdis.enerium200.core;

import com.energyict.protocols.util.ProtocolUtils;

import java.util.Date;

public class MeterInfo {

	private static final int DEBUG = 0;

	private String serialNumber 	= null;
	private String version			= null;
	private Date time				= null;
	private Date readTime			= null;
	private byte[] rawData			= null;

	/*
	 * Constructors
	 */

	public MeterInfo(String serialNumber, Date time, String version, byte[] rawData) {
		this.serialNumber = serialNumber;
		this.time = time;
		this.version = version;
		this.rawData = rawData;
		this.readTime = new Date();
	}

	public MeterInfo(String serialNumber, Date time, String version) {
		this.serialNumber = serialNumber;
		this.time = time;
		this.version = version;
	}

	/*
	 * Public methods
	 */

	public String toString() {
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("MeterInfo:\n");
		strBuff.append("   serialNumber="+getSerialNumber()+"\n");
		strBuff.append("   time="+getMeterTime()+"\n");
		strBuff.append("   version="+getVersion()+"\n");
		strBuff.append("   rawData="+ProtocolUtils.getResponseData(rawData));
		strBuff.append("\n");
		return strBuff.toString();
	}

	public void printInfo() {
		System.out.println(this.toString());
	}

	/*
	 * Public getters and setters
	 */

	public String getSerialNumber() {
		return serialNumber;
	}
	public String getVersion() {
		return version;
	}
	public Date getMeterTime() {
		return time;
	}
	public Date getReadTime() {
		return readTime;
	}
	public byte[] getRawData() {
		return rawData;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public void setTime(Date time) {
		this.time = time;
	}

	public void setRawData(byte[] rawData) {
		this.rawData = rawData;
	}
}

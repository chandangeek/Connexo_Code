package com.energyict.protocolimpl.CM32;

import java.util.Date;

import com.energyict.protocol.ProtocolUtils;

public class HeaderField {

	private String sourceType;
	private String sourceversion;
	private String sourceIdentity;
	private Date timeSent;
	private int configStart;
	private int configSize;
	private String dataType;
	private Date dataStartDate;
	private int numberOfDays;
	private int dataRecordSize;
	private int dataFieldNumberOfRecords;
	private int dataStart;
	private int dataSize;
	private int interval;
	private String configVersion;

	public void HeaderField() {}
	
	public void parse(byte[] header) {
		parseSourceType(ProtocolUtils.getSubArray(header, 0, 15));
		parseSourceVersion(ProtocolUtils.getSubArray(header, 15, 31));
		parseSourceIdetity(ProtocolUtils.getSubArray(header, 31, 47));
		parseTimeSent(ProtocolUtils.getSubArray(header, 47, 79));
		parseConfigStart(ProtocolUtils.getSubArray(header, 79, 95));
		parseConfigSize(ProtocolUtils.getSubArray(header, 95, 111));
		parseDataType(ProtocolUtils.getSubArray(header, 111, 127));
		parseDataStartDate(ProtocolUtils.getSubArray(header, 127, 143));
		parseNumberOfDays(ProtocolUtils.getSubArray(header, 143, 159));
		parseDataRecordSize(ProtocolUtils.getSubArray(header, 159, 175));
		parseNumberOfRecords(ProtocolUtils.getSubArray(header, 175, 191));
		parseDataStart(ProtocolUtils.getSubArray(header, 191, 207));
		parseDataSize(ProtocolUtils.getSubArray(header, 207, 223));
		parseInterval(ProtocolUtils.getSubArray(header, 223, 239));
		parseConfigVersion(ProtocolUtils.getSubArray(header, 239, 255));
	}
	
	public String toString() {
		return 
			"SrcType=" + this.sourceType + "\n" +
			"SrcVer=" + this.sourceversion + "\n" +
			"Src ID=" + this.sourceIdentity + "\n" +
			"SentTime=" + this.timeSent + "\n" +
			"ConfigStart=" + this.configStart + "\n" +
			"ConfigSize=" + this.configSize + "\n" +
			"DataType=" + this.dataType + "\n" +
			"StartDate=" + this.dataStartDate + "\n" +
			"No of Days=" + this.numberOfDays + "\n" +
			"RecSize=" + this.dataRecordSize + "\n" +
			"NumRec=" + this.dataFieldNumberOfRecords + "\n" +
			"DataStart=" + this.dataStart + "\n" +
			"dataSize=" + this.dataSize + "\n" +
			"Interval=" + this.interval + "\n" +
			"ConfigVersion=" + this.configVersion + "\n";
			
	}
	
	public static void main(String[] args) {
		HeaderField header = new HeaderField();
		header.parse(exampleHeader);
		System.out.println(header.toString());
	}
	
	protected String parseValues(byte[] data) {
		StringBuffer buf = new StringBuffer("");
		for (int i = 7; i < 16; i++) {
			char kar = (char) data[i];
			if ((int) kar != 0)
				buf.append(kar);
			else
				return buf.toString();
		}
		return buf.toString();
	}

	protected void parseSourceType(byte[] data) {
		this.sourceType = parseValues(data);
	}
	
	protected void parseSourceVersion(byte[] data) {
		this.sourceversion = parseValues(data);
	}
	
	protected void parseSourceIdetity(byte[] data) {
		this.sourceIdentity = parseValues(data);
	}
	
	protected void parseTimeSent(byte[] data) {
		
	}
	
	protected void parseConfigStart(byte[] data) {
		
	}
	
	protected void parseConfigSize(byte[] data) {
		
	}
	
	protected void parseDataType(byte[] data) {
		
	}
	
	protected void parseDataStartDate(byte[] data) {
		
	}
	
	protected void parseNumberOfDays(byte[] data) {
		
	}
	
	protected void parseDataRecordSize(byte[] data) {
		
	}
	
	protected void parseNumberOfRecords(byte[] data) {
		
	}

	protected void parseDataStart(byte[] data) {
	
	}

	protected void parseDataSize(byte[] data) {
	
	}

	protected void parseInterval(byte[] data) {
	
	}

	protected void parseConfigVersion(byte[] data) {
	
	}
	
	public String getSourceType() {
		return sourceType;
	}

	protected void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getSourceversion() {
		return sourceversion;
	}

	protected void setSourceversion(String sourceversion) {
		this.sourceversion = sourceversion;
	}

	public String getSourceIdentity() {
		return sourceIdentity;
	}

	protected void setSourceIdentity(String sourceIdentity) {
		this.sourceIdentity = sourceIdentity;
	}

	public Date getTimeSent() {
		return timeSent;
	}

	protected void setTimeSent(Date timeSent) {
		this.timeSent = timeSent;
	}

	public int getConfigStart() {
		return configStart;
	}

	protected void setConfigStart(int configStart) {
		this.configStart = configStart;
	}

	public int getConfigSize() {
		return configSize;
	}

	protected void setConfigSize(int configSize) {
		this.configSize = configSize;
	}

	public String getDataType() {
		return dataType;
	}

	protected void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Date getDataStartDate() {
		return dataStartDate;
	}

	protected void setDataStartDate(Date dataStartDate) {
		this.dataStartDate = dataStartDate;
	}

	public int getNumberOfDays() {
		return numberOfDays;
	}

	protected void setNumberOfDays(int numberOfDays) {
		this.numberOfDays = numberOfDays;
	}

	public int getDataRecordSize() {
		return dataRecordSize;
	}

	protected void setDataRecordSize(int dataRecordSize) {
		this.dataRecordSize = dataRecordSize;
	}

	public int getDataFieldNumberOfRecords() {
		return dataFieldNumberOfRecords;
	}

	protected void setDataFieldNumberOfRecords(int dataFieldNumberOfRecords) {
		this.dataFieldNumberOfRecords = dataFieldNumberOfRecords;
	}

	public int getDataStart() {
		return dataStart;
	}

	protected void setDataStart(int dataStart) {
		this.dataStart = dataStart;
	}

	public int getDataSize() {
		return dataSize;
	}

	protected void setDataSize(int dataSize) {
		this.dataSize = dataSize;
	}

	public int getInterval() {
		return interval;
	}

	protected void setInterval(int interval) {
		this.interval = interval;
	}

	public String getConfigVersion() {
		return configVersion;
	}

	protected void setConfigVersion(String configVersion) {
		this.configVersion = configVersion;
	}
	
	static byte[] exampleHeader = {
	0x53, 0x72, 0x63, 0x54, 0x79, 0x70, 0x3D, 0x43, 0x4D, 0x2D, 0x33, 0x32, 0x00, 0x00, 0x00, 0x00,
	0x53, 0x72, 0x63, 0x56, 0x65, 0x72, 0x3D, 0x31, 0x30, 0x31, 0x33, 0x31, 0x5F, 0x34, 0x2E, 0x35,	
	0x53, 0x72, 0x63, 0x20, 0x49, 0x44, 0x3D, 0x30, 0x30, 0x33, 0x36, 0x30, 0x00, 0x00, 0x00, 0x00,
	0x54, 0x58, 0x20, 0x54, 0x69, 0x6D, 0x3D, 0x31, 0x35, 0x3A, 0x32, 0x38, 0x3A, 0x33, 0x37, 0x00,
	0x54, 0x58, 0x20, 0x44, 0x61, 0x74, 0x3D, 0x30, 0x38, 0x2F, 0x31, 0x31, 0x2F, 0x30, 0x36, 0x00,
	0x43, 0x68, 0x53, 0x65, 0x65, 0x6B, 0x3D, 0x32, 0x35, 0x36, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x43, 0x68, 0x53, 0x69, 0x7A, 0x65, 0x3D, 0x31, 0x35, 0x33, 0x36, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x44, 0x61, 0x74, 0x61, 0x20, 0x20, 0x3D, 0x44, 0x65, 0x6D, 0x61, 0x6E, 0x64, 0x73, 0x00, 0x00,
	0x53, 0x74, 0x44, 0x61, 0x74, 0x65, 0x3D, 0x30, 0x31, 0x2F, 0x31, 0x31, 0x2F, 0x30, 0x36, 0x00,
	0x4E, 0x6F, 0x44, 0x61, 0x79, 0x73, 0x3D, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x52, 0x65, 0x63, 0x53, 0x69, 0x7A, 0x3D, 0x36, 0x34, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x4E, 0x75, 0x6D, 0x52, 0x65, 0x63, 0x3D, 0x39, 0x36, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x44, 0x20, 0x53, 0x65, 0x65, 0x6B, 0x3D, 0x31, 0x37, 0x39, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x44, 0x20, 0x53, 0x69, 0x7A, 0x65, 0x3D, 0x36, 0x31, 0x34, 0x34, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x44, 0x20, 0x4D, 0x69, 0x6E, 0x73, 0x3D, 0x33, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	0x43, 0x66, 0x67, 0x56, 0x65, 0x72, 0x3D, 0x33, 0x34, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	

}

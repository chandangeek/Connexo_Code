/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * FileAccessRead.java
 *
 * Created on 31 maart 2006, 15:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.command;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author koen
 */
public class FileAccessReadCommand extends AbstractCommand {

	private int SurveyLog;
	private int Options;
	private long StartRecord;
	private int NumberOfRecords;
	private byte[] data;


	/** Creates a new instance of FileAccessRead */
	public FileAccessReadCommand(CommandFactory commandFactory) {
		super(commandFactory);
	}

	protected byte[] prepareBuild() {
		byte[] data = new byte[9];

		data[0] = 'F';
		data[1] = (byte)getSurveyLog();
		data[2] = (byte)getOptions();

		data[3] = (byte)((getStartRecord()>>24)&0xFF);
		data[4] = (byte)((getStartRecord()>>16)&0xFF);
		data[5] = (byte)((getStartRecord()>>8)&0xFF);
		data[6] = (byte)((getStartRecord())&0xFF);

		data[7] = (byte)((getNumberOfRecords()>>8)&0xFF);
		data[8] = (byte)((getNumberOfRecords())&0xFF);

		return data;
	}

	public String toString() {
		// Generated code by ToStringBuilder
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("FileAccessRead:\n");
		strBuff.append("   startRecord="+getStartRecord()+"\n");
		strBuff.append("   numberOfRecords="+getNumberOfRecords()+"\n");
		strBuff.append("   data="+ProtocolUtils.outputHexString(getData())+"\n");
		strBuff.append("   data (string)="+new String(getData())+", ");
		return strBuff.toString();
	}

	protected void parse(byte[] data) throws IOException {
		int offset = 3;
		setStartRecord(ProtocolUtils.getInt(data,offset,4));
		offset+=4;
		setNumberOfRecords(ProtocolUtils.getInt(data,offset,2));
		offset+=2;
		setData(ProtocolUtils.getSubArray(data,offset));
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getSurveyLog() {
		return SurveyLog;
	}

	public void setSurveyLog(int surveyLog) {
		SurveyLog = surveyLog;
	}

	public int getOptions() {
		return Options;
	}

	public void setOptions(int options) {
		Options = options;
	}

	public long getStartRecord() {
		return StartRecord;
	}

	public void setStartRecord(long startRecord) {
		StartRecord = startRecord;
	}

	public int getNumberOfRecords() {
		return NumberOfRecords;
	}

	public void setNumberOfRecords(int numberOfRecords) {
		NumberOfRecords = numberOfRecords;
	}

}

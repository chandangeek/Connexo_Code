package com.energyict.protocolimpl.edmi.common.command;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Arrays;

/**
 *
 * @author koen
 */
public class Atlas1FileAccessReadCommand extends AbstractCommand {

	private static final char FILE_ACCESS_READ_COMMAND = 'F';
    private static int ASCII_ZERO = 0x30;
    private static final int HEADER_SIZE = 9;
    private static final int START_RECORD_SIZE = 4;
    private static final int NUMBER_OF_RECORDS_SIZE = 2;

	private int surveyLog;
	private int options;
	private long startRecord;
	private int numberOfRecords;
	private byte[] data;


	/** Creates a new instance of FileAccessRead */
	public Atlas1FileAccessReadCommand(CommandFactory commandFactory) {
		super(commandFactory);
	}

	protected byte[] prepareBuild() {
		byte[] data = new byte[9];

		data[0] = FILE_ACCESS_READ_COMMAND;
		data[1] = (byte)  (ASCII_ZERO + getSurveyLog());
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
		StringBuilder strBuff = new StringBuilder();
		strBuff.append("FileAccessRead:\n");
		strBuff.append("   startRecord="+getStartRecord()+"\n");
		strBuff.append("   numberOfRecords="+getNumberOfRecords()+"\n");
		strBuff.append("   data="+ProtocolUtils.outputHexString(getData())+"\n");
		strBuff.append("   data (string)="+new String(getData())+", ");
		return strBuff.toString();
	}

	protected void parse(byte[] data) throws CommandResponseException {
		if (data.length < HEADER_SIZE) {
			throw new CommandResponseException("Response for File access read command should have a minimum length of 9; actual size was " + data.length);
		}

		int offset = 3;
		setStartRecord(ProtocolTools.getIntFromBytes(data, offset, START_RECORD_SIZE));
		offset += START_RECORD_SIZE;
		setNumberOfRecords(ProtocolTools.getIntFromBytes(data, offset, NUMBER_OF_RECORDS_SIZE));
		offset += NUMBER_OF_RECORDS_SIZE;

		setData(ProtocolUtils.getSubArray(data, offset, calculateEndOffset(data)));
	}

	private int calculateEndOffset(byte[] data) {
		// sometimes the devices sends a double frame, in this case the same frame is repeated
		if (data.length > (HEADER_SIZE * 2)) {
			byte[] headerBytes = ProtocolUtils.getSubArray(data, 0, HEADER_SIZE);
			for (int i = HEADER_SIZE; i < data.length - HEADER_SIZE; i++) {
				byte[] bytesToTest = ProtocolUtils.getSubArray(data, i, i + HEADER_SIZE);
				if (Arrays.equals(headerBytes, bytesToTest)) {
					// also strip the CRC of first frame and leading byte of the second frame
					return i - 4;
				}
			}
		}
		return data.length - 1;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getSurveyLog() {
		return surveyLog;
	}

	public void setSurveyLog(int surveyLog) {
		this.surveyLog = surveyLog;
	}

	public int getOptions() {
		return options;
	}

	public void setOptions(int options) {
		this.options = options;
	}

	public long getStartRecord() {
		return startRecord;
	}

	public void setStartRecord(long startRecord) {
		this.startRecord = startRecord;
	}

	public int getNumberOfRecords() {
		return numberOfRecords;
	}

	public void setNumberOfRecords(int numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}
}
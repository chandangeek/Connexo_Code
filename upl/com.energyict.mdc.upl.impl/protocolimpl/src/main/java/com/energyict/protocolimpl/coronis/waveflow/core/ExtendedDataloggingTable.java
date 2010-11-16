package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.*;
import java.util.*;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

public class ExtendedDataloggingTable extends AbstractRadioCommand {

	// ********************************************************************************************
	// Request specific parameters
	
	/**
	 * bit0: input A
	 * bit1: input B
	 * bit2: input C
	 * bit3: input D
	 */
	int inputMask=0x0F;
	
	/**
	 * nr of logged values to expect
	 */
	int nrOfValues=1;
	/**
	 * starting at most recent value - offsetFromMostRecentValue
	 */
	int offsetFromMostRecentValue=0;

	final void setInputMask(int inputMask) {
		this.inputMask = inputMask;
	}

	final void setNrOfValues(int nrOfValues) {
		this.nrOfValues = nrOfValues;
	}

	final void setOffsetFromMostRecentValue(int offsetFromMostRecentValue) {
		this.offsetFromMostRecentValue = offsetFromMostRecentValue;
	}
	
	
	// ********************************************************************************************
	// Datalogging parameters, page 15 in the Waveflow 100mw Applicative specifications document
	/**
	 * Reading Sampling Period 
	 */
	int samplingPeriod; // 1 byte
	
	/**
	 * Sampling activation type 
	 */
	int SamplingActivationType; // 1 byte
	
	/**
	 * Measurement Period (datalogging in time steps) expressed	in multiple of "Reading Sampling Period"
	 */
	int measurementPeriod; // 1 byte
	
	/**
	 * Day of the week, or of the month (datalogging)
	 */
	int dayOfTheWeek; // 1 byte
	
	/**
	 * Hour of measurement (datalogging once a week, or once a month)
	 */
	int hourOfManagement; // 1 byte
	
	/**
	 * number of records in the datalogging table (all ports records cumulated) 
	 */
	int nrOfRecordsInDatalogging; // 2 bytes
	
	final int getSamplingPeriod() {
		return samplingPeriod;
	}

	final int getSamplingActivationType() {
		return SamplingActivationType;
	}

	final int getMeasurementPeriod() {
		return measurementPeriod;
	}

	final int getDayOfTheWeek() {
		return dayOfTheWeek;
	}

	final int getHourOfManagement() {
		return hourOfManagement;
	}

	final int getNrOfRecordsInDatalogging() {
		return nrOfRecordsInDatalogging;
	}

	// ********************************************************************************************
	// Datalogging table reading page 23

	/**
	 * Timestamp of the last logging
	 */
	private Date lastLoggingRTC;

	private int frameCounter=0;

	private int nrOfReadingsPortA=0;
	private int nrOfReadingsPortB=0;
	private int readingPortAIndex=0;
	private int readingPortBIndex=0;

	private long[] encoderReadingsInputA;
	private long[] encoderReadingsInputB;
	
	
	
	final public Date getLastLoggingRTC() {
		return lastLoggingRTC;
	}

	final public int getNrOfReadingsPortA() {
		return nrOfReadingsPortA;
	}

	final public int getNrOfReadingsPortB() {
		return nrOfReadingsPortB;
	}

	final public long[] getEncoderReadingsPortA() {
		return encoderReadingsInputA;
	}

	final public long[] getEncoderReadingsPortB() {
		return encoderReadingsInputB;
	}

	ExtendedDataloggingTable(final WaveFlow waveFlow) {
		super(waveFlow);
	}
	
	ExtendedDataloggingTable(final WaveFlow waveFlow, final boolean inputA, final boolean inputB, final boolean inputC, final boolean inputD, final int nrOfValues, final int offsetFromMostRecentValue) {
		super(waveFlow);
		this.nrOfValues=nrOfValues;
		this.offsetFromMostRecentValue=offsetFromMostRecentValue;
		inputMask=0x00;
		if (inputA) inputMask |= 0x01;
		if (inputB) inputMask |= 0x02;
		if (inputC) inputMask |= 0x04;
		if (inputD) inputMask |= 0x08;
		
	}

	public String toString() {
		
		StringBuilder strBuilder = new StringBuilder();
		
		strBuilder.append("DataloggingTable (datalogging parameters):\n");
		
		strBuilder.append("samplingPeriod: "+WaveflowProtocolUtils.toHexString(samplingPeriod)+"\n");
		strBuilder.append("SamplingActivationType: "+WaveflowProtocolUtils.toHexString(SamplingActivationType)+"\n");
		strBuilder.append("measurementPeriod: "+WaveflowProtocolUtils.toHexString(measurementPeriod)+"\n");
		strBuilder.append("dayOfTheWeek: "+WaveflowProtocolUtils.toHexString(dayOfTheWeek)+"\n");
		strBuilder.append("dayOfTheWeek: "+WaveflowProtocolUtils.toHexString(hourOfManagement)+"\n");
		strBuilder.append("nrOfRecordsInDatalogging: "+nrOfRecordsInDatalogging+"\n");
		
		strBuilder.append("EncoderDataloggingTable (datalogging data [portmask "+WaveflowProtocolUtils.toHexString(inputMask)+"], [nr of values "+nrOfValues+"], [offset "+offsetFromMostRecentValue+"]\n");
		
		strBuilder.append("lastLoggingRTC: "+lastLoggingRTC+"\n");
		
		if (nrOfReadingsPortA > 0) {
			strBuilder.append("nrOfReadingsPortA: "+nrOfReadingsPortA+"\n");
			for(int i=0;i<encoderReadingsInputA.length;i++) {
				strBuilder.append("encoderReadingsPortA["+i+"]: "+encoderReadingsInputA[i]+"\n");
			}
		}
		else {
			strBuilder.append("No readings for PortA\n");
		}
		
		if (nrOfReadingsPortB > 0) {
			strBuilder.append("nrOfReadingsPortB: "+nrOfReadingsPortB+"\n");
			for(int i=0;i<encoderReadingsInputB.length;i++) {
				strBuilder.append("encoderReadingsPortB["+i+"]: "+WaveflowProtocolUtils.toHexString(encoderReadingsInputB[i])+"\n");
			}
		}
		else {
			strBuilder.append("No readings for PortB\n");
		}
		
		
		return strBuilder.toString();
	}
	
	
	@Override
	RadioCommandId getRadioCommandId() {
		return RadioCommandId.ExtendedDataloggingTable;
	}

	@Override
	void parse(byte[] data) throws IOException {
		
		System.out.println("KV_DEBUG> "+ProtocolUtils.outputHexString(data));
		
		if (WaveflowProtocolUtils.toInt(data[0]) == 0xFF) {
			throw new WaveFlowException("Error requesting load profile, returned [FF]");
		}
		
		DataInputStream dais = null;
		try {
			
			dais = new DataInputStream(new ByteArrayInputStream(data));
			List<Long> encoderReadingsInputAList = new ArrayList<Long>();
			List<Long> encoderReadingsInputBList = new ArrayList<Long>();
			List<Long> encoderReadingsInputCList = new ArrayList<Long>();
			List<Long> encoderReadingsInputDList = new ArrayList<Long>();
			do {
				if (frameCounter==0) {
					
					
					
					// read the datalogging parameters
					samplingPeriod = WaveflowProtocolUtils.toInt(dais.readByte());
					SamplingActivationType = WaveflowProtocolUtils.toInt(dais.readByte());
					measurementPeriod = WaveflowProtocolUtils.toInt(dais.readByte());
					dayOfTheWeek = WaveflowProtocolUtils.toInt(dais.readByte());
					hourOfManagement = WaveflowProtocolUtils.toInt(dais.readByte());
					nrOfRecordsInDatalogging = WaveflowProtocolUtils.toInt(dais.readShort());		
					
					// read the datalogging
					byte[] temp = new byte[7];
					dais.read(temp);
					lastLoggingRTC = TimeDateRTCParser.parse(temp, getWaveFlow().getTimeZone()).getTime();
				}
				else {
					// in case of a multiple frame, the first byte of the following data is the commmandId acknowledge
					int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
					if (commandIdAck != (0x80 | getRadioCommandId().getCommandId())) {
						throw new WaveFlowException("Invalid response tag ["+WaveflowProtocolUtils.toHexString(commandIdAck)+"]");
					}
				}
				
				frameCounter = WaveflowProtocolUtils.toInt(dais.readByte());
				int nrOfReadingsPortA = WaveflowProtocolUtils.toInt(dais.readByte());
				int nrOfReadingsPortB = WaveflowProtocolUtils.toInt(dais.readByte());
				dais.readShort(); // skip 2 bytes unused 0x0000 value
				
				for (int i=0;i<nrOfReadingsPortA;i++) {
					encoderReadingsInputAList.add((long)dais.readInt() & 0xFFFFFFFF);
				}
				
				for (int i=0;i<nrOfReadingsPortB;i++) {
					encoderReadingsInputAList.add((long)dais.readInt() & 0xFFFFFFFF);
				}
				
				this.nrOfReadingsPortA += nrOfReadingsPortA;
				this.nrOfReadingsPortB += nrOfReadingsPortB;
			
			} while(frameCounter>1);
			
			encoderReadingsInputA = new long[encoderReadingsInputAList.size()];
			for (int index=0;index<encoderReadingsInputAList.size();index++) {
				encoderReadingsInputA[index]=encoderReadingsInputAList.get(index);
			}
			
			encoderReadingsInputB = new long[encoderReadingsInputBList.size()];
			for (int index=0;index<encoderReadingsInputBList.size();index++) {
				encoderReadingsInputB[index]=encoderReadingsInputBList.get(index);
			}
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					getWaveFlow().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}			
	}

	@Override
	byte[] prepare() throws IOException {
		ByteArrayOutputStream baos = null;
		try {	
			baos = new ByteArrayOutputStream();
			DataOutputStream daos = new DataOutputStream(baos);
			daos.writeByte(inputMask);
			daos.writeShort(nrOfValues);
			daos.writeShort(offsetFromMostRecentValue);
			return baos.toByteArray();
		}
		finally {
			if (baos != null) {
				try {
					baos.close();
				}
				catch(IOException e) {
					getWaveFlow().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}
	}
}

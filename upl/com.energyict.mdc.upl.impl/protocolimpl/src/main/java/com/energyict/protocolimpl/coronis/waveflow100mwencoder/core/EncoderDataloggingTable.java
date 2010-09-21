package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;
import java.util.*;

public class EncoderDataloggingTable extends AbstractRadioCommand {

	// ********************************************************************************************
	// Request specific parameters
	
	/**
	 * bit0: read port A
	 * bit1: read port B
	 */
	int portMask=0x03;
	/**
	 * nr of logged values to expect
	 */
	int nrOfValues=1;
	/**
	 * starting at most recent value - offsetFromMostRecentValue
	 */
	int offsetFromMostRecentValue=0;

	final void setPortMask(int portMask) {
		this.portMask = portMask;
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

	private long[] encoderReadingsPortA;
	private long[] encoderReadingsPortB;
	
	
	
	final Date getLastLoggingRTC() {
		return lastLoggingRTC;
	}

	final int getNrOfReadingsPortA() {
		return nrOfReadingsPortA;
	}

	final int getNrOfReadingsPortB() {
		return nrOfReadingsPortB;
	}

	final long[] getEncoderReadingsPortA() {
		return encoderReadingsPortA;
	}

	final long[] getEncoderReadingsPortB() {
		return encoderReadingsPortB;
	}

	EncoderDataloggingTable(final WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}
	
	EncoderDataloggingTable(final WaveFlow100mW waveFlow100mW, final boolean portA, final boolean portB, final int nrOfValues, final int offsetFromMostRecentValue) {
		super(waveFlow100mW);
		this.nrOfValues=nrOfValues;
		this.offsetFromMostRecentValue=offsetFromMostRecentValue;
		portMask=0x00;
		if (portA) portMask |= 0x01;
		if (portB) portMask |= 0x02;
	}

	public String toString() {
		
		StringBuilder strBuilder = new StringBuilder();
		
		strBuilder.append("EncoderDataloggingTable (generic header):\n"+getEncoderGenericHeader()+"\n");
		
		
		strBuilder.append("EncoderDataloggingTable (datalogging parameters):\n");
		
		strBuilder.append("samplingPeriod: "+Utils.toHexString(samplingPeriod)+"\n");
		strBuilder.append("SamplingActivationType: "+Utils.toHexString(SamplingActivationType)+"\n");
		strBuilder.append("measurementPeriod: "+Utils.toHexString(measurementPeriod)+"\n");
		strBuilder.append("dayOfTheWeek: "+Utils.toHexString(dayOfTheWeek)+"\n");
		strBuilder.append("dayOfTheWeek: "+Utils.toHexString(hourOfManagement)+"\n");
		strBuilder.append("nrOfRecordsInDatalogging: "+nrOfRecordsInDatalogging+"\n");
		
		strBuilder.append("EncoderDataloggingTable (datalogging data [portmask "+Utils.toHexString(portMask)+"], [nr of values "+nrOfValues+"], [offset "+offsetFromMostRecentValue+"]\n");
		
		strBuilder.append("lastLoggingRTC: "+lastLoggingRTC+"\n");
		
		if (nrOfReadingsPortA > 0) {
			strBuilder.append("nrOfReadingsPortA: "+nrOfReadingsPortA+"\n");
			for(int i=0;i<encoderReadingsPortA.length;i++) {
				strBuilder.append("encoderReadingsPortA["+i+"]: "+encoderReadingsPortA[i]+"\n");
			}
		}
		else {
			strBuilder.append("No readings for PortA\n");
		}
		
		if (nrOfReadingsPortB > 0) {
			strBuilder.append("nrOfReadingsPortB: "+nrOfReadingsPortB+"\n");
			for(int i=0;i<encoderReadingsPortB.length;i++) {
				strBuilder.append("encoderReadingsPortB["+i+"]: "+Utils.toHexString(encoderReadingsPortB[i])+"\n");
			}
		}
		else {
			strBuilder.append("No readings for PortB\n");
		}
		
		
		return strBuilder.toString();
	}
	
	
	@Override
	EncoderRadioCommandId getEncoderRadioCommandId() {
		return EncoderRadioCommandId.EncoderDataloggingTable;
	}

	@Override
	void parse(byte[] data) throws IOException {
		
		DataInputStream dais = null;
		try {
			
			dais = new DataInputStream(new ByteArrayInputStream(data));
			List<Long> encoderReadingsPortAList = new ArrayList<Long>();
			List<Long> encoderReadingsPortBList = new ArrayList<Long>();
			do {
				if (frameCounter==0) {
					
					// read the datalogging parameters
					samplingPeriod = Utils.toInt(dais.readByte());
					SamplingActivationType = Utils.toInt(dais.readByte());
					measurementPeriod = Utils.toInt(dais.readByte());
					dayOfTheWeek = Utils.toInt(dais.readByte());
					hourOfManagement = Utils.toInt(dais.readByte());
					nrOfRecordsInDatalogging = Utils.toInt(dais.readShort());		
					
					// read the datalogging
					byte[] temp = new byte[7];
					dais.read(temp);
					lastLoggingRTC = TimeDateRTCParser.parse(temp, getWaveFlow100mW().getTimeZone()).getTime();
				}
				else {
					// in case of a multiple frame, the first byte of the following data is the commmandId acknowledge
					int commandIdAck = Utils.toInt(dais.readByte());
					if (commandIdAck != (0x80 | getEncoderRadioCommandId().getCommandId())) {
						throw new WaveFlow100mwEncoderException("Invalid response tag ["+Utils.toHexString(commandIdAck)+"]");
					}
				}
				
				frameCounter = Utils.toInt(dais.readByte());
				int nrOfReadingsPortA = Utils.toInt(dais.readByte());
				int nrOfReadingsPortB = Utils.toInt(dais.readByte());
				dais.readShort(); // skip 2 bytes unused 0x0000 value
				
				for (int i=0;i<nrOfReadingsPortA;i++) {
					encoderReadingsPortAList.add((long)dais.readInt() & 0xFFFFFFFF);
				}
				
				for (int i=0;i<nrOfReadingsPortB;i++) {
					encoderReadingsPortBList.add((long)dais.readInt() & 0xFFFFFFFF);
				}
				
				this.nrOfReadingsPortA += nrOfReadingsPortA;
				this.nrOfReadingsPortB += nrOfReadingsPortB;
			
			} while(frameCounter>1);
			
			encoderReadingsPortA = new long[encoderReadingsPortAList.size()];
			for (int index=0;index<encoderReadingsPortAList.size();index++) {
				encoderReadingsPortA[index]=encoderReadingsPortAList.get(index);
			}
			
			encoderReadingsPortB = new long[encoderReadingsPortBList.size()];
			for (int index=0;index<encoderReadingsPortBList.size();index++) {
				encoderReadingsPortB[index]=encoderReadingsPortBList.get(index);
			}
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					getWaveFlow100mW().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
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
			daos.writeByte(portMask);
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
					getWaveFlow100mW().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}
	}
}

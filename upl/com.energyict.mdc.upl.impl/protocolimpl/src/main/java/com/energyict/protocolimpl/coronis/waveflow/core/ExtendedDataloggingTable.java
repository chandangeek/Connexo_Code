package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.*;
import java.util.*;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

public class ExtendedDataloggingTable extends AbstractRadioCommand {

	// ********************************************************************************************
	// Request specific parameters
	
	private final int MAX_NR_OF_INPUTS=4;
	
	
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
	// Datalogging table reading page 23

	/**
	 * Timestamp of the last logging
	 */
	private Date lastLoggingRTC;

	private int[] nrOfReadings = new int[MAX_NR_OF_INPUTS];
	
	public final int[] getNrOfReadings() {
		return nrOfReadings;
	}

	private long[][] readingsInputs = new long[MAX_NR_OF_INPUTS][];
	
	final public long[][] getReadingsInputs() {
		return readingsInputs;
	}

	final public Date getLastLoggingRTC() {
		return lastLoggingRTC;
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

	/**
	 * Validate if the mask has inputId true or false
	 * @param inputId
	 * @return true or false for the input
	 */
	private final boolean validateMask(int inputMask,int inputId) {
		int inputIdMask = 0x01 << inputId;
		return (inputMask&inputIdMask) == inputIdMask; 
	}
	
	public int getSmallestNrOfReadings() {
		int smallestNrOfReadings=0;
		int startIndex=0;
		for (int inputIndex=1;inputIndex<MAX_NR_OF_INPUTS;inputIndex++) {
			if (validateMask(inputMask,inputIndex)) {
				smallestNrOfReadings=nrOfReadings[inputIndex];
				startIndex = inputIndex;
				break;
			}
		}
		for (int inputIndex=startIndex;inputIndex<MAX_NR_OF_INPUTS;inputIndex++) {
			if ((validateMask(inputMask,inputIndex)) && (nrOfReadings[inputIndex]<smallestNrOfReadings)) {
				smallestNrOfReadings = nrOfReadings[inputIndex];
			}
		}
		return smallestNrOfReadings;
	}
	
	
	public String toString() {
		
		StringBuilder strBuilder = new StringBuilder();
		
		strBuilder.append("ExtendedDataloggingTable (datalogging parameters):\n");
		
		strBuilder.append("ExtendedDataloggingTable (datalogging data [input mask "+WaveflowProtocolUtils.toHexString(inputMask)+"], [nr of values "+nrOfValues+"], [offset "+offsetFromMostRecentValue+"]\n");
		
		strBuilder.append("lastLoggingRTC: "+lastLoggingRTC+"\n");
		
		for (int inputIndex=0;inputIndex<MAX_NR_OF_INPUTS;inputIndex++) {
			if (nrOfReadings[inputIndex] > 0) {
				strBuilder.append("nrOfReadings input index ["+inputIndex+"]: "+nrOfReadings[inputIndex]+"\n");
				for(int i=0;i<readingsInputs[inputIndex].length;i++) {
					strBuilder.append("input index ["+inputIndex+"] reading  ["+i+"]: "+readingsInputs[inputIndex][i]+"\n");
				}
			}
			else {
				strBuilder.append("No readings for input index ["+inputIndex+"]\n");
			}
		}
		
		return strBuilder.toString();
	}
	
	
	@Override
	RadioCommandId getRadioCommandId() {
		return RadioCommandId.ExtendedDataloggingTable;
	}

	/*  example
frame $01/$04 
$11$0B$0A$03$09$1C$00 last logging timestamp
            input $01 $02$0D to $01$EE $00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19
$89 $02/$04 input $01 $01$ED to $01$D1 $00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19$00$00$00$19
$89 $03/$04 input $02 $02$0D to $01$EE $00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C
$89 $04/$04 input $02 $01$ED to $01$D1 $00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C$00$00$00$0C
	 */
	
	@Override
	void parse(byte[] data) throws IOException {
		
		int frameCounter=0;
		int nrOfFrames;
		
		//System.out.println("KV_DEBUG> "+ProtocolUtils.outputHexString(data));
		
		if (WaveflowProtocolUtils.toInt(data[0]) == 0xFF) {
			throw new WaveFlowException("Error requesting load profile, returned [FF]");
		}
		
		DataInputStream dais = null;
		try {
			
			dais = new DataInputStream(new ByteArrayInputStream(data));
			List<Long>[] readingsInputLists = new List[MAX_NR_OF_INPUTS];
			for (int inputIndex=0;inputIndex<MAX_NR_OF_INPUTS;inputIndex++) {
				readingsInputLists[inputIndex] = new ArrayList<Long>();
			}
			
			do {
				if (frameCounter==0) {
					
					frameCounter = WaveflowProtocolUtils.toInt(dais.readByte());
					nrOfFrames = WaveflowProtocolUtils.toInt(dais.readByte());
					
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
					
					frameCounter = WaveflowProtocolUtils.toInt(dais.readByte());
					nrOfFrames = WaveflowProtocolUtils.toInt(dais.readByte());
				}
				
				int inputIndex = log2Lookup(WaveflowProtocolUtils.toInt(dais.readByte()));
				
				int indexFirst = dais.readShort();
				int indexLast = dais.readShort();
				
				int nrOfReadings = (indexFirst - indexLast) + 1;
				
				this.nrOfReadings[inputIndex] += nrOfReadings;
			
				for (int i=0;i<nrOfReadings;i++) {
					readingsInputLists[inputIndex].add((long)dais.readInt() & 0xFFFFFFFF);
				}				
				
			} while(frameCounter<nrOfFrames);
			
			for (int inputIndex=0;inputIndex<MAX_NR_OF_INPUTS;inputIndex++) {
				readingsInputs[inputIndex] = new long[readingsInputLists[inputIndex].size()];
				for (int index=0;index<readingsInputLists[inputIndex].size();index++) {
					readingsInputs[inputIndex][index]=readingsInputLists[inputIndex].get(index);
				}
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

	private int log2Lookup(int mask) throws IOException {
		if (mask == 8) return 3;
		else if (mask == 4) return 2;
		else if (mask == 2) return 1;
		else if (mask == 1) return 0;
		else throw new WaveFlowException("Invalid input mask, ["+mask+"]");
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

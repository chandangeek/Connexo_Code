package com.energyict.protocolimpl.iec1107.siemenss4s;

import java.util.Calendar;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProtocolUtils;

/**
 * Contains one profileRecord 
 * @author gna
 *
 */
public class SiemensS4sProfileRecord {

	private byte[] rawData;
	private Calendar timeStamp;
	private int numberOfChannels;
	
	public SiemensS4sProfileRecord(byte[] rawData, Calendar timeStamp, int numberOfChannels){
		this.rawData = rawData;
		this.timeStamp = timeStamp;
		this.numberOfChannels = numberOfChannels;
	}
	
	//TODO add the status
	public IntervalData getIntervalData(){
		IntervalData iv = new IntervalData(timeStamp.getTime());
		iv.addValues(getIntervalValues());
		return iv;
	}
	
	private Number[] getIntervalValues(){
		int offset = (numberOfChannels-1)*4;
		Integer[] values = new Integer[numberOfChannels];
		for(int i = 0; i < numberOfChannels; i++){
			values[i] = Integer.valueOf(new String(ProtocolUtils.getSubArray2(rawData, offset, 4)));
			offset -= 4;
		}
		return values;
	}
	
}

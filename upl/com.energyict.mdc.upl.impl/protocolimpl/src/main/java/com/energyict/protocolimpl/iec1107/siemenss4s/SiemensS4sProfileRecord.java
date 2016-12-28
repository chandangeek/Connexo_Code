package com.energyict.protocolimpl.iec1107.siemenss4s;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.siemenss4s.objects.S4sObjectUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Contains one profileRecord, in EIServer terms, an Interval
 * @author gna
 *
 */
public class SiemensS4sProfileRecord {

	private final static int HOURES 	= 0;
	private final static int MINUTES 	= 1;
	private final static int MONTHS		= 0;
	private final static int DAYS		= 1;

	private final static int PHASE_FAILURE = 0x01;
	private final static int GENERAL_ERROR = 0x02;
	private final static int BATTERY_ALARM = 0x04;
	private final static int SERIAL_PORT_WRITE = 0x08;
	private final static int REVERSE_DETECT = 0x01;
	private final static int SUPPLY_INTERRUPTED = 0x02;
	private final static int CLOCK_WRITE = 0x04;
	private final static int GLITCH_RESET = 0x08;

	private byte[] rawData;
	private Calendar timeStamp;
	private int numberOfChannels;
	private boolean possibleDelete;

	/**
	 * Create a new instance of a profileRecord
	 * @param rawData - the byteArray containing the value and sometimes a dateTime
	 * @param timeStamp - the calculated timeStamp from building the loadProfile
	 * @param numberOfChannels - the number of values to retrieve from the byteArray
	 */
	public SiemensS4sProfileRecord(byte[] rawData, Calendar timeStamp, int numberOfChannels){
		if(rawData != null){
			this.rawData = rawData.clone();
		}
		this.timeStamp = timeStamp;
		this.numberOfChannels = numberOfChannels;
		this.possibleDelete = false;
	}

	/**
	 * Construct an intervalData object with the given rawData
	 * If a timeStamp is included in the rawData, then we use that one, otherwise the one set in the constructor
	 * @return an IntervalData object
	 * @throws IOException when parsing of the byteArray fails
	 */
	public IntervalData getIntervalData() throws IOException{
		IntervalData iv;
		int status = getStatus();
		if(S4sObjectUtils.itsActuallyADateIntervalRecord(this.rawData)){
			this.timeStamp = getDataIntervalDate();
			this.possibleDelete = true;
		}
		iv = new IntervalData(timeStamp.getTime(), status);
		iv.addValues(getIntervalValues());
		return iv;
	}

	/**
	 * @return the timeStamp of the current intervalRecord
	 */
	public Calendar getLastIntervalCalendar(){
		return this.timeStamp;
	}

	/**
	 * Construct a Calendar with the date and time from the rawData byteArray
	 * @return the current intervals calendar(timeStamp)
	 */
	private Calendar getDataIntervalDate(){
		int offset = this.rawData.length-8;
		byte[] time = S4sObjectUtils.getAsciiConvertedDecimalByteArray(ProtocolUtils.getSubArray2(rawData, offset, 4));
		offset -= 4;
		byte[] date = S4sObjectUtils.getAsciiConvertedDecimalByteArray(ProtocolUtils.getSubArray2(rawData, offset, 4));

		Calendar cal = ProtocolUtils.getCleanGMTCalendar();
		cal.setTimeInMillis(timeStamp.getTimeInMillis());	// first set it to the current date
		cal.set(Calendar.MONTH, date[MONTHS]-1);
		cal.set(Calendar.DAY_OF_MONTH, date[DAYS]);
		cal.set(Calendar.HOUR_OF_DAY, time[HOURES]);
		cal.set(Calendar.MINUTE, time[MINUTES]);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		return cal;
	}

	/**
	 * @return all intervalValues
	 */
	private List<Number> getIntervalValues() {
		int offset = (numberOfChannels-1)*4;
		return IntStream
                .range(0, numberOfChannels - 1)
                .mapToObj(i -> Integer.valueOf(new String(ProtocolUtils.getSubArray2(rawData, offset, 4))))
                .collect(Collectors.toList());
	}

	/**
	 * In some case you will construct twice the same intervalData because the date and time part is in one block,
	 * and the value is in the previous block. This results in twice the same interval, so we delete the previous
	 * @return  true if the rawData contains a dateTime
	 */
	public boolean possibleDelete() {
		return this.possibleDelete;
	}

	/**
	 * Construct the EIStatus code from the rawData
	 * @return the value of the EIStatuscode, mapped to the intervalStateBits
	 * @throws IOException if parsing of the rawData byteArray fails
	 */
	private int getStatus() throws IOException{
		int offset = this.rawData.length-2;
		int rawStatusHigh = ProtocolUtils.hex2nibble(rawData[offset]);
		offset++;
		int rawStatusLow = ProtocolUtils.hex2nibble(rawData[offset]);
		int eiCode = 0;

		if((rawStatusLow & PHASE_FAILURE) == PHASE_FAILURE){
			eiCode |= IntervalStateBits.PHASEFAILURE;
		}
		if((rawStatusLow & GENERAL_ERROR) == GENERAL_ERROR){
			eiCode |= IntervalStateBits.DEVICE_ERROR;
		}
		if((rawStatusLow & BATTERY_ALARM) == BATTERY_ALARM){
			eiCode |= IntervalStateBits.BATTERY_LOW;
		}
//		if((rawStatusLow & SERIAL_PORT_WRITE) == SERIAL_PORT_WRITE){	// don't know exactly how to map this one
//			eiCode |= IntervalStateBits.OTHER;
//		}
		if((rawStatusHigh & REVERSE_DETECT) == REVERSE_DETECT){
			eiCode |= IntervalStateBits.REVERSERUN;
		}
		if((rawStatusHigh & SUPPLY_INTERRUPTED) == SUPPLY_INTERRUPTED){
			eiCode |= IntervalStateBits.POWERDOWN;
		}
		if((rawStatusHigh & CLOCK_WRITE) == CLOCK_WRITE){
			eiCode |= IntervalStateBits.SHORTLONG;
		}
//		if((rawStatusHigh & GLITCH_RESET) == GLITCH_RESET){				// don't know exactly how to map this one
//			eiCode |= IntervalStateBits.OTHER;
//		}
		return eiCode;
	}

}

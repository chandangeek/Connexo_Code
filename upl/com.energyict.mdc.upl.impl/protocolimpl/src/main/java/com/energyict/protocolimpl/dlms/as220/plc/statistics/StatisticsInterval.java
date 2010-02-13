package com.energyict.protocolimpl.dlms.as220.plc.statistics;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.DateTime;

/**
 * @author jme
 *
 */
public class StatisticsInterval extends Structure {

	private TimeZone	timeZone;

	private Date		timeStamp;
	private int			intervalLength;
	private PlcSNR		plcSNR;
	private long		framesCRCOk;
	private long		framesCRCNotOk;
	private long		framesTransmitted;
	private long		framesRepeated;
	private long		framesCorrected;
	private long		badFrameIndicator;

	public StatisticsInterval(byte[] berEncodedByteArray, TimeZone timezone) throws IOException {
		super(berEncodedByteArray, 0, 0);
		this.timeZone = timezone;
	}

	public Date getTimeStamp() {
		DateTime dt = getDataType(0).getOctetString().getDateTime(getTimeZone());
		timeStamp = dt.getValue().getTime();
		return timeStamp;
	}

	public int getIntervalLength() {
		Unsigned8 il = getDataType(1).getUnsigned8();
		this.intervalLength = il.getValue();
		return intervalLength;
	}

	private TimeZone getTimeZone() {
		return timeZone;
	}

	@Override
	public String toString() {
		final String crlf = "\r\n";

		StringBuffer sb = new StringBuffer();
		sb.append("[").append(getTimeStamp()).append("] ");
		sb.append(getIntervalLength());
		return sb.toString();
	}

}

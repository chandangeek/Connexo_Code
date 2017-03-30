/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220.plc.statistics;

import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.DateTime;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author jme
 *
 */
public class StatisticsInterval extends Structure {

	private TimeZone	timeZone;

	public StatisticsInterval(byte[] berEncodedByteArray, TimeZone timezone) throws IOException {
		super(berEncodedByteArray, 0, 0);
		this.timeZone = timezone;
	}

	public Date getTimeStamp() {
		DateTime dt = getDataType(0).getOctetString().getDateTime(getTimeZone());
		return dt.getValue().getTime();
	}

	public int getIntervalLength() {
		Unsigned8 il = getDataType(1).getUnsigned8();
		return il.getValue();
	}

	public PlcSNR getPlcSNR() {
		try {
			return new PlcSNR(getDataType(2).getBEREncodedByteArray());
		} catch (IOException e) {
			return null;
		}
	}

	public long getFramesCRCOk() {
		return getDataType(3).getUnsigned32().getValue();
	}

	public long getFramesCRCNotOk() {
		return getDataType(4).getUnsigned32().getValue();
	}

	public long getFramesTransmitted() {
		return getDataType(5).getUnsigned32().getValue();
	}

	public long getFramesRepeated() {
		return getDataType(6).getUnsigned32().getValue();
	}

	private TimeZone getTimeZone() {
		return timeZone;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[").append(getTimeStamp()).append("] ");
		sb.append("IL=").append(getIntervalLength()).append(", ");
		sb.append(getPlcSNR()).append(", ");
		sb.append("CRC_OK=").append(getFramesCRCOk()).append(", ");
		sb.append("CRC_NOK=").append(getFramesCRCNotOk()).append(", ");
		sb.append("TX=").append(getFramesTransmitted()).append(", ");
		sb.append("REP=").append(getFramesRepeated());
		return sb.toString();
	}

}

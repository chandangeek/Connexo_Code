package com.energyict.protocolimpl.dlms.as220.plc.statistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;

public class PLCStatistics extends Array {

	private final TimeZone timeZone;

	public PLCStatistics(byte[] profile, TimeZone timeZone) throws IOException {
		super(profile, 0, 0);
		this.timeZone = timeZone;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public List<IntervalData> getIntervalDatas() throws IOException {
		List<IntervalData> intervals = new ArrayList<IntervalData>();

		for (int i = 0; i < nrOfDataTypes(); i++) {
			StatisticsInterval statsInterval = new StatisticsInterval(getDataType(i).getBEREncodedByteArray(), getTimeZone());
			IntervalData id = new IntervalData(getIntervalTimeStamp(statsInterval.getTimeStamp(), statsInterval.getIntervalLength()));
			id.addValue(statsInterval.getPlcSNR().getChannelNr());
			id.addValue(statsInterval.getPlcSNR().getSnr0());
			id.addValue(statsInterval.getPlcSNR().getSnr1());
			id.addValue(statsInterval.getFramesCRCOk());
			id.addValue(statsInterval.getFramesCRCNotOk());
			id.addValue(statsInterval.getFramesTransmitted());
			id.addValue(statsInterval.getFramesRepeated());
			id.addValue(statsInterval.getFramesCorrected());
			id.addValue(statsInterval.getBadFramesIndicator());
			intervals.add(id);
		}

		return intervals;
	}

	/**
	 * @param timeStamp
	 * @param intervalLength
	 * @return
	 */
	private static Date getIntervalTimeStamp(Date timeStamp, int intervalLength) {
		int intervalMillis = intervalLength * 1000 * 60;

		Calendar cal = Calendar.getInstance();
		cal.setTime(timeStamp);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);


		long diff = timeStamp.getTime() - cal.getTimeInMillis();
		long overTime = diff % intervalMillis;
		long beforeTime = intervalMillis - overTime;

		Calendar returnDate = Calendar.getInstance();
		returnDate.setTime(timeStamp);
		returnDate.add(Calendar.MILLISECOND, (int) beforeTime);

		return returnDate.getTime();
	}

	/**
	 * @return
	 */
	public List<ChannelInfo> getChannelInfos() {
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		channelInfos.add(new ChannelInfo(0, "ActiveChannel", Unit.getUndefined()));
		channelInfos.add(new ChannelInfo(1, "SNR0", Unit.getUndefined()));
		channelInfos.add(new ChannelInfo(2, "SNR1", Unit.getUndefined()));
		channelInfos.add(new ChannelInfo(3, "CRC_OK", Unit.getUndefined()));
		channelInfos.add(new ChannelInfo(4, "CRC_NOK", Unit.getUndefined()));
		channelInfos.add(new ChannelInfo(5, "Frames TX", Unit.getUndefined()));
		channelInfos.add(new ChannelInfo(6, "Frames repeated", Unit.getUndefined()));
		channelInfos.add(new ChannelInfo(7, "Frames corrected", Unit.getUndefined()));
		channelInfos.add(new ChannelInfo(8, "Bad frames", Unit.getUndefined()));
		return channelInfos;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		try {
			getIntervalDatas();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

}

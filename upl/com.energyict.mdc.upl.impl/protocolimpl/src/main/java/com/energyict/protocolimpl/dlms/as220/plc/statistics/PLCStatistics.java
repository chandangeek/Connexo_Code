package com.energyict.protocolimpl.dlms.as220.plc.statistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocolimpl.utils.ProtocolTools;

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
			IntervalData id = new IntervalData(ProtocolTools.roundUpToNearestInterval(statsInterval.getTimeStamp(), statsInterval.getIntervalLength()));
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

		return ProtocolTools.mergeDuplicateIntervals(intervals);
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
		final String crlf = "\r\n";
		StringBuilder sb = new StringBuilder();
		sb.append("ChannelInfos:");
		for (ChannelInfo ci : getChannelInfos()) {
			sb.append(" ").append(ci.toString()).append(crlf);
		}
		sb.append("IntervalDatas:");
		try {
			for (IntervalData ci : getIntervalDatas()) {
				sb.append(" ").append(ci.toString()).append(crlf);
			}
		} catch (IOException e) {
		}
		return sb.toString();
	}

}

package com.energyict.protocolimpl.dlms.as220.plc.statistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * This class parses the raw data of the PLC statistics ({@link ProfileGeneric}) <br>
 * The PLC statistics contains 8 channels and are assembled as follows:
 * <li>CH1=SNR0</li> <li>CH2=SNR1</li> <li>CH3=CRC_OK</li> <li>CH4=CRC_NOK</li>
 * <li>CH5=FR_TX</li> <li>CH6=FR_REP</li> <li>CH7=FR_CORR</li> <li>CH8=FR_BAD</li>
 * <br>
 * @author jme
 */
public class PLCStatistics extends Array {

	/**
	 * The {@link TimeZone} used to parse the time stamps on the PLC profile data
	 */
	private final TimeZone	timeZone;

	/**
	 * @param profile
	 * @param timeZone
	 * @throws IOException
	 */
	public PLCStatistics(byte[] profile, TimeZone timeZone) throws IOException {
		super(profile, 0, 0);
		this.timeZone = timeZone;
	}

	/**
	 * @return
	 */
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

			Date timeStamp = statsInterval.getTimeStamp();
			Date correctedTimeStamp = ProtocolTools.roundUpToNearestInterval(timeStamp, statsInterval.getIntervalLength());

			IntervalData id = new IntervalData(correctedTimeStamp);
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
		final Unit countUnit = Unit.get(BaseUnit.COUNT);
		final Unit ratioUnit = Unit.get(BaseUnit.RATIO);

		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		channelInfos.add(new ChannelInfo(1, "SNR0", ratioUnit));
		channelInfos.add(new ChannelInfo(2, "SNR1", ratioUnit));
		channelInfos.add(new ChannelInfo(3, "CRC_OK", countUnit));
		channelInfos.add(new ChannelInfo(4, "CRC_NOK", countUnit));
		channelInfos.add(new ChannelInfo(5, "Frames TX", countUnit));
		channelInfos.add(new ChannelInfo(6, "Frames repeated", countUnit));
		channelInfos.add(new ChannelInfo(7, "Frames corrected", countUnit));
		channelInfos.add(new ChannelInfo(8, "Bad frames", countUnit));
		return channelInfos;
	}

	/**
	 * @return
	 */
	private String getChannelInfosToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ChannelInfos:");
		for (ChannelInfo ci : getChannelInfos()) {
			sb.append(" ").append(ci.toString()).append("\r\n");
		}
		return sb.toString();
	}

	/**
	 * @return
	 */
	private String getIntervalDatasToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("IntervalDatas:");
		try {
			for (IntervalData ci : getIntervalDatas()) {
				sb.append(" ").append(ci.toString()).append("\r\n");
			}
		} catch (IOException e) {
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getChannelInfosToString());
		sb.append(getIntervalDatasToString());
		return sb.toString();
	}

}

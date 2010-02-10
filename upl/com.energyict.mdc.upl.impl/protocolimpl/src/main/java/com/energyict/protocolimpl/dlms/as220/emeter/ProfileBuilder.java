package com.energyict.protocolimpl.dlms.as220.emeter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.energyict.dlms.ScalerUnit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.dlms.as220.DLMSSNAS220;

/**
 * @author jme
 *
 */
public class ProfileBuilder {

	private final DLMSSNAS220	as220;

	public ProfileBuilder(DLMSSNAS220 as220) {
		this.as220 = as220;
	}

	private DLMSSNAS220 getAs220() {
		return as220;
	}

	/**
	 * @param nrOfChannels
	 * @return
	 * @throws IOException
	 */
	public ScalerUnit[] buildScalerUnits(byte nrOfChannels) throws IOException {
		ScalerUnit[] scalerUnits = new ScalerUnit[nrOfChannels];
		for (int i = 0; i < nrOfChannels; i++) {
	        ObisCode obisCode = getAs220().getMeterConfig().getMeterDemandObject(i).getObisCode();
	        scalerUnits[i] = getAs220().getCosemObjectFactory().getCosemObject(obisCode).getScalerUnit();

		}
		return scalerUnits;
	}

	/**
	 * @param scalerunit
	 * @return
	 * @throws IOException
	 */
	public List<ChannelInfo> buildChannelInfos(ScalerUnit[] scalerunit) throws IOException {
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		for (int i = 0; i < scalerunit.length; i++) {
			ChannelInfo channelInfo = new ChannelInfo(i, "dlms" + getAs220().getDeviceID() + "_channel_" + i, scalerunit[i].getUnit());
			if (getAs220().getMeterConfig().getChannelObject(i).isCapturedObjectCumulative()) {
				if (getAs220().getMeterConfig().getChannelObject(i).isCapturedObjectPulses()) {
					if ((getAs220().getChannelMap() != null) && (getAs220().getChannelMap().getProtocolChannel(i) != null)) {
						channelInfo.setCumulativeWrapValue(getAs220().getChannelMap().getProtocolChannel(i).getWrapAroundValue());
					} else {
						channelInfo.setCumulativeWrapValue(BigDecimal.valueOf(Long.MAX_VALUE));
					}
				} else {
					if ((getAs220().getChannelMap() != null) && (getAs220().getChannelMap().getProtocolChannel(i) != null)) {
						channelInfo.setCumulativeWrapValue(getAs220().getChannelMap().getProtocolChannel(i).getWrapAroundValue());
					} else {
						channelInfo.setCumulativeWrapValue(BigDecimal.valueOf(2 ^ 32));
					}
				}
			}
			channelInfos.add(channelInfo);
		}
		return channelInfos;
	}

	/**
	 * @param scalerunit
	 * @param loadProfileCompArrEntries
	 * @return
	 * @throws UnsupportedException
	 * @throws IOException
	 */
	public List<IntervalData> buildIntervalData(ScalerUnit[] scalerunit, List<LoadProfileCompactArrayEntry> loadProfileCompArrEntries) throws UnsupportedException, IOException {
		List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
		int latestProfileInterval = getAs220().getProfileInterval();
		int eiCode = 0;

		LoadProfileCompactArrayEntry dateStamp = null;
		Calendar calendar = null;
		for (int i = 0; i < loadProfileCompArrEntries.size(); i++) {
			LoadProfileCompactArrayEntry lpcae = loadProfileCompArrEntries.get(i);

			if (lpcae.isValue()) { // normal interval value
				if (calendar == null) {
					continue; // first the calendar has to be initialized with the start of load profile marker
				}
				IntervalData ivd = new IntervalData(calendar.getTime());
				ivd.addValue(new BigDecimal("" + lpcae.getValue()));
				intervalDatas.add(ivd);
				latestProfileInterval = lpcae.getIntervalInSeconds();
				calendar.add(Calendar.SECOND, latestProfileInterval); // set the calendar to the next interval endtime
			} else if (lpcae.isPartialValue()) { // partial interval value
				//eiCode |= IntervalStateBits.SHORTLONG;
				if (calendar == null) {
					continue; // first the calendar has to be initialized with the start of load profile marker
				}
				IntervalData ivd = new IntervalData(calendar.getTime(), eiCode);
				eiCode = 0;
				ivd.addValue(new BigDecimal("" + lpcae.getValue()));
				intervalDatas.add(ivd);
				latestProfileInterval = lpcae.getIntervalInSeconds();
				calendar.add(Calendar.SECOND, latestProfileInterval); // set the calendar to the next interval endtime
			} else if (lpcae.isDate()) { // date stamp
				// date always followed by time? Do the processing if time is received
				dateStamp = lpcae;
			} else if (lpcae.isTime()) { // time stamp
				if (dateStamp == null) {

					// change of the interval...
					// only timestamp is received...
					// adjust time here...
				} else {
					// set the calendar
					calendar = ProtocolUtils.getCleanCalendar(getAs220().getTimeZone());
					calendar.set(Calendar.YEAR, dateStamp.getYear());
					calendar.set(Calendar.MONTH, dateStamp.getMonth());
					calendar.set(Calendar.DATE, dateStamp.getDay());
					calendar.set(Calendar.HOUR_OF_DAY, lpcae.getHours());
					calendar.set(Calendar.MINUTE, lpcae.getMinutes());
					calendar.set(Calendar.SECOND, lpcae.getSeconds());
					dateStamp = null; // reset the dateStamp

					if (lpcae.isStartOfLoadProfile()) {
						// do nothing special...
					} else if (lpcae.isPowerOff()) {
						ParseUtils.roundUp2nearestInterval(calendar, latestProfileInterval);
						eiCode = IntervalStateBits.POWERDOWN;
					} else if (lpcae.isPowerOn()) {
						ParseUtils.roundUp2nearestInterval(calendar, latestProfileInterval);
						eiCode = IntervalStateBits.POWERUP;
					} else if (lpcae.isChangeclockOldTime()) {
						ParseUtils.roundUp2nearestInterval(calendar, latestProfileInterval);
						eiCode = IntervalStateBits.SHORTLONG;

					} else if (lpcae.isChangeclockNewTime()) {
						ParseUtils.roundUp2nearestInterval(calendar, latestProfileInterval);
						eiCode = IntervalStateBits.SHORTLONG;
					}
				}
			} // time

		} // for (i=0;i<loadProfileCompactArrayEntries.size();i++) {

		return intervalDatas;
	}

}

package com.energyict.protocolimpl.dlms.as220.gmeter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CapturedObjectsHelper;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.dlms.as220.GasDevice;
import com.energyict.protocolimpl.dlms.as220.emeter.LoadProfileCompactArrayEntry;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * @author jme
 *
 */
public class GProfileBuilder {

	private final GasDevice	as220;
	private final CapturedObjectsHelper coh;

	public GProfileBuilder(GasDevice as220, CapturedObjectsHelper coh) {
		this.as220 = as220;
		this.coh = coh;
	}

	private GasDevice getGasDevice() {
		return as220;
	}

	/**
	 * @param coh
	 * @return
	 * @throws IOException
	 */
	public ScalerUnit[] buildScalerUnits() throws IOException {
		ScalerUnit[] scalerUnits = new ScalerUnit[coh.getNrOfchannels()];
		for (int i = 0; i < scalerUnits.length; i++) {
	        ObisCode obisCode = coh.getProfileDataChannelObisCode(i);
//	        scalerUnits[i] = getGasDevice().getCosemObjectFactory().getGenericRead(getGasDevice().getCorrectedChannelObisCode(obisCode), (byte)0x08, 4).getScalerUnit();
	        scalerUnits[i] = new ScalerUnit(Unit.get(BaseUnit.LITER));

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
			ChannelInfo channelInfo = new ChannelInfo(i, "dlms" + getGasDevice().getDeviceID() + "_channel_" + i, scalerunit[i].getUnit());
			channelInfo.setCumulative();
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
		int latestProfileInterval = getGasDevice().getProfileInterval();
		int eiCode = 0;

		LoadProfileCompactArrayEntry dateStamp = null;
		Calendar calendar = null;
		for (int i = 0; i < loadProfileCompArrEntries.size(); i++) {
			LoadProfileCompactArrayEntry lpcae = loadProfileCompArrEntries.get(i);

			if (lpcae.isValue()) { // normal interval value
				if (calendar == null) {
					continue; // first the calendar has to be initialized with the start of load profile marker
				}
				IntervalData ivd = new IntervalData(ProtocolTools.roundUpToNearestInterval(calendar.getTime(), latestProfileInterval/60));
//				IntervalData ivd = new IntervalData(calendar.getTime());
				ivd.addValue(new BigDecimal("" + lpcae.getValue()));
				intervalDatas.add(ivd);
				latestProfileInterval = lpcae.getIntervalInSeconds();
				calendar.add(Calendar.SECOND, latestProfileInterval); // set the calendar to the next interval endtime
			} else if (lpcae.isPartialValue()) { // partial interval value
				//eiCode |= IntervalStateBits.SHORTLONG;
				if (calendar == null) {
					continue; // first the calendar has to be initialized with the start of load profile marker
				}
				IntervalData ivd = new IntervalData(ProtocolTools.roundUpToNearestInterval(calendar.getTime(), latestProfileInterval/60), eiCode);
//				IntervalData ivd = new IntervalData(calendar.getTime(), eiCode);
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
					calendar = ProtocolUtils.getCleanCalendar(getGasDevice().getTimeZone());
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
			}

		}

		return ProtocolTools.mergeDuplicateIntervals(intervalDatas);
	}

}

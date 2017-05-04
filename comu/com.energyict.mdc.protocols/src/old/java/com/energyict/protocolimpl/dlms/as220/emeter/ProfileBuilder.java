package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.RetryHandler;
import com.energyict.protocolimpl.dlms.as220.DLMSSNAS220;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author jme
 *
 */
public class ProfileBuilder {

	private final DLMSSNAS220	as220;
	private final ObisCode energyObisCode;

	public ProfileBuilder(DLMSSNAS220 as220, ObisCode obisCode) {
		this.as220 = as220;
		this.energyObisCode = obisCode;
	}

	private DLMSSNAS220 getAs220() {
		return as220;
	}

	private ObisCode getEnergyObisCode() {
		return energyObisCode;
	}

    /**
     * @param nrOfChannels
     * @return
     * @throws IOException
     */
    public ScalerUnit[] buildScalerUnits(byte nrOfChannels) throws IOException {
        RetryHandler retryHandler = new RetryHandler(this.as220.getProtocolRetries());
        ScalerUnit[] scalerUnits = new ScalerUnit[nrOfChannels];
        do {
            try {
                List<CapturedObject> co = getAs220().getCosemObjectFactory().getProfileGeneric(getEnergyObisCode()).getCaptureObjects();
                int index = 0;
                for (CapturedObject capturedObject : co) {
                    ObisCode obis = capturedObject.getLogicalName().getObisCode();
                    if (obis.getA() != 0) {
                        if (index <= nrOfChannels) {
                            scalerUnits[index] = getAs220().getCosemObjectFactory().getCosemObject(obis).getScalerUnit();
                            index++;
                        } else {
                            throw new IOException("There are more channels in the captured objects [" + getEnergyObisCode() + "] than needed [" + nrOfChannels + "].");
                        }
                    }
                }
                return scalerUnits;
            } catch (DataAccessResultException e) {
                retryHandler.logFailure(e);
            }
        }
        while (retryHandler.canRetry());

        /*
        We should never get here. The scalerUnits array should be properly build and returned from within the try/catch.
        If not, then an exception should be thrown.
        */
        return scalerUnits;
    }

    /**
	 * @param scalerunit
	 * @return
	 */
	public List<ChannelInfo> buildChannelInfos(ScalerUnit[] scalerunit){
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		for (int i = 0; i < scalerunit.length; i++) {
			ChannelInfo channelInfo = new ChannelInfo(i, "dlms" + getAs220().getDeviceID() + "_channel_" + i, scalerunit[i].getEisUnit());
			channelInfos.add(channelInfo);
		}
		return channelInfos;
	}

	/**
	 * @param scalerunit
	 * @param loadProfileCompArrEntries
	 * @return
	 * @throws IOException
	 */
	public List<IntervalData> buildIntervalData(ScalerUnit[] scalerunit, List<LoadProfileCompactArrayEntry> loadProfileCompArrEntries) throws IOException {
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
				IntervalData ivd = new IntervalData(ProtocolTools.roundUpToNearestInterval(calendar.getTime(), latestProfileInterval/60));
				ivd.addValue(new BigDecimal("" + loadProfileCompArrEntries.get(i).getValue()));
				ivd.addValue(new BigDecimal("" + loadProfileCompArrEntries.get(i+1).getValue()));
				intervalDatas.add(ivd);
				latestProfileInterval = lpcae.getIntervalInSeconds();
				calendar.add(Calendar.SECOND, latestProfileInterval); // set the calendar to the next interval endtime
				i++;
			} else if (lpcae.isPartialValue()) { // partial interval value
				//eiCode |= IntervalStateBits.SHORTLONG;
				if (calendar == null) {
					continue; // first the calendar has to be initialized with the start of load profile marker
				}
				IntervalData ivd = new IntervalData(ProtocolTools.roundUpToNearestInterval(calendar.getTime(), latestProfileInterval/60), eiCode);
//				IntervalData ivd = new IntervalData(calendar.getTime(), eiCode);
				eiCode = 0;
				ivd.addValue(new BigDecimal("" + loadProfileCompArrEntries.get(i).getValue()));
				ivd.addValue(new BigDecimal("" + loadProfileCompArrEntries.get(i+1).getValue()));
				intervalDatas.add(ivd);
				latestProfileInterval = lpcae.getIntervalInSeconds();
				calendar.add(Calendar.SECOND, latestProfileInterval); // set the calendar to the next interval endtime
				i++;
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
			}

		}

		return ProtocolTools.mergeDuplicateIntervals(intervalDatas);
	}

}

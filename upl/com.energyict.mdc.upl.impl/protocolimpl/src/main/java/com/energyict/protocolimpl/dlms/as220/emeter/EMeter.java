package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.base.ClockController;
import com.energyict.protocolimpl.base.ContactorController;
import com.energyict.protocolimpl.base.LoadLimitController;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.EventLogs;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author jme
 *
 */
public class EMeter {

	private static final int SEC_PER_MIN = 60;
	private static final ObisCode ENERGY_PROFILE_OBISCODE = ObisCode.fromString("1.1.99.1.0.255");

	private final AS220 as220;
    /** The used {@link com.energyict.protocolimpl.base.ClockController} */
	private final ClockController clockController;
    /** The used {@link com.energyict.protocolimpl.base.ContactorController} */
	private final ContactorController contactorController;
    /** The used {@link com.energyict.protocolimpl.base.ActivityCalendarController} */
    private final ActivityCalendarController activityCalendarController;
    /** The used {@link com.energyict.protocolimpl.base.LoadLimitController} */
    private final LoadLimitController loadLimitController;

    private int interval = -1;

	/**
	 * @param as220
	 */
	public EMeter(AS220 as220) {
		this.as220 = as220;
		this.clockController = new AS220ClockController(as220);
		this.contactorController = new AS220ContactorController(as220);
        this.activityCalendarController = new AS220ActivityCalendarController(as220);
        this.loadLimitController = new AS220LoadLimitController(as220);
	}

	/**
     * Getter for the {@link com.energyict.protocolimpl.dlms.as220.emeter.AS220ClockController}
	 * @return the {@link #clockController}
	 */
	public ClockController getClockController() {
		return clockController;
	}

	/**
     * Getter for the {@link com.energyict.protocolimpl.dlms.as220.emeter.AS220ClockController}
	 * @return the {@link #contactorController}
	 */
	public ContactorController getContactorController() {
		return contactorController;
	}

    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.as220.emeter.AS220ActivityCalendarController}
     * @return the current ActivityCalendarController
     */
    public ActivityCalendarController getActivityCalendarController() {
        return this.activityCalendarController;
    }

    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.as220.emeter.AS220LoadLimitController}
     * @return the current LoadLimitController
     */
    public LoadLimitController getLoadLimitController() {
        return loadLimitController;
    }

	public AS220 getAs220() {
		return as220;
	}

    /**
     * Read the profile dta from the device
     *
     * @param from
     * @param to
     * @param includeEvents
     * @return the {@link ProfileData}
     * @throws IOException
     */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {

		Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getAs220().getTimeZone());
		Calendar toCalendar = ProtocolUtils.getCleanCalendar(getAs220().getTimeZone());
		fromCalendar.setTime(from);
		toCalendar.setTime(to);

    	ProfileBuilder profileBuilder = new ProfileBuilder(getAs220(), ENERGY_PROFILE_OBISCODE);
		ProfileData profileData = new ProfileData();
		ScalerUnit[] scalerunit = profileBuilder.buildScalerUnits((byte) getNrOfChannels());

        List<ChannelInfo> channelInfos = profileBuilder.buildChannelInfos(scalerunit);
        profileData.setChannelInfos(channelInfos);

        // decode the compact array here and convert to a universallist...
        ProfileGeneric pg = getAs220().getCosemObjectFactory().getProfileGeneric(ENERGY_PROFILE_OBISCODE);
        byte[] profileRawData = pg.getBufferData(fromCalendar, toCalendar);
		LoadProfileCompactArray loadProfileCompactArray = new LoadProfileCompactArray();
		loadProfileCompactArray.parse(profileRawData);
		List<LoadProfileCompactArrayEntry> loadProfileCompactArrayEntries = loadProfileCompactArray.getLoadProfileCompactArrayEntries();

        List<IntervalData> intervalDatas = profileBuilder.buildIntervalData(scalerunit,loadProfileCompactArrayEntries);
        profileData.setIntervalDatas(intervalDatas);

        if (includeEvents) {
			EventLogs eventLogs = new EventLogs(getAs220());
			List<MeterEvent> meterEvents = eventLogs.getEventLog(fromCalendar, toCalendar);
			profileData.setMeterEvents(meterEvents);
			profileData.applyEvents(getAs220().getProfileInterval() / SEC_PER_MIN);
        }

        profileData.sort();
        return profileData;

    }

    /**
     * @return
     * @throws IOException
     */
    public int getNrOfChannels() throws IOException {
    	int nrOfChannels = 0;
    	List<CapturedObject> co = getAs220().getCosemObjectFactory().getProfileGeneric(ENERGY_PROFILE_OBISCODE).getCaptureObjects();
    	for (CapturedObject capturedObject : co) {
			if (capturedObject.getLogicalName().getObisCode().getA() != 0) {
				nrOfChannels++;
			}
		}
    	return nrOfChannels;
    }

    /**
     * Getter for the profileInterval
     *
     * @return the profileInterval
     * @throws IOException if the interval can not be fetched from the meter
     */
    public int getProfileInterval() throws IOException {
        if (interval == -1) {
            interval = getAs220().getCosemObjectFactory().getProfileGeneric(ENERGY_PROFILE_OBISCODE).getCapturePeriod();
}
        return interval;
    }
}

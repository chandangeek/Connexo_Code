package com.energyict.protocolimpl.dlms.as220.emeter;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.ClockController;
import com.energyict.protocolimpl.base.ContactorController;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.EventLogs;

public class EMeter {

	private static final int SEC_PER_MIN = 60;
	private static final ObisCode ENERGY_PROFILE_OBISCODE = ObisCode.fromString("1.1.99.1.0.255");

	private final AS220 as220;
	private final ClockController clockController;
	private final ContactorController contactorController;

	public EMeter(AS220 as220) {
		this.as220 = as220;
		this.clockController = new AS220ClockController(as220);
		this.contactorController = new AS220ContactorController(as220);
	}

	public ClockController getClockController() {
		return clockController;
	}

	public ContactorController getContactorController() {
		return contactorController;
	}

	public AS220 getAs220() {
		return as220;
	}

    /**
     * Read the profile dta from the device
     *
     * @param fromCalendar
     * @param toCalendar
     * @param includeEvents
     * @return the {@link ProfileData}
     * @throws IOException
     */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {

		Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getAs220().getTimeZone());
		Calendar toCalendar = ProtocolUtils.getCleanCalendar(getAs220().getTimeZone());
		fromCalendar.setTime(from);
		toCalendar.setTime(to);

    	ProfileBuilder profileBuilder = new ProfileBuilder(getAs220());
		ProfileData profileData = new ProfileData();
		ScalerUnit[] scalerunit = profileBuilder.buildScalerUnits((byte) getAs220().getNumberOfChannels());

        List<ChannelInfo> channelInfos = profileBuilder.buildChannelInfos(scalerunit);
        profileData.setChannelInfos(channelInfos);

        // decode the compact array here and convert to a universallist...
        ProfileGeneric pg = getAs220().getCosemObjectFactory().getProfileGeneric(ENERGY_PROFILE_OBISCODE);
		LoadProfileCompactArray loadProfileCompactArray = new LoadProfileCompactArray();
		loadProfileCompactArray.parse(pg.getBufferData(fromCalendar, toCalendar));
		List<LoadProfileCompactArrayEntry> loadProfileCompactArrayEntries = loadProfileCompactArray.getLoadProfileCompactArrayEntries();

        List<IntervalData> intervalDatas = profileBuilder.buildIntervalData(scalerunit,loadProfileCompactArrayEntries);
        profileData.setIntervalDatas(intervalDatas);

        if (includeEvents) {
			EventLogs eventLogs = new EventLogs(getAs220());
			List<MeterEvent> meterEvents = eventLogs.getEventLog(fromCalendar, toCalendar);
			for (MeterEvent meterEvent : meterEvents) {
				System.out.println("Events: " + meterEvent);
			}

			profileData.setMeterEvents(meterEvents);
			profileData.applyEvents(getAs220().getProfileInterval() / SEC_PER_MIN);
        }

        profileData.sort();
        return profileData;

    } // private ProfileData doGetDemandValues(Calendar fromCalendar,Calendar toCalendar, byte bNROfChannels) throws IOException

}

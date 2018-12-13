package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.profiles;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.DSMRProfileIntervalStatusBits;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.WatchTalk;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.WatchTalkProperties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.profiles.Dsmr40LoadProfileBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

/**
 * @author sva
 * @since 20/03/2014 - 15:50
 */
public class WatchTalkLoadProfileBuilder extends Dsmr40LoadProfileBuilder {

    public WatchTalkLoadProfileBuilder(AbstractSmartNtaProtocol meterProtocol) {
        super(meterProtocol);
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        List<ProfileData> profileDataList = new ArrayList<ProfileData>();
        ProfileGeneric profile;
        ProfileData profileData;
        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = getMeterProtocol().getPhysicalAddressCorrectedObisCode(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            LoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (getChannelInfoMap().containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                getMeterProtocol().getLogger().log(Level.INFO, "Getting LoadProfile data for " + lpr + " from " + lpr.getStartReadingTime() + " to " + lpr.getEndReadingTime());
                profile = getMeterProtocol().getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                profileData = new ProfileData(lpr.getLoadProfileId());
                profileData.setChannelInfos(getChannelInfoMap().get(lpr));
                Calendar fromCalendar = Calendar.getInstance(getMeterProtocol().getTimeZone());
                fromCalendar.setTime(lpr.getStartReadingTime());
                Calendar toCalendar = Calendar.getInstance(getMeterProtocol().getTimeZone());
                toCalendar.setTime(lpr.getEndReadingTime());

                DLMSProfileIntervals intervals = new DLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar), DLMSProfileIntervals.DefaultClockMask,
                        getStatusMasksMap().get(lpr), getChannelMaskMap().get(lpr), new DSMRProfileIntervalStatusBits(ignoreDstStatusCode()));
                profileData.setIntervalDatas(intervals.parseIntervals(lpc.getProfileInterval(), getMeterProtocol().getTimeZone())); // The TimeZone must be specified, or else the parsed time will be wrong!

                profileDataList.add(profileData);
            }
        }

        return profileDataList;
    }

    private boolean ignoreDstStatusCode() {
        return ((WatchTalkProperties) ((WatchTalk) getMeterProtocol()).getProperties()).ignoreDstStatusCode();
    }
}

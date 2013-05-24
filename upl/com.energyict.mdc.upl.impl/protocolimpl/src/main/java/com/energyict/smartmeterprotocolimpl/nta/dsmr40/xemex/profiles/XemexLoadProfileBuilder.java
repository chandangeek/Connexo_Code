package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.profiles;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGDLMSProfileIntervals;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGLoadProfileBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

/**
 * @author sva
 * @since 3/05/13 - 17:01
 */
public class XemexLoadProfileBuilder extends LGLoadProfileBuilder {

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     */
    public XemexLoadProfileBuilder(AbstractSmartNtaProtocol meterProtocol) {
        super(meterProtocol);
    }

    @Override
    public List<ProfileData> getLoadProfileData(final List<LoadProfileReader> loadProfiles) throws IOException {
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

                XemexDSMRProfileIntervals intervals = new XemexDSMRProfileIntervals(profile.getBufferData(fromCalendar, toCalendar), LGDLMSProfileIntervals.DefaultClockMask,
                        getStatusMasksMap().get(lpr), -1, getProfileIntervalStatusBits());
                profileData.setIntervalDatas(intervals.parseIntervals(lpc.getProfileInterval(), getMeterProtocol().getTimeZone()));

                profileDataList.add(profileData);
            }
        }

        return profileDataList;
    }

    @Override
    public ProfileIntervalStatusBits getProfileIntervalStatusBits() {
        return new XemexDSMRProfileIntervalStatusBits();
    }
}

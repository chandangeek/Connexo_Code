package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.DSMRProfileIntervalStatusBits;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.profiles.Dsmr40LoadProfileBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * The L&G E350 uses a custom {@link LoadProfileBuilder} because their interpretation of the deviation in the dateTime is different then the default.
 * <p/>
 * <pre>
 * Copyrights EnergyICT
 * Date: 13-okt-2011
 * Time: 11:55:59
 * </pre>
 */
public class LGLoadProfileBuilder extends Dsmr40LoadProfileBuilder {

    /**
     * If true, subtract 15 minutes from the to date to read out MBus load profiles
     */
    private boolean fixMBusToDate = true;

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     */
    public LGLoadProfileBuilder(AbstractSmartNtaProtocol meterProtocol) {
        super(meterProtocol);
    }

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link com.energyict.protocol.LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned in the <CODE>ProfileData</CODE>.
     * </p>
     * <p>
     * <b>Implementors should throw an exception if all data since {@link com.energyict.protocol.LoadProfileReader#getStartReadingTime()} can NOT be fetched</b>,
     * as the collecting system will update its lastReading setting based on the returned ProfileData
     * </p>
     *
     * @param loadProfiles a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return a list of <CODE>ProfileData</CODE> objects containing interval records
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    @Override
    public List<ProfileData> getLoadProfileData(final List<LoadProfileReader> loadProfiles) throws IOException {
        List<ProfileData> profileDataList = new ArrayList<ProfileData>();
        ProfileGeneric profile;
        ProfileData profileData;
        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = getMeterProtocol().getPhysicalAddressCorrectedObisCode(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            LoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (getChannelInfoMap().containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                Date toDate = lpr.getEndReadingTime();
                if (fixMBusToDate && !lpr.getMeterSerialNumber().equals(getMeterProtocol().getSerialNumber())) {     //MBus load profile
                    toDate.setTime(toDate.getTime() - 900000);                                      //Read to current time - 15 minutes, see RFC 168
                }
                fixMBusToDate = true;       //Reset to default for next LP requests
                getMeterProtocol().getLogger().log(Level.INFO, "Getting LoadProfile data for " + lpr + " from " + lpr.getStartReadingTime() + " to " + toDate);
                profile = getMeterProtocol().getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                enableDsmr40SelectiveAccessFormat(profile);
                profileData = new ProfileData(lpr.getLoadProfileId());
                profileData.setChannelInfos(getChannelInfoMap().get(lpr));

                DLMSProfileIntervals intervals = getDLMSProfileIntervals(profile, lpr, getCalendar(lpr.getStartReadingTime()), getCalendar(toDate));
                profileData.setIntervalDatas(intervals.parseIntervals(lpc.getProfileInterval(), getMeterProtocol().getTimeZone()));

                profileDataList.add(profileData);
            }
        }

        return profileDataList;
    }

    protected Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance(getMeterProtocol().getTimeZone());
        calendar.setTime(date);
        return calendar;
    }

    protected DLMSProfileIntervals getDLMSProfileIntervals(ProfileGeneric profile, LoadProfileReader lpr, Calendar fromCalendar, Calendar toCalendar) throws IOException {
        return new LGDLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar), LGDLMSProfileIntervals.DefaultClockMask,
                getStatusMasksMap().get(lpr), this.channelMaskMap.get(lpr), getProfileIntervalStatusBits());
    }

    protected void enableDsmr40SelectiveAccessFormat(ProfileGeneric profile) {
        profile.setDsmr4SelectiveAccessFormat(true);
    }

    public void setFixMBusToDate(boolean fixMBusToDate) {
        this.fixMBusToDate = fixMBusToDate;
    }

    public ProfileIntervalStatusBits getProfileIntervalStatusBits() {
        return new DSMRProfileIntervalStatusBits();
    }
}

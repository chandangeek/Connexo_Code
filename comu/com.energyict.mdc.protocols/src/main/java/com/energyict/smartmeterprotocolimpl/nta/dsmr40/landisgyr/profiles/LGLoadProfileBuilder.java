/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.DSMRProfileIntervalStatusBits;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.profiles.Dsmr40LoadProfileBuilder;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

public class LGLoadProfileBuilder extends Dsmr40LoadProfileBuilder {

    /**
     * If true, subtract 15 minutes from the to date to read out MBus load profiles
     */
    private boolean fixMBusToDate = true;

    public LGLoadProfileBuilder(AbstractSmartNtaProtocol meterProtocol, MdcReadingTypeUtilService readingTypeUtilService) {
        super(meterProtocol, readingTypeUtilService);
    }

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned in the <CODE>ProfileData</CODE>.
     * </p>
     * <p>
     * <b>Implementors should throw an exception if all data since {@link LoadProfileReader#getStartReadingTime()} can NOT be fetched</b>,
     * as the collecting system will update its lastReading setting based on the returned ProfileData
     * </p>
     *
     * @param loadProfiles a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return a list of <CODE>ProfileData</CODE> objects containing interval records
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    @Override
    public List<ProfileData> getLoadProfileData(final List<LoadProfileReader> loadProfiles) throws IOException {
        List<ProfileData> profileDataList = new ArrayList<>();
        ProfileGeneric profile;
        ProfileData profileData;
        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = getMeterProtocol().getPhysicalAddressCorrectedObisCode(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            LoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (getChannelInfoMap().containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                Instant toDate = lpr.getEndReadingTime();
                if (fixMBusToDate && !lpr.getMeterSerialNumber().equals(getMeterProtocol().getSerialNumber())) {     //MBus load profile
                    toDate = toDate.minus(Duration.ofMinutes(15));                                      //Read to current time - 15 minutes, see RFC 168
                }
                fixMBusToDate = true;       //Reset to default for next LP requests
                getMeterProtocol().getLogger().log(Level.INFO, "Getting LoadProfile data for " + lpr + " from " + lpr.getStartReadingTime() + " to " + toDate);
                profile = getMeterProtocol().getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                profile.setDsmr4SelectiveAccessFormat(true);
                profileData = new ProfileData(lpr.getLoadProfileId());
                profileData.setChannelInfos(getChannelInfoMap().get(lpr));
                Calendar fromCalendar = Calendar.getInstance(getMeterProtocol().getTimeZone());
                fromCalendar.setTimeInMillis(lpr.getStartReadingTime().toEpochMilli());
                Calendar toCalendar = Calendar.getInstance(getMeterProtocol().getTimeZone());
                toCalendar.setTimeInMillis(toDate.toEpochMilli());

                LGDLMSProfileIntervals intervals = new LGDLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar), LGDLMSProfileIntervals.DefaultClockMask,
                        getStatusMasksMap().get(lpr), this.channelMaskMap.get(lpr), getProfileIntervalStatusBits());
                profileData.setIntervalDatas(intervals.parseIntervals(lpc.getProfileInterval(), getMeterProtocol().getTimeZone()));

                profileDataList.add(profileData);
            }
        }

        return profileDataList;
    }

    public void setFixMBusToDate(boolean fixMBusToDate) {
        this.fixMBusToDate = fixMBusToDate;
    }

    public ProfileIntervalStatusBits getProfileIntervalStatusBits() {
        return new DSMRProfileIntervalStatusBits();
    }
}

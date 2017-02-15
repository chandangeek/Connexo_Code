/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.nta.dsmr40.landisgyr.profiles;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.ResultType;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.dsmr40.common.profiles.Dsmr40LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.DSMRProfileIntervalStatusBits;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGDLMSProfileIntervals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

public class LGLoadProfileBuilder extends Dsmr40LoadProfileBuilder {

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     */
    public LGLoadProfileBuilder(AbstractDlmsProtocol meterProtocol, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, boolean supportsBulkRequests, CollectedDataFactory collectedDataFactory) {
        super(meterProtocol, issueService, readingTypeUtilService, supportsBulkRequests, collectedDataFactory);
    }

    /**
     * Difference: uses Dsmr4SelectiveAccessFormat, LGDLMSProfileIntervals and DSMRProfileIntervalStatusBits
     * <p/>
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels(LoadProfileReader#channelInfos) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned in the <CODE>ProfileData</CODE>.
     * </p>
     * <p>
     * <b>Implementors should throw an exception if all data since LoadProfileReader#getStartReadingTime can NOT be fetched</b>,
     * as the collecting system will update its lastReading setting based on the returned ProfileData
     * </p>
     *
     * @param loadProfiles a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return a list of <CODE>ProfileData</CODE> objects containing interval records
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> collectedLoadProfileList = new ArrayList<>();
        ProfileGeneric profile;
        ProfileData profileData;

        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            CollectedLoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            CollectedLoadProfile collectedLoadProfile = getCollectedDataFactory().createCollectedLoadProfile(lpr.getLoadProfileIdentifier());

            if (this.getChannelInfoMap().containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                this.getMeterProtocol().getLogger().log(Level.FINE, () -> "Getting LoadProfile data for " + lpr + " from " + lpr.getStartReadingTime() + " to " + lpr.getEndReadingTime());

                try {
                    List<ChannelInfo> channelInfos = this.getChannelInfoMap().get(lpr);
                    profile = this.getMeterProtocol().getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                    profile.setDsmr4SelectiveAccessFormat(true);
                    profileData = new ProfileData(lpr.getLoadProfileId());
                    profileData.setChannelInfos(channelInfos);
                    Calendar fromCalendar = Calendar.getInstance(this.getMeterProtocol().getTimeZone());
                    fromCalendar.setTimeInMillis(lpr.getStartReadingTime().toEpochMilli());
                    Calendar toCalendar = Calendar.getInstance(this.getMeterProtocol().getTimeZone());
                    toCalendar.setTimeInMillis(lpr.getEndReadingTime().toEpochMilli());

                    LGDLMSProfileIntervals intervals = new LGDLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar), LGDLMSProfileIntervals.DefaultClockMask,
                            getStatusMasksMap().get(lpr), getChannelMaskMap().get(lpr), getProfileIntervalStatusBits());
                    List<IntervalData> collectedIntervalData = intervals.parseIntervals(lpc.getProfileInterval(), getMeterProtocol().getTimeZone());

                    collectedLoadProfile.setCollectedData(collectedIntervalData, channelInfos);
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, getMeterProtocol().getDlmsSession())) {
                        Issue problem = getIssueService().newIssueCollector().addProblem(lpr, MessageSeeds.LOADPROFILE_ISSUE.getKey(), lpr.getProfileObisCode(), e);
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
            } else {
                Issue problem = getIssueService().newIssueCollector().addWarning(lpr, MessageSeeds.LOADPROFILE_NOT_SUPPORTED.getKey(), lpr.getProfileObisCode());
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }
            collectedLoadProfileList.add(collectedLoadProfile);
        }
        return collectedLoadProfileList;
    }

    public ProfileIntervalStatusBits getProfileIntervalStatusBits() {
        return new DSMRProfileIntervalStatusBits();
    }
}
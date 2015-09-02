package com.energyict.protocolimplv2.nta.dsmr40.landisgyr.profiles;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.dsmr40.common.profiles.Dsmr40LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.DSMRProfileIntervalStatusBits;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGDLMSProfileIntervals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/12/2014 - 9:28
 */
public class LGLoadProfileBuilder extends Dsmr40LoadProfileBuilder {

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     */
    public LGLoadProfileBuilder(AbstractDlmsProtocol meterProtocol) {
        super(meterProtocol);
    }

    /**
     * Difference: uses Dsmr4SelectiveAccessFormat, LGDLMSProfileIntervals and DSMRProfileIntervalStatusBits
     * <p/>
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
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> collectedLoadProfileList = new ArrayList<CollectedLoadProfile>();
        ProfileGeneric profile;
        ProfileData profileData;

        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            CollectedLoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            CollectedLoadProfile collectedLoadProfile = MdcManager.getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierById(lpr.getLoadProfileId(), lpr.getProfileObisCode()));

            if (this.getChannelInfoMap().containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                this.getMeterProtocol().getLogger().log(Level.INFO, "Getting LoadProfile data for " + lpr + " from " + lpr.getStartReadingTime() + " to " + lpr.getEndReadingTime());

                try {
                    List<ChannelInfo> channelInfos = this.getChannelInfoMap().get(lpr);
                    profile = this.getMeterProtocol().getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                    profile.setDsmr4SelectiveAccessFormat(true);
                    profileData = new ProfileData(lpr.getLoadProfileId());
                    profileData.setChannelInfos(channelInfos);
                    Calendar fromCalendar = Calendar.getInstance(this.getMeterProtocol().getTimeZone());
                    fromCalendar.setTime(lpr.getStartReadingTime());
                    Calendar toCalendar = Calendar.getInstance(this.getMeterProtocol().getTimeZone());
                    toCalendar.setTime(lpr.getEndReadingTime());

                    LGDLMSProfileIntervals intervals = new LGDLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar), LGDLMSProfileIntervals.DefaultClockMask,
                            getStatusMasksMap().get(lpr), this.channelMaskMap.get(lpr), getProfileIntervalStatusBits());
                    List<IntervalData> collectedIntervalData = intervals.parseIntervals(lpc.getProfileInterval(), getMeterProtocol().getTimeZone());

                    collectedLoadProfile.setCollectedIntervalData(collectedIntervalData, channelInfos);
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, getMeterProtocol().getDlmsSession())) {
                        Issue<LoadProfileReader> problem = MdcManager.getIssueFactory().createProblem(lpr, "loadProfileXIssue", lpr.getProfileObisCode(), e);
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
            } else {
                Issue<LoadProfileReader> problem = MdcManager.getIssueFactory().createWarning(lpr, "loadProfileXnotsupported", lpr.getProfileObisCode());
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
package com.energyict.smartmeterprotocolimpl.elster.apollo5;

import com.energyict.dlms.OctetString;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;
import com.energyict.smartmeterprotocolimpl.elster.apollo.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 30/06/11
 * Time: 13:49
 */
public class AS300DPETLoadProfileBuilder extends AS300LoadProfileBuilder {

    public AS300DPETLoadProfileBuilder(AS300 meterProtocol) {
        super(meterProtocol);
    }

    /**
     * Read out load profile data.
     * If all channels are requested, no selective accessor for channels is used (optimization for frame size limits)
     *
     * @param loadProfiles a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return
     * @throws java.io.IOException
     */
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        List<ProfileData> profileDataList = new ArrayList<ProfileData>();
        ProfileGeneric profile;
        ProfileData profileData;
        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = lpr.getProfileObisCode();
            LoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (this.channelInfoMap.containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                this.meterProtocol.getLogger().log(Level.INFO, "Getting LoadProfile data for " + lpr + " from " + lpr.getStartReadingTime() + " to " + lpr.getEndReadingTime());
                profile = this.meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                profileData = new ProfileData(lpr.getLoadProfileId());
                profileData.setChannelInfos(this.channelInfoMap.get(lpr));
                Calendar fromCalendar = Calendar.getInstance(this.meterProtocol.getTimeZone());
                fromCalendar.setTime(lpr.getStartReadingTime());
                Calendar toCalendar = Calendar.getInstance(this.meterProtocol.getTimeZone());
                toCalendar.setTime(lpr.getEndReadingTime());

                List<CapturedObject> channels = capturedObjectsToRequest.get(lpObisCode);
                if (channels == null) {
                    continue;
                }
                DLMSProfileIntervals intervals;
                if (isRequestAllChannels(channels, lpr.getProfileObisCode())) {
                    intervals = new DLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar), masks.get(lpr).getClockMask(), masks.get(lpr).getStatusMask(), -1, new AS300ProfileIntervalStatusBits());
                } else {   //Selective channel access
                    intervals = new DLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar, channels), masks.get(lpr).getClockMask(), masks.get(lpr).getStatusMask(), -1, new AS300ProfileIntervalStatusBits());
                }
                profileData.setIntervalDatas(intervals.parseIntervals(lpc.getProfileInterval()));

                profileDataList.add(profileData);
            }
        }

        return profileDataList;
    }

    @Override
    protected boolean isStatus(ObisCode obisCode) {
        return obisCode.equals(AS300ObisCodeProvider.Apollo5LoadProfileStatusEncrypted) || obisCode.equals(AS300ObisCodeProvider.Apollo5LoadProfileStatusHourly) || obisCode.equals(AS300ObisCodeProvider.Apollo5LoadProfileStatusDaily) || super.isStatus(obisCode);
    }

    private boolean isRequestAllChannels(List<CapturedObject> requestedCapturedObjects, ObisCode loadProfileObisCode) {
        List<CapturedObject> meterCapturedObjects = capturedObjectsMap.get(loadProfileObisCode);
        if (meterCapturedObjects != null) {
            if (meterCapturedObjects.size() == requestedCapturedObjects.size()) {
                for (int index = 0; index < meterCapturedObjects.size(); index++) {
                    CapturedObject meterCapturedObject = meterCapturedObjects.get(index);
                    CapturedObject requestedCapturedObject = requestedCapturedObjects.get(index);
                    if (!isStatus(meterCapturedObject.getObisCode()) && (!isClock(meterCapturedObject))) {
                        LogicalName logicalName = requestedCapturedObject.getLogicalName();
                        byte[] ln = logicalName.getObisCode().getLN();
                        ln[5] = (byte) 0xFF;
                        LogicalName logicalName2 = new LogicalName(new OctetString(ln));

                        if (!logicalName2.getObisCode().equals(meterCapturedObject.getLogicalName().getObisCode())) {
                            return false;
                        }
                    }
                }
                return true;    //If all captured objects are requested
            }
        }
        return false;
    }

}
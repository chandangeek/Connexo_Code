package com.energyict.protocolimpl.dlms.as220.powerquality;

import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.emeter.LoadProfileCompactArray;
import com.energyict.protocolimpl.dlms.as220.emeter.LoadProfileCompactArrayEntry;

import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 11-okt-2010
 * Time: 10:29:24
 * To change this template use File | Settings | File Templates.
 */
public class PowerQuality {

    private static final ObisCode POWER_QUALITY_PROFILE_OBISCODE = ObisCode.fromString("1.1.99.1.0.255");

    //TODO we can not hardcode the ShortName address, Razvan has to fix it in the ObjectList
    private static final int POWER_QUALITY_PROFILE_SN = 4600;

    private final AS220 as220;

    /** Represents the number of channels of the profile */
    private int nbOfChannels = -1;

    public PowerQuality(AS220 as220) {
        this.as220 = as220;
    }

    public ProfileData getPowerQualities(Date from, Date to) throws IOException {
        ProfileGeneric pg = getGenericPowerQualityProfile();
        Calendar fromCal = Calendar.getInstance(getAs220().getTimeZone());
        fromCal.setTime(from);

        Calendar toCal = Calendar.getInstance(getAs220().getTimeZone());
        toCal.setTime(to);

        ProfileData profileData = new ProfileData();
        PowerQualityProfileBuilder pqpb = new PowerQualityProfileBuilder(this);
        ScalerUnit[] scalerunit = pqpb.buildScalerUnits((byte) getNrOfChannels());

        List<ChannelInfo> channelInfos = pqpb.buildChannelInfos(scalerunit);
        profileData.setChannelInfos(channelInfos);

        byte[] profile = pg.getBufferData(fromCal, toCal);

        LoadProfileCompactArray loadProfileCompactArray = new LoadProfileCompactArray();
        loadProfileCompactArray.parse(profile);
        List<LoadProfileCompactArrayEntry> loadProfileCompactArrayEntries = loadProfileCompactArray.getLoadProfileCompactArrayEntries();

        List<IntervalData> intervalDatas = pqpb.buildIntervalData(scalerunit,loadProfileCompactArrayEntries);
        profileData.setIntervalDatas(intervalDatas);

        profileData.sort();
        return profileData;
    }

    public int getNrOfChannels() throws IOException {
        if(this.nbOfChannels == -1){
            List<CapturedObject> co = getGenericPowerQualityProfile().getCaptureObjects();

            nbOfChannels = 0;
            for (CapturedObject capturedObject : co) {
                if (capturedObject.getLogicalName().getObisCode().getD() == 7) {
                    nbOfChannels++;
                }
            }

        }
        return nbOfChannels;
    }

    /**
     * Getter for the AS220
     *
     * @return the current AS220
     */
    public AS220 getAs220() {
        return this.as220;
    }

    public ProfileGeneric getGenericPowerQualityProfile() throws IOException {
        return getAs220().getCosemObjectFactory().getProfileGeneric(POWER_QUALITY_PROFILE_SN);
    }
}

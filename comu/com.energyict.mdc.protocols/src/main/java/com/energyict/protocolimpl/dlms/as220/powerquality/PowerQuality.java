/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220.powerquality;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocolimpl.dlms.as220.AS220;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 11-okt-2010
 * Time: 10:29:24
 * To change this template use File | Settings | File Templates.
 */
public class PowerQuality {

    private static final ObisCode POWER_QUALITY_PROFILE_OBISCODE = ObisCode.fromString("1.0.99.13.0.255");



    private final AS220 as220;
    private PowerQualityProfileBuilder pqpb;


    public PowerQuality(AS220 as220) {
        this.as220 = as220;
        this.pqpb = new PowerQualityProfileBuilder(this);
    }

    public ProfileData getPowerQualities(Date from, Date to) throws IOException {
        ProfileGeneric pg = getGenericPowerQualityProfile();
        Calendar fromCal = Calendar.getInstance(getAs220().getTimeZone());
        fromCal.setTime(from);

        Calendar toCal = Calendar.getInstance(getAs220().getTimeZone());
        toCal.setTime(to);

        ProfileData profileData = new ProfileData();
        ScalerUnit[] scalerUnit = pqpb.buildScalerUnits();

        List<ChannelInfo> channelInfos = pqpb.buildChannelInfos(scalerUnit);
        profileData.setChannelInfos(channelInfos);

        DataContainer dc = pg.getBuffer(fromCal, toCal);

        profileData.setIntervalDatas(pqpb.buildIntervalData(dc, as220));

        profileData.sort();
        return profileData;
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
        return getAs220().getCosemObjectFactory().getProfileGeneric(POWER_QUALITY_PROFILE_OBISCODE);
    }

    /**
     * Get the number of channels from the PowerQualityProfileBuilder
     * @return the number of channels
     * @throws IOException when an exception occurs during reading
     */
    public int getNrOfChannels() throws IOException {
        return this.pqpb.getNrOfChannels();
    }

    /**
     * Getter for the PowerQualityLoadProfile
     * @return the interval of the loadProfile
     * @throws IOException
     */
    public int getProfileInterval() throws IOException {
        return this.pqpb.getProfileInterval();
}
}

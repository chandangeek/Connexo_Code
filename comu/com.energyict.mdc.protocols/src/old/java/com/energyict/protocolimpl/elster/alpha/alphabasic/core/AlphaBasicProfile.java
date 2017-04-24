/*
 * AlphaPlusProfile.java
 *
 * Created on 26 juli 2005, 15:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.elster.alpha.alphabasic.AlphaBasic;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.Class14LoadProfileConfiguration;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.Class17LoadProfileData;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author koen
 */
public class AlphaBasicProfile {
    AlphaBasic alphaBasic;
    /** Creates a new instance of AlphaPlusProfile */
    public AlphaBasicProfile(AlphaBasic alphaBasic) {
        this.alphaBasic=alphaBasic;
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        int nrOfDays = getNrOfDays(lastReading);
        Class17LoadProfileData class17LoadProfileData = alphaBasic.getClassFactory().getClass17LoadProfileData(nrOfDays);
        Class14LoadProfileConfiguration class14LoadProfileConfiguration = alphaBasic.getClassFactory().getClass14LoadProfileConfiguration();
        ProfileData profileData = new ProfileData();

        // build ChannelInfos
        for (int profileChannel=0;profileChannel<alphaBasic.getNumberOfChannels();profileChannel++) {
            Unit unit=Unit.get("");

            if (alphaBasic.getProtocolChannelMap()!=null) {
                if (alphaBasic.getProtocolChannelMap().isProtocolChannel(profileChannel) &&
                    (alphaBasic.getProtocolChannelMap().getProtocolChannel(profileChannel).getValue()==1)) {
                    // demand
                    unit = class14LoadProfileConfiguration.getUnit(profileChannel).getFlowUnit();
                }
                else if (alphaBasic.getProtocolChannelMap().isProtocolChannel(profileChannel) &&
                    (alphaBasic.getProtocolChannelMap().getProtocolChannel(profileChannel).getValue()==2)) {
                    // energy
                    unit = class14LoadProfileConfiguration.getUnit(profileChannel).getVolumeUnit();
                }
            }

            ChannelInfo channelInfo = new ChannelInfo(class14LoadProfileConfiguration.getMeterChannelIndex(profileChannel),"ELSTER ALPHA+ CHANNEL "+profileChannel,unit);
            profileData.addChannel(channelInfo);
        }

        // build Intervaldatas
        profileData.setIntervalDatas(class17LoadProfileData.getIntervalDatas());

        // build MeterEvents
        if (includeEvents) {
           profileData.setMeterEvents(alphaBasic.getClassFactory().getClass16LoadProfileHistory().getMeterEvents());
           profileData.applyEvents(alphaBasic.getProfileInterval()/60);
        }

        profileData.sort();

        return profileData;
    }

    private int getNrOfDays(Date lastReading) throws IOException {
        Date now = new Date();
        int nrOfDays = ParseUtils.getNrOfDays(lastReading,now,alphaBasic.getTimeZone());
        if (nrOfDays > alphaBasic.getClassFactory().getClass14LoadProfileConfiguration().getLPMEM()) {
            alphaBasic.getLogger().warning("AlphaPlusProfile, getNrOfDays(lastReading), requesting for "+nrOfDays+" days of profile data while the maximum days of profiledata in the meter is "+alphaBasic.getClassFactory().getClass14LoadProfileConfiguration().getLPMEM()+". So, limiting the nr of days to request to "+alphaBasic.getClassFactory().getClass14LoadProfileConfiguration().getLPMEM());
            nrOfDays = alphaBasic.getClassFactory().getClass14LoadProfileConfiguration().getLPMEM();
        }
        return nrOfDays;
    }


}

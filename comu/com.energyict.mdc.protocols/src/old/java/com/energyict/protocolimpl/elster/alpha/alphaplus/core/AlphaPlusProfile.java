/*
 * AlphaPlusProfile.java
 *
 * Created on 26 juli 2005, 15:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.elster.alpha.alphaplus.AlphaPlus;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.Class14LoadProfileConfiguration;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.Class17LoadProfileData;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.DayRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author koen
 */
public class AlphaPlusProfile {
    AlphaPlus alphaPlus;
    /** Creates a new instance of AlphaPlusProfile */
    public AlphaPlusProfile(AlphaPlus alphaPlus) {
        this.alphaPlus=alphaPlus;
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        int nrOfDays = getNrOfDays(lastReading);
        Class17LoadProfileData class17LoadProfileData = alphaPlus.getClassFactory().getClass17LoadProfileData(nrOfDays);
        Class14LoadProfileConfiguration class14LoadProfileConfiguration = alphaPlus.getClassFactory().getClass14LoadProfileConfiguration();
        ProfileData profileData = new ProfileData();

        // build ChannelInfos
        for (int profileChannel=0;profileChannel<alphaPlus.getNumberOfChannels();profileChannel++) {
            Unit unit=Unit.get("");

            if (alphaPlus.getProtocolChannelMap()!=null) {
                if (alphaPlus.getProtocolChannelMap().isProtocolChannel(profileChannel) &&
                    (alphaPlus.getProtocolChannelMap().getProtocolChannel(profileChannel).getValue()==1)) {
                    // demand
                    unit = class14LoadProfileConfiguration.getUnit(profileChannel).getFlowUnit();
                }
                else if (alphaPlus.getProtocolChannelMap().isProtocolChannel(profileChannel) &&
                    (alphaPlus.getProtocolChannelMap().getProtocolChannel(profileChannel).getValue()==2)) {
                    // energy
                    unit = class14LoadProfileConfiguration.getUnit(profileChannel).getVolumeUnit();
                }
            }

            ChannelInfo channelInfo = new ChannelInfo(class14LoadProfileConfiguration.getMeterChannelIndex(profileChannel),"ELSTER ALPHA+ CHANNEL "+profileChannel,unit);
            profileData.addChannel(channelInfo);
        }

        // build Intervaldatas
        List intervalDatas= new ArrayList();
        Iterator it = class17LoadProfileData.getDayRecords().iterator();
        while(it.hasNext()) {
            DayRecord dayRecord = (DayRecord)it.next();
            intervalDatas.addAll(dayRecord.getIntervalDatas());
        }
        profileData.setIntervalDatas(intervalDatas);

        // build MeterEvents
        if (includeEvents) {
           profileData.setMeterEvents(alphaPlus.getClassFactory().getClass16EventLogData().getMeterEvents());
           profileData.applyEvents(alphaPlus.getProfileInterval()/60);
        }

        profileData.sort();

        return profileData;
    }

    private int getNrOfDays(Date lastReading) throws IOException {
        Date now = new Date();
        int nrOfDays = ParseUtils.getNrOfDays(lastReading,now,ProtocolUtils.getWinterTimeZone(alphaPlus.getTimeZone()));
        if (nrOfDays > alphaPlus.getClassFactory().getClass14LoadProfileConfiguration().getLPMEM()) {
            alphaPlus.getLogger().warning("AlphaPlusProfile, getNrOfDays(lastReading), requesting for "+nrOfDays+" days of profile data while the maximum days of profiledata in the meter is "+alphaPlus.getClassFactory().getClass14LoadProfileConfiguration().getLPMEM()+". So, limiting the nr of days to request to "+alphaPlus.getClassFactory().getClass14LoadProfileConfiguration().getLPMEM());
            nrOfDays = alphaPlus.getClassFactory().getClass14LoadProfileConfiguration().getLPMEM();
        }
        return nrOfDays;
    }


}

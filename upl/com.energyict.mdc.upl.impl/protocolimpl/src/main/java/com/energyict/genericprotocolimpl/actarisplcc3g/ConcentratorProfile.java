/*
 * ConcentratorProfile.java
 *
 * Created on 6 december 2007, 10:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g;

import com.energyict.cbo.*;
import com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects.*;
import com.energyict.protocol.*;
import java.math.*;
import java.util.*;
import java.io.*;
/**
 *
 * @author kvds
 */
public class ConcentratorProfile {
    
    Concentrator concentrator;
    
    /** Creates a new instance of ConcentratorProfile */
    public ConcentratorProfile(Concentrator concentrator) {
        this.concentrator=concentrator;
    }
    
    public ProfileData getProfileData(Date from, boolean intervalData, boolean logbook) throws IOException {
        return getProfileData(from,new Date(),from,new Date(),intervalData,logbook);
    }
    
    public ProfileData getProfileData(Date fromLoadProfile, Date toLoadProfile, Date fromLogbook, Date toLogbook, boolean intervalData, boolean logbook) throws IOException {
         ProfileData profileData = new ProfileData();
         profileData.setChannelInfos(buildChannelInfos());
         if (intervalData)
             profileData.setIntervalDatas(buildIntervalDatas(fromLoadProfile,toLoadProfile));
         if (logbook) {
             profileData.setMeterEvents(buildMeterEvents(fromLogbook,toLogbook));
             profileData.applyEvents(concentrator.getCurrentSelectedDevice().getIntervalInSeconds()/60);
         }
         
         profileData.sort();
         return profileData;
    }
     
    private List buildChannelInfos() throws IOException {
        List channelInfos = new ArrayList();
        ChannelInfo chi = new ChannelInfo(0,0, concentrator.getSelectedMeterName()+" channel "+0, Unit.get("Wh"));
        if (concentrator.getCurrentSelectedDevice().getChannel(0).getCumulative())
              chi.setCumulativeWrapValue(BigDecimal.valueOf(0xFFFFFFFF));
        channelInfos.add(chi);
        return channelInfos;
    }
    private List buildMeterEvents(Date from, Date to) throws IOException {
        return concentrator.getPLCCObjectFactory().getPLCCMeterLogbook(from,to).getMeterEvents();
    }
    private List buildIntervalDatas(Date from,Date to) throws IOException {
        PLCCMeterLoadProfileEnergy o = concentrator.getPLCCObjectFactory().getPLCCMeterLoadProfileEnergy(from,to);
        if (concentrator.getCurrentSelectedDevice().getIntervalInSeconds() != o.getCapturePeriod())
            throw new IOException("Load profile interval in meter ("+o.getCapturePeriod()+") is different from configuration ("+concentrator.getCurrentSelectedDevice().getIntervalInSeconds()+")");
        return o.toIntervalDatas();
    }
    
}

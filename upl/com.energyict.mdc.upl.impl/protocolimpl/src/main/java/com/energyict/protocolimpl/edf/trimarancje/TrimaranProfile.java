/*
 * TrimeranProfile.java
 *
 * Created on 27 juni 2006, 9:14
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje;

import java.io.IOException;

import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.edf.trimarancje.core.DemandData;

/**
 *
 * @author Koen
 */
public class TrimaranProfile {
    
    Trimaran trimaran;
    
    /** Creates a new instance of TrimeranProfile */
    public TrimaranProfile(Trimaran trimaran) {
        this.trimaran=trimaran;
    }
    
    
    public ProfileData getProfileData() throws IOException {
        ProfileData profileData = new ProfileData();
        DemandData demandData = trimaran.getDataFactory().getDemandData();
        profileData.setChannelInfos(demandData.getChannelInfos());
        profileData.setIntervalDatas(demandData.getIntervalDatas());
        return profileData;
    }
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TrimeranProfile.java
 *
 * Created on 27 juni 2006, 9:14
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran;

import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.protocolimpl.edf.trimaran.core.DemandData;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class TrimaranProfile {

    Trimaran trimeran;

    /** Creates a new instance of TrimeranProfile */
    public TrimaranProfile(Trimaran trimeran) {
        this.trimeran=trimeran;
    }


    public ProfileData getProfileData() throws IOException {
        ProfileData profileData = new ProfileData();
        DemandData demandData = trimeran.getDataFactory().getDemandData();
        profileData.setChannelInfos(demandData.getChannelInfos());
        profileData.setIntervalDatas(demandData.getIntervalDatas());
        return profileData;
    }
}

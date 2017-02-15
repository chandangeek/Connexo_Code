/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * S4Profile.java
 *
 * Created on 6 juni 2006, 15:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom;

import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.LoadProfileDataCommand;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class S4Profile {

    final int DEBUG=0;
    private S4 s4;

    /** Creates a new instance of S4Profile */
    public S4Profile(S4 s4) {
        this.setS4(s4);
    }

    public S4 getS4() {
        return s4;
    }

    private void setS4(S4 s4) {
        this.s4 = s4;
    }


    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        if (getS4().getCommandFactory().getTOUAndLoadProfileOptions().isLoadProfileActive()) {
            ProfileData profileData = new ProfileData();
            // to is not used
            int memorySize = getS4().getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getLoadProfileMemorySize(from);

            if (DEBUG>=1) {
                System.out.println("KV_DEBUG> memorySize=" + memorySize);
            }

            LoadProfileDataCommand lpdc = getS4().getCommandFactory().getLoadProfileDataCommand(memorySize);

            if (DEBUG>=1) {
                System.out.println("KV_DEBUG> lpdc=" + lpdc);
            }

            profileData.setIntervalDatas(lpdc.getIntervalDatas());
            profileData.setChannelInfos(lpdc.getChannelInfos());

            if (includeEvents) {
                profileData.setMeterEvents(lpdc.getMeterEvents());
                // KV_TO_DO read the event log...
            }

            return profileData;
        }
        else {
            throw new IOException("Load profile not enabled in meter!");
        }

    }

}
/*
 * S4sProfile.java
 *
 * Created on 6 juni 2006, 15:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom;

import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.command.LoadProfileDataCommand;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class S4sProfile {

    final int DEBUG=0;
    private S4s s4s;

    /** Creates a new instance of S4sProfile */
    public S4sProfile(S4s s4s) {
        this.setS4s(s4s);
    }

    public S4s getS4s() {
        return s4s;
    }

    private void setS4s(S4s s4s) {
        this.s4s = s4s;
    }


    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {


        if (getS4s().getCommandFactory().getTOUAndLoadProfileOptions().isLoadProfileActive()) {
            ProfileData profileData = new ProfileData();
            // to is not used
            int memorySize = getS4s().getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getLoadProfileMemorySize(from);

            if (DEBUG>=1) System.out.println("KV_DEBUG> memorySize="+memorySize);

            LoadProfileDataCommand lpdc = getS4s().getCommandFactory().getLoadProfileDataCommand(memorySize);

            if (DEBUG>=1) System.out.println("KV_DEBUG> lpdc="+lpdc);

            profileData.setIntervalDatas(lpdc.getIntervalDatas());
            profileData.setChannelInfos(lpdc.getChannelInfos());

            if (includeEvents) {
                profileData.setMeterEvents(lpdc.getMeterEvents());
                // KV_TO_DO read the event log...
            }

            return profileData;
        }
        else throw new IOException("Load profile not enabled in meter!");



    } // public ProfileData getProfileData(Date from, Date to, boolean includeEvents)


}

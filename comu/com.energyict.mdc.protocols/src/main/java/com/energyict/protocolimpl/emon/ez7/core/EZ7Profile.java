/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EZ7Profile.java
 *
 * Created on 13 mei 2005, 11:26
 */

package com.energyict.protocolimpl.emon.ez7.core;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.protocolimpl.emon.ez7.EZ7;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 *
 * @author  Koen
 */
public class EZ7Profile {

    private static final int NR_OF_CHANNELS=8;
    EZ7 ez7=null;

    /** Creates a new instance of EZ7Profile */
    public EZ7Profile(EZ7 ez7) {
        this.ez7=ez7;
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        ProfileData profileData = new ProfileData();
        List intervalDatas=new ArrayList();

        int dayBlockNr=0;
        for (int i=0;i<ez7.getEz7CommandFactory().getProfileStatus().getNrOfDayBlocks();i++) {
            if (ez7.getEz7CommandFactory().getProfileHeader().getBlockDate(i)!= null) {
                dayBlockNr=i;
                if (from.before(ez7.getEz7CommandFactory().getProfileHeader().getBlockDate(i))) {
                    if (dayBlockNr > 0)
                        dayBlockNr--;
                    break;
                }
            }
        }

        //if ((dayBlockNr > 0) && (dayBlockNr != (ez7.getEz7CommandFactory().getProfileStatus().getCurrentDayBlock()-1)))
        //   dayBlockNr--;

        for (int i=dayBlockNr;i<ez7.getEz7CommandFactory().getProfileStatus().getCurrentDayBlock();i++) {
            if (ez7.getEz7CommandFactory().getProfileHeader().getBlockDate(dayBlockNr)!= null) {
                intervalDatas.addAll(ez7.getEz7CommandFactory().getProfileDataCompressed(i).getIntervalDatas());
            }

        }
        profileData.setIntervalDatas(intervalDatas);

        for (int channel=0;channel<NR_OF_CHANNELS;channel++) {
           if (ez7.getEz7CommandFactory().getHookUp().isChannelEnabled(channel)) {
              // see EZ7 protocoldescription page 1-28
              ChannelInfo chi=null;
              if (ez7.getProtocolChannelMap() == null) {
                  chi = ez7.getEz7CommandFactory().getMeterInformation().getChannelInfo(channel,true);
              }
              else {
                  if (ez7.getProtocolChannelValue(channel) == -1) {
                      chi = new ChannelInfo(channel,"EZ7 channel "+(channel+1),Unit.get(""));
                  }
                  else {
                      Unit unit = ez7.getEz7CommandFactory().getMeterInformation().getUnit(channel, true);
                      chi = new ChannelInfo(channel,"EZ7 channel "+(channel+1),unit);
                  }
              }
              profileData.addChannel(chi);

           } // if (ez7.getEz7CommandFactory().getHookUp().isChannelEnabled(channel))
        }

        if (includeEvents) {
            List meterEvents = new ArrayList();
            meterEvents.addAll(ez7.getEz7CommandFactory().getEventGeneral().toMeterEvents());
            meterEvents.addAll(ez7.getEz7CommandFactory().getFlagsStatus().toMeterEvents(from, to == null ? new Date() : to));
            profileData.setMeterEvents(meterEvents);
            profileData.applyEvents(ez7.getProfileInterval()/60);
        }

        profileData.sort();
        return profileData;
    }
}

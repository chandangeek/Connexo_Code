/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ProfileDataReverser.java
 *
 * Created on 30 november 2004, 15:26
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class ProfileDataReverser extends ProfileData {


    /** Creates a new instance of ProfileDataReverser */
    public ProfileDataReverser(ProfileData profileData) {
        setIntervalDatas(profileData.getIntervalDatas());
        setChannelInfos(profileData.getChannelInfos());
        setMeterEvents(profileData.getMeterEvents());
    }

    /*
     *   Flip channeinfos and interval data. Some meters report different sequence of their channels
     *   depending on the mode used. E.g. the PRI meters have PAKNET streaming mode where all channels are
     *   in reversed sequence.
     */
    public void reverse() {
         reverseChannelInfos();
         reverseIntervalDatas();
    }

    private void reverseIntervalDatas() {
        Iterator it = getIntervalIterator();
        while(it.hasNext()) {
           reverseIntervalValues((IntervalData)it.next());
        }
    }

    private void reverseIntervalValues(IntervalData intervalData) {
        List flippedIntervalValues = new ArrayList();
        for (int i = intervalData.getIntervalValues().size()-1; i>=0 ; i--) {
           flippedIntervalValues.add(intervalData.getIntervalValues().get(i));
        }
        intervalData.setIntervalValues(flippedIntervalValues);
    }

    private void reverseChannelInfos() {
        List flippedChannelInfos = new ArrayList();
        int[] ids = new int[getChannelInfos().size()];
        int[] channelIds = new int[getChannelInfos().size()];
        for (int i = getChannelInfos().size()-1; i>=0 ; i--) {
           ChannelInfo channelInfo = getChannelInfos().get(i);
           ids[i] = channelInfo.getId();
           channelIds[i] = channelInfo.getChannelId();
           flippedChannelInfos.add(channelInfo);
        }

        // set flipped channelinfos
        setChannelInfos(flippedChannelInfos);

        // correct channelIds
        for (int i = getChannelInfos().size()-1; i>=0 ; i--) {
           ChannelInfo channelInfo = getChannelInfos().get(i);
           channelInfo.setId(ids[i]);
           channelInfo.setChannelId(channelIds[i]);
        }
    }

}

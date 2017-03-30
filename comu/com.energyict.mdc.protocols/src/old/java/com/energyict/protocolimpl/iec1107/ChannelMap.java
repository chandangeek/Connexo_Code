/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ChannelMap.java
 *
 * Created on 12 september 2003, 9:46
 */

package com.energyict.protocolimpl.iec1107;

import com.energyict.mdc.protocol.api.InvalidPropertyException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
/**
 *
 * @author  Koen
 *
 * Individual channels are separated by ':'.
 *
 */
public class ChannelMap {

    List channels = null;

    /** Creates a new instance of ChannelMap */
    public ChannelMap(String channelConfig) throws InvalidPropertyException {
        channels = new ArrayList();
        parse(channelConfig);
    }
    public ChannelMap(List channels) {
        this.channels = channels;
    }

    public String getChannelRegisterMap() {
        boolean init = true;
        StringBuffer strBuff = new StringBuffer();
        Iterator it = channels.iterator();
        while(it.hasNext()) {
           if (!init) strBuff.append(":");
           init = false;
           Channel channel = (Channel)it.next();
           strBuff.append(channel.getRegister());
        }
        return strBuff.toString();
    }

    public boolean hasEqualRegisters(ChannelMap channelMap) {
        return (getChannelRegisterMap().compareTo(channelMap.getChannelRegisterMap())==0);
    }

    public List getChannels() {
       return channels;
    }

    public int getNrOfChannels() {
        return channels.size();
    }

    public Channel getChannel(int index) {
        return (Channel)channels.get(index);
    }

    public boolean channelExists(String register) {
        Iterator it = channels.iterator();
        while(it.hasNext()) {
           Channel channel = (Channel)it.next();
           if (channel.getRegister().compareTo(register) == 0) return true;
        }
        return false;
    }

    private void parse(String channelConfig) throws InvalidPropertyException {
        int channelNr=0;
        StringTokenizer st1 = new StringTokenizer(channelConfig,":");
        while (st1.countTokens() > 0) {
            String strChannel = st1.nextToken();
            channels.add(new Channel(strChannel));
        }
    }

}
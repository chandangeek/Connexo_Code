/*
 * ChannelMap.java
 *
 * Created on 12 september 2003, 9:46
 */

package com.energyict.protocolimpl.iec1107;

import com.energyict.mdc.upl.properties.InvalidPropertyException;

import java.util.ArrayList;
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

    private List<Channel> channels = null;

    public ChannelMap(String channelConfig) throws InvalidPropertyException {
        channels = new ArrayList<>();
        parse(channelConfig);
    }

    public ChannelMap(List<Channel> channels) {
        this.channels = new ArrayList<>(channels);
    }

    public String getChannelRegisterMap() {
        boolean init = true;
        StringBuilder builder = new StringBuilder();
        for (Object channel1 : channels) {
            if (!init) {
                builder.append(":");
            }
            init = false;
            Channel channel = (Channel) channel1;
            builder.append(channel.getRegister());
        }
        return builder.toString();
    }

    public boolean hasEqualRegisters(ChannelMap channelMap) {
        return (getChannelRegisterMap().compareTo(channelMap.getChannelRegisterMap())==0);
    }

    public List<Channel> getChannels() {
       return channels;
    }

    public int getNrOfChannels() {
        return channels.size();
    }

    public Channel getChannel(int index) {
        return channels.get(index);
    }

    private void parse(String channelConfig) throws InvalidPropertyException {
        StringTokenizer st1 = new StringTokenizer(channelConfig, ":");
        while (st1.countTokens() > 0) {
            String strChannel = st1.nextToken();
            channels.add(new Channel(strChannel));
        }
    }

}
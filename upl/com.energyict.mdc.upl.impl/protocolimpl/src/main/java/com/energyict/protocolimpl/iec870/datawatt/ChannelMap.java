/*
 * ChannelMap.java
 *
 * Created on 14 juli 2003, 9:21
 */

package com.energyict.protocolimpl.iec870.datawatt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
/**
 *
 * @author  Koen
 * Changes:
 * KV 07052004 Check foir ':' or ',' as separator for ChannelMap entry
 */
public class ChannelMap {

    private List<Channel> channels = null;

    public ChannelMap(String channelConfig) throws IOException {
        channels = new ArrayList<>();
        parse(channelConfig);
    }

    public List<Channel> getChannels() {
       return channels;
    }

    public int getNrOfChannels() {
        return channels.size();
    }

    public Channel getChannel(int channelNr) {
        return channels.get(channelNr);
    }

    public Channel getChannel(int channelId, int channelType) {
        for (Channel ch : channels) {
            if (ch.isEqual(channelId, channelType)) {
                return ch;
            }
        }
        return null;
    }
    public Channel getChannel(Channel channel) {
        for (Channel ch : channels) {
            if (ch.isEqual(channel)) {
                return ch;
            }
        }
        return null;
    }

    private void parse(String channelConfig) throws IOException {
        StringTokenizer st1;
        if (!channelConfig.contains(":")) {
            st1 = new StringTokenizer(channelConfig, ",");
        } else {
            st1 = new StringTokenizer(channelConfig, ":");
        }
        while (st1.countTokens() > 0) {
            String strChannel = st1.nextToken();
            channels.add(Channel.parseChannel(strChannel));
        }
    }

}
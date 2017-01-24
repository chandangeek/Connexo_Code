/*
 * ChannelMap.java
 *
 * Created on 14 juli 2003, 9:21
 */

package com.energyict.protocolimpl.iec870.datawatt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
/**
 *
 * @author  Koen
 * Changes:
 * KV 07052004 Check foir ':' or ',' as separator for ChannelMap entry
 */
public class ChannelMap {

    List channels = null;

    /** Creates a new instance of ChannelMap */
    public ChannelMap(String channelConfig) throws IOException {
        channels = new ArrayList();
        parse(channelConfig);
    }

    public List getChannels() {
       return channels;
    }

    public int getNrOfChannels() {
        return channels.size();
    }

    public Channel getChannel(int channelNr) {
        return (Channel)channels.get(channelNr);
    }

    public Channel getChannel(int channelId, int channelType) {
        Iterator it = channels.iterator();
        while(it.hasNext()) {
            Channel ch = (Channel)it.next();
            if (ch.isEqual(channelId,channelType)) return ch;
        }
        return null;
    }
    public Channel getChannel(Channel channel) {
        Iterator it = channels.iterator();
        while(it.hasNext()) {
            Channel ch = (Channel)it.next();
            if (ch.isEqual(channel)) return ch;
        }
        return null;
    }

    private void parse(String channelConfig) throws IOException {
        int channelNr=0;
        StringTokenizer st1;
        if (channelConfig.indexOf(":") == -1)
           st1 = new StringTokenizer(channelConfig,",");
        else
           st1 = new StringTokenizer(channelConfig,":");

        while (st1.countTokens() > 0) {
            String strChannel = st1.nextToken();
            channels.add(Channel.parseChannel(strChannel));
        }
    }

}

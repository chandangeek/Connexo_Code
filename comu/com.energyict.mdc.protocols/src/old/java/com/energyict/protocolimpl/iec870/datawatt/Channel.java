/*
 * Channel.java
 *
 * Created on 14 juli 2003, 11:48
 */

package com.energyict.protocolimpl.iec870.datawatt;

import java.io.IOException;
import java.util.StringTokenizer;
/**
 *
 * @author  Koen
 */
public class Channel {

    static int[] addressTypes = {0x4000,0x2000,0x2400,0x0000};
    static int TIMESTAMPMASK=0x0800;

    static public final int COUNTERINPUTCHANNEL=1;
    static public final int ANALOGINPUTCHANNEL=2;
    static public final int ANALOGOUTPUTCHANNEL=3;
    static public final int DIGITALINPUTCHANNEL=4;

    int channelId;
    int channelType;
    int cumul;


    public boolean isCounterInput() {
        return getChannelType() == COUNTERINPUTCHANNEL;
    }
    public boolean isAnalogInput() {
        return getChannelType() == ANALOGINPUTCHANNEL;
    }
    public boolean isAnalogOutput() {
        return getChannelType() == ANALOGOUTPUTCHANNEL;
    }
    public boolean isDigitalInput() {
        return getChannelType() == DIGITALINPUTCHANNEL;
    }

    public Channel(int channelId, int channelType, int cumul) {
        this.channelId=channelId;
        this.channelType=channelType;
        this.cumul = cumul;
    }

    public void setCumulative(int cumul) {
        this.cumul = cumul;
    }

    public boolean isCumulative() {
        return (cumul>0);
    }

    public int getChannelId() {
        return channelId;
    }
    public int getChannelType() {
        return channelType;
    }

    public boolean isEqual(int channelId, int channelType) {
        if ((channelId == getChannelId()) && (channelType == getChannelType())) return true;
        else return false;
    }
    public boolean isEqual(Channel channel) {
        if ((channel.getChannelId() == getChannelId()) && (channel.getChannelType() == getChannelType())) return true;
        else return false;
    }

    static public int toChannelType(int addressType) throws IOException {
        for (int i=0;i<addressTypes.length;i++) {
           if ((addressType & (TIMESTAMPMASK ^ 0xFFFF))==addressTypes[i]) return i+1;
        }
        throw new IOException("Channel, toChannelType, invalid addressType 0x"+Integer.toHexString(addressType));
    }

    static public Channel parseChannel(String strChannel) throws IOException {
            StringTokenizer st2 = new StringTokenizer(strChannel,".");
            if ((st2.countTokens() != 3) && (st2.countTokens() != 2))
                throw new IOException ("Channel, parseChannel, invalid nr of tokens in parsestring, "+strChannel);
            int channelType = Integer.parseInt(st2.nextToken());
            if ((channelType != COUNTERINPUTCHANNEL) &&
                (channelType != ANALOGINPUTCHANNEL) &&
                (channelType != ANALOGOUTPUTCHANNEL) &&
                (channelType != DIGITALINPUTCHANNEL))
                throw new IOException ("Channel, parseChannel, invalid channelType "+channelType+" found in parsestring "+strChannel);
            int channelId = Integer.parseInt(st2.nextToken());
            int cumul=0;
            if (st2.countTokens() != 0)
                // get cumul from configuration
                cumul = Integer.parseInt(st2.nextToken());
            else {
                // get cumul from default for datawatt channels
                if (channelType == COUNTERINPUTCHANNEL) cumul=1;
                else if (channelType == ANALOGINPUTCHANNEL) cumul=0;
                else if (channelType == ANALOGOUTPUTCHANNEL) cumul=0;
                else if (channelType == DIGITALINPUTCHANNEL) cumul=0;
                else cumul=0;
            }
            return new Channel(channelId, channelType, cumul);
    }

} // public class Channel

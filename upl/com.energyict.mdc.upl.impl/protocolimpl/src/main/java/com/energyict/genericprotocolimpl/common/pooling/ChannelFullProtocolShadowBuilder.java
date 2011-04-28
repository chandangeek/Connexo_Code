package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.mdw.core.Channel;

/**
 * Provides functionality to create a fullShadow channel object
 */
public class ChannelFullProtocolShadowBuilder {

    private ChannelFullProtocolShadowBuilder(){
    }

    /**
     * Creates a <CODE>ChannelFullProtocolShadow</CODE> object
     * @param rtuChannel the <CODE>Channel</CODE> to convert
     * @return a fully build channel Object
     */
    public static ChannelFullProtocolShadow createChannelFullProtocolShadow(Channel rtuChannel){
        ChannelFullProtocolShadow channelShadow = new ChannelFullProtocolShadowImpl();
        channelShadow.setChannelIndex(rtuChannel.getOrdinal()); //TODO check if this is correct
        channelShadow.setLastReading(rtuChannel.getLastReading());
        channelShadow.setTimeDuration(rtuChannel.getInterval());
        channelShadow.setLoadProfileIndex(rtuChannel.getLoadProfileIndex());
        return channelShadow;
    }
}

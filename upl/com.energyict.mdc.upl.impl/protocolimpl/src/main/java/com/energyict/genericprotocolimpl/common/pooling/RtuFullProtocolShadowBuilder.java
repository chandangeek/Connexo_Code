package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Device;

import java.util.*;

/**
 * Provides functionality to create a fullShadow <CODE>Device</CODE> which will contain all necessary information for a protocol to execute his tasks
 */
public class RtuFullProtocolShadowBuilder {

    /**
     * Hidden constructor
     */
    private RtuFullProtocolShadowBuilder() {
    }

    /**
     * Create an <CODE>RtuFullProtocolShadow</CODE> from the given <CODE>Device</CODE>
     *
     * @param rtu the Device to convert
     * @return the converted object
     */
    public static RtuFullProtocolShadow createRtuFullProtocolShadow(Device rtu) {
        RtuFullProtocolShadow rfps = new RtuFullProtocolShadowImpl();
//        rfps.setChannelFullProtocolShadow(createChannelFullProtocolShadows(rtu));
//        rfps.setDeviceId(rtu.getDeviceId());
//        rfps.setDeviceTimeZone(rtu.getDeviceTimeZone());
//        rfps.setDialHomeId(rtu.getDialHomeId());
//        rfps.setModemInit(rtu.getModemInit());
//        rfps.setName(rtu.getName());
//        rfps.setNodeAddress(rtu.getNodeAddress());
//        rfps.setOverruleCommunicationSettings(rtu.getOverruleCommunicationSettings());
//        rfps.setPassword(rtu.getPassword());
//        rfps.setPhoneNumber(rtu.getPhoneNumber());
//        rfps.setPostDialCommand(rtu.getPostDialCommand());
//        rfps.setRtuId(rtu.getId());
//        rfps.setFolderId(rtu.getFolderId());
//        rfps.setRtuIntervalInSeconds(rtu.getIntervalInSeconds());
//        rfps.setRtuLastLogBook(rtu.getLastLogbook());
//        rfps.setRtuLastReading(rtu.getLastReading());
//        Properties props = rtu.getProperties().toStringProperties();
//        props.putAll(rtu.getProtocol() == null ? new HashMap() : rtu.getProtocol().getProperties().toStringProperties());
//        rfps.setRtuProperties(props);
//        rfps.setRtuTypeShadow(rtu.getDeviceType().getShadow());
//        rfps.setSerialCommunicationSettings(rtu.getCommunicationSettings());
//        rfps.setSerialNumber(rtu.getSerialNumber());
//        rfps.setTimeZone(rtu.getTimeZone());
        return rfps;
    }

    /**
     * Create a <CODE>List</CODE> of <CODE>ChannelFullProtocolShadow</CODE> objects
     *
     * @param rtu the <CODE>Device</CODE> containing the channels
     * @return the newborn list
     */
    private static List<ChannelFullProtocolShadow> createChannelFullProtocolShadows(final Device rtu) {
        List<ChannelFullProtocolShadow> channelFullProtocolShadowList = new ArrayList<ChannelFullProtocolShadow>();
        for (Channel channel : rtu.getChannels()) {
            channelFullProtocolShadowList.add(ChannelFullProtocolShadowBuilder.createChannelFullProtocolShadow(channel));
        }
        return channelFullProtocolShadowList;
    }
}

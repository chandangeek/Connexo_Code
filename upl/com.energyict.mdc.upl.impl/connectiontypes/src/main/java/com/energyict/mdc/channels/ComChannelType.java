package com.energyict.mdc.channels;

import com.energyict.mdc.protocol.ComChannel;

/**
 * The ComChannel type is added as a typed property to the comchannel instances.
 * This can be used by the V2 protocols to determine which connection layer should be used.
 * <p/>
 * Copyrights EnergyICT
 * Date: 7/11/13
 * Time: 9:27
 * Author: khe
 */
public enum ComChannelType {

    Invalid(-1),
    SerialComChannel(0),            //HDLC connection layer
    SocketComChannel(1),            //TCP connection layer
    DatagramComChannel(2),          //UDP connection layer
    WavenisGatewayComChannel(3),
    WavenisSerialComChannel(4),
    ProximusSmsComChannel(5);

    public static final String TYPE = "ComChannelType";
    private Integer type;

    private ComChannelType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public boolean is(ComChannel comChannel) {
        Integer type = comChannel.getProperties().<Integer>getTypedProperty(TYPE);
        return getType().equals(type);
    }
}

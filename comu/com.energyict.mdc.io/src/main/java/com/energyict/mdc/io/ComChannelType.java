package com.energyict.mdc.io;

/**
 * The ComChannel type is added as a typed property to the comchannel instances.
 * This can be used by the V2 protocols to determine which connection layer should be used.
 * <p>
 * Copyrights EnergyICT
 * Date: 7/11/13
 * Time: 9:27
 * Author: khe
 */
public enum ComChannelType {

    NOT_DEFINED(-1),
    SERIAL_COM_CHANNEL(0),            //HDLC connection layer
    OPTICAL_COM_CHANNEL(1),           //HDLC connection layer
    SOCKET_COM_CHANNEL(2),            //TCP connection layer
    DATAGRAM_COM_CHANNEL(3),          //UDP connection layer
    WAVENIS_GATEWAY_COM_CHANNEL(4),
    WAVENIS_SERIAL_COM_CHANNEL(5),
    PROXIMUS_SMS_COM_CHANNEL(6);

    public static final String TYPE = "ComChannelType";
    private Integer type;

    private ComChannelType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public boolean is(ComChannel comChannel) {
        return comChannel.getComChannelType().getType().equals(this.type);
    }
}

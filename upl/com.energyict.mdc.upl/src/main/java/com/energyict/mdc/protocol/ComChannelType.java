package com.energyict.mdc.protocol;

/**
 * This type can be used by the protocols to determine which connection layer should be used.
 * <p>
 * Copyrights EnergyICT
 * Date: 7/11/13
 * Time: 9:27
 * Author: khe
 */
public enum ComChannelType {

    Invalid(-1),
    SerialComChannel(0),            //HDLC connection layer
    OpticalComChannel(1),           //HDLC connection layer
    SocketComChannel(2),            //TCP connection layer
    DatagramComChannel(3),          //UDP connection layer
    WavenisGatewayComChannel(4),
    WavenisSerialComChannel(5),
    ProximusSmsComChannel(6);

    private Integer type;

    ComChannelType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }
}
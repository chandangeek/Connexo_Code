/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io;

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

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

/**
 * User: gde
 * Date: 2/11/12
 */
public enum DeviceCommunicationFunction {

    // Make sure the codes can be used as bitmask!
    CONNECTION(1, "communicationFunction.connection"),
    GATEWAY(1<<1, "communicationFunction.gateway"),
    PROTOCOL_SESSION(1<<2, "communicationFunction.protocolSession"),
    PROTOCOL_MASTER(1<<3, "communicationFunction.protocolMaster"),
    PROTOCOL_SLAVE(1<<4, "communicationFunction.protocolSlave");

    private int code;
    private String nameKey;

    private DeviceCommunicationFunction(int code, String nameKey) {
        this.code = code;
        this.nameKey = nameKey;
    }

    public int getCode() {
        return code;
    }

    public String getNameKey() {
        return nameKey;
    }

}
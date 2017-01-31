/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.dialer.serialserviceprovider;


public class SerialConfig {

    // Because the dialer framework initial implmentation only implemented the serialio interface.
    // Therefore, to keep most of the code, we emulate the serial io interfaces for the serial port service provider.
    int baudIndex = Serialio.SerialConfig.BR_9600;
    int databits = Serialio.SerialConfig.LN_8BITS;
    int stopbits = Serialio.SerialConfig.ST_1BITS;
    int parity = Serialio.SerialConfig.PY_NONE;
    String comPortStr;

    public SerialConfig(String comPortStr) {
        this.comPortStr = comPortStr;
    }

    public void setBitRate(int baudIndex) {
        this.baudIndex = baudIndex;
    }

    public void setDataBits(int databits) {
        this.databits = databits;
    }

    public void setStopBits(int stopbits) {
        this.stopbits = stopbits;
    }

    public void setParity(int parity) {
        this.parity = parity;
    }

    public int getBitRate() {
        return baudIndex;
    }

    public int getDataBits() {
        return databits;
    }

    public int getStopBits() {
        return stopbits;
    }

    public int getParity() {
        return parity;
    }

    public String getComPortStr() {
        return comPortStr;
    }


}

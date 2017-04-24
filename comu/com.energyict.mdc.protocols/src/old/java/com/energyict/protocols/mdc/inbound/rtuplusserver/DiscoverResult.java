/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DiscoverResult.java
 *
 * Created on 19 oktober 2007, 16:56
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocols.mdc.inbound.rtuplusserver;

import java.io.IOException;
import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * @author kvds
 */
public class DiscoverResult implements Serializable {

    public static final int MODBUS = 0;
    public static final int MBUS = 1;
    private int protocol;
    private boolean discovered;
    private String result;
    private String protocolName;
    private int address;
    private String deviceTypeName;
    private String shortDeviceTypeName = null;
    private String networkId;
    private String serialNumber = null;
    private String deviceName = null;

    /**
     * Creates a new instance of DiscoverResult
     */
    public DiscoverResult() {
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("DiscoverResult:\n");
        strBuff.append("   discovered=" + isDiscovered() + "\n");
        if (isDiscovered()) {
            strBuff.append("   result=" + getResult() + "\n");
        }
        strBuff.append("   address=" + address + "\n");
        strBuff.append("   protocolName=" + protocolName + "\n");
        strBuff.append("   deviceTypeName=" + deviceTypeName + "\n");
        strBuff.append("   serialNumber=" + serialNumber + "\n");
        strBuff.append("   networkId=" + networkId + "\n");
        return strBuff.toString();
    }


    public boolean isMODBUS() {
        return getProtocol() == MODBUS;
    }

    public boolean isMBUS() {
        return getProtocol() == MBUS;
    }


    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public int getAddress() {
        return address;
    }

    private String networkIdPortTypeName() throws IOException {
        StringTokenizer strtok = new StringTokenizer(getNetworkId(), "_");
        if ((strtok.countTokens() < 2) || (strtok.countTokens() > 3)) {
            throw new IOException("DiscoverResult, init, invalid networkID property " + networkId);
        }
        return (String) strtok.nextElement() + "_" + (String) strtok.nextElement();
    }

    public String deviceName() throws IOException {
        return (getDeviceName() == null ? "" : getDeviceName() + " ") + (getShortDeviceTypeName() == null ? getDeviceTypeName() : getShortDeviceTypeName()) + " " + networkIdPortTypeName() + " " + getAddress();
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public String getDeviceTypeName() {
        return deviceTypeName;
    }

    public void setDeviceTypeName(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public void setProtocolMODBUS() {
        setProtocol(MODBUS);
    }

    public void setProtocolMBUS() {
        setProtocol(MBUS);
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public String getShortDeviceTypeName() {
        return shortDeviceTypeName;
    }

    public void setShortDeviceTypeName(String shortDeviceTypeName) {
        this.shortDeviceTypeName = shortDeviceTypeName;
    }


}

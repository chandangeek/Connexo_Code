/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DiscoverProtocolInfo.java
 *
 * Created on 22 oktober 2007, 16:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.discover;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kvds
 */
public class DiscoverProtocolInfo {

    // all string values case insensitive

    final int DISCOVERMETHOD_SLAVEID=0; // slave id string
    final int DISCOVERMETHOD_METERID=1; // ';' separated --> vendorname;productcode
    final int DISCOVERMETHOD_HOLDINGREGISTER=2;  // detectionstring = ';' separated all possible integer register value

    static List list=new ArrayList();

    static {
        list.add(new DiscoverProtocolInfo(0,"veris format","com.energyict.protocolimpl.modbus.eictmodbusrtu.eictveris.EictVeris","PNP Veris format EnergyICT"));
        list.add(new DiscoverProtocolInfo(0,"veris h8036","com.energyict.protocolimpl.modbus.veris.hawkeye.Hawkeye","PNP Veris Hawkeye h8036"));
        list.add(new DiscoverProtocolInfo(0,"veris h8076","com.energyict.protocolimpl.modbus.veris.hawkeye.Hawkeye","PNP Veris Hawkeye h8076"));
        list.add(new DiscoverProtocolInfo(0,"veris h80","com.energyict.protocolimpl.modbus.veris.hawkeye.Hawkeye","PNP Veris Hawkeye h80xx"));
        list.add(new DiscoverProtocolInfo(1,"square d;15210","com.energyict.protocolimpl.modbus.squared.pm800.PM800","PNP SquareD PM800 15210"));
        list.add(new DiscoverProtocolInfo(1,"square d;15211","com.energyict.protocolimpl.modbus.squared.pm800.PM800","PNP SquareD PM820 15211"));
        // meter model register
        list.add(new DiscoverProtocolInfo(2,"1350","com.energyict.protocolimpl.modbus.eimeter.EIMeter","PNP EIMeter"));
        list.add(new DiscoverProtocolInfo(1,"Merlin Gerin;PM750","com.energyict.protocolimpl.modbus.squared.pm750.PM750","PNP Merlin Gerin PM750"));
        list.add(new DiscoverProtocolInfo(0,"PM750 Power Meter","com.energyict.protocolimpl.modbus.squared.pm750.PM750","PNP Merlin Gerin PM750"));
        list.add(new DiscoverProtocolInfo(0,"PM750","com.energyict.protocolimpl.modbus.squared.pm750.PM750","PNP SquareD PM750"));

        // product id register
        list.add(new DiscoverProtocolInfo(2,"0x8","com.energyict.protocolimpl.modbus.cutlerhammer.iq200.IQ200","PNP Cutler Hammer IQ200"));
        list.add(new DiscoverProtocolInfo(2,"0x80000","com.energyict.protocolimpl.modbus.cutlerhammer.iq230.IQ230","PNP Cutler Hammer IQ230"));

        // slot info of the meter 000000 000001 0000ff 010000 010001 0100ff ff0000 ff0001 ff00ff
        //list.add(new DiscoverProtocolInfo(2,"0;1;255;65536;65537;65791;16711680;16711681;16711935","com.energyict.protocolimpl.modbus.socomec.a20.A20","PNP Socomec Diris A20"));
        list.add(new DiscoverProtocolInfo(2,"141","com.energyict.protocolimpl.modbus.socomec.a20.A20","PNP Socomec Diris A20"));
        list.add(new DiscoverProtocolInfo(2,"142","com.energyict.protocolimpl.modbus.socomec.a40.A40","PNP Socomec Diris A40"));
        list.add(new DiscoverProtocolInfo(2,"73","com.energyict.protocolimpl.modbus.ge.pqm2.PQM2","PNP GE PQM2"));

        list.add(new DiscoverProtocolInfo(1,"Schneider Electric;TRV00210","com.energyict.protocolimpl.modbus.schneider.compactnsx.CompactNSX","PNP Schneider CompactNSX"));

    }

    private int discoverMethod;
    private String detectionString;
    private String protocolName;
    private String deviceType;

    /** Creates a new instance of DiscoverProtocolInfo */
    private DiscoverProtocolInfo(int discoverMethod,String detectionString,String protocolName,String deviceType) {
        this.discoverMethod=discoverMethod;
        this.detectionString=detectionString;
        this.protocolName=protocolName;
        this.deviceType=deviceType;
    }

    public String toString() {
        return deviceType+", "+protocolName;
    }

    public String[] getMeterId() {
       return getDetectionString().split(";");
    }

    public boolean isDiscoverMethodHoldingRegister() {
        return getDiscoverMethod()==DISCOVERMETHOD_HOLDINGREGISTER;
    }

    public boolean isDiscoverMethodSlaveId() {
        return getDiscoverMethod()==DISCOVERMETHOD_SLAVEID;
    }

    public boolean isDiscoverMethodMeterId() {
        return getDiscoverMethod()==DISCOVERMETHOD_METERID;
    }

    static public List getSupportedDevicesList() {
        return list;
    }

    public int getDiscoverMethod() {
        return discoverMethod;
    }

    public void setDiscoverMethod(int discoverMethod) {
        this.discoverMethod = discoverMethod;
    }

    public String getDetectionString() {
        return detectionString;
    }

    public void setDetectionString(String detectionString) {
        this.detectionString = detectionString;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

}

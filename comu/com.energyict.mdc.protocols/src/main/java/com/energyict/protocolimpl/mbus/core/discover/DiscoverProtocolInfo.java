/*
 * DiscoverProtocolInfo.java
 *
 * Created on 22 oktober 2007, 16:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core.discover;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kvds
 */
public class DiscoverProtocolInfo {

    // all string values case insensitive

    static List list=new ArrayList();

    static {
        list.add(new DiscoverProtocolInfo(1,"KAM",0x0C,"com.energyict.protocolimpl.mbus.generic.Generic","PNP MBus Kamstrup Multical 41","Multical 41"));
        list.add(new DiscoverProtocolInfo(1,"KAM",2,"com.energyict.protocolimpl.mbus.generic.Generic","PNP MBus Kamstrup 382","382"));
        list.add(new DiscoverProtocolInfo(7,"KAM",4,"com.energyict.protocolimpl.mbus.generic.Generic","PNP MBus Kamstrup Multical 601","Multical 601"));
        list.add(new DiscoverProtocolInfo(3,"LUG",4,"com.energyict.protocolimpl.mbus.generic.Generic","PNP MBus Siemens PT500","PT500"));
        //list.add(new DiscoverProtocolInfo(0,"@@@",0,"com.energyict.protocolimpl.mbus.generic.Generic","PNP MBus Generic"));
    }

    private int version;
    private String manufacturer;
    private String protocolName;
    private String deviceType;
    private int medium;
    private String shortDeviceType;

    /** Creates a new instance of DiscoverProtocolInfo */
    private DiscoverProtocolInfo(int version,String manufacturer,int medium, String protocolName,String deviceType,String shortDeviceType) {
        this.setVersion(version);
        this.setManufacturer(manufacturer);
        this.protocolName=protocolName;
        this.deviceType=deviceType;
        this.setMedium(medium);
        this.setShortDeviceType(shortDeviceType);
    }

    static public DiscoverProtocolInfo getUnknown() {
        return new DiscoverProtocolInfo(0,"UNKNOWN",0,"com.energyict.protocolimpl.mbus.generic.Generic","PNP MBus Generic","Generic");
    }

    public String toString() {
        return deviceType+", "+protocolName;
    }



    static public List getSupportedDevicesList() {
        return list;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getMedium() {
        return medium;
    }

    public void setMedium(int medium) {
        this.medium = medium;
    }

    public String getShortDeviceType() {
        return shortDeviceType;
    }

    public void setShortDeviceType(String shortDeviceType) {
        this.shortDeviceType = shortDeviceType;
    }

}

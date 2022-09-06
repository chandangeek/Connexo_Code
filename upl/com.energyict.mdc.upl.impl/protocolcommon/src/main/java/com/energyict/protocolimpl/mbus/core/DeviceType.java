/*
 * DeviceType.java
 *
 * Created on 3 oktober 2007, 14:05
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import com.energyict.mdc.upl.ProtocolException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kvds
 */
public class DeviceType {

    private static final List<DeviceType> INSTANCES = new ArrayList<>();
    static {
        // Unknown A obis code values are coded from 100 & up
        INSTANCES.add(new DeviceType("Other",0,100));
        INSTANCES.add(new DeviceType("Oil",1,101));
        INSTANCES.add(new DeviceType("Electricity",2,1));
        INSTANCES.add(new DeviceType("Gas",0x3,7));
        INSTANCES.add(new DeviceType("Heat",0x4,6));
        INSTANCES.add(new DeviceType("Steam",0x5,7));
        INSTANCES.add(new DeviceType("Warm Water (30C...90C)",0x6,9));
        INSTANCES.add(new DeviceType("Water",0x7,8));
        INSTANCES.add(new DeviceType("Heat Cost Allocator",0x8,4));
        INSTANCES.add(new DeviceType("Compressed Air",0x9,7));
        INSTANCES.add(new DeviceType("Cooling load meter (Volume measured at return temperature: outlet)","Cooling (out ret)",0x0A,5));
        INSTANCES.add(new DeviceType("Cooling load meter (Volume measured at flow temperature: inlet)","Cooling (in flow)",0x0B,5));
        INSTANCES.add(new DeviceType("Heat (Volume measured at flow temperature: inlet)","Heat (in flow)",0x0C,6));
        INSTANCES.add(new DeviceType("Heat / Cooling load meter","Heat/Cooling ",0x0D,6));
        INSTANCES.add(new DeviceType("Bus / System component","Bus/System",0x0E,10));
        INSTANCES.add(new DeviceType("Unknown Medium",0x0F,11));

        INSTANCES.add(new DeviceType("Reserved (0x10)",0x10,255));
        INSTANCES.add(new DeviceType("Reserved (0x11)",0x11,255));
        INSTANCES.add(new DeviceType("Reserved (0x12)",0x12,255));
        INSTANCES.add(new DeviceType("Reserved (0x13)",0x13,255));
        INSTANCES.add(new DeviceType("Reserved (0x14)",0x14,255));

        INSTANCES.add(new DeviceType("Hot water (>=90C)",0x15,9));
        INSTANCES.add(new DeviceType("Cold water",0x16,8));
        INSTANCES.add(new DeviceType("Dual register (hot/cold) Water meter (see NOTE )",0x17,9));
        INSTANCES.add(new DeviceType("Pressure",0x18,7));
        INSTANCES.add(new DeviceType("A/D Converter",0x19,102));
        INSTANCES.add(new DeviceType("Reserved for valve",0x21,103));

        INSTANCES.add(new DeviceType("Unknown MET (0x78)",0x78,255));
        INSTANCES.add(new DeviceType("Unknown ABB (0x35)",0x35,255));

        INSTANCES.add(new DeviceType("Unknown ABB (0x35)",67,255));

        INSTANCES.add(new DeviceType("Wireless MBus Encrypted",123,255));
        INSTANCES.add(new DeviceType("Wireless MBus Encrypted",132,255));
    }

    private String description;
    private String shortDescription;
    private int id;
    private int obisA;

    private DeviceType(String description, int id, int obisA) {
        this(description,description,id,obisA);
    }
    private DeviceType(String description,String shortDescription, int id, int obisA) {
        this.description=description;
        this.setShortDescription(shortDescription);
        this.id=id;
        this.obisA = obisA;
    }

    public String toString() {
        return "DeviceType: "+description;
    }

    static DeviceType findDeviceType(int id) throws IOException {
        for (DeviceType d : INSTANCES) {
            if (d.getId() == id) {
                return d;
            }
        }
        throw new ProtocolException("DeviceType, findDeviceType, error invalid id "+id);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getObisA() {
    	return obisA;
    }

    public void setObisA(int obisA) {
    	this.obisA = obisA;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
}

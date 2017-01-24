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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author kvds
 */
public class DeviceType {

    static List list = new ArrayList();
    static {

        // Unknown A obis code values are coded from 100 & up

        list.add(new DeviceType("Other",0,100));
        list.add(new DeviceType("Oil",1,101));
        list.add(new DeviceType("Electricity",2,1));
        list.add(new DeviceType("Gas",0x3,7));
        list.add(new DeviceType("Heat",0x4,6));
        list.add(new DeviceType("Steam",0x5,7));
        list.add(new DeviceType("Warm Water (30C...90C)",0x6,9));
        list.add(new DeviceType("Water",0x7,8));
        list.add(new DeviceType("Heat Cost Allocator",0x8,4));
        list.add(new DeviceType("Compressed Air",0x9,7));
        list.add(new DeviceType("Cooling load meter (Volume measured at return temperature: outlet)","Cooling (out ret)",0x0A,5));
        list.add(new DeviceType("Cooling load meter (Volume measured at flow temperature: inlet)","Cooling (in flow)",0x0B,5));
        list.add(new DeviceType("Heat (Volume measured at flow temperature: inlet)","Heat (in flow)",0x0C,6));
        list.add(new DeviceType("Heat / Cooling load meter","Heat/Cooling ",0x0D,6));
        list.add(new DeviceType("Bus / System component","Bus/System",0x0E,10));
        list.add(new DeviceType("Unknown Medium",0x0F,11));

        list.add(new DeviceType("Reserved (0x10)",0x10,255));
        list.add(new DeviceType("Reserved (0x11)",0x11,255));
        list.add(new DeviceType("Reserved (0x12)",0x12,255));
        list.add(new DeviceType("Reserved (0x13)",0x13,255));
        list.add(new DeviceType("Reserved (0x14)",0x14,255));

        list.add(new DeviceType("Hot water (>=90C)",0x15,9));
        list.add(new DeviceType("Cold water",0x16,8));
        list.add(new DeviceType("Dual register (hot/cold) Water meter (see NOTE )",0x17,9));
        list.add(new DeviceType("Pressure",0x18,7));
        list.add(new DeviceType("A/D Converter",0x19,102));
        list.add(new DeviceType("Reserved for valve",0x21,103));
    }

    private String description;
    private String shortDescription;
    private int id;
    private int obisA;


    /**
     * Creates a new instance of DeviceType
     */
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
        Iterator it = list.iterator();
        while(it.hasNext()) {
            DeviceType d = (DeviceType)it.next();
            if (d.getId()==id)
                return d;
        }
        throw new IOException("DeviceType, findDeviceType, error invalid id "+id);
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

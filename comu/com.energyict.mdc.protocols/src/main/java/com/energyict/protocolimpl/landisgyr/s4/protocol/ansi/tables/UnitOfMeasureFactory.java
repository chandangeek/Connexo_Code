/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * UnitOfMeasureFactory.java
 *
 * Created on 10 juli 2006, 9:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

import com.energyict.mdc.common.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Koen
 */
public class UnitOfMeasureFactory {

    static List list = new ArrayList();

    static {

        list.add(new UnitOfMeasure(0x00, Unit.get("kWh"), 0xF, "kWh manipulated", 15, 1, 0x9, "kWh imported",1 ));
        list.add(new UnitOfMeasure(0x01, Unit.get("kWh"), 0x6, "kWh exported", 2, 1));
        list.add(new UnitOfMeasure(0x03, Unit.get("kVAh"), 0xF, "kVAh rms manipulated", 9, 1));
        list.add(new UnitOfMeasure(0x04, Unit.get(""), 0xF, "Power factor", 13, 0));
        list.add(new UnitOfMeasure(0x06, Unit.get("kvarh"), 0xF, "kvarh manipulated", 128, 1, 0x5, "kvarh lagging", 130));
        list.add(new UnitOfMeasure(0x07, Unit.get("kvarh"), 0xA, "kvarh leading", 129, 1));
        list.add(new UnitOfMeasure(0x08, Unit.get("V"), 0xF, "Vh(a)", 32, 2));
        list.add(new UnitOfMeasure(0x09, Unit.get("V"), 0xF, "Vh(b)", 52, 2));
        list.add(new UnitOfMeasure(0x0A, Unit.get("V"), 0xF, "Vh(c)", 72, 2));
        list.add(new UnitOfMeasure(0x0B, Unit.get("A"), 0xF, "In", 11, 3));
        list.add(new UnitOfMeasure(0x0C, Unit.get("A"), 0xF, "Ih(a)", 31, 3));
        list.add(new UnitOfMeasure(0x0D, Unit.get("A"), 0xF, "Ih(b)", 51, 3));
        list.add(new UnitOfMeasure(0x0E, Unit.get("A"), 0xF, "Ih(c)", 71, 3));
        list.add(new UnitOfMeasure(0x0F, Unit.get(""), 0x0, "External input 1", 142, 0));
        list.add(new UnitOfMeasure(0x10, Unit.get(""), 0x0, "External input 2", 143, 0));
        list.add(new UnitOfMeasure(0x11, Unit.get("kVAh"), 0xF, "kVAh td manipulated", 131, 0x5, 1, "kVAh td lagging", 141));
        list.add(new UnitOfMeasure(0x12, Unit.get("kVAh"), 0xA, "kVAh td leading", 132, 1));
        list.add(new UnitOfMeasure(0x13, Unit.get("V"), 0xF, "Sag V(a)", 133, 2));
        list.add(new UnitOfMeasure(0x14, Unit.get("V"), 0xF, "Sag V(b)", 134, 2));
        list.add(new UnitOfMeasure(0x15, Unit.get("V"), 0xF, "Sag V(c)", 135, 2));
        list.add(new UnitOfMeasure(0x16, Unit.get("V"), 0xF, "Swell V(a)", 136, 2));
        list.add(new UnitOfMeasure(0x17, Unit.get("V"), 0xF, "Swell V(b)", 137, 2));
        list.add(new UnitOfMeasure(0x18, Unit.get("V"), 0xF, "Swell V(c)", 138, 2));
        list.add(new UnitOfMeasure(0x19, Unit.get("V"), 0xF, "Sag V any phase", 139, 2));
        list.add(new UnitOfMeasure(0x1A, Unit.get("V"), 0xF, "Swell V any phase", 140, 2));

    }



    /** Creates a new instance of UnitOfMeasureFactory */
    private UnitOfMeasureFactory() {
    }


    static public UnitOfMeasure findUnitOfMeasure(int id) throws IOException {
        for (int i=0;i<list.size();i++) {
            UnitOfMeasure uom = (UnitOfMeasure)list.get(i);
            if (uom.getId()==id)
                return uom;
        }
        throw new IOException("UnitOfMeasureFactory, findUnitOfMeasure, no unit of measure for id "+id);
    }


}

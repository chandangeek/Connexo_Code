/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TOUConfig.java
 *
 * Created on 11 oktober 2005, 17:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.mdc.common.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Koen
 */
public class TOUConfig {

    static List touConfigEntries = new ArrayList();
    static {

        // TOU BLOCKS 1..4
        touConfigEntries.add(new TOUConfigEntry(0x80, -1, 1, Unit.get("kW"), "Active import"));
        touConfigEntries.add(new TOUConfigEntry(0x40, -1, 2, Unit.get("kW"), "Active export"));
        touConfigEntries.add(new TOUConfigEntry(0x20, Class8FirmwareConfiguration.K_TYPE, 9, Unit.get("kVA"),"Apparent import"));
        touConfigEntries.add(new TOUConfigEntry(0x20, Class8FirmwareConfiguration.R_TYPE, 3, Unit.get("kvar"),"Reactive import"));
        touConfigEntries.add(new TOUConfigEntry(0x20, Class8FirmwareConfiguration.OTHER_TYPE, 3, Unit.get("kvar"),"Reactive import"));
        touConfigEntries.add(new TOUConfigEntry(0x10, Class8FirmwareConfiguration.K_TYPE, 10, Unit.get("kVA"),"Apparent export"));
        touConfigEntries.add(new TOUConfigEntry(0x10, Class8FirmwareConfiguration.R_TYPE, 4, Unit.get("kvar"),"Reactive export"));
        touConfigEntries.add(new TOUConfigEntry(0x10, Class8FirmwareConfiguration.OTHER_TYPE, 4, Unit.get("kvar"),"Reactive export"));
        touConfigEntries.add(new TOUConfigEntry(0x8, -1, 5, Unit.get("kvar"), "Reactive Q1"));
        touConfigEntries.add(new TOUConfigEntry(0x4, -1, 6, Unit.get("kvar"), "Reactive Q2"));
        touConfigEntries.add(new TOUConfigEntry(0x2, -1, 7, Unit.get("kvar"), "Reactive Q3"));
        touConfigEntries.add(new TOUConfigEntry(0x1, -1, 8, Unit.get("kvar"), "Reactive Q4"));

        // TOU BLOCK 5..6
        touConfigEntries.add(new TOUConfigEntry(0x89, -1, 128, Unit.get("kVA"), "Apparent Q1&Q4"));
        touConfigEntries.add(new TOUConfigEntry(0x81, -1, 129, Unit.get("kVA"), "Apparent Q1"));
        touConfigEntries.add(new TOUConfigEntry(0x88, -1, 130, Unit.get("kVA"), "Apparent Q4"));
        touConfigEntries.add(new TOUConfigEntry(0x46, -1, 131, Unit.get("kVA"), "Apparent Q2&Q3"));
        touConfigEntries.add(new TOUConfigEntry(0x42, -1, 132, Unit.get("kVA"), "Apparent Q2"));
        touConfigEntries.add(new TOUConfigEntry(0x44, -1, 133, Unit.get("kVA"), "Apparent Q3"));
        touConfigEntries.add(new TOUConfigEntry(0x83, -1, 134, Unit.get("kvar"), "Reactive import"));
        touConfigEntries.add(new TOUConfigEntry(0x4C, -1, 135, Unit.get("kvar"), "Reactive export"));

    }


    /** Creates a new instance of TOUConfig */
    public TOUConfig() {

    }

    static public Unit getTOUUnit(int toucfg, int meterType, boolean energy) throws IOException {

        for (int i=0;i<touConfigEntries.size();i++) {
            TOUConfigEntry touce = (TOUConfigEntry)touConfigEntries.get(i);

            if (touce.getToucfg() == toucfg) {
                if (touce.getMeterType() != -1) {
                    if (touce.getMeterType() == meterType) {
                        return energy?touce.getUnit().getVolumeUnit():touce.getUnit().getFlowUnit();
                    }
                }
                else {
                    return energy?touce.getUnit().getVolumeUnit():touce.getUnit().getFlowUnit();
                }
            }
        }
        throw new IOException("TOUConfig, getTOUUnit, No unit found for TOUCFG code "+toucfg+" and meterType "+meterType);
    }

    static public int getTOUObisCField(int toucfg, int meterType) throws IOException {
        for (int i=0;i<touConfigEntries.size();i++) {
            TOUConfigEntry touce = (TOUConfigEntry)touConfigEntries.get(i);
            if (touce.getToucfg() == toucfg) {
                if (touce.getMeterType() != -1) {
                    if (touce.getMeterType() == meterType) {
                        return touce.getObisCodeCField();
                    }
                }
                else {
                    return touce.getObisCodeCField();
                }
            }
        }
        throw new IOException("TOUConfig, getTOUUnit, No obiscode C field found for TOUCFG code "+toucfg+" and meterType "+meterType);
    }

    static public String getTOUDescription(int toucfg, int meterType) throws IOException {
        for (int i=0;i<touConfigEntries.size();i++) {
            TOUConfigEntry touce = (TOUConfigEntry)touConfigEntries.get(i);
            if (touce.getToucfg() == toucfg) {
                if (touce.getMeterType() != -1) {
                    if (touce.getMeterType() == meterType) {
                        return touce.getDescription();
                    }
                }
                else {
                    return touce.getDescription();
                }
            }
        }
        throw new IOException("TOUConfig, getTOUUnit, No description found for TOUCFG code "+toucfg+" and meterType "+meterType);
    }

}

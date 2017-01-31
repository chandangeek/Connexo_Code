/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TOUConfigEntry.java
 *
 * Created on 11 oktober 2005, 17:12
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import com.energyict.mdc.common.Unit;

/**
 *
 * @author Koen
 */
public class TOUConfigEntry {
    private int toucfg;
    private int meterType; // -1 = niet van toepassing
    private int obisCodeCField;
    private Unit unit;
    private String description;

    /** Creates a new instance of TOUConfigEntry */
    public TOUConfigEntry(int toucfg, int meterType, int obisCodeCField, Unit unit, String description) {
        this.toucfg=toucfg;
        this.meterType=meterType;
        this.obisCodeCField=obisCodeCField;
        this.unit=unit;
        this.description=description;
    }

    public int getToucfg() {
        return toucfg;
    }

    public void setToucfg(int toucfg) {
        this.toucfg = toucfg;
    }

    public int getObisCodeCField() {
        return obisCodeCField;
    }

    public void setObisCodeCField(int obisCodeCField) {
        this.obisCodeCField = obisCodeCField;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public int getMeterType() {
        return meterType;
    }

    public void setMeterType(int meterType) {
        this.meterType = meterType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}

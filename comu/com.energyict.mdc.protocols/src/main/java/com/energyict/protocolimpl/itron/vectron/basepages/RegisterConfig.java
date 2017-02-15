/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterConfig.java
 *
 * Created on 12 oktober 2006, 14:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.mdc.common.Unit;

/**
 *
 * @author Koen
 */
public class RegisterConfig {

    private int id;
    private Unit unit;
    private String description;

    private int obisCField;

    /**
     * Creates a new instance of RegisterConfig
     */
    public RegisterConfig(int id, Unit unit, String description, int obisCField) {
        this.setUnit(unit);
        this.setDescription(description);
        this.id=id;
        this.setObisCField(obisCField);
    }

    public String toString() {
        return  "id="+getId()+", unit="+unit+", description="+description+"\n";
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
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

    public boolean isNoMapping() {
        return getUnit()==null;
    }

    public int getObisCField() {
        return obisCField;
    }

    public void setObisCField(int obisCField) {
        this.obisCField = obisCField;
    }

    public boolean isEnergy() {
        return getId()<5;
    }
    public boolean isDemand() {
        return getId()>=5;
    }
}

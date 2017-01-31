/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * QuantityId.java
 *
 * Created on 13 december 2006, 16:31
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.mdc.common.Unit;

/**
 *
 * @author Koen
 */
public class QuantityId {

    private int id;
    private String description;
    private int obisBField;
    private int obisCField;
    private Unit unit;

    /** Creates a new instance of QuantityId */
    public QuantityId(int id,String description, int obisBField, int obisCField, Unit unit) {
        this.setId(id);
        this.setDescription(description);
        this.setObisBField(obisBField);
        this.setObisCField(obisCField);
        this.setUnit(unit);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("QuantityId:\n");
        strBuff.append("   description="+getDescription()+"\n");
        strBuff.append("   id="+getId()+"\n");
        strBuff.append("   obisBField="+getObisBField()+"\n");
        strBuff.append("   obisCField="+getObisCField()+"\n");
        strBuff.append("   unit="+getUnit()+"\n");
        return strBuff.toString();
    }

    public boolean isValid() {
        return id != 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getObisBField() {
        return obisBField;
    }

    public void setObisBField(int obisBField) {
        this.obisBField = obisBField;
    }

    public int getObisCField() {
        return obisCField;
    }

    public void setObisCField(int obisCField) {
        this.obisCField = obisCField;
    }


    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

}

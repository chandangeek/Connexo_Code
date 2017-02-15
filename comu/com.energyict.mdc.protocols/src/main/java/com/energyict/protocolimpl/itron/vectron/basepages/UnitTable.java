/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * UnitTable.java
 *
 * Created on 27 september 2006, 14:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.base.ObisCodeExtensions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class UnitTable {

    static List units=new ArrayList();
    static {

    }

    private int registerNr;
    private int obisCField;
    private int obisDField;
    private Unit unit;
    private String description;

    public boolean isVoltSquare() {
        return getObisCField()==ObisCodeExtensions.OBISCODE_C_VOLTSQUARE;
    }
    public boolean isAmpSquare() {
        return getObisCField()==ObisCodeExtensions.OBISCODE_C_AMPSQUARE;
    }

    public BigDecimal getRegisterValue(byte[] data, int offset, int firmwareRevision, int scale) throws IOException {

        return null;
    } // public BigDecimal getRegisterValue(byte[] data, int offset, int firmwareRevision)


    public String toString() {
       return getUnit()+", "+getDescription();
    }

    /** Creates a new instance of UnitTable */
    private UnitTable(int registerNr, int obisCField, int obisDField, Unit unit, String description) {
        this.setRegisterNr(registerNr);
        this.setObisCField(obisCField);
        this.setObisDField(obisDField);
        this.setUnit(unit);
        this.description=description;
    }

    public int getObisCField() {
        return obisCField;
    }

    public void setObisCField(int obisCField) {
        this.obisCField = obisCField;
    }

    public int getObisDField() {
        return obisDField;
    }

    public void setObisDField(int obisDField) {
        this.obisDField = obisDField;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public int getRegisterNr() {
        return registerNr;
    }

    public void setRegisterNr(int registerNr) {
        this.registerNr = registerNr;
    }

    static public UnitTable findUnitTable(int registerNr) throws IOException {
        Iterator it = units.iterator();
        while(it.hasNext()) {
            UnitTable u = (UnitTable)it.next();
            if (u.getRegisterNr() == registerNr)
                return u;
        }

        throw new IOException("UnitTable, findUnitTable, invalid registerNr "+registerNr);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}

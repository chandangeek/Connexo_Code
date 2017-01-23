/*
 * RegisterMap.java
 *
 * Created on 9 januari 2007, 17:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class RegisterMap {

    private ObisCode obisCode;
    private String descriptions;
    private Unit unit;
    private int id; // depends on register, demand or selfread
    private int type; // register=0, demand=1, selfread=2
    private BigDecimal multiplier;

    static private final int REGISTER=0;
    static private final int DEMAND=1;
    static private final int SELFREAD=2;


    /** Creates a new instance of RegisterMap */
    public RegisterMap(ObisCode obisCode,String descriptions, Unit unit,int id,int type,BigDecimal multiplier) {
        this.setObisCode(obisCode);
        this.setUnit(unit);
        this.setId(id);
        this.setType(type);
        this.setDescriptions(getObisCode().getDescription()+", "+descriptions);
        this.setMultiplier(multiplier);
    }

    public String toString() {
        return obisCode+", "+descriptions+", "+unit+"\n";
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    public String getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    static public int getREGISTER() {
        return REGISTER;
    }

    static public int getDEMAND() {
        return DEMAND;
    }

    static public int getSELFREAD() {
        return SELFREAD;
    }

    public boolean isREGISTER() {
        return getType()==getREGISTER();
    }

    public boolean isDEMAND() {
        return getType()==getDEMAND();
    }

    public boolean isSELFREAD() {
        return getType()==getSELFREAD();
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

}

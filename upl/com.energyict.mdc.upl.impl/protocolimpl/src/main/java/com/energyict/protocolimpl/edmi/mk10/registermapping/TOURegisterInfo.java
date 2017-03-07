/*
 * ObisCodeInfo.java
 *
 * Created on 24 maart 2006, 11:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.registermapping;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

/**
 * @author koen
 */
public class TOURegisterInfo {

    private ObisCode obisCode;
    private int edmiEnergyRegisterId;
    private int edmiMaxDemandRegisterId;
    private String description;
    private boolean hasTimeOfMaxDemandDate;
    private boolean hasBillingTimestampDate;
    private int decimalPoint;
    private Unit unit;

    /**
     * Creates a new instance of ObisCodeInfo
     */
    public TOURegisterInfo(ObisCode obisCode, int edmiEnergyRegisterId, String description, boolean hasTimeOfMaxDemandDate, boolean hasBillingTimestampDate, int decimal, Unit unit) {
        this.obisCode = obisCode;
        this.edmiEnergyRegisterId = edmiEnergyRegisterId;
        this.description = description;
        this.hasTimeOfMaxDemandDate = hasTimeOfMaxDemandDate;
        this.hasBillingTimestampDate = hasBillingTimestampDate;
        this.decimalPoint = decimal;
        this.unit = unit;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public int getEdmiEnergyRegisterId() {
        return edmiEnergyRegisterId;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasTimeOfMaxDemandDate() {
        return hasTimeOfMaxDemandDate;
    }

    public boolean hasBillingTimestampDate() {
        return hasBillingTimestampDate;
    }

    public int getDecimalPoint() {
        return decimalPoint;
    }

    public Unit getUnit() {
        return unit;
    }

    // Register address is of the following format:
    // 0aab bbbc cccc dddd
    // aa = the type of data.
    // 		aa = 0 > accumulated energy
    //		aa = 1 > mac demand
    //		aa = 2 > time of max demand

    // Set bits aa to 2 to get the time of maxdemand register,
    // else return a invalid register to generate a "CAN register not found" error when read.
    public int getEdmiMaxDemandRegisterId() {
        if (this.hasTimeOfMaxDemandDate()) {
            this.edmiMaxDemandRegisterId = (this.edmiEnergyRegisterId & 0x9FFF) | 0x4000;
        } else {
            this.edmiMaxDemandRegisterId = 0xFFFF;
        }
        return edmiMaxDemandRegisterId;
    }
}
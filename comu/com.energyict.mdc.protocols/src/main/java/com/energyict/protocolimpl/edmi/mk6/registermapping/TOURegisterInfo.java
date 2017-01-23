/*
 * ObisCodeInfo.java
 *
 * Created on 24 maart 2006, 11:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.registermapping;

import com.energyict.obis.ObisCode;

/**
 *
 * @author koen
 */
public class TOURegisterInfo {

    private ObisCode obisCode;
    private int edmiEnergyRegisterId;
    private String description;
    private boolean timeOfMaxDemand;
    private boolean billingTimestampFrom;
    private boolean billingTimestampTo;

    /** Creates a new instance of ObisCodeInfo */
    public TOURegisterInfo(ObisCode obisCode, int edmiEnergyRegisterId, String description, boolean timeOfMaxDemand, boolean billingTimestampFrom, boolean billingTimestampTo) {
        this.obisCode=obisCode;
        this.setEdmiEnergyRegisterId(edmiEnergyRegisterId);
        this.setDescription(description);
        this.setTimeOfMaxDemand(timeOfMaxDemand);
        this.billingTimestampFrom=billingTimestampFrom;
        this.billingTimestampTo=billingTimestampTo;
    }



    public ObisCode getObisCode() {
        return obisCode;
    }

    private void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    public int getEdmiEnergyRegisterId() {
        return edmiEnergyRegisterId;
    }

    private void setEdmiEnergyRegisterId(int edmiEnergyRegisterId) {
        this.edmiEnergyRegisterId = edmiEnergyRegisterId;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public boolean isTimeOfMaxDemand() {
        return timeOfMaxDemand;
    }

    private void setTimeOfMaxDemand(boolean timeOfMaxDemand) {
        this.timeOfMaxDemand = timeOfMaxDemand;
    }

    public boolean isBillingTimestampFrom() {
        return billingTimestampFrom;
    }

    public void setBillingTimestampFrom(boolean billingTimestampFrom) {
        this.billingTimestampFrom = billingTimestampFrom;
    }

    public boolean isBillingTimestampTo() {
        return billingTimestampTo;
    }

    public void setBillingTimestampTo(boolean billingTimestampTo) {
        this.billingTimestampTo = billingTimestampTo;
    }



}

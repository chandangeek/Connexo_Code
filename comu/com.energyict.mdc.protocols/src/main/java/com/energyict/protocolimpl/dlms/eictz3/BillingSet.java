/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * BillingSet.java
 *
 * Created on 12 oktober 2004, 15:06
 */

package com.energyict.protocolimpl.dlms.eictz3;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @author  Koen
 */
public class BillingSet {

    private static final int REASON_COMM=0;
    private static final int REASON_BUTTON=1;
    private static final int REASON_CONTROL_INPUT=2;
    private static final int REASON_INTERNAL=20;
    String[] reasons={"Communication","Reset button","Control input","","","","","","","","","","","","","","","","","","Internal"};

    Date billingDate;
    int billingReason;
    int daysSinceLastReset;
    int nrOfResets;

    List billingValues = new ArrayList();; // of type BillingValue


    /** Creates a new instance of BillingSet */
    public BillingSet(Date billingDate, int billingReason, int daysSinceLastReset, int nrOfResets) {
        this.billingDate=billingDate;
        this.billingReason=billingReason;
        this.daysSinceLastReset=daysSinceLastReset;
        this.nrOfResets=nrOfResets;
    }

    public BillingValue find(ObisCode obisCode) throws NoSuchRegisterException {
        Iterator it = billingValues.iterator();
        while(it.hasNext()) {
            BillingValue billingValue = (BillingValue)it.next();
            if (billingValue.getObisCode().equals(obisCode))
                return billingValue;
        }
        throw new NoSuchRegisterException("BillingSet, find, no register for obisCode "+obisCode);
    }

    public String toString() {
        return "billing on "+getBillingDate()+", reason="+reasons[getBillingReason()]+", days passed="+getDaysSinceLastReset()+", nr of resets="+getNrOfResets();
    }

    /**
     * Getter for property billingDate.
     * @return Value of property billingDate.
     */
    public java.util.Date getBillingDate() {
        return billingDate;
    }

    /**
     * Setter for property billingDate.
     * @param billingDate New value of property billingDate.
     */
    public void setBillingDate(java.util.Date billingDate) {
        this.billingDate = billingDate;
    }

    /**
     * Getter for property billingReason.
     * @return Value of property billingReason.
     */
    public int getBillingReason() {
        return billingReason;
    }

    /**
     * Setter for property billingReason.
     * @param billingReason New value of property billingReason.
     */
    public void setBillingReason(int billingReason) {
        this.billingReason = billingReason;
    }

    /**
     * Getter for property daysSinceLastReset.
     * @return Value of property daysSinceLastReset.
     */
    public int getDaysSinceLastReset() {
        return daysSinceLastReset;
    }

    /**
     * Setter for property daysSinceLastReset.
     * @param daysSinceLastReset New value of property daysSinceLastReset.
     */
    public void setDaysSinceLastReset(int daysSinceLastReset) {
        this.daysSinceLastReset = daysSinceLastReset;
    }

    /**
     * Getter for property nrOfResets.
     * @return Value of property nrOfResets.
     */
    public int getNrOfResets() {
        return nrOfResets;
    }

    /**
     * Setter for property nrOfResets.
     * @param nrOfResets New value of property nrOfResets.
     */
    public void setNrOfResets(int nrOfResets) {
        this.nrOfResets = nrOfResets;
    }

    /**
     * Getter for property billingValues.
     * @return Value of property billingValues.
     */
    public java.util.List getBillingValues() {
        return billingValues;
    }

    /**
     * Setter for property billingValues.
     * @param billingValues New value of property billingValues.
     */
    public void setBillingValues(java.util.List billingValues) {
        this.billingValues = billingValues;
    }

    public void addBillingValue(BillingValue billingValue) {
        getBillingValues().add(billingValue);
    }
    public void addBillingValues(List billingValues) {
        getBillingValues().addAll(billingValues);
    }
    public BillingValue getBillingValue(int index) {
        return (BillingValue)getBillingValues().get(index);
    }

    public BillingValue findBillingValue(ObisCode obisCode) {
        Iterator it = getBillingValues().iterator();
        while(it.hasNext()) {
            BillingValue bv = (BillingValue)it.next();
            if (bv.getObisCode().equals(obisCode))
                return bv;
        }
        return null;
    }

}

/*
 * BillingSet.java
 *
 * Created on 12 oktober 2004, 15:06
 */

package com.energyict.genericprotocolimpl.iskragprs;

import java.util.ArrayList;
import java.util.Date;
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
    String[] reasons={"Communication","Reset button","Control input","Daily","Monthly","","","","","","","","","","","","","","","","Internal"};
    
    Date billingDate;
    int billingReason;
    int daysSinceLastReset;
    int nrOfResets;
    
//    BillingValue billingValues; // of type BillingValue
    List billingValues = new ArrayList();
    
    
    /** Creates a new instance of BillingSet */
    public BillingSet(Date billingDate, int billingReason, int daysSinceLastReset, int nrOfResets) {
        this.billingDate=billingDate;
        this.billingReason=billingReason;
        this.daysSinceLastReset=daysSinceLastReset;
        this.nrOfResets=nrOfResets;
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

    public void addBillingValue(BillingValue billingValue) {
    	getBillingValues().add(billingValue);
    }
    
    public List getBillingValues(){
    	return billingValues;
    }
}

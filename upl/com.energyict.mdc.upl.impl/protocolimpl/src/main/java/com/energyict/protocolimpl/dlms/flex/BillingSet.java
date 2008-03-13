/*
 * BillingSet.java
 *
 * Created on 10 januari 2008
 */

package com.energyict.protocolimpl.dlms.flex;

import java.util.Date;
/**
*
* @author  Koen
*  <B>@beginchanges</B><BR>
	GN|10012008|Taken over everything from the Iskra protocol and adapted where needed
* @endchanges
*/
public class BillingSet {

    String[] reasons={"Communication","Reset button","Control input","Daily","","","","","","","","","","","","","","","","","Internal"};
    
    Date billingDate;
    int billingReason;
    int daysSinceLastReset;
    int nrOfResets;
    
    BillingValue billingValues; // of type BillingValue
    
    
    /** Creates a new instance of BillingSet */
    public BillingSet(Date billingDate, int billingReason, int daysSinceLastReset, int nrOfResets) {
        this.billingDate=billingDate;
        this.billingReason=billingReason;
        this.daysSinceLastReset=daysSinceLastReset;
        this.nrOfResets=nrOfResets;
    }
    
    
    public BillingValue giveBillingValue(){
    	return billingValues;
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
    public BillingValue getBillingValues(){
    	return billingValues;
    }
    
    /**
     * Setter for property billingValues.
     * @param billingValues New value of property billingValues.
     */
    
    public void addBillingValue(BillingValue billingValue){
    	this.billingValues = billingValue;
    }
    
}

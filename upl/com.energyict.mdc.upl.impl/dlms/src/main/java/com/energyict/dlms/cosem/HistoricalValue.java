/*
 * HistoricalValue.java
 *
 * Created on 19 augustus 2004, 15:54
 */

package com.energyict.dlms.cosem;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.ScalerUnit;

import java.io.IOException;
import java.util.Date;
/**
 *
 * @author  Koen
 */
public class HistoricalValue implements CosemObject {
    
    CosemObject cosemObject;
    Date billingDate;
    int resetCounter;
    
    /** Creates a new instance of HistoricalValue */
    public HistoricalValue() {
    }
    public HistoricalValue(CosemObject cosemObject,Date billingDate,int resetCounter) {
        this.cosemObject=cosemObject;
        this.billingDate=billingDate;
        this.resetCounter=resetCounter;
    }
    public String toString() {
        return "cosemObject="+getCosemObject().toString()+", resetCounter="+getResetCounter()+", billingDate="+getBillingDate();
    }
    
    public String getText() {
        return null;
    }
    /**
     * Getter for property cosemObject.
     * @return Value of property cosemObject.
     */
    public CosemObject getCosemObject() {
        return cosemObject;
    }
    
    /**
     * Setter for property cosemObject.
     * @param object New value of property cosemObject.
     */
    public void setCosemObject(CosemObject cosemObject) {
        this.cosemObject = cosemObject;
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
     * Getter for property resetCounter.
     * @return Value of property resetCounter.
     */
    public int getResetCounter() {
        return resetCounter;
    }
    
    /**
     * Setter for property resetCounter.
     * @param resetCounter New value of property resetCounter.
     */
    public void setResetCounter(int resetCounter) {
        this.resetCounter = resetCounter;
    }

    
    public long getValue() throws IOException {
        return getCosemObject().getValue();
    }
    public Date getCaptureTime() throws IOException {
        return getCosemObject().getCaptureTime();
    }
    public ScalerUnit getScalerUnit() throws IOException {
        return getCosemObject().getScalerUnit();
    }
    public Quantity getQuantityValue() throws IOException {
        return getCosemObject().getQuantityValue();
    }
    
}

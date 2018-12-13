/*
 * BillingPoint.java
 *
 * Created on 22 maart 2004, 12:05
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocolimpl.pact.core.common.PactUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class BillingPoint extends MeterReadingsBlockImpl {
    
    private Date billingDate;
    private int register;
    private int tariffFlags;
    
    /** Creates a new instance of BillingPoint */
    public BillingPoint(byte[] data,TimeZone timeZone) {
        super(data,timeZone);
    }
    
    protected void parse() throws java.io.IOException {
        setBillingDate(PactUtils.getCalendar(ProtocolUtils.getIntLE(getData(),1,2),0,getTimeZone()).getTime());
        setRegister(ProtocolUtils.getIntLE(getData(),3,3));
        setTariffFlags(ProtocolUtils.getIntLE(getData(),6,2));
    }
    
    protected String print() {
        return "BILLINGDATE="+getBillingDate()+", REGISTER="+getRegister()+", TARIFFFLAGS="+getTariffFlags();
    }
    
    /** Getter for property billingDate.
     * @return Value of property billingDate.
     *
     */
    public java.util.Date getBillingDate() {
        return billingDate;
    }
    
    /** Setter for property billingDate.
     * @param billingDate New value of property billingDate.
     *
     */
    public void setBillingDate(java.util.Date billingDate) {
        this.billingDate = billingDate;
    }
    
    /** Getter for property register.
     * @return Value of property register.
     *
     */
    public int getRegister() {
        return register;
    }
    
    /** Setter for property register.
     * @param register New value of property register.
     *
     */
    public void setRegister(int register) {
        this.register = register;
    }
    
    /** Getter for property tariffFlags.
     * @return Value of property tariffFlags.
     *
     */
    public int getTariffFlags() {
        return tariffFlags;
    }
    
    /** Setter for property tariffFlags.
     * @param tariffFlags New value of property tariffFlags.
     *
     */
    public void setTariffFlags(int tariffFlags) {
        this.tariffFlags = tariffFlags;
    }
    
}

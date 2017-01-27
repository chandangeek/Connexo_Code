/*
 * BillingPeriodDefinition.java
 *
 * Created on 15 juli 2004, 9:31
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author  Koen
 */
public class BillingPeriodDefinition extends AbstractLogicalAddress {
    
    private static final int NR_OF_BILLING_DATES=20;
    Date[] date = new Date[NR_OF_BILLING_DATES];
    
    /** Creates a new instance of BillingPeriodDefinition */
    public BillingPeriodDefinition(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Billing dates: ");
        for (int i=0;i<NR_OF_BILLING_DATES;i++) {
            if (i!=0) strBuff.append(", ");
            strBuff.append(i+"="+getDate(i).toString());
        }
        return strBuff.toString();
    }
    
    public void parse(byte[] data, java.util.TimeZone timeZone) throws IOException {
        for (int i=0;i<NR_OF_BILLING_DATES;i++) {
            date[i] = parseDate(data,i*3,timeZone);
        }
    }
    
    private Date parseDate(byte[] data, int offset, java.util.TimeZone timeZone) throws IOException {
        Calendar calendar = ProtocolUtils.getCleanCalendar(timeZone);
        calendar.set(Calendar.YEAR,ProtocolUtils.getInt(data,0+offset,1)+2000);
        calendar.set(Calendar.MONTH,ProtocolUtils.getInt(data,1+offset,1)-1);
        calendar.set(Calendar.DATE,ProtocolUtils.getInt(data,2+offset,1));
        return calendar.getTime();
    }
    
    /**
     * Getter for property date.
     * @return Value of property date.
     */
    public java.util.Date[] getDate() {
        return this.date;
    }
    
    /**
     * Getter for property date[index].
     * @return Value of property date[index].
     */
    public java.util.Date getDate(int index) {
        return date[index];
    }    
    /**
     * Setter for property date.
     * @param date New value of property date.
     */
    public void setDate(java.util.Date[] date) {
        this.date = date;
    }
    
}

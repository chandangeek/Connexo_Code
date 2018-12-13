/*
 * HistoricalValueSetInfo.java
 *
 * Created on 11 juni 2004, 14:27
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Date;
import java.util.TimeZone;

/**
 * @author  Koen
 */
public class HistoricalValueSetInfo {

    private static final int BILLING_DATA=0x01;
    private static final int SEASON_CHANGE=0x02;
    private static final int TARIFF_CHANGE=0x04;
    private static final int SERIAL_COMM_PORT=0x08;
    private static final int OPTICAL_PORT=0x10;
    private static final int PUSH_BUTTON=0x20;
    private static final int EXTERNAL_INPUT=0x40;
    private static final int POWER_UP=0x80;

    private static final String[] triggerSources={"billing data","season change","tariff change","serial comm","optical comm","push button","external input","power up"};

    int billingCount;
    Date billingStartDateTime;
    Date billingEndDateTime;
    int billingTriggerSource;
    Date billingResetDateTime;


    TimeZone timeZone;

    public HistoricalValueSetInfo() {
    }

    /**
     * Creates a new instance of HistoricalValueSetInfo
     */
    public HistoricalValueSetInfo(byte[] data, TimeZone timeZone) throws ProtocolException {
        this.timeZone=timeZone;
        parse(data);
    }

    private String getTriggerSourceDescription() {
        StringBuffer strBuff = new StringBuffer();
        for (int i = 0; i < 8; i++) {
            if ((getBillingTriggerSource() & (0x01 << i)) == (0x01 << i)) {
                strBuff.append(", "+triggerSources[i]);
            }
        }
        return strBuff.toString();
    }


    public String toString() {
        return getBillingCount()+", "+getBillingEndDateTime()+", "+getBillingResetDateTime()+", "+getBillingStartDateTime()+", "+getBillingTriggerSource()+getTriggerSourceDescription();
    }

    private void parse(byte[] data) throws ProtocolException {
       setBillingCount(ProtocolUtils.getIntLE(data,0,2));
       long shift = (long)ProtocolUtils.getIntLE(data,2,4)&0xFFFFFFFFL;
       setBillingStartDateTime(ProtocolUtils.getCalendar(timeZone,shift).getTime());
       shift = (long)ProtocolUtils.getIntLE(data,2+4,4)&0xFFFFFFFFL;
       setBillingEndDateTime(ProtocolUtils.getCalendar(timeZone,shift).getTime());
       setBillingTriggerSource(ProtocolUtils.getIntLE(data,2+4+4,1));
       shift = (long)ProtocolUtils.getIntLE(data,2+4+4+1,4)&0xFFFFFFFFL;
       setBillingResetDateTime(ProtocolUtils.getCalendar(timeZone,shift).getTime());
    }

    /**
     * Getter for property billingCount.
     *
     * @return Value of property billingCount.
     */
    public int getBillingCount() {
        return billingCount;
    }

    /**
     * Setter for property billingCount.
     *
     * @param billingCount New value of property billingCount.
     */
    public void setBillingCount(int billingCount) {
        this.billingCount = billingCount;
    }

    /**
     * Getter for property billingStartDateTime.
     *
     * @return Value of property billingStartDateTime.
     */
    public java.util.Date getBillingStartDateTime() {
        return filterMissingDates(billingStartDateTime);
    }

    /**
     * Setter for property billingStartDateTime.
     *
     * @param billingStartDateTime New value of property billingStartDateTime.
     */
    public void setBillingStartDateTime(java.util.Date billingStartDateTime) {
        this.billingStartDateTime = billingStartDateTime;
    }

    /**
     * Getter for property billingEndDateTime.
     *
     * @return Value of property billingEndDateTime.
     */
    public java.util.Date getBillingEndDateTime() {
        return filterMissingDates(billingEndDateTime);
    }

    /**
     * Setter for property billingEndDateTime.
     *
     * @param billingEndDateTime New value of property billingEndDateTime.
     */
    public void setBillingEndDateTime(java.util.Date billingEndDateTime) {
        this.billingEndDateTime = billingEndDateTime;
    }

    /**
     * Getter for property billingTriggerSource.
     *
     * @return Value of property billingTriggerSource.
     */
    public int getBillingTriggerSource() {
        return billingTriggerSource;
    }

    /**
     * Setter for property billingTriggerSource.
     *
     * @param billingTriggerSource New value of property billingTriggerSource.
     */
    public void setBillingTriggerSource(int billingTriggerSource) {
        this.billingTriggerSource = billingTriggerSource;
    }

    /**
     * Getter for property billingResetDateTime.
     *
     * @return Value of property billingResetDateTime.
     */
    public java.util.Date getBillingResetDateTime() {
        return filterMissingDates(billingResetDateTime);
    }

    /**
     * Setter for property billingResetDateTime.
     *
     * @param billingResetDateTime New value of property billingResetDateTime.
     */
    public void setBillingResetDateTime(java.util.Date billingResetDateTime) {
        this.billingResetDateTime = billingResetDateTime;
    }

    private Date filterMissingDates(Date date) {
        if ((date != null) && (date.getTime() == 0)) {
            return null;
        } else {
            return date;
        }
    }

}

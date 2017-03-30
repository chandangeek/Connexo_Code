/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * BillingPointIdentifier.java
 *
 * Created on 19 maart 2004, 15:03
 */

package com.energyict.protocolimpl.pact.core.meterreading;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.pact.core.common.PactUtils;

import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class BillingPointIdentifier extends MeterReadingsBlockImpl {

	private int channelId;
	private int bpIndex;

	private Date dateTime;
	private int cause;

    /** Creates a new instance of BillingPointIdentifier */
    public BillingPointIdentifier(byte[] data,TimeZone timeZone) {
        super(data,true,timeZone);
    }

    public String print() {
       return "CHN_ID=0x"+Integer.toHexString(getChannelId())+" (BP_INDEX="+getBpIndex()+", CHAN_NUM=unused"+
              "), date&time="+(getDateTime()!=null ? getDateTime().toString():"INVALID" )+", CAUSE="+getCause();
    }

    protected void parse() throws java.io.IOException {
       setChannelId(ProtocolUtils.byte2int(getData()[1]));
       setBpIndex(getChannelId()>>4);
       int iDate = ProtocolUtils.getIntLE(getData(),2,2);
       if (iDate == 0xFFFF) {
		setDateTime(null);
	} else {
		setDateTime(PactUtils.getCalendar(ProtocolUtils.getIntLE(getData(),2,2),ProtocolUtils.getIntLE(getData(),4,2),getTimeZone()).getTime());
	}
       setCause(ProtocolUtils.byte2int(getData()[6]));

    }

    /** Getter for property channelId.
     * @return Value of property channelId.
     *
     */
    public int getChannelId() {
        return channelId;
    }

    /** Setter for property channelId.
     * @param channelId New value of property channelId.
     *
     */
    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    /** Getter for property dateTime.
     * @return Value of property dateTime.
     *
     */
    public java.util.Date getDateTime() {
        return dateTime;
    }

    /** Setter for property dateTime.
     * @param dateTime New value of property dateTime.
     *
     */
    public void setDateTime(java.util.Date dateTime) {
        this.dateTime = dateTime;
    }

    /** Getter for property cause.
     * @return Value of property cause.
     *
     */
    public int getCause() {
        return cause;
    }

    /** Setter for property cause.
     * @param cause New value of property cause.
     *
     */
    public void setCause(int cause) {
        this.cause = cause;
    }

    /** Getter for property bpIndex.
     * @return Value of property bpIndex.
     *
     */
    public int getBpIndex() {
        return bpIndex;
    }

    /** Setter for property bpIndex.
     * @param bpIndex New value of property bpIndex.
     *
     */
    public void setBpIndex(int bpIndex) {
        this.bpIndex = bpIndex;
    }

}

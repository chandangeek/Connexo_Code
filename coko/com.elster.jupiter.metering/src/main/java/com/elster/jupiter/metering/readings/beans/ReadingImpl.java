package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.metering.readings.Reading;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Our default implementation of a {@link Reading}.
 * A Reading serves as a single reading for ex. a RegisterValue
 *
 * Copyrights EnergyICT
 * Date: 25/11/13
 * Time: 15:16
 */
public class ReadingImpl extends BaseReadingImpl implements Reading {

    private final String mrid;
    private String reason;
    private String text;

    public ReadingImpl(String mrid, BigDecimal value, Date timeStamp) {
        this(mrid,value,null,timeStamp);
    }

    public ReadingImpl(String mrid, String text, Date timeStamp) {
    	this(mrid,null,text,timeStamp);
    }
    
    private ReadingImpl(String mrid, BigDecimal value, String text, Date timeStamp) {
    	super(timeStamp,value);
    	this.mrid = mrid;
    	this.text = text;
    }
    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public String getReadingTypeCode() {
        return this.mrid;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
    
    @Override
    public String getText() {
    	return text;
    }
    
}

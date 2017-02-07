package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.metering.readings.Reading;

import java.math.BigDecimal;
import java.time.Instant;

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
    
    private ReadingImpl(String mrid, BigDecimal value, String text, Instant timeStamp) {
    	super(timeStamp,value);
    	this.mrid = mrid;
    	this.text = text;
    }
    
    public static ReadingImpl of(String mrid, BigDecimal value, Instant timeStamp) {
    	return new ReadingImpl(mrid,value,null,timeStamp);
    }
    
    public static ReadingImpl of(String mrid, String text, Instant timeStamp) {
    	return new ReadingImpl(mrid,null,text,timeStamp);
    }

    public static ReadingImpl of(String mrid, BigDecimal value, Instant timestamp, Instant from, Instant to){
        ReadingImpl reading = new ReadingImpl(mrid,value,null, timestamp);
        reading.setTimePeriod(from,to);
        return reading;
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
    
    public void setText(String text) {
    	this.text = text;
    }
    
}

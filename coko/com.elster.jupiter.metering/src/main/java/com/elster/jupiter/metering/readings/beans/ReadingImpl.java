/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.metering.readings.Reading;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.time.Instant;

@XmlRootElement
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public class ReadingImpl extends BaseReadingImpl implements Reading {

    private String mrid;
    private String reason;
    private String text;

    public ReadingImpl() {
        super();
    }

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

    public void setReadingTypeCode(String mrid) {
        this.mrid = mrid;
    }

    @Override
    public String getText() {
    	return text;
    }
    
    public void setText(String text) {
    	this.text = text;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
    
}

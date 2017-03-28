/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents register data.<br>
 * There are a lot of register types that are represented differently in front end.<br>
 * Type of register type depends on attributes such as {@link RegisterDataInfo#isCumulative}, {@link RegisterDataInfo#hasEvent},
 * {@link RegisterDataInfo#isBilling} etc.<br>
 * Different set of attributes are used for different register types.
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NumericalRegisterDataInfo.class, name = "numerical"),
        @JsonSubTypes.Type(value = TextRegisterDataInfo.class, name = "text"),
        @JsonSubTypes.Type(value = FlagRegisterDataInfo.class, name = "flags"),
})
public abstract class RegisterDataInfo {

    /*
        register.getReadingType().isCumulative()
     */
    /**
     * Cumulative register type flag
     */
    public boolean isCumulative;
    /*
    private final List<Aggregate> aggregatesWithEventDate = Arrays.asList(Aggregate.MAXIMUM, Aggregate.FIFTHMAXIMIMUM,
            Aggregate.FOURTHMAXIMUM, Aggregate.MINIMUM, Aggregate.SECONDMAXIMUM, Aggregate.SECONDMINIMUM, Aggregate.THIRDMAXIMUM);

            value = aggregatesWithEventDate.contains(getReadingType().getAggregate());
     */
    /**
     * Event register type flag
     */
    public boolean hasEvent;

    /*
        register.getReadingType().getMacroPeriod().equals(MacroPeriod.BILLINGPERIOD);
     */
    /**
     * Billing register type flag
     */
    public boolean isBilling;

    /*

    get range:

if (getRegister().getReadingType().isCumulative()) {

            if(getRegister().isBilling() && getActualReading().getTimePeriod().isPresent()){
            return Optional.of(Range.openClosed(getPreviousReading().get().getTimeStamp(), getActualReading().getTimePeriod().get().upperEndpoint()));
        } else {
            return Optional.of(Range.openClosed(getPreviousReading().get().getTimeStamp(), getActualReading().getTimeStamp()));
        }


} else if (getRegister().isBilling()) {

            readingRecord.getTimePeriod();

 } else {
            return Optional.empty();
        }

!!! only for billing ???
     */
    /**
     * Measurement period for register value. Applicable for billing and/or cumulative register types
     */
    public MeasurementPeriod measurementPeriod;

    /*


    for cumulative only (numeric)

    BigDecimal value = getValue(); -> reading.getValue
        BigDecimal previousValue = getPreviousValue();
        if(value != null && previousValue != null) {
            return value.subtract(previousValue);
        }
        return null;


     */
    /**
     * Delta value for cumulative register type
     */
    public BigDecimal deltaValue;


    /*
    only if hasEvent() == true

    readingRecord.getTimeStamp()

     */
    /**
     * Event date for event register type
     */
    public Instant eventDate;



}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling;

import com.elster.jupiter.time.TemporalExpression;

import aQute.bnd.annotation.ProviderType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Models the specifications that will allow a component
 * to calculate the next execution timestamp of a task.
 * <p/>
 * The calucation is based on a {@link com.elster.jupiter.time.TemporalExpression}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @see NextExecutionCalculator
 * @since 2012-04-11 (17:51)
 */
@ProviderType
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
public interface NextExecutionSpecs extends NextExecutionCalculator {

    /**
     * Returns the number that uniquely identifies this NextExecutionSpecs.
     *
     * @return The unique identifier
     */
    @XmlAttribute
    long getId();

    /**
     * Gets the {@link com.elster.jupiter.time.TemporalExpression} that specifies
     * the recurring time of the execution of a task.
     *
     * @return The recurring time
     */
    @XmlAttribute
    TemporalExpression getTemporalExpression();

    void setTemporalExpression(TemporalExpression temporalExpression);

    void update();

    void delete();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public String getXmlType();

    public void setXmlType(String ignore);
}
/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import aQute.bnd.annotation.ConsumerType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.time.Instant;
import java.util.Optional;

/**
 * This class wraps an actual passphrase with the information required to read it from db or renew it.
 * Through the offering of PropertySpecs & properties, a generic interface is offered for the UI
 */
@ConsumerType
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
public interface PassphraseWrapper extends HasDynamicPropertiesWithUpdatableValues, SecurityValueWrapper {

    /**
     * Defines the method used to store keys by this implementation.
     * @return
     */
    String getKeyEncryptionMethod();

    /**
     * The exact date when the value of this element will expire. The value should be renewed by this date.
     * @return date until which this element is valid
     */
    Optional<Instant> getExpirationTime();

    /**
     * Deletes this wrapper and the key it contains
     */
    void delete();

    /**
     * Allows the generation of a random value for an empty wrapper, in this case, a passphrase.
     * Any existing value will be overwritten.
     * It's up to the implementing class to make sure all renewal information is available (through linking
     * KeyTypes/KeyAccessorTypes)
     * Note that not all key encryption methods might permit automatic renewal.
     */
    public void generateValue();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public default String getXmlType() {
        return this.getClass().getName();
    }

    public default void setXmlType(String ignore) {}
}

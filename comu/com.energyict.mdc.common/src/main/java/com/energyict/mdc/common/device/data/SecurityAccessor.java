/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.pki.SecurityValueWrapper;

import aQute.bnd.annotation.ConsumerType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Represents access to a wrapper object on device level. This security object can be a certificate, symmetric key or password.
 * The Security accessor stores the real value(s) for a KeyAccessorType
 * An accessor stores two values: one for current use (ActualValue), and one value that is stored during the renew
 * process (TempValue).
 */
@ConsumerType
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
public interface SecurityAccessor<T extends SecurityValueWrapper> extends com.elster.jupiter.pki.SecurityAccessor<T> {

    /**
     * Returns id of {@link com.elster.jupiter.pki.SecurityAccessor}, not the id of security accessor on device.
     * To identify the instance of this class please use this id together with {@link #getDevice()}.{@link Device#getId() getId()}.
     * @return Id of {@link com.elster.jupiter.pki.SecurityAccessor}.
     */
    @Override
    default long getId() {
        return com.elster.jupiter.pki.SecurityAccessor.super.getId();
    }

    /**
     * Get the device this KeyAccessor holds a value for
     */
    Device getDevice();

    /**
     * Status indicates if this KeyAccessor is ready for use or not. A KeyAccessor is considered complete/ready for use
     * if it has an actual value and all properties of this actual value have been filled id
     * @return Complete if ready for use, Incomplete otherwise.
     */
    KeyAccessorStatus getStatus();

    /**
     *
     * @return {@code true} if it's allowed to change this security accessor or its values (properties) on device level, {@code false} otherwise.
     */
    boolean isEditable();

    /**
     * Sets/unsets service key flag for the key.
     */
    void setServiceKey(boolean serviceKey);

    /**
     * @return {@code true} if security accessor has service key, {@code false} otherwise.
     */
    boolean isServiceKey();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public default String getXmlType() {
        return this.getClass().getName();
    }

    default void setXmlType(String ignore) {
    }

    default void touch() {
    }
}

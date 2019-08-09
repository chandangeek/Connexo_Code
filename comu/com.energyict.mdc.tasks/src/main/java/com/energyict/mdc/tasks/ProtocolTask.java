/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Describes an action that needs to be performed when communicating with a device.
 * A ProtocolTask can be used by only one ComTask
 *
 * @author gna
 * @since 19/04/12 - 13:49
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
public interface ProtocolTask {

    /**
     * Returns the {@link ComTask} this ProtocolTask belongs to
     *
     * @return the ComTask of this ProtocolTask
     */
    public ComTask getComTask ();

    public long getId();

    public void save();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public String getXmlType();

    public void setXmlType(String ignore);
}
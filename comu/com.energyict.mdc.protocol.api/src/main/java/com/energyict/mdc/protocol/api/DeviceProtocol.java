/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.pluggable.Pluggable;
import com.energyict.mdc.protocol.api.tasks.support.ConnectionTypeSupport;
import com.energyict.mdc.protocol.api.tasks.support.DeviceProtocolDialectSupport;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Defines an Interface between the Data Collection System and a Device. The interface can both be
 * used at operational time and at configuration time.
 */
@ProviderType
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
public interface DeviceProtocol extends Pluggable, DeviceProtocolDialectSupport,
        DeviceSecuritySupport, ConnectionTypeSupport, com.energyict.mdc.upl.DeviceProtocol {

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public default String getXmlType() {
        return this.getClass().getName();
    }

    public default void setXmlType(String ignore) {
    }
}
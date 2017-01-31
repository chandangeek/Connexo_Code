/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data.identifiers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.BaseLogBook;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * Uniquely identifies a log book that is stored in a physical device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (17:51)
 */
public interface LogBookIdentifier<T extends BaseLogBook> extends Serializable {

    /**
     * Finds the LogBook that is uniquely identified by this LogBookIdentifier.
     *
     * @return the LogBook
     */
    public T getLogBook();

    /**
     * Returns the ObisCode of the LogBook referenced by this identifier.<br></br>
     * <b>Note: </b>This is the ObisCode which is configured/overwritten in the DeviceConfiguration.
     * If no ObisCode was overruled, then the ObisCode of the spec is returned.
     */
    @XmlAttribute
    public ObisCode getLogBookObisCode();


    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public String getXmlType();

    public void setXmlType(String ignore);

    /**
     * @return the DeviceIdentifier for this LogBookIdentifier
     */
    public DeviceIdentifier<?> getDeviceIdentifier();
}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data.identifiers;

import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * Identifies a device that started inbound communication.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (16:51)
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface DeviceIdentifier<T extends BaseDevice< ? extends BaseChannel, ? extends BaseLoadProfile<?  extends BaseChannel>, ? extends  BaseRegister>> extends Serializable {

    /**
     * Finds the {@link com.energyict.mdc.protocol.api.device.BaseDevice} that is uniquely identified by this DeviceIdentifier.
     * An <b>CanNotFindForIdentifier</b> can be thrown if the device is not found
     *
     * @return The Device
     */
    public T findDevice();

    /**
     * The essential part of this identifier: the serial number, the database ID, the call home Id or something else.
     */
    public String getIdentifier();

    /**
     * The type of this identifier. E.g. SerialNumber, DataBaseId, ...
     */
    public DeviceIdentifierType getDeviceIdentifierType();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public String getXmlType();

    public void setXmlType(String ignore);

}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data.identifiers;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Uniquely identifies a message that is sent to physical devices.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (17:45)
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface MessageIdentifier {

    /**
     * Returns the {@link DeviceMessage} BusinessObject referenced by this identifier
     *
     * @return the referenced DeviceMessage
     * @throws com.energyict.mdc.common.NotFoundException when the DeviceMessage could not be found
     */
    public DeviceMessage getDeviceMessage();

    /**
     * The type of this identifier.
     */
    public MessageIdentifierType getMessageIdentifierType();

    /**
     * The essential part(s) of this identifier: the database ID, deviceIdentifier and ObisCode, ...
     */
    public List<Object> getIdentifier();


    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public String getXmlType();

    public void setXmlType(String ignore);

    /**
     * @return the DeviceIdentifier for this MessageIdentifier
     */
    public DeviceIdentifier<?> getDeviceIdentifier();

}
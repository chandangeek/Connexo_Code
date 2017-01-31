/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data.identifiers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

/**
 * Uniquely identifies a load profile that is stored in a physical device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (17:51)
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface LoadProfileIdentifier<T extends BaseLoadProfile> extends Serializable {

    /**
     * Returns the LoadProfile BusinessObject referenced by this identifier.
     * <b>Remark:</b> Make sure the LoadProfileFactory if retrieved from the LoadProfileFactoryProvider;
     * Direct calls to MeteringWarehouse.getCurrent() are not allowed here!
     *
     * @return the referenced LoadProfile
     * @throws com.energyict.mdc.common.NotFoundException when the LoadProfile could not be found
     */
    public T findLoadProfile();

    /**
     * The type of this identifier.
     */
    public LoadProfileIdentifierType getLoadProfileIdentifierType();

    /**
     * The essential part(s) of this identifier: the database ID, deviceIdentifier and ObisCode, ...
     */
    public List<Object> getIdentifier();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public String getXmlType();

    public void setXmlType(String ignore);

    /**
     * @return the DeviceIdentifier for this LoadProfileIdentifier
     */
    public DeviceIdentifier<?> getDeviceIdentifier();

    ObisCode getProfileObisCode();
}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import aQute.bnd.annotation.ProviderType;
import com.energyict.obis.ObisCode;

import java.util.List;
import java.util.Optional;

/**
 * Provides services that relate to {@link LogBook}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:15)
 */
@ProviderType
public interface LogBookService {

    /**
     * Finds the LogBook with the given unique ID
     *
     * @param id the unique ID of the LogBook
     * @return the requested LogBook or null if none exists with that ID
     */
    Optional<LogBook> findById(long id);

    /**
     * Finds the LogBook with the given unique {@link LogBookIdentifier}.
     *
     * @param identifier The LogBookIdentifier
     * @return the requested LogBook or null if none exists with that ID
     */
    Optional<LogBook> findByIdentifier(LogBookIdentifier identifier);

    /**
     * Finds the LogBook with the given {@link ObisCode} linked to the given {@link Device}
     *
     * @param device the device
     * @param obisCode the obis code of the LogBook
     * @return the requested LogBook or null if none exists
     */
    Optional<LogBook> findByDeviceAndObisCode(Device device, ObisCode obisCode);

    /**
     * Finds all the LogBooks for the given Device
     *
     * @param device the device
     * @return a list of LogBooks which exist for the given Device
     */
    List<LogBook> findLogBooksByDevice(Device device);

}
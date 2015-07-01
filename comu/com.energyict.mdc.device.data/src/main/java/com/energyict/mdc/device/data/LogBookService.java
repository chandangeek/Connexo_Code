package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

import java.util.List;

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
    public Optional<LogBook> findById(long id);

    /**
     * Finds all the LogBooks for the given Device
     *
     * @param device the device
     * @return a list of LogBooks which exist for the given Device
     */
    public List<LogBook> findLogBooksByDevice(Device device);

}
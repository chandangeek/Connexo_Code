package com.energyict.mdc.masterdata;

import com.energyict.mdc.common.ObisCode;

import java.util.List;

/**
 * Provides services that relate to all types of master data.
 * Examples are:
 * <ul>
 * <li>{@link LogBookType}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:34)
 */
public interface MasterDataService {

    public static String COMPONENTNAME = "MDS";

    /**
     * Find all {@link LogBookType LogBookTypes} in the system.
     *
     * @return all the LogBookTypes in the system
     */
    public List<LogBookType> findAllLogBookTypes();

    /**
     * Find a {@link LogBookType} with the given ID.
     *
     * @param id the ID of the LogBookType
     * @return the LogBookType or <code>null</code> if there is no such LogBookType
     */
    public LogBookType findLogBookType(long id);

    public LogBookType findLogBookTypeByName(String name);

    /**
     * Creates a new LogBookType based on the given parameters.
     *
     * @param name     the name of the LogBookType
     * @param obisCode the ObisCode of the LogBookType
     * @return the newly created LogBookType
     */
    public LogBookType newLogBookType(String name, ObisCode obisCode);

}
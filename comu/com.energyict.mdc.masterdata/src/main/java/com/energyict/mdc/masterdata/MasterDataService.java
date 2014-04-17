package com.energyict.mdc.masterdata;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.services.Finder;
import com.google.common.base.Optional;

import java.util.List;

/**
 * Provides services that relate to all types of master data.
 * Examples are:
 * <ul>
 * <li>{@link LogBookType}</li>
 * <li>{@link RegisterGroup}</li>
 * <li>{@link RegisterMapping}</li>
 * <li>{@link LoadProfileType}</li>
 * <li>{@link Phenomenon}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:34)
 */
public interface MasterDataService {

    public static String COMPONENTNAME = "MDS";

    /**
     * Finds all the {@link Phenomenon} in the system.
     *
     * @return The List of Phenomenon
     */
    public List<Phenomenon> findAllPhenomena();

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
    public Optional<LogBookType> findLogBookType(long id);

    public Optional<LogBookType> findLogBookTypeByName(String name);

    /**
     * Creates a new LogBookType based on the given parameters.
     *
     * @param name     the name of the LogBookType
     * @param obisCode the ObisCode of the LogBookType
     * @return the newly created LogBookType
     */
    public LogBookType newLogBookType(String name, ObisCode obisCode);

    public Optional<Phenomenon> findPhenomenon(int phenomenonId);

    public Phenomenon newPhenomenon(String name, Unit unit);

    public Optional<Phenomenon> findPhenomenonByUnit(Unit unit);

    public Optional<Phenomenon> findPhenomenonByNameAndUnit(String name, Unit unit);

    /**
     * Finds the {@link RegisterGroup} that is uniquely identified by the specified number.
     *
     * @param id The unique identifier
     * @return The RegisterGroup or <code>null</code> if there is no such RegisterGroup
     */
    public Optional<RegisterGroup> findRegisterGroup(long id);

    /**
     * Creates a new {@link RegisterGroup} with the specified name.
     * Note that the RegisterGroup is only saved in the database
     * after a call to the "save" method.
     *
     * @param name The name of the RegisterGroup
     * @return The new RegisterGroup
     * @see RegisterGroup#save()
     */
    public RegisterGroup newRegisterGroup(String name);

    public List<RegisterGroup> findAllRegisterGroups();

    public Finder<RegisterMapping> findAllRegisterMappings();

    /**
     * Finds the {@link RegisterMapping} that is uniquely identified by the specified number.
     *
     * @param id The unique identifier
     * @return The RegisterMapping or <code>null</code> if there is no such RegisterMapping
     */
    public Optional<RegisterMapping> findRegisterMapping(long id);

    /**
     * Finds the {@link RegisterMapping} that is uniquely identified by the name.
     *
     * @param name The name
     * @return The RegisterMapping or <code>null</code> if there is no such RegisterMapping
     */
    public Optional<RegisterMapping> findRegisterMappingByName(String name);

    public Optional<RegisterMapping> findRegisterMappingByReadingType(ReadingType readingType);

    /**
     * Creates a new {@link RegisterMapping} with the specified required properties.
     * Note that {@link ObisCode} uniquely identifies the RegisterMapping,
     * i.e. there can only be 1 RegisterMapping for every ObisCode.
     *
     * @param name        The RegisterMapping name
     * @param obisCode    The ObisCode
     * @param unit        The unit
     * @param readingType The reading type
     * @param timeOfUse   Customer specif timeOfUse id
     * @return The new RegisterMapping
     * @see RegisterMapping#save()
     */
    public RegisterMapping newRegisterMapping(String name, ObisCode obisCode, Unit unit, ReadingType readingType, int timeOfUse);

    public Optional<RegisterMapping> findRegisterMappingByObisCodeAndUnitAndTimeOfUse(ObisCode obisCode, Unit unit, int timeOfUse);

    /**
     * Finds all the {@link LoadProfileType LoadProfileTypes} in the systesm
     *
     * @return all LoadProfileTypes
     */
    public List<LoadProfileType> findAllLoadProfileTypes();

    /**
     * Creates a new LoadProfileType based on the given parameters
     *
     * @param name     the Name of the LoadProfileType
     * @param obisCode the ObisCode of the LoadProfileType
     * @param interval the interval of the LoadProfileType
     * @return the newly created LoadProfileType
     */
    public LoadProfileType newLoadProfileType(String name, ObisCode obisCode, TimeDuration interval);

    /**
     * Find the {@link LoadProfileType} with the given ID
     *
     * @param loadProfileTypeId the ID of the {@link LoadProfileType}
     * @return the LoadProfileType or <code>null</code> if there is no such LoadProfileType
     */
    public Optional<LoadProfileType> findLoadProfileType(long loadProfileTypeId);

    public List<LoadProfileType> findLoadProfileTypesByName(String name);

}
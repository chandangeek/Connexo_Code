package com.energyict.mdc.metering;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.Unit;

import java.util.Optional;

/**
 * Provides converters from ReadingType to ObisCode/Unit/Interval and visa versa
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/02/14
 * Time: 14:30
 */
public interface MdcReadingTypeUtilService {

    /**
     * Creates a {@link ReadingTypeInformation} based on the given readingType
     *
     * @param readingType the readingType string
     * @return the ReadingTypeInformation modeled by the given readingType string
     */
    public ReadingTypeInformation getReadingTypeInformationFor(String readingType);

    /**
     * Creates a {@link ReadingTypeInformation} based on the given readingType
     *
     * @param readingType the ReadingType
     * @return the ReadingTypeInformation modeled by the given ReadingType
     */
    public ReadingTypeInformation getReadingTypeInformationFor(ReadingType readingType);

    /**
     * Creates a ReadingType string based on the given arguments
     *
     * @param obisCode the ObisCode that models the ReadingType
     * @param unit     the Unit that models the ReadingType
     * @return the ReadingType string modeled by the given arguments
     */
    public String getReadingTypeMridFrom(ObisCode obisCode, Unit unit);

    public ReadingType getReadingTypeFrom(ObisCode obisCode, Unit unit);

    /**
     * Creates a ReadingType string based on the given arguments
     *
     * @param obisCode the ObisCode that models the ReadingType
     * @param unit     the Unit that models the ReadingType
     * @param interval the Interval that models the ReadingType
     * @return the ReadingType string modeled by the given arguments
     */
    public String getReadingTypeFrom(ObisCode obisCode, Unit unit, TimeDuration interval);

    /**
     * Creates a readingType from the given ReadingType with the given interval applied.
     *
     * @param readingType the ReadingType to start from
     * @param interval the Interval to apply
     * @param registerObisCode the ObisCode of the register for the ReadingType
     *
     * @return the interval applied ReadingType
     */
    public Optional<ReadingType> getIntervalAppliedReadingType(ReadingType readingType, Optional<TimeDuration> interval, ObisCode registerObisCode);

    /**
     * Gets the MDC unit according to the CIM ReadingType.
     * If no proper ReadingType is provided or nor proper unit matching can be done, the unit "UNDEFINED" will be returned
     *
     * @param readingType the ReadingType
     * @return the mdc Unit
     */
    public Unit getMdcUnitFor(String readingType);

    /**
     * Get the fullAliasName of the ReadingType
     *
     * @param readingType the ReadingType to get the FullAlias from
     * @return the fullAliasName
     */
    public String getFullAlias(ReadingType readingType);

    /**
     * Get the fullAliasName of the ReadingType
     *
     * @param readingType the ReadingType to get the FullAlias from
     * @return the fullAliasName
     */
    public String getFullAlias(String readingType);
}

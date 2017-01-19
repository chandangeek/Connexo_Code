package com.energyict.mdc.metering;

import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;

import aQute.bnd.annotation.ProviderType;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.util.Optional;

/**
 * Provides converters from ReadingType to ObisCode/Unit/Interval and visa versa
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/02/14
 * Time: 14:30
 */
@ProviderType
public interface MdcReadingTypeUtilService {

    /**
     * Creates a {@link ReadingTypeInformation} based on the given readingType
     *
     * @param readingType the readingType string
     * @return the ReadingTypeInformation modeled by the given readingType string
     */
    ReadingTypeInformation getReadingTypeInformationFor(String readingType);

    /**
     * Creates a {@link ReadingTypeInformation} based on the given readingType
     *
     * @param readingType the ReadingType
     * @return the ReadingTypeInformation modeled by the given ReadingType
     */
    ReadingTypeInformation getReadingTypeInformationFor(ReadingType readingType);

    /**
     * Creates a ReadingType string based on the given arguments
     *
     * @param obisCode the ObisCode that models the ReadingType
     * @param unit     the Unit that models the ReadingType
     * @return the ReadingType string modeled by the given arguments
     */
    String getReadingTypeMridFrom(ObisCode obisCode, Unit unit);

    ReadingType getReadingTypeFrom(ObisCode obisCode, Unit unit);

    /**
     * Creates a ReadingType string based on the given arguments
     *
     * @param obisCode the ObisCode that models the ReadingType
     * @param unit     the Unit that models the ReadingType
     * @param interval the Interval that models the ReadingType
     * @return the ReadingType string modeled by the given arguments
     */
    String getReadingTypeFrom(ObisCode obisCode, Unit unit, TimeDuration interval);

    /**
     * Gets a readingType from the given ReadingType with the given interval applied.
     *
     * @param readingType the ReadingType to start from
     * @param interval the Interval to apply
     * @param registerObisCode the ObisCode of the register for the ReadingType
     *
     * @return an optional ReadingType
     */
    Optional<ReadingType> getIntervalAppliedReadingType(ReadingType readingType, Optional<TimeDuration> interval, ObisCode registerObisCode);

    /**
     * Creates a readingType from the given ReadingType with the given interval applied.
     * A new ReadingType will be created if it doesnt exits yet
     *
     * @param readingType the ReadingType to start from
     * @param interval the Interval to apply
     * @param registerObisCode the ObisCode of the register for the ReadingType
     *
     * @return the interval applied ReadingType
     */
    ReadingType getOrCreateIntervalAppliedReadingType(ReadingType readingType, Optional<TimeDuration> interval, ObisCode registerObisCode);

    /**
     * Finds the ReadingType which is
     *
     * @param mrid  of the ReadingType to find or create
     * @param alias the alias to give to the readingType in case you have to create one
     * @return the requested ReadingType
     */
    ReadingType findOrCreateReadingType(String mrid, String alias);

    /**
     * Gets the MDC unit according to the CIM ReadingType.
     * If no proper ReadingType is provided or no proper unit matching can be done, the unit "UNDEFINED" will be returned.
     *
     * @param readingType the ReadingType
     * @return the mdc Unit
     */
    Unit getMdcUnitFor(String readingType);

    /**
     * Gets the MDC unit according to the CIM ReadingType.
     * If no proper ReadingType is provided or no proper unit matching can be done, the unit "UNDEFINED" will be returned.
     *
     * @param readingType the ReadingType
     * @return the mdc Unit
     */
    Unit getMdcUnitFor(ReadingType readingType);

    /**
     * Creates a ReadingTypeCodeBuilder from a given readingType
     *
     * @param readingType the readingType which will serve as template for the ReadingTypeCodeBuilder
     * @return the readingTypeCodeBuilder
     */
    ReadingTypeCodeBuilder createReadingTypeCodeBuilderFrom(ReadingType readingType);

    }

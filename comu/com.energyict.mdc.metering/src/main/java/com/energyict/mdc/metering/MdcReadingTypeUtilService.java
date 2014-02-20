package com.energyict.mdc.metering;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;

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
    public String getReadingTypeFrom(ObisCode obisCode, Unit unit);

    /**
     * Creates a ReadingType string based on the given arguments
     *
     * @param obisCode the ObisCode that models the ReadingType
     * @param unit     the Unit that models the ReadingType
     * @param interval the Interval that models the ReadingType
     * @return the ReadingType string modeled by the given arguments
     */
    public String getReadingTypeFrom(ObisCode obisCode, Unit unit, TimeDuration interval);
}

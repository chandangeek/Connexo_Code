package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.config.NumericalRegisterSpec;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Models a {@link Register} that strictly stores numerical data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (12:05)
 */
@ProviderType
public interface NumericalRegister extends Register<NumericalReading, NumericalRegisterSpec> {

    /**
     * Returns the readingtype of the calculated value.
     * <ul>
     *     <li>Either the delta if the readingType was a bulk and no multiplier was provided</li>
     *     <li>Or the multiplied readingType if a multiplier was provided</li>
     * </ul>
     * @return the calculated ReadingType
     */
    Optional<ReadingType> getCalculatedReadingType();

    /**
     * Provides the value of the multiplier of this channel. The value will only be present if
     * the multiplier is larger than one (1)
     *
     * @return the optional multiplier
     */
    Optional<BigDecimal> getMultiplier();
}
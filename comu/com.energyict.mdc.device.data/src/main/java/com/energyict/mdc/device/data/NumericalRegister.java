package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.config.NumericalRegisterSpec;

import java.util.Optional;

/**
 * Models a {@link Register} that strictly stores numerical data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (12:05)
 */
@ProviderType
public interface NumericalRegister extends Register<NumericalReading, NumericalRegisterSpec> {

    Optional<ReadingType> getCalculatedReadingType();
}
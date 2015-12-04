package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.device.config.NumericalRegisterSpec;

/**
 * Models a {@link Register} that stores sets of bit flags.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (12:05)
 */
@ProviderType
public interface FlagsRegister extends Register<FlagsReading, NumericalRegisterSpec> {
}
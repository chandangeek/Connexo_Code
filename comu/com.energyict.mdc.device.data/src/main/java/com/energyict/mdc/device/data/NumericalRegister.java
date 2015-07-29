package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a {@link Register} that strictly stores numerical data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (12:05)
 */
@ProviderType
public interface NumericalRegister extends Register<NumericalReading> {
}
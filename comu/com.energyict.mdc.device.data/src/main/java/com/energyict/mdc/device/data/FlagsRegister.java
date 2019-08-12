/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.energyict.mdc.common.device.config.NumericalRegisterSpec;
import com.energyict.mdc.common.device.data.Register;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a {@link Register} that stores sets of bit flags.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (12:05)
 */
@ProviderType
public interface FlagsRegister extends Register<FlagsReading, NumericalRegisterSpec> {
}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.energyict.mdc.device.config.TextualRegisterSpec;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a {@link Register} that stores alphanumerical data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (12:05)
 */
@ProviderType
public interface TextRegister extends Register<TextReading, TextualRegisterSpec> {
}
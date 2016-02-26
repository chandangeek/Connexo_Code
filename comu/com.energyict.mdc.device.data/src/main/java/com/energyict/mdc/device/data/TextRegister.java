package com.energyict.mdc.device.data;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.device.config.TextualRegisterSpec;

/**
 * Models a {@link Register} that stores alphanumerical data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (12:05)
 */
@ProviderType
public interface TextRegister extends Register<TextReading, TextualRegisterSpec> {
}
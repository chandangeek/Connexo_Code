package com.energyict.mdc.device.data;

import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

/**
 * Provides services for {@link Register}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-23 (12:45)
 */
@ProviderType
public interface RegisterService {

    Optional<Register> find(RegisterIdentifier identifier);
}
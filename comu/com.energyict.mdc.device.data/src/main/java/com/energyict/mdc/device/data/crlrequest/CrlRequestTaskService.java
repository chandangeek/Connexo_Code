package com.energyict.mdc.device.data.crlrequest;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface CrlRequestTaskService {

    CrlRequestTaskProperty newCrlRequestTaskProperties();

    Optional<CrlRequestTaskProperty> findCrlRequestTaskProperties();

}

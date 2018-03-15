package com.energyict.mdc.device.data.crlrequest;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface CrlRequestTaskService {

    CrlRequestTaskProperty newCrlRequestTaskProperties();

    CrlRequestTaskProperty findCrlRequestTaskProperties();

}

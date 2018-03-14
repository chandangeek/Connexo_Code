package com.energyict.mdc.device.data.crlrequest;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface CrlRequestTaskService {

    CrlRequestTaskProperty newCrlRequestTaskProperty();

    List<CrlRequestTaskProperty> findAllCrlRequestTaskProperties();

}

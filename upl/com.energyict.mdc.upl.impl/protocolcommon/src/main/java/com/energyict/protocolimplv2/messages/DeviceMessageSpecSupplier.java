package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

/**
 * Produces {@link DeviceMessageSpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-01 (13:04)
 */
public interface DeviceMessageSpecSupplier {
    long id();
    DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter);
}
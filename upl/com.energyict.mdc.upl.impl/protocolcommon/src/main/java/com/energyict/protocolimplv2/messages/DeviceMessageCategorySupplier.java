package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

/**
 * Produces {@link DeviceMessageCategory DeviceMessageCategories}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-01 (14:10)
 */
public interface DeviceMessageCategorySupplier {
    DeviceMessageCategory get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter);
}
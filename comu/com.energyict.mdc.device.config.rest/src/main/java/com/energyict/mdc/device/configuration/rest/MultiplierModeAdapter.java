package com.energyict.mdc.device.configuration.rest;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.configuration.rest.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.device.MultiplierMode;

public class MultiplierModeAdapter extends MapBasedXmlAdapter<MultiplierMode> {

    public MultiplierModeAdapter() {
        register("", null);
        register(MultiplierMode.NONE.getNameKey(), MultiplierMode.NONE);
        register(MultiplierMode.VERSIONED.getNameKey(), MultiplierMode.VERSIONED);
        register(MultiplierMode.CONFIGURED_ON_OBJECT.getNameKey(), MultiplierMode.CONFIGURED_ON_OBJECT);
    }
}

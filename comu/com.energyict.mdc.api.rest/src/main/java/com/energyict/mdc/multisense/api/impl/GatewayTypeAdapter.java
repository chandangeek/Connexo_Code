package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.config.GatewayType;

public class GatewayTypeAdapter extends MapBasedXmlAdapter<GatewayType> {

    public GatewayTypeAdapter() {
        register("HAN", GatewayType.HOME_AREA_NETWORK);
        register("LAN", GatewayType.LOCAL_AREA_NETWORK);
        register("None", GatewayType.NONE);
    }
}

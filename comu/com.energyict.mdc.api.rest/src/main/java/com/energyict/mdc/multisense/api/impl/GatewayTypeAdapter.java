/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.common.device.config.GatewayType;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class GatewayTypeAdapter extends MapBasedXmlAdapter<GatewayType> {

    public GatewayTypeAdapter() {
        register("HAN", GatewayType.HOME_AREA_NETWORK);
        register("LAN", GatewayType.LOCAL_AREA_NETWORK);
        register("None", GatewayType.NONE);
    }
}

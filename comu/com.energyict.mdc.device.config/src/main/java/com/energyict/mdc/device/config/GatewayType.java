/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import java.util.Optional;

public enum GatewayType {
    NONE("NONE"),
    HOME_AREA_NETWORK("HAN"),
    LOCAL_AREA_NETWORK("LAN"),
    ;

    private String key;

    private GatewayType(String key){
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static Optional<GatewayType> fromKey(String key){
        for (GatewayType gatewayType : GatewayType.values()) {
            if (gatewayType.getKey().equals(key)){
                return Optional.of(gatewayType);
            }
        }
        return Optional.empty();
    }
}

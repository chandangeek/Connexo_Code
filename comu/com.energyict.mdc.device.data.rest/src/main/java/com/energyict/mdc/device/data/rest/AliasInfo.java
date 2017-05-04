/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

/**
 * Created by gde on 27/04/2017.
 */
public class AliasInfo {
    public String alias;

    public AliasInfo from(String alias) {
        AliasInfo aliasInfo = new AliasInfo();
        aliasInfo.alias = alias;
        return aliasInfo;
    }

}

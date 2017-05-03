/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by gde on 27/04/2017.
 */
public class AliasInfo {
    public String alias;

    public static AliasInfo from(String alias) {
        AliasInfo aliasInfo = new AliasInfo();
        aliasInfo.alias = alias;
        return aliasInfo;
    }

    public static List<AliasInfo> fromAliases(List<String> aliases) {
        return aliases.stream().map(alias -> AliasInfo.from(alias)).collect(Collectors.toList());
    }

}

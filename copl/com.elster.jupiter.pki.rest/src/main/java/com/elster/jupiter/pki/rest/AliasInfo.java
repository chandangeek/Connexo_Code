/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public class AliasInfo {
    public String alias;

    public AliasInfo(String alias) {
        this.alias = alias;
    }
}

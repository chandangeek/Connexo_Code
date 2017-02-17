/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.slp;

import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class SyntheticLoadProfileModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SyntheticLoadProfileService.class).to(SyntheticLoadProfileServiceImpl.class).in(Scopes.SINGLETON);
    }
}

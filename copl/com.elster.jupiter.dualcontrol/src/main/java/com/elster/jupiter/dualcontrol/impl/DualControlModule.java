/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.DualControlService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class DualControlModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DualControlService.class).to(DualControlServiceImpl.class).in(Scopes.SINGLETON);

    }
}

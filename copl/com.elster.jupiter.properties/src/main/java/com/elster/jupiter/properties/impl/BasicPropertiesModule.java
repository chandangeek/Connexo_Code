/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.PropertySpecService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class BasicPropertiesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PropertySpecService.class).to(PropertySpecServiceImpl.class).in(Scopes.SINGLETON);
    }
}
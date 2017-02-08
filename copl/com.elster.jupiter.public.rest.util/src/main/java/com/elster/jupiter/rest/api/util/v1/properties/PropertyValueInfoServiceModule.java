/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.properties;

import com.elster.jupiter.rest.api.util.v1.properties.impl.PropertyValueInfoServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class PropertyValueInfoServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PropertyValueInfoService.class).to(PropertyValueInfoServiceImpl.class).in(Scopes.SINGLETON);
    }
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest;

import com.elster.jupiter.properties.rest.impl.PropertyValueInfoServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Created by mbarinov on 30.08.2016.
 */
public class PropertyValueInfoServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PropertyValueInfoService.class).to(PropertyValueInfoServiceImpl.class).in(Scopes.SINGLETON);
    }

}

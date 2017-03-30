/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Module to resolve dependencies during integration testing
 */
public class MdcReadingTypeUtilServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MdcReadingTypeUtilService.class).to(MdcReadingTypeUtilServiceImpl.class).in(Scopes.SINGLETON);
    }

}
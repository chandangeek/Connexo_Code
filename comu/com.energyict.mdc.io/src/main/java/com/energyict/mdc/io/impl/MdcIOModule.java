/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.SocketService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-04 (10:31)
 */
public class MdcIOModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SocketService.class).to(SocketServiceImpl.class).in(Scopes.SINGLETON);
    }

}
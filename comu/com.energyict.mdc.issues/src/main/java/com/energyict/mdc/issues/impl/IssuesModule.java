/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issues.impl;

import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.issues.IssueService;

import com.google.inject.AbstractModule;

import java.time.Clock;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-16 (15:47)
 */
public class IssuesModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(NlsService.class);

        bind(IssueService.class).to(IssueServiceImpl.class);
    }

}
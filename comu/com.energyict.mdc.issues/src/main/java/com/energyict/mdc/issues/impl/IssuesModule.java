package com.energyict.mdc.issues.impl;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.issues.IssueService;
import com.google.inject.AbstractModule;

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

        bind(IssueService.class).to(IssueServiceImpl.class);
    }

}
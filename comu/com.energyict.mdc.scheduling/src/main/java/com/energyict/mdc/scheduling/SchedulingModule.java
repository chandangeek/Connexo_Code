package com.energyict.mdc.scheduling;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.scheduling.model.impl.SchedulingServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Module intended for use by integration tests
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:49)
 */
public class SchedulingModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(EventService.class);
        requireBinding(NlsService.class);

        bind(SchedulingService.class).to(SchedulingServiceImpl.class).in(Scopes.SINGLETON);
    }

}
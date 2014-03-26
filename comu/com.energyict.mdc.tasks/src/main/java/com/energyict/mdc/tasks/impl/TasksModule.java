package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.tasks.TaskService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class TasksModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);
        bind(TaskService.class).to(TaskServiceImpl.class).in(Scopes.SINGLETON);
    }
}

package com.elster.jupiter.demo;

import com.elster.jupiter.demo.impl.DemoServiceImpl;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.TaskService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class DemoModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(EngineModelService.class);
        requireBinding(TransactionService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(ProtocolPluggableService.class);
        requireBinding(MasterDataService.class);
        requireBinding(MeteringService.class);
        requireBinding(TaskService.class);

        bind(DemoService.class).to(DemoServiceImpl.class).in(Scopes.SINGLETON);
    }
}

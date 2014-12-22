package com.elster.jupiter.demo.impl.generators;

import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;

import javax.inject.Inject;

public class ComServerGenerator extends NamedGenerator<ComServerGenerator> {

    private final EngineModelService engineModelService;
    private final Store store;

    @Inject
    public ComServerGenerator(EngineModelService engineModelService, Store store) {
        super(ComServerGenerator.class);
        this.engineModelService = engineModelService;
        this.store = store;
    }

    public void create(){
        System.out.println("==> Creating ComServer '" + getName() + "' ...");
        OnlineComServer comServer = engineModelService.newOnlineComServerInstance();
        comServer.setName(getName().toUpperCase());
        comServer.setActive(true);
        comServer.setServerLogLevel(ComServer.LogLevel.INFO);
        comServer.setCommunicationLogLevel(ComServer.LogLevel.INFO);
        comServer.setChangesInterPollDelay(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES));
        comServer.setSchedulingInterPollDelay(new TimeDuration(60, TimeDuration.TimeUnit.SECONDS));
        comServer.setStoreTaskQueueSize(50);
        comServer.setNumberOfStoreTaskThreads(1);
        comServer.setStoreTaskThreadPriority(5);
        comServer.save();
        store.add(ComServer.class, comServer);
    }
}

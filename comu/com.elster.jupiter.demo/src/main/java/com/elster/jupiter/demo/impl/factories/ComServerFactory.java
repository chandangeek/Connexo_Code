package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;

import javax.inject.Inject;

public class ComServerFactory extends NamedFactory<ComServerFactory, ComServer> {
    private final EngineModelService engineModelService;
    private final Store store;

    @Inject
    public ComServerFactory(EngineModelService engineModelService, Store store) {
        super(ComServerFactory.class);
        this.engineModelService = engineModelService;
        this.store = store;
    }

    @Override
    public ComServer get(){
        Log.write(this);
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
        return comServer;
    }
}

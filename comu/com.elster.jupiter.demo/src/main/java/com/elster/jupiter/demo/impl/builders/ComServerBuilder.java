package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OnlineComServer;

import javax.inject.Inject;
import java.util.Optional;

public class ComServerBuilder extends NamedBuilder<ComServer, ComServerBuilder> {
    private final EngineConfigurationService engineModelService;

    private boolean isActive;

    @Inject
    public ComServerBuilder(EngineConfigurationService engineModelService) {
        super(ComServerBuilder.class);
        this.engineModelService = engineModelService;
        this.isActive = true;
    }

    public ComServerBuilder withActiveStatus(boolean status){
        this.isActive = status;
        return this;
    }

    @Override
    public Optional<ComServer> find() {
        return engineModelService.findComServer(getName().toUpperCase());
    }

    @Override
    public ComServer create(){
        Log.write(this);
        OnlineComServer comServer = engineModelService.newOnlineComServerInstance();
        comServer.setName(getName().toUpperCase());
        comServer.setActive(this.isActive);
        comServer.setServerLogLevel(ComServer.LogLevel.WARN);
        comServer.setCommunicationLogLevel(ComServer.LogLevel.WARN);
        comServer.setChangesInterPollDelay(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES));
        comServer.setSchedulingInterPollDelay(new TimeDuration(60, TimeDuration.TimeUnit.SECONDS));
        comServer.setStoreTaskQueueSize(50);
        comServer.setNumberOfStoreTaskThreads(5);
        comServer.setStoreTaskThreadPriority(5);
        comServer.save();
        return comServer;
    }
}

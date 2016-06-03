package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OnlineComServer;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Optional;

public class ComServerBuilder extends NamedBuilder<ComServer, ComServerBuilder> {

    private static final String EVENT_REGISTRATIION_URI_PATTERN = "ws://{0}:8888/events/registration";
    private static final String QUERY_API_URI_PATTERN = "ws://{0}:8889/remote/queries";
    private static final String STATUS_URI_PATTERN = "http://{0}:8080/api/dsr/comserverstatus";

    private final EngineConfigurationService engineModelService;

    private boolean isActive;

    @Inject
    public ComServerBuilder(EngineConfigurationService engineModelService) {
        super(ComServerBuilder.class);
        this.engineModelService = engineModelService;
        this.isActive = true;
    }

    public ComServerBuilder withActiveStatus(boolean status) {
        this.isActive = status;
        return this;
    }

    @Override
    public Optional<ComServer> find() {
        return engineModelService.findComServer(getName());
    }

    @Override
    public ComServer create() {
        Log.write(this);
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> comServer = engineModelService.newOnlineComServerBuilder();
        comServer.name(getName());
        comServer.active(this.isActive);
        comServer.serverLogLevel(ComServer.LogLevel.WARN);
        comServer.communicationLogLevel(ComServer.LogLevel.WARN);
        comServer.changesInterPollDelay(new TimeDuration(5, TimeDuration.TimeUnit.MINUTES));
        comServer.schedulingInterPollDelay(new TimeDuration(60, TimeDuration.TimeUnit.SECONDS));
        comServer.storeTaskQueueSize(50);
        comServer.numberOfStoreTaskThreads(5);
        comServer.storeTaskThreadPriority(5);
        comServer.statusUri(MessageFormat.format(STATUS_URI_PATTERN, getName()));
        comServer.queryApiPostUri(MessageFormat.format(QUERY_API_URI_PATTERN, getName()));
        comServer.eventRegistrationUri(MessageFormat.format(EVENT_REGISTRATIION_URI_PATTERN, getName()));
        return comServer.create();
    }

}
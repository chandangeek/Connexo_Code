package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.users.UserService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import java.time.Clock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MeteringModule extends AbstractModule {

    private boolean createReadingTypes;
    private final String readingTypes;

    public MeteringModule() {
        this.createReadingTypes = false;
        this.readingTypes = "";
    }

    @Deprecated
    public static MeteringModule withAllReadingTypes_AVOID_AVOID() {
        MeteringModule meteringModule = new MeteringModule();
        meteringModule.createReadingTypes=true;
        return meteringModule;
    }

    public MeteringModule(String readingType, String... requiredReadingTypes) {
        this.readingTypes = Stream.concat(Stream.of(readingType), Stream.of(requiredReadingTypes)).collect(Collectors.joining(";"));
        this.createReadingTypes = false;
    }

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(OrmService.class);
        requireBinding(IdsService.class);
        requireBinding(EventService.class);
        requireBinding(PartyService.class);
        requireBinding(QueryService.class);
        requireBinding(UserService.class);
        requireBinding(FiniteStateMachineService.class);

        bindConstant().annotatedWith(Names.named("requiredReadingTypes")).to(readingTypes);
        bindConstant().annotatedWith(Names.named("createReadingTypes")).to(createReadingTypes);
        bind(MeteringService.class).to(MeteringServiceImpl.class).in(Scopes.SINGLETON);
        bind(ServerMeteringService.class).to(MeteringServiceImpl.class).in(Scopes.SINGLETON);
    }
}

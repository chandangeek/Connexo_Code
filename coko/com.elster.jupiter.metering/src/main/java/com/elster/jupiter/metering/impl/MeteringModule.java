package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.impl.aggregation.ReadingTypeDeliverableForMeterActivationFactory;
import com.elster.jupiter.metering.impl.aggregation.ReadingTypeDeliverableForMeterActivationFactoryImpl;
import com.elster.jupiter.metering.impl.aggregation.SqlBuilderFactory;
import com.elster.jupiter.metering.impl.aggregation.SqlBuilderFactoryImpl;
import com.elster.jupiter.metering.impl.aggregation.VirtualFactory;
import com.elster.jupiter.metering.impl.aggregation.VirtualFactoryImpl;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.users.UserService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import javax.inject.Inject;
import javax.inject.Provider;
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
        meteringModule.createReadingTypes = true;
        return meteringModule;
    }

    public MeteringModule(String... requiredReadingTypes) {
        this.readingTypes = Stream.of(requiredReadingTypes).collect(Collectors.joining(";"));
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
        requireBinding(CustomPropertySetService.class);
        requireBinding(PropertySpecService.class);
        requireBinding(SearchService.class);

        bindConstant().annotatedWith(Names.named("requiredReadingTypes")).to(readingTypes);
        bindConstant().annotatedWith(Names.named("createReadingTypes")).to(createReadingTypes);
        bind(MeteringDataModelService.class).to(MeteringDataModelServiceImpl.class).in(Scopes.SINGLETON);
        bind(MeteringService.class).toProvider(MeteringServiceProvider.class);
        bind(ServerMeteringService.class).toProvider(MeteringServiceProvider.class);
        bind(ServerMetrologyConfigurationService.class).toProvider(MetrologyConfigurationServiceProvider.class);
        bind(MetrologyConfigurationService.class).toProvider(MetrologyConfigurationServiceProvider.class);
        bind(VirtualFactory.class).to(VirtualFactoryImpl.class).in(Scopes.SINGLETON);
        bind(SqlBuilderFactory.class).to(SqlBuilderFactoryImpl.class).in(Scopes.SINGLETON);
        bind(ReadingTypeDeliverableForMeterActivationFactory.class).to(ReadingTypeDeliverableForMeterActivationFactoryImpl.class).in(Scopes.SINGLETON);
        bind(DataAggregationService.class).toProvider(DataAggregationServiceProvider.class);
    }

    public static class MeteringServiceProvider implements Provider<ServerMeteringService> {
        private final MeteringDataModelService meteringDataModelService;

        @Inject
        public MeteringServiceProvider(MeteringDataModelService meteringDataModelService) {
            this.meteringDataModelService = meteringDataModelService;
        }

        @Override
        public ServerMeteringService get() {
            return this.meteringDataModelService.getMeteringService();
        }
    }

    public static class MetrologyConfigurationServiceProvider implements Provider<ServerMetrologyConfigurationService> {
        private final MeteringDataModelService meteringDataModelService;

        @Inject
        public MetrologyConfigurationServiceProvider(MeteringDataModelService meteringDataModelService) {
            this.meteringDataModelService = meteringDataModelService;
        }

        @Override
        public ServerMetrologyConfigurationService get() {
            return this.meteringDataModelService.getMetrologyConfigurationService();
        }
    }

    public static class DataAggregationServiceProvider implements Provider<DataAggregationService> {
        private final MeteringDataModelService meteringDataModelService;

        @Inject
        public DataAggregationServiceProvider(MeteringDataModelService meteringDataModelService) {
            this.meteringDataModelService = meteringDataModelService;
        }

        @Override
        public DataAggregationService get() {
            return this.meteringDataModelService.getDataAggregationService();
        }
    }
}
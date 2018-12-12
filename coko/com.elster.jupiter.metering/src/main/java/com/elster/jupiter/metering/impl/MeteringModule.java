/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.CalculatedMetrologyContractData;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.aggregation.MetrologyContractCalculationIntrospector;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.aggregation.MeterActivationSet;
import com.elster.jupiter.metering.impl.aggregation.ServerDataAggregationService;
import com.elster.jupiter.metering.impl.aggregation.SqlBuilderFactory;
import com.elster.jupiter.metering.impl.aggregation.SqlBuilderFactoryImpl;
import com.elster.jupiter.metering.impl.aggregation.VirtualFactory;
import com.elster.jupiter.metering.impl.aggregation.VirtualFactoryImpl;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.metering.impl.search.UsagePointSearchDomain;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.users.UserService;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MeteringModule extends AbstractModule {

    private boolean createReadingTypes;
    private final String readingTypes;
    private DataAggregationService dataAggregationMock;

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

    public MeteringModule withDataAggregationService(DataAggregationService dataAggregationService) {
        if (dataAggregationService != null && !(dataAggregationService instanceof ServerDataAggregationService)) {
            this.dataAggregationMock = new MockedDataAggregationService(dataAggregationService);
        } else {
            this.dataAggregationMock = null;
        }
        return this;
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
        bind(MeteringTranslationService.class).toProvider(MeteringTranslationServiceProvider.class);

        bind(ServerMeteringService.class).toProvider(MeteringServiceProvider.class);
        bind(ServerMetrologyConfigurationService.class).toProvider(MetrologyConfigurationServiceProvider.class);
        bind(MetrologyConfigurationService.class).toProvider(MetrologyConfigurationServiceProvider.class);
        bind(SyntheticLoadProfileService.class).toProvider(SyntheticLoadProfileServiceProvider.class);
        bind(VirtualFactory.class).to(VirtualFactoryImpl.class).in(Scopes.SINGLETON);
        bind(SqlBuilderFactory.class).to(SqlBuilderFactoryImpl.class).in(Scopes.SINGLETON);
        bind(DataAggregationService.class).annotatedWith(Names.named("dataAggregationMock")).toProvider(() -> dataAggregationMock);
        bind(DataAggregationService.class).toProvider(DataAggregationServiceProvider.class);
        bind(UsagePointSearchDomain.class).in(Scopes.SINGLETON);
    }

    private static class MeteringServiceProvider implements Provider<ServerMeteringService> {
        private final MeteringDataModelService meteringDataModelService;

        @Inject
        private MeteringServiceProvider(MeteringDataModelService meteringDataModelService) {
            this.meteringDataModelService = meteringDataModelService;
        }

        @Override
        public ServerMeteringService get() {
            return this.meteringDataModelService.getMeteringService();
        }
    }

    private static class MeteringTranslationServiceProvider implements Provider<MeteringTranslationService> {
        private final MeteringDataModelService meteringDataModelService;

        @Inject
        private MeteringTranslationServiceProvider(MeteringDataModelService meteringDataModelService) {
            this.meteringDataModelService = meteringDataModelService;
        }

        @Override
        public MeteringTranslationService get() {
            return this.meteringDataModelService.getMeteringTranslationService();
        }
    }

    private static class MetrologyConfigurationServiceProvider implements Provider<ServerMetrologyConfigurationService> {
        private final MeteringDataModelService meteringDataModelService;

        @Inject
        private MetrologyConfigurationServiceProvider(MeteringDataModelService meteringDataModelService) {
            this.meteringDataModelService = meteringDataModelService;
        }

        @Override
        public ServerMetrologyConfigurationService get() {
            return this.meteringDataModelService.getMetrologyConfigurationService();
        }
    }

    private static class SyntheticLoadProfileServiceProvider implements Provider<SyntheticLoadProfileService> {
        private final MeteringDataModelService meteringDataModelService;

        @Inject
        private SyntheticLoadProfileServiceProvider(MeteringDataModelService meteringDataModelService) {
            this.meteringDataModelService = meteringDataModelService;
        }

        @Override
        public SyntheticLoadProfileService get() {
            return this.meteringDataModelService.getSyntheticLoadProfileService();
        }
    }

    private static class DataAggregationServiceProvider implements Provider<DataAggregationService> {
        private final MeteringDataModelService meteringDataModelService;

        @Inject
        private DataAggregationServiceProvider(MeteringDataModelService meteringDataModelService) {
            this.meteringDataModelService = meteringDataModelService;
        }

        @Override
        public DataAggregationService get() {
            return this.meteringDataModelService.getDataAggregationService();
        }
    }

    private static class MockedDataAggregationService implements ServerDataAggregationService {
        private final DataAggregationService dataAggregationService;

        MockedDataAggregationService(DataAggregationService dataAggregationService) {
            this.dataAggregationService = dataAggregationService;
        }

        @Override
        public Clock getClock() {
            return null;
        }

        @Override
        public Thesaurus getThesaurus() {
            return null;
        }

        @Override
        public boolean hasContract(EffectiveMetrologyConfigurationOnUsagePoint mistery, MetrologyContract contract) {
            return false;
        }

        @Override
        public List<MeterActivationSet> getMeterActivationSets(ServerUsagePoint usagePoint, Range<Instant> period) {
            return Collections.emptyList();
        }

        @Override
        public List<MeterActivationSet> getMeterActivationSets(ServerUsagePoint usagePoint, Instant when) {
            return Collections.emptyList();
        }

        @Override
        public CalculatedMetrologyContractData calculate(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period) {
            return dataAggregationService.calculate(usagePoint, contract, period);
        }

        @Override
        public MetrologyContractCalculationIntrospector introspect(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period) {
            return dataAggregationService.introspect(usagePoint, contract, period);
        }

        @Override
        public List<DetailedCalendarUsage> introspect(ServerUsagePoint usagePoint, Instant instant) {
            return Collections.emptyList();
        }

        @Override
        public MetrologyContractDataEditor edit(UsagePoint usagePoint, MetrologyContract contract, ReadingTypeDeliverable deliverable, QualityCodeSystem system) {
            return dataAggregationService.edit(usagePoint, contract, deliverable, system);
        }

        @Override
        public Category getTimeOfUseCategory() {
            return new TimeOfUse();
        }
    }

    private static class TimeOfUse implements Category {
        @Override
        public String getDisplayName() {
            return OutOfTheBoxCategory.TOU.name();
        }

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public String getName() {
            return getDisplayName();
        }
    }

}
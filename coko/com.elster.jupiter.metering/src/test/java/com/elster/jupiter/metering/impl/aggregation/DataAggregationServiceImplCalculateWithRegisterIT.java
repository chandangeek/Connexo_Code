/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.license.impl.LicenseServiceImpl;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.time.Instant;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the {@link DataAggregationServiceImpl#calculate(UsagePoint, MetrologyContract, Range)} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-08 (16:50)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAggregationServiceImplCalculateWithRegisterIT {

    private static final String NET_CONSUMPTION_INDEX_MRID = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static ReadingType netConsumptionIndex;
    private static ServiceCategory ELECTRICITY;
    private static MetrologyPurpose METROLOGY_PURPOSE;
    private static Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
    private static Instant feb1st2016 = Instant.ofEpochMilli(1454281200000L);
    private static SqlBuilderFactory sqlBuilderFactory = mock(SqlBuilderFactory.class);
    private static ClauseAwareSqlBuilder clauseAwareSqlBuilder = mock(ClauseAwareSqlBuilder.class);

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(injector.getInstance(TransactionService.class));

    private MetrologyContract contract;
    private UsagePoint usagePoint;
    private UsagePointMetrologyConfiguration configuration;
    private long consumptionRequirementId;
    private long netConsumptionDeliverableId;
    private SqlBuilder consumptionWithClauseBuilder;
    private SqlBuilder netConsumptionWithClauseBuilder;
    private SqlBuilder selectClauseBuilder;
    private SqlBuilder completeSqlBuilder;
    private Meter meter;
    private MeterActivation meterActivation;

    @Mock
    private Thesaurus thesaurus;

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(LicenseService.class).to(LicenseServiceImpl.class).in(Scopes.SINGLETON);
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(DataVaultService.class).toInstance(mock(DataVaultService.class));
            bind(SearchService.class).toInstance(mockSearchService());
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @BeforeClass
    public static void setUp() {
        setupServices();
        setupReadingTypes();
        setupMetrologyPurpose();
        setupDefaultUsagePointLifeCycle();
        ELECTRICITY = getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
    }

    private static void setupServices() {
        when(sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(clauseAwareSqlBuilder);
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new MeteringModule(NET_CONSUMPTION_INDEX_MRID),
                    new BasicPropertiesModule(),
                    new TimeModule(),
                    new UserModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new BpmModule(),
                    new FiniteStateMachineModule(),
                    new NlsModule(),
                    new CustomPropertySetsModule(),
                    new UsagePointLifeCycleConfigurationModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            getMeteringService();
            getDataAggregationService();
            ctx.commit();
        }
    }

    @After
    public void clearCache() {
        injector.getInstance(OrmService.class).invalidateCache("NLS", "NLS_KEY");
    }

    private static MeteringService getMeteringService() {
        return injector.getInstance(MeteringService.class);
    }

    private static ServerMetrologyConfigurationService getMetrologyConfigurationService() {
        return injector.getInstance(ServerMetrologyConfigurationService.class);
    }

    private static SqlBuilderFactory getSqlBuilderFactory() {
        return sqlBuilderFactory;
    }

    private static DataAggregationService getDataAggregationService() {
        ServerMeteringService meteringService = injector.getInstance(ServerMeteringService.class);
        return new DataAggregationServiceImpl(
                injector.getInstance(CustomPropertySetService.class),
                meteringService,
                new InstantTruncaterFactory(meteringService),
                DataAggregationServiceImplCalculateWithRegisterIT::getSqlBuilderFactory,
                VirtualFactoryImpl::new,
                () -> new ReadingTypeDeliverableForMeterActivationFactoryImpl(meteringService));
    }

    private static void setupReadingTypes() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            netConsumptionIndex = getMeteringService().getReadingType(NET_CONSUMPTION_INDEX_MRID).get();
            ctx.commit();
        }
    }

    private static void setupMetrologyPurpose() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            NlsKey name = mock(NlsKey.class);
            when(name.getKey()).thenReturn(DataAggregationServiceImplCalculateWithRegisterIT.class.getSimpleName());
            when(name.getDefaultMessage()).thenReturn(DataAggregationServiceImplCalculateWithRegisterIT.class.getSimpleName());
            when(name.getComponent()).thenReturn(MeteringService.COMPONENTNAME);
            when(name.getLayer()).thenReturn(Layer.DOMAIN);
            NlsKey description = mock(NlsKey.class);
            when(description.getKey()).thenReturn(DataAggregationServiceImplCalculateWithRegisterIT.class.getSimpleName() + ".description");
            when(description.getDefaultMessage()).thenReturn(DataAggregationServiceImplCalculateWithRegisterIT.class.getSimpleName());
            when(description.getComponent()).thenReturn(MeteringService.COMPONENTNAME);
            when(description.getLayer()).thenReturn(Layer.DOMAIN);
            METROLOGY_PURPOSE = getMetrologyConfigurationService().createMetrologyPurpose(description, description);
            ctx.commit();
        }
    }

    private static void setupDefaultUsagePointLifeCycle() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService = injector.getInstance(UsagePointLifeCycleConfigurationService.class);
            usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("Default life cycle").markAsDefault();
            ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void initializeMocks() {
        this.consumptionWithClauseBuilder = new SqlBuilder();
        this.netConsumptionWithClauseBuilder = new SqlBuilder();
        this.selectClauseBuilder = new SqlBuilder();
        this.completeSqlBuilder = new SqlBuilder();
    }

    private void initializeSqlBuilders() {
        when(sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(clauseAwareSqlBuilder);
        when(clauseAwareSqlBuilder.with(matches("rid" + consumptionRequirementId + ".*1"), any(Optional.class), anyVararg())).thenReturn(this.consumptionWithClauseBuilder);
        when(clauseAwareSqlBuilder.with(matches("rod" + netConsumptionDeliverableId + ".*1"), any(Optional.class), anyVararg())).thenReturn(this.netConsumptionWithClauseBuilder);
        when(clauseAwareSqlBuilder.select()).thenReturn(this.selectClauseBuilder);
        when(clauseAwareSqlBuilder.finish()).thenReturn(this.completeSqlBuilder);
    }

    private static SearchService mockSearchService() {
        SearchService searchService = mock(SearchService.class);
        SearchDomain searchDomain = mock(SearchDomain.class);
        when(searchService.findDomain(any())).thenReturn(Optional.of(searchDomain));
        return searchService;
    }

    @After
    public void resetSqlBuilders() {
        reset(sqlBuilderFactory);
        reset(clauseAwareSqlBuilder);
    }

    /**
     * Tests the simplest case:
     * Metrology configuration
     * requirements:
     * A- ::= Wh bulk quantity register (0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0)
     * deliverables:
     * netConsumptionIndex (Wh) ::= A-
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever
     * A- -> Wh bulk quantity register
     * In other words, expose the register as a value on the contract.
     */
    @Test
    @Transactional
    public void simplestNetConsumption() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("simplestNetConsumption");
        this.setupUsagePoint("simplestNetConsumption");
        this.activateMeterWithBulkWattRegister();

        // Setup MetrologyConfiguration
        MeterRole defaultMeterRole = getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT);
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("simplestNetConsumption", ELECTRICITY).create();
        this.configuration.addMeterRole(defaultMeterRole);

        // Setup configuration requirements
        FullySpecifiedReadingTypeRequirement consumption = this.configuration.newReadingTypeRequirement("A-", defaultMeterRole).withReadingType(netConsumptionIndex);
        this.consumptionRequirementId = consumption.getId();
        System.out.println("simplestNetConsumption::CONSUMPTION_REQUIREMENT_ID = " + this.consumptionRequirementId);

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder = this.configuration.newReadingTypeDeliverable("netConsumptionIndex", netConsumptionIndex, Formula.Mode.AUTO);
        ReadingTypeDeliverable netConsumption = builder.build(builder.requirement(consumption));

        this.netConsumptionDeliverableId = netConsumption.getId();
        System.out.println("simplestNetConsumption::NET_CONSUMPTION_DELIVERABLE_ID = " + this.netConsumptionDeliverableId);

        // Now that all requirements and deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016.plusSeconds(60));

        this.contract = this.configuration.addMetrologyContract(METROLOGY_PURPOSE);
        this.contract.addDeliverable(netConsumption);

        // Business method
        try {
            service.calculate(this.usagePoint, this.contract, year2016());
        } catch (UnderlyingSQLFailedException e) {
            // Expected because the statement contains WITH clauses
            // Asserts:
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rid" + this.consumptionRequirementId + ".*" + this.netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            assertThat(this.consumptionWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                            matches("rod" + this.netConsumptionDeliverableId + ".*1"),
                            any(Optional.class),
                            anyVararg());
            // Assert that the requirement is used as source for the timeline
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT -1, rid" + this.consumptionRequirementId + "_" + this.netConsumptionDeliverableId + "_1\\.timestamp,.*");
            // Assert that the requirements' value is used in the select clause
            assertThat(this.netConsumptionWithClauseBuilder.getText())
                    .matches("SELECT.*rid" + this.consumptionRequirementId + "_" + this.netConsumptionDeliverableId + "_1\\.value.*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(NET_CONSUMPTION_INDEX_MRID) + "'.*");
            // Assert that the overall select statement selects the value and the timestamp from the with clause for the deliverable
            assertThat(overallSelectWithoutNewlines).matches(".*rod" + netConsumptionDeliverableId + "_1\\.value, rod" + netConsumptionDeliverableId + "_1\\.localdate, rod" + netConsumptionDeliverableId + "_1\\.timestamp.*");
        }
    }

    private Range<Instant> year2016() {
        return Range.atLeast(jan1st2016);
    }

    private DataAggregationService testInstance() {
        return getDataAggregationService();
    }

    private void setupMeter(String amrIdBase) {
        AmrSystem mdc = getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        this.meter = mdc.newMeter(amrIdBase, amrIdBase).create();
    }

    private void setupUsagePoint(String name) {
        ServiceCategory electricity = getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        this.usagePoint = electricity.newUsagePoint(name, jan1st2016).create();
        UsagePointMetrologyConfiguration usagePointMetrologyConfiguration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("UP1", electricity).create();
        usagePointMetrologyConfiguration.addMeterRole(getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT));
        usagePoint.apply(usagePointMetrologyConfiguration, jan1st2016);
    }

    private void activateMeterWithBulkWattRegister() {
        this.meterActivation = this.usagePoint.activate(this.meter, jan1st2016);
        this.meterActivation.getChannelsContainer().createChannel(netConsumptionIndex);
    }

    private String mRID2GrepPattern(String mRID) {
        return mRID.replace(".", "\\.");
    }

}

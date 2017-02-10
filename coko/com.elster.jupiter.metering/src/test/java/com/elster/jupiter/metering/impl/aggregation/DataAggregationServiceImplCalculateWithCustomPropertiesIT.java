/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
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
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
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
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
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
 * Integration tests for the {@link DataAggregationServiceImpl} component
 * with formula's that use custom properties defined on the usagepoint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-30 (14:28)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAggregationServiceImplCalculateWithCustomPropertiesIT {

    private static final String FIFTEEN_MINS_PRIMARY_METERED_POWER_MRID = "0.0.2.4.1.2.12.0.0.0.0.0.0.0.0.3.38.0";
    private static final String FIFTEEN_MINS_NET_CONSUMPTION_MRID = "0.0.2.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String MONTHLY_NET_CONSUMPTION_MRID = "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0";
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static ReadingType fifteenMinuteskWForward;
    private static ReadingType fifteenMinutesNetConsumption;
    private static ReadingType monthlyNetConsumption;
    private static ServiceCategory ELECTRICITY;
    private static MetrologyPurpose METROLOGY_PURPOSE;
    private static Instant jan1st2016 = Instant.ofEpochMilli(1451602800000L);
    private static SqlBuilderFactory sqlBuilderFactory = mock(SqlBuilderFactory.class);
    private static ClauseAwareSqlBuilder clauseAwareSqlBuilder = mock(ClauseAwareSqlBuilder.class);
    private static Thesaurus thesaurus;

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(injector.getInstance(TransactionService.class));

    private MetrologyContract contract;
    private UsagePoint usagePoint;
    private MeterActivation meterActivation;
    private UsagePointMetrologyConfiguration configuration;
    private long customPropertySetId;
    private long localPowerRequirementId;
    private long netConsumptionDeliverableId;
    private SqlBuilder antennaCountPropertyWithClauseBuilder;
    private SqlBuilder antennaPowerPropertyWithClauseBuilder;
    private SqlBuilder localPowerWithClauseBuilder;
    private SqlBuilder netConsumptionWithClauseBuilder;
    private SqlBuilder selectClauseBuilder;
    private SqlBuilder completeSqlBuilder;
    private Meter meter;

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(LicenseService.class).to(LicenseServiceImpl.class).in(Scopes.SINGLETON);
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(DataVaultService.class).toInstance(mock(DataVaultService.class));
            bind(SearchService.class).toInstance(mockSearchService());
            bind(UpgradeService.class).toInstance(new UpgradeModule.FakeUpgradeService());
        }
    }

    @BeforeClass
    public static void setUp() {
        setupThesaurus();
        setupServices();
        setupReadingTypes();
        setupCustomPropertySets();
        setupMetrologyPurpose();
        setupDefaultUsagePointLifeCycle();
        ELECTRICITY = getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
    }

    private static void setupThesaurus() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        thesaurus = mock(Thesaurus.class);
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
    }

    private static void setupServices() {
        when(sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(clauseAwareSqlBuilder);
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new TimeModule(),
                    new IdsModule(),
                    new MeteringModule(
                            FIFTEEN_MINS_PRIMARY_METERED_POWER_MRID,
                            FIFTEEN_MINS_NET_CONSUMPTION_MRID,
                            MONTHLY_NET_CONSUMPTION_MRID
                    ),
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
                    new BasicPropertiesModule(),
                    new CalendarModule(),
                    new CustomPropertySetsModule(),
                    new UsagePointLifeCycleConfigurationModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            getCustomPropertySetService();
            getMeteringService();
            getDataAggregationService();
            ctx.commit();
        }
    }

    private static PropertySpecService getPropertySpecService() {
        return injector.getInstance(PropertySpecService.class);
    }

    private static CalendarService getCalendarService() {
        return injector.getInstance(CalendarService.class);
    }

    private static CustomPropertySetService getCustomPropertySetService() {
        return injector.getInstance(CustomPropertySetService.class);
    }

    private static ServerMeteringService getMeteringService() {
        return injector.getInstance(ServerMeteringService.class);
    }

    private static ServerMetrologyConfigurationService getMetrologyConfigurationService() {
        return injector.getInstance(ServerMetrologyConfigurationService.class);
    }

    private static SqlBuilderFactory getSqlBuilderFactory() {
        return sqlBuilderFactory;
    }

    private static DataAggregationService getDataAggregationService() {
        ServerMeteringService meteringService = getMeteringService();
        return new DataAggregationServiceImpl(
                        getCalendarService(),
                        getCustomPropertySetService(),
                        meteringService,
                        new InstantTruncaterFactory(meteringService),
                        DataAggregationServiceImplCalculateWithCustomPropertiesIT::getSqlBuilderFactory,
                        () -> new VirtualFactoryImpl(thesaurus),
                        () -> new ReadingTypeDeliverableForMeterActivationFactoryImpl(meteringService));
    }

    private static void setupReadingTypes() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            fifteenMinuteskWForward = getMeteringService().getReadingType(FIFTEEN_MINS_PRIMARY_METERED_POWER_MRID).get();
            fifteenMinutesNetConsumption = getMeteringService().getReadingType(FIFTEEN_MINS_NET_CONSUMPTION_MRID).get();
            monthlyNetConsumption = getMeteringService().getReadingType(MONTHLY_NET_CONSUMPTION_MRID).get();
            ctx.commit();
        }
    }

    private static void setupCustomPropertySets() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            getCustomPropertySetService().addCustomPropertySet(new AntennaDetailsCustomPropertySet(getPropertySpecService()));
            ctx.commit();
        }
    }

    private static void setupMetrologyPurpose() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            NlsKey name = mock(NlsKey.class);
            when(name.getKey()).thenReturn(DataAggregationServiceImplCalculateWithCustomPropertiesIT.class.getSimpleName());
            when(name.getDefaultMessage()).thenReturn(DataAggregationServiceImplCalculateWithCustomPropertiesIT.class.getSimpleName());
            when(name.getComponent()).thenReturn(MeteringService.COMPONENTNAME);
            when(name.getLayer()).thenReturn(Layer.DOMAIN);
            NlsKey description = mock(NlsKey.class);
            when(description.getKey()).thenReturn(DataAggregationServiceImplCalculateWithCustomPropertiesIT.class.getSimpleName() + ".description");
            when(description.getDefaultMessage()).thenReturn(DataAggregationServiceImplCalculateWithCustomPropertiesIT.class.getSimpleName());
            when(description.getComponent()).thenReturn(MeteringService.COMPONENTNAME);
            when(description.getLayer()).thenReturn(Layer.DOMAIN);
            METROLOGY_PURPOSE = getMetrologyConfigurationService().createMetrologyPurpose(name, description);
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
        this.antennaCountPropertyWithClauseBuilder = new SqlBuilder();
        this.antennaPowerPropertyWithClauseBuilder = new SqlBuilder();
        this.localPowerWithClauseBuilder = new SqlBuilder();
        this.netConsumptionWithClauseBuilder = new SqlBuilder();
        this.selectClauseBuilder = new SqlBuilder();
        this.completeSqlBuilder = new SqlBuilder();
    }

    private void initializeSqlBuilders() {
        when(sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(clauseAwareSqlBuilder);
        when(clauseAwareSqlBuilder
                .with(matches("cps" + this.customPropertySetId + "_" + Math.abs(AntennaFields.POWER.javaName().hashCode()) + ".*"), any(Optional.class), anyVararg()))
                .thenReturn(this.antennaPowerPropertyWithClauseBuilder);
        when(clauseAwareSqlBuilder
                .with(matches("cps" + this.customPropertySetId + "_" + Math.abs(AntennaFields.COUNT.javaName().hashCode()) + ".*"), any(Optional.class), anyVararg()))
                .thenReturn(this.antennaCountPropertyWithClauseBuilder);
        when(clauseAwareSqlBuilder
                .with(matches("rid" + this.localPowerRequirementId + ".*1"), any(Optional.class), anyVararg()))
                .thenReturn(this.localPowerWithClauseBuilder);
        when(clauseAwareSqlBuilder
                .with(matches("rod" + this.netConsumptionDeliverableId + ".*1"), any(Optional.class), anyVararg()))
                .thenReturn(this.netConsumptionWithClauseBuilder);
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
     * Tests the case that uses only custom property values:
     * Metrology configuration
     * custom properties:
     * number of antenna's (antennaCount)
     * antenna power (antennaPower)
     * requirements: none
     * deliverables:
     * netConsumption (monthly kWh) ::= antennaCount * antennaPower * 30
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever: no specific data needed
     * custom properties:
     * Jan 1st 2016 -> forever
     * antennaCount -> 2
     * antennaPower -> 30
     */
    @Test
    @Transactional
    public void monthlyNetConsumptionFromCustomProperties() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("monthlyNetConsumption");
        this.setupUsagePoint("monthlyNetConsumption");
        this.activateMeter();

        // Setup MetrologyConfiguration
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("monthlyNetConsumption", ELECTRICITY).create();

        // Add the CustomPropertySet
        RegisteredCustomPropertySet registeredCustomPropertySet = getCustomPropertySetService().findActiveCustomPropertySet(AntennaDetailsCustomPropertySet.ID).get();
        this.customPropertySetId = registeredCustomPropertySet.getId();
        System.out.println("monthlyNetConsumption::CUSTOM_PROPERTY_SET_ID = " + this.customPropertySetId);
        this.configuration.addCustomPropertySet(registeredCustomPropertySet);
        PropertySpec antennaCountPropertySpec = this.getAntennaCountPropertySpec(registeredCustomPropertySet);
        PropertySpec antennaPowerPropertySpec = this.getAntennaPowerPropertySpec(registeredCustomPropertySet);

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder = this.configuration.newReadingTypeDeliverable("consumption", monthlyNetConsumption, Formula.Mode.AUTO);
        ReadingTypeDeliverable netConsumption =
                builder.build(
                        builder.multiply(
                                builder.constant(BigDecimal.valueOf(30)),
                                builder.multiply(
                                        builder.property(registeredCustomPropertySet.getCustomPropertySet(), antennaCountPropertySpec),
                                        builder.property(registeredCustomPropertySet.getCustomPropertySet(), antennaPowerPropertySpec))));

        this.netConsumptionDeliverableId = netConsumption.getId();
        System.out.println("monthlyNetConsumption::NET_CONSUMPTION_DELIVERABLE_ID = " + this.netConsumptionDeliverableId);

        // Now that all custom properties have been configured and all deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016);

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
                        matches("cps" + this.customPropertySetId + "_" + Math.abs(AntennaFields.COUNT.javaName().hashCode()) + "_\\d*"),
                        any(Optional.class),
                        anyVararg());
            assertThat(this.antennaCountPropertyWithClauseBuilder.getText()).isNotEmpty();
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("cps" + this.customPropertySetId + "_" + Math.abs(AntennaFields.POWER.javaName().hashCode()) + "_\\d*"),
                        any(Optional.class),
                        anyVararg());
            String propertySql = this.antennaPowerPropertyWithClauseBuilder.getText().replace("\n", " ").toLowerCase();
            assertThat(propertySql).matches(".*\\(select cps." + AntennaFields.POWER.databaseName()
                    .toLowerCase() + " as value, cps.starttime, cps.endtime from \\(select " + AntennaFields.POWER.databaseName().toLowerCase() + ", starttime, endtime from tst_antenna.*cps\\)");
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rod" + this.netConsumptionDeliverableId + ".*1"),
                        any(Optional.class),
                        anyVararg());
            String netConsumptionSql = this.netConsumptionWithClauseBuilder.getText().replace("\n", " ");
            String countPropertyTableName = "cps" + this.customPropertySetId + "_" + Math.abs(AntennaFields.COUNT.javaName().hashCode()) + "_1";
            String powerPropertyTableName = "cps" + this.customPropertySetId + "_" + Math.abs(AntennaFields.POWER.javaName().hashCode()) + "_1";
            assertThat(netConsumptionSql).matches(".*" + countPropertyTableName + "\\.value \\* " + powerPropertyTableName + "\\.value.*");
            // Assert that the with clauses for both properties are joined on the utc timestamp
            assertThat(netConsumptionSql)
                    .matches("SELECT.*FROM\\s*" + powerPropertyTableName + "\\s*JOIN.*");
            assertThat(netConsumptionSql)
                    .matches("SELECT.*JOIN\\s*" + countPropertyTableName + "\\s*ON\\s*\\(\\s*\\(\\s*" + powerPropertyTableName + "\\.starttime <= " + countPropertyTableName + "\\.starttime\\s*AND\\s*" + powerPropertyTableName + "\\.endtime >= " + countPropertyTableName+ "\\.starttime\\).*");
            assertThat(netConsumptionSql)
                    .matches(".*OR\\s*\\(\\s*" + countPropertyTableName + "\\.starttime <= " + powerPropertyTableName + ".starttime\\s*AND\\s*" + countPropertyTableName + "\\.endtime >= " + powerPropertyTableName+ "\\.starttime\\)\\)");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(MONTHLY_NET_CONSUMPTION_MRID) + "'.*");
        }
    }

    /**
     * Tests the case that combines custom property values with 15min values:
     * Metrology configuration
     * custom properties:
     * number of antenna's (antennaCount) - not used in this case
     * antenna power (antennaPower)
     * requirements:
     * localPower ::= any W  with flow = forward
     * deliverables:
     * netConsumption (15min kWh) ::= antennaPower + localPower
     * Device:
     * meter activations:
     * Jan 1st 2016 -> forever: no specific data needed
     * custom properties:
     * Jan 1st 2016 -> forever
     * antennaCount -> 2
     * antennaPower -> 30
     */
    @Test
    @Transactional
    public void fifteenMinsNetConsumptionFromCustomPropertiesAndOther15MinValue() {
        DataAggregationService service = this.testInstance();
        this.setupMeter("15minNetConsumption");
        this.setupUsagePoint("15minNetConsumption");
        this.activateMeterWith15MinPower();

        // Setup MetrologyConfiguration
        MeterRole meterRole = getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT);
        this.configuration = getMetrologyConfigurationService().newUsagePointMetrologyConfiguration("15minNetConsumption", ELECTRICITY).create();
        this.configuration.addMeterRole(meterRole);

        // Add the CustomPropertySet
        RegisteredCustomPropertySet registeredCustomPropertySet = getCustomPropertySetService().findActiveCustomPropertySet(AntennaDetailsCustomPropertySet.ID).get();
        this.customPropertySetId = registeredCustomPropertySet.getId();
        System.out.println("15minNetConsumption::CUSTOM_PROPERTY_SET_ID = " + this.customPropertySetId);
        this.configuration.addCustomPropertySet(registeredCustomPropertySet);
        PropertySpec antennaPowerPropertySpec = this.getAntennaPowerPropertySpec(registeredCustomPropertySet);

        // Setup configuration requirements
        ReadingTypeRequirement localPower = this.configuration.newReadingTypeRequirement("localPower", meterRole).withReadingType(fifteenMinuteskWForward);
        this.localPowerRequirementId = localPower.getId();
        System.out.println("15minNetConsumption::LOCALPOWER_REQUIREMENT_ID = " + this.localPowerRequirementId);

        // Setup configuration deliverables
        ReadingTypeDeliverableBuilder builder = this.configuration.newReadingTypeDeliverable("consumption", fifteenMinutesNetConsumption, Formula.Mode.AUTO);
        ReadingTypeDeliverable netConsumption =
                builder.build(
                        builder.plus(
                                builder.requirement(localPower),
                                builder.property(registeredCustomPropertySet.getCustomPropertySet(), antennaPowerPropertySpec)));

        this.netConsumptionDeliverableId = netConsumption.getId();
        System.out.println("15minNetConsumption::NET_CONSUMPTION_DELIVERABLE_ID = " + this.netConsumptionDeliverableId);

        // Now that all custom properties have been configured and all deliverables have been created, we can mock the SqlBuilders
        this.initializeSqlBuilders();

        // Apply MetrologyConfiguration to UsagePoint
        this.usagePoint.apply(this.configuration, jan1st2016);

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
                        matches("cps" + this.customPropertySetId + "_" + Math.abs(AntennaFields.POWER.javaName().hashCode()) + "_\\d*"),
                        any(Optional.class),
                        anyVararg());
            String propertySql = this.antennaPowerPropertyWithClauseBuilder.getText().replace("\n", " ").toLowerCase();
            assertThat(propertySql).matches(".*\\(select cps." + AntennaFields.POWER.databaseName()
                    .toLowerCase() + " as value, cps.starttime, cps.endtime from \\(select " + AntennaFields.POWER.databaseName().toLowerCase() + ", starttime, endtime from tst_antenna.*cps\\)");
            verify(clauseAwareSqlBuilder)
                    .with(
                        matches("rod" + this.netConsumptionDeliverableId + ".*1"),
                        any(Optional.class),
                        anyVararg());
            String netConsumptionSql = this.netConsumptionWithClauseBuilder.getText().replace("\n", " ");
            assertThat(netConsumptionSql).matches(".*rid" + this.localPowerRequirementId + "_" + this.netConsumptionDeliverableId + "_1\\.value \\+ cps" + this.customPropertySetId + "_" + Math.abs(AntennaFields.POWER.javaName().hashCode()) + "_1\\.value.*");
            // Assert that the with clauses for both properties are joined on the utc timestamp
            assertThat(netConsumptionSql)
                    .matches("SELECT.*FROM\\s*rid" + this.localPowerRequirementId + "_" + this.netConsumptionDeliverableId + "_1\\s*JOIN.*");
            String customPropertyTableName = "cps" + this.customPropertySetId + "_" + Math.abs(AntennaFields.POWER.javaName().hashCode()) + "_1";
            String requirementTableName = "rid" + this.localPowerRequirementId + "_" + this.netConsumptionDeliverableId + "_1";
            assertThat(netConsumptionSql)
                    .matches("SELECT.*JOIN\\s*" + customPropertyTableName + "\\s*ON\\s*\\(\\s*" + customPropertyTableName + "\\.starttime < " + requirementTableName + ".timestamp\\s*AND\\s*" + requirementTableName + "\\.timestamp <= " + customPropertyTableName + "\\.endtime\\).*");
            verify(clauseAwareSqlBuilder).select();
            // Assert that the overall select statement selects the target reading type
            String overallSelectWithoutNewlines = this.selectClauseBuilder.getText().replace("\n", " ");
            assertThat(overallSelectWithoutNewlines).matches(".*'" + this.mRID2GrepPattern(FIFTEEN_MINS_NET_CONSUMPTION_MRID) + "'.*");
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

    @SuppressWarnings("unchecked")
    private PropertySpec getAntennaCountPropertySpec(RegisteredCustomPropertySet customPropertySet) {
        return this.getPropertySpec(customPropertySet.getCustomPropertySet().getPropertySpecs(), AntennaFields.COUNT.javaName());
    }

    @SuppressWarnings("unchecked")
    private PropertySpec getAntennaPowerPropertySpec(RegisteredCustomPropertySet customPropertySet) {
        return this.getPropertySpec(customPropertySet.getCustomPropertySet().getPropertySpecs(), AntennaFields.POWER.javaName());
    }

    private PropertySpec getPropertySpec(List<PropertySpec> specs, String specName) {
        return specs.stream().filter(each -> each.getName().equals(specName)).findFirst().get();
    }

    private void setupUsagePoint(String name) {
        ServiceCategory electricity = getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        this.usagePoint = electricity.newUsagePoint(name, jan1st2016).create();
    }

    private void activateMeter() {
        this.meterActivation = this.usagePoint.activate(this.meter, jan1st2016);
    }

    private void activateMeterWith15MinPower() {
        this.activateMeter();
        this.meterActivation.getChannelsContainer().createChannel(fifteenMinuteskWForward);
    }

    private String mRID2GrepPattern(String mRID) {
        return mRID.replace(".", "\\.");
    }

    private enum AntennaFields {
        DOMAIN {
            @Override
            public String javaName() {
                return "usagePoint";
            }
        },
        COUNT {
            @Override
            public String javaName() {
                return "numberOfAntennas";
            }

            @Override
            public String databaseName() {
                return "NBR";
            }
        },
        POWER {
            @Override
            public String javaName() {
                return "antennaPower";
            }

            @Override
            public String databaseName() {
                return "PWR";
            }
        };

        public abstract String javaName();

        public String databaseName() {
            return name();
        }
    }

    private static class AntennaDetails extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<UsagePoint> {
        @SuppressWarnings("unused")
        @IsPresent
        private Reference<UsagePoint> usagePoint = ValueReference.absent();
        private BigDecimal antennaPower = BigDecimal.ZERO;
        private BigDecimal numberOfAntennas = BigDecimal.ONE;

        BigDecimal getAntennaPower() {
            return antennaPower;
        }

        void setAntennaPower(BigDecimal antennaPower) {
            this.antennaPower = antennaPower;
        }

        BigDecimal getNumberOfAntennas() {
            return numberOfAntennas;
        }

        void setNumberOfAntennas(BigDecimal numberOfAntennas) {
            this.numberOfAntennas = numberOfAntennas;
        }

        @Override
        public void copyFrom(UsagePoint domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
            this.usagePoint.set(domainInstance);
            this.setAntennaPower((BigDecimal) propertyValues.getProperty(AntennaFields.POWER.javaName()));
            this.setNumberOfAntennas((BigDecimal) propertyValues.getProperty(AntennaFields.COUNT.javaName()));
        }

        @Override
        public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
            propertySetValues.setProperty(AntennaFields.POWER.javaName(), this.getAntennaPower());
            propertySetValues.setProperty(AntennaFields.COUNT.javaName(), this.getNumberOfAntennas());
        }

        @Override
        public void validateDelete() {
            // Nothing to validate
        }
    }

    private static class AntennaDetailsCustomPropertySet implements CustomPropertySet<UsagePoint, AntennaDetails> {
        static final String ID = "AntennaDetailsCustomPropertySet";

        private final PropertySpecService propertySpecService;

        private AntennaDetailsCustomPropertySet(PropertySpecService propertySpecService) {
            this.propertySpecService = propertySpecService;
        }

        @Override
        public String getId() {
            return ID;
        }

        @Override
        public String getName() {
            return "Antenna details";
        }

        @Override
        public Class<UsagePoint> getDomainClass() {
            return UsagePoint.class;
        }

        @Override
        public String getDomainClassDisplayName() {
            return this.getDomainClass().getName();
        }

        @Override
        public PersistenceSupport<UsagePoint, AntennaDetails> getPersistenceSupport() {
            return new AntennaDetailsPersistenceSupport();
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        public boolean isVersioned() {
            return true;
        }

        @Override
        public Set<ViewPrivilege> defaultViewPrivileges() {
            return EnumSet.allOf(ViewPrivilege.class);
        }

        @Override
        public Set<EditPrivilege> defaultEditPrivileges() {
            return EnumSet.allOf(EditPrivilege.class);
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.antennaPowerPropertySpec(),
                    this.antennaCountPropertySpec());
        }

        private PropertySpec antennaPowerPropertySpec() {
            return this.propertySpecService
                    .bigDecimalSpec()
                    .named("antennaPower", "Antenna power")
                    .describedAs("Power (in Wh) of the antenna")
                    .finish();
        }

        private PropertySpec antennaCountPropertySpec() {
            return this.propertySpecService
                    .bigDecimalSpec()
                    .named("numberOfAntennas", "Number of antenna's")
                    .describedAs("Number of antenna's in use")
                    .setDefaultValue(BigDecimal.ONE)
                    .finish();
        }
    }

    private static class AntennaDetailsPersistenceSupport implements PersistenceSupport<UsagePoint, AntennaDetails> {
        @Override
        public String componentName() {
            return "TST";
        }

        @Override
        public String tableName() {
            return "TST_ANTENNA";
        }

        @Override
        public String domainFieldName() {
            return "usagePoint";
        }

        @Override
        public String domainForeignKeyName() {
            return "TST_FK_USAGEPOINT";
        }

        @Override
        public Class<AntennaDetails> persistenceClass() {
            return AntennaDetails.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            EnumSet.of(AntennaFields.POWER, AntennaFields.COUNT).forEach(field -> this.addCustomPropertyColumnsTo(table, field));
        }

        private void addCustomPropertyColumnsTo(Table table, AntennaFields field) {
            table
                    .column(field.databaseName())
                    .number()
                    .map(field.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(AntennaFields.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(AntennaFields::databaseName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }

        @Override
        public String application() {
            return "Testing";
        }
    }
}

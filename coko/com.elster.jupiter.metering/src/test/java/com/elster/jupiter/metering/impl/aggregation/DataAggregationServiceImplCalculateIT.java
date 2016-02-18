package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
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
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.config.OperationNode;
import com.elster.jupiter.metering.impl.config.Operator;
import com.elster.jupiter.metering.impl.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.impl.config.ServerFormula;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the {@link DataAggregationServiceImpl#calculate(UsagePoint, MetrologyContract, Range)} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (08:57)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAggregationServiceImplCalculateIT {

    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static ReadingType fifteenMinuteskWh;
    private static Instant jan1st2016 = Instant.ofEpochMilli(1420070400000L);

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(injector.getInstance(TransactionService.class));

    @Mock
    private MetrologyConfiguration configuration;
    @Mock
    private MetrologyContract contract;
    private Meter productionMeter;
    private MeterActivation productionMeterActivation;
    private Meter consumptionMeter;
    private Channel production15MinChannel;
    private MeterActivation consumptionMeterActivation;
    private Channel consumption15MinChannel;
    private UsagePoint usagePoint;

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(LicenseService.class).to(LicenseServiceImpl.class).in(Scopes.SINGLETON);
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(DataVaultService.class).toInstance(mock(DataVaultService.class));
        }
    }

    @BeforeClass
    public static void setUp() {
        setupServices();
        setupReadingTypes();
    }

    private static void setupServices() {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new MeteringModule(
                            "0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.0.72.0"   // no macro period, measuring period =  15 min, primary metered
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
                    new CustomPropertySetsModule()
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

    private static MeteringService getMeteringService() {
        return injector.getInstance(MeteringService.class);
    }

    private static DataAggregationService getDataAggregationService() {
        return injector.getInstance(DataAggregationService.class);
    }

    private static void setupReadingTypes() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            fifteenMinuteskWh = getMeteringService().getReadingType("0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.0.72.0").get();
            ctx.commit();
        }
    }

    private void setupMeters(String amrIdBase) {
        AmrSystem mdc = getMeteringService().findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        this.productionMeter = mdc.newMeter(amrIdBase + "A+").create();
        this.consumptionMeter = mdc.newMeter(amrIdBase + "A-").create();
    }

    private void setupUsagePoints(String mRID) {
        ServiceCategory electricity = getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
        this.usagePoint = electricity.newUsagePoint(mRID).create();
    }

    private void activateMeters() {
        this.productionMeterActivation = this.productionMeter.activate(this.usagePoint, jan1st2016);
        this.production15MinChannel = this.productionMeterActivation.createChannel(fifteenMinuteskWh);
        this.consumptionMeterActivation = this.consumptionMeter.activate(this.usagePoint, jan1st2016);
        this.consumption15MinChannel = this.consumptionMeterActivation.createChannel(fifteenMinuteskWh);
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    /**
     * Tests the simplest case:
     * Metrology configuration
     *    requirements:
     *       A- ::= any Wh with flow = forward (aka consumption)
     *       A+ ::= any Wh with flow = reverse (aka production)
     *    deliverables:
     *       netConsumption (15m kWh) ::= A- + A+
     * Device:
     *    meter activations:
     *       Jan 1st 2015 -> forever
     *           A- -> 15 min kWh
     *           A+ -> 15 min kWh
     * In other words, simple sum of 2 requirements that are provided
     * by exactly one matching channel with a single meter activation.
     */
    @Test
    @Transactional
    public void simplestNetConsumptionOfProsumer() {
        DataAggregationService service = this.testInstance();
        this.setupMeters("simplestNetConsumptionOfProsumer");
        this.setupUsagePoints("simplestNetConsumptionOfProsumer");
        this.activateMeters();

        // Setup configuration requirements
        ReadingTypeRequirement consumption = mock(ReadingTypeRequirement.class);
        when(consumption.getName()).thenReturn("A-");
        ReadingTypeRequirement production = mock(ReadingTypeRequirement.class);
        when(production.getName()).thenReturn("A+");
        when(this.configuration.getRequirements()).thenReturn(Arrays.asList(consumption, production));
        // Setup configuration deliverables
        ReadingTypeDeliverable netConsumption = mock(ReadingTypeDeliverable.class);
        when(netConsumption.getName()).thenReturn("consumption");
        ReadingType netConsumptionReadingType = this.mock15minReadingType();
        when(netConsumption.getReadingType()).thenReturn(netConsumptionReadingType);
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        doReturn(
                new OperationNode(
                        Operator.PLUS,
                        new ReadingTypeRequirementNode(production),
                        new ReadingTypeRequirementNode(consumption)))
                .when(formula).expressionNode();
        when(netConsumption.getFormula()).thenReturn(formula);
        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(netConsumption));
        // Setup meter activations
        when(consumption.getMatchesFor(this.consumptionMeterActivation)).thenReturn(Collections.singletonList(fifteenMinuteskWh));
        when(consumption.getMatchingChannelsFor(this.consumptionMeterActivation)).thenReturn(Collections.singletonList(this.consumption15MinChannel));
        when(production.getMatchesFor(this.productionMeterActivation)).thenReturn(Collections.singletonList(fifteenMinuteskWh));
        when(production.getMatchingChannelsFor(this.productionMeterActivation)).thenReturn(Collections.singletonList(this.production15MinChannel));

        // Business method
        List<? extends BaseReadingRecord> aggregatedRecords = service.calculate(this.usagePoint, this.contract, year2016());

        // Asserts: happy to get here without exceptions for now
        assertThat(aggregatedRecords).isNotNull();
    }

    private Range<Instant> year2016() {
        return Range.atLeast(jan1st2016);
    }

    private ReadingType mock15minReadingType() {
        ReadingType meterActivationReadingType = mock(ReadingType.class);
        when(meterActivationReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(meterActivationReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        return meterActivationReadingType;
    }

    private ReadingType mockHourlyReadingType() {
        ReadingType meterActivationReadingType = mock(ReadingType.class);
        when(meterActivationReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(meterActivationReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        return meterActivationReadingType;
    }

    private DataAggregationService testInstance() {
        return getDataAggregationService();
    }

}
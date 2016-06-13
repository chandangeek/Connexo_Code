package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.metering.UsagePointReadingTypeConfiguration;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointConfigurationIT {

    public static final String MULTIPLIER_TYPE_NAME = "Pulse";
    public static final BigDecimal VALUE = BigDecimal.valueOf(2, 0);
    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    private Injector injector;

    private static final ZonedDateTime ACTIVE_DATE = ZonedDateTime.of(2014, 4, 9, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime END_DATE = ZonedDateTime.of(2014, 8, 9, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;


    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private UsagePoint usagePoint;
    private MeterActivation meterActivation;
    private MultiplierType multiplierType;
    private MeteringService meteringService;
    private TransactionService transactionService;
    private ReadingType secondaryMetered;
    private ReadingType primaryMetered;


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(SearchService.class).toInstance(mock(SearchService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Before
    public void setUp() throws SQLException {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new MeteringModule(
                            "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0",  // no macro period, measuring period =  15 min, secondary metered
                            "0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.0.72.0"  // no macro period, measuring period =  15 min, primary metered
                    ),
                    new BasicPropertiesModule(),
                    new TimeModule(),
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
                    new BasicPropertiesModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(FiniteStateMachineService.class);
            meteringService = injector.getInstance(MeteringService.class);
            return null;
        });
        secondaryMetered = meteringService.getReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0").get();
        primaryMetered = meteringService.getReadingType("0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.0.72.0").get();
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testCreateConfiguration() {
        createAndActivateUsagePoint();
        createMultiplierType();

        UsagePointConfiguration usagePointConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            usagePointConfiguration = usagePoint
                    .startingConfigurationOn(ACTIVE_DATE.toInstant())
                    .endingAt(END_DATE.toInstant())
                    .configureReadingType(secondaryMetered)
                    .withMultiplierOfType(multiplierType)
                    .calculating(primaryMetered)
                    .create();
            context.commit();
        }

        assertThat(usagePoint.getConfiguration(ACTIVE_DATE.toInstant())).contains(usagePointConfiguration);

        usagePoint = meteringService.findUsagePoint(usagePoint.getId()).get();
        usagePointConfiguration = usagePoint.getConfiguration(ACTIVE_DATE.toInstant()).get();

        assertThat(usagePointConfiguration.getRange()).isEqualTo(Range.closedOpen(ACTIVE_DATE.toInstant(), END_DATE.toInstant()));
        assertThat(usagePointConfiguration.getReadingTypeConfigs()).hasSize(1);

        UsagePointReadingTypeConfiguration usagePointReadingTypeConfiguration = usagePointConfiguration.getReadingTypeConfigs().get(0);

        assertThat(usagePointReadingTypeConfiguration.getMeasured()).isEqualTo(secondaryMetered);
        assertThat(usagePointReadingTypeConfiguration.getCalculated()).contains(primaryMetered);
        assertThat(usagePointReadingTypeConfiguration.getMultiplierType()).isEqualTo(multiplierType);
    }

    @Test
    public void testEndConfiguration() {
        createAndActivateUsagePoint();
        createMultiplierType();

        UsagePointConfiguration usagePointConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            usagePointConfiguration = usagePoint
                    .startingConfigurationOn(ACTIVE_DATE.toInstant())
                    .configureReadingType(secondaryMetered)
                    .withMultiplierOfType(multiplierType)
                    .calculating(primaryMetered)
                    .create();
            context.commit();
        }

        assertThat(usagePoint.getConfiguration(ACTIVE_DATE.toInstant())).contains(usagePointConfiguration);

        usagePoint = meteringService.findUsagePoint(usagePoint.getId()).get();
        usagePointConfiguration = usagePoint.getConfiguration(ACTIVE_DATE.toInstant()).get();

        try (TransactionContext context = transactionService.getContext()) {
            usagePointConfiguration.endAt(END_DATE.toInstant());
            context.commit();
        }

        usagePoint = meteringService.findUsagePoint(usagePoint.getId()).get();
        usagePointConfiguration = usagePoint.getConfiguration(ACTIVE_DATE.toInstant()).get();

        Range<Instant> range = usagePointConfiguration.getRange();
        assertThat(range.hasUpperBound());
        assertThat(range.upperEndpoint()).isEqualTo(END_DATE.toInstant());
    }

    private void createMultiplierType() {
        try (TransactionContext context = transactionService.getContext()) {
            multiplierType = meteringService.createMultiplierType(MULTIPLIER_TYPE_NAME);
            context.commit();
        }
    }

    private void createAndActivateUsagePoint() {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCategory electricity = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            usagePoint = electricity.newUsagePoint("mrId", Instant.EPOCH).create();
            AmrSystem system = meteringService.findAmrSystem(1).get();
            Meter meter = system.newMeter("meter").create();
            meterActivation = usagePoint.activate(meter, injector.getInstance(ServerMetrologyConfigurationService.class)
                    .findDefaultMeterRole(DefaultMeterRole.DEFAULT), ACTIVE_DATE.toInstant());
            context.commit();
        }
        usagePoint = meteringService.findUsagePoint(usagePoint.getId()).get();
        meterActivation = usagePoint.getMeterActivations().get(0);
    }


}
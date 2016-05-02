package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class NtaReadingTypeTest {
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    private MeterActivation meterActivation;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static final String[] readingTypeCodes = {
    	"0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
    	"11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.3.72.0",
    	"13.0.0.9.1.1.12.0.0.0.0.1.0.0.0.3.72.0",
    };

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(SearchService.class).toInstance(mock(SearchService.class));
            bind(PropertySpecService.class).toInstance(mock(PropertySpecService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Before
    public void setUp() throws SQLException {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new MeteringModule("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.3.72.0",
                        "11.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0",
                        "13.0.0.9.1.1.12.0.0.0.0.1.0.0.0.3.72.0",
                        "13.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0"),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(false),
                new BpmModule(),
                new FiniteStateMachineModule(),
                new NlsModule(),
                new CustomPropertySetsModule()
        );
        TransactionService txService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = txService.execute(() -> {
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(FiniteStateMachineService.class);
            return injector.getInstance(MeteringService.class);
        });
        Meter meter = txService.execute(() -> {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            return amrSystem.newMeter("myMeter").create(); });
        meterActivation = txService.execute(() -> meter.activate(Instant.now()));
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void test() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        for (String code : readingTypeCodes) {
        	Optional<ReadingType> readingType = meteringService.getReadingType(code);
        	assertThat(readingType.isPresent()).isTrue();
        	Optional<TemporalAmount> interval = ((ReadingTypeImpl) readingType.get()).getIntervalLength();
        	assertThat(interval.isPresent()).isTrue();
        }
        TransactionService txService = injector.getInstance(TransactionService.class);
        for (String code : readingTypeCodes) {
        	ReadingType readingType = meteringService.getReadingType(code).get();
        	Channel channel = txService.execute(() -> meterActivation.createChannel(readingType));
        	assertThat (((ChannelImpl) channel).getRecordSpecDefinition()).isEqualTo(RecordSpecs.BULKQUANTITYINTERVAL);
        }
    }


}

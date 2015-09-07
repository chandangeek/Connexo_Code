package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MeterActivationImplIT {

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;


    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
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
                    new MeteringModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
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
                    new NlsModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringService.class);
            return null;
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testPersistence() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	AmrSystem system = meteringService.findAmrSystem(1).get();
        	Meter meter = system.newMeter("1");
        	meter.save();
        	MeterActivation meterActivation = meter.activate(ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault()).toInstant());
        	ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            Channel channel = meterActivation.createChannel(readingType);
            MeterActivation loaded = meteringService.findMeterActivation(meterActivation.getId()).get();
            assertThat(loaded.getChannels()).hasSize(1).contains(channel);
        }
    }

    @Test
    public void testCOPL854() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        long meterId, usagePointId;
        Instant start3 = Instant.now();
        Instant start1 = start3.minusSeconds(86400);
        Instant start2 = start3.minusSeconds(43200);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
            Meter meter = amrSystem.newMeter("1");
            meter.save();
            meterId = meter.getId();
            UsagePoint up = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).orElseThrow(IllegalArgumentException::new).newUsagePoint("abcd");
            up.save();
            usagePointId = up.getId();

            meter.activate(up, start1);

            ctx.commit();
        }
        assertMeterActivations(meterId, usagePointId, start1);

        try (TransactionContext context = injector.getInstance(TransactionService.class).getContext()) {
            Meter meter = meteringService.findMeter(meterId).get();
            meter.getUsagePoint(Instant.now()).get().activate(meter, start2);
            context.commit();
        }
        assertMeterActivations(meterId, usagePointId, start1, start2);

        try (TransactionContext context = injector.getInstance(TransactionService.class).getContext()) {
            UsagePoint up = meteringService.findUsagePoint(usagePointId).get();
            Meter meter = meteringService.findMeter(meterId).get();

            up.activate(meter, start3);
            context.commit();
        }
        assertMeterActivations(meterId, usagePointId, start1, start2, start3);
    }

    private void assertMeterActivations(long meterId, long usagePointId, Instant... startTimes) {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        List<? extends MeterActivation> meterActivations = meteringService.findMeter(meterId).get().getMeterActivations();
        assertThat(meterActivations).hasSize(startTimes.length);
        for (int i = 0; i < startTimes.length; i++) {
            Instant startTime = startTimes[i];
            Instant endTime = (i < startTimes.length - 1 ? startTimes[i + 1] : null);
            assertThat(meterActivations.get(i).getMeter()).isPresent();
            assertThat(meterActivations.get(i).getMeter().get().getId()).isEqualTo(meterId);
            assertThat(meterActivations.get(i).getUsagePoint()).isPresent();
            assertThat(meterActivations.get(i).getUsagePoint().get().getId()).isEqualTo(usagePointId);
            assertThat(meterActivations.get(i).getStart()).as("Start date of meter activation " + i + " not as expected").isEqualTo(startTime);
            assertThat(meterActivations.get(i).getEnd()).as("End date of meter activation " + i + " not as expected").isEqualTo(endTime);
        }

    }

}

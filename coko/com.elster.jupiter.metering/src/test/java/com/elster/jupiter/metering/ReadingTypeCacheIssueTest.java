package com.elster.jupiter.metering;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.Instant;

import com.elster.jupiter.bpm.impl.BpmModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeCacheIssueTest {

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
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new MeteringModule(false),
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
    public void test() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        Meter meter;
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        	meter = amrSystem.newMeter("myMeter");
        	meter.save();
        	ctx.commit();
        }
    	ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.AIR)
    			.period(TimeAttribute.NOTAPPLICABLE)
    			.accumulate(Accumulation.BULKQUANTITY)
    			.flow(FlowDirection.FORWARD)
    			.measure(MeasurementKind.ENERGY)
    			.in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
    	String readingTypeCode = builder.code();
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	Instant instant = LocalDate.of(2014,1,1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        	Reading reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1000), instant);
        	meter.store(MeterReadingImpl.of(reading));
        	//rollback
        }
        assertThat(meteringService.getReadingType(readingTypeCode).isPresent()).isFalse();
        meter = meteringService.findMeter(meter.getId()).get(); // get fresh copy from DB
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	Instant instant = ZonedDateTime.of(2014,1,1,0,0,0,0,ZoneId.systemDefault()).toInstant();
        	Reading reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1000), instant);
        	meter.store(MeterReadingImpl.of(reading));
        	ctx.commit();
        }
        assertThat(meteringService.getReadingType(readingTypeCode).isPresent()).isTrue();
    }

}

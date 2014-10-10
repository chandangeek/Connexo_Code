package com.elster.jupiter.metering;

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
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
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
import com.elster.jupiter.util.time.Interval;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CumulativeChannelTest {

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
                new MeteringModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule());
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                injector.getInstance(MeteringService.class);
                return null;
            }
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void test() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
        	AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        	Meter meter = amrSystem.newMeter("myMeter");
        	meter.save();
        	MeterActivation activation = meter.activate(ZonedDateTime.of(2014,1,1,0,0,0,0,ZoneId.systemDefault()).toInstant());
        	String readingTypeCode = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
        			.period(TimeAttribute.MINUTE15)
        			.accumulate(Accumulation.BULKQUANTITY)
        			.flow(FlowDirection.FORWARD)
        			.measure(MeasurementKind.ENERGY)
        			.in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
        			.code();
        	ReadingType readingType = meteringService.getReadingType(readingTypeCode).get();
            Channel channel = activation.createChannel(readingType);
            assertThat(channel.getBulkQuantityReadingType().isPresent()).isTrue();
            ReadingStorer storer = meteringService.createOverrulingStorer();
            DateTime dateTime = new DateTime(2014,1,1,0,0,0);
            storer.addReading(channel, new IntervalReadingImpl(dateTime.toDate(), BigDecimal.valueOf(1000)));
            storer.addReading(channel, new IntervalReadingImpl(dateTime.plus(15*60*1000L).toDate(), BigDecimal.valueOf(1100)));
            storer.execute();
            List<BaseReadingRecord> readings = channel.getReadings(new Interval(dateTime.minus(15*60*1000L).toDate(),dateTime.plus(15*60*1000L).toDate()));
            assertThat(readings).hasSize(2);
            assertThat(readings.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(100));
            assertThat(readings.get(1).getQuantity(1).getValue()).isEqualTo(BigDecimal.valueOf(1100));
            ctx.commit();
        }
    }

}

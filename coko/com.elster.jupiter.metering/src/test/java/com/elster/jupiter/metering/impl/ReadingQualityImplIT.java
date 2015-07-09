package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.events.impl.LocalEventImpl;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.pubsub.impl.PublisherImpl;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class ReadingQualityImplIT {

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private Subscriber topicHandler;

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
                    new MeteringModule(),
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
                    new NlsModule());
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
    public void test() throws SQLException {

        getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                when(topicHandler.getClasses()).thenReturn(new Class[]{LocalEventImpl.class});
                ((PublisherImpl) injector.getInstance(Publisher.class)).addHandler(topicHandler);

                Instant date = ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
                doTest(getMeteringService(), date);

                ArgumentCaptor<LocalEvent> localEventCapture = ArgumentCaptor.forClass(LocalEvent.class);
                verify(topicHandler, times(4)).handle(localEventCapture.capture());

                LocalEvent localEvent = localEventCapture.getAllValues().get(3);
                assertThat(localEvent.getType().getTopic()).isEqualTo(EventType.READING_QUALITY_CREATED.topic());
                Event event = localEvent.toOsgiEvent();
                assertThat(event.containsProperty("readingTimestamp")).isTrue();
                assertThat(event.containsProperty("channelId")).isTrue();
                assertThat(event.containsProperty("readingQualityTypeCode")).isTrue();

            }
        });

    }

    @Test
    public void testDelete() {
        getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                Instant date = ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
                ReadingQualityRecord readingQuality = doTest(getMeteringService(), date);

                when(topicHandler.getClasses()).thenReturn(new Class[]{LocalEventImpl.class});
                ((PublisherImpl) injector.getInstance(Publisher.class)).addHandler(topicHandler);


                readingQuality.delete();
                ArgumentCaptor<LocalEvent> localEventCapture = ArgumentCaptor.forClass(LocalEvent.class);
                verify(topicHandler).handle(localEventCapture.capture());

                LocalEvent localEvent = localEventCapture.getValue();
                assertThat(localEvent.getType().getTopic()).isEqualTo(EventType.READING_QUALITY_DELETED.topic());
                Event event = localEvent.toOsgiEvent();
                assertThat(event.containsProperty("readingTimestamp")).isTrue();
                assertThat(event.containsProperty("channelId")).isTrue();
                assertThat(event.containsProperty("readingQualityTypeCode")).isTrue();

            }
        });
    }

    private MeteringService getMeteringService() {
        return injector.getInstance(MeteringService.class);
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    private ReadingQualityRecord doTest(MeteringService meteringService, Instant date) {
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("mrID");
        usagePoint.save();
        ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        MeterActivation meterActivation = usagePoint.activate(date);
        Channel channel = meterActivation.createChannel(readingType);
        ReadingStorer regularStorer = meteringService.createNonOverrulingStorer();
        regularStorer.addReading(channel.getCimChannel(readingType).get(), IntervalReadingImpl.of(date, BigDecimal.valueOf(561561, 2)));
        regularStorer.execute();
        BaseReadingRecord reading = channel.getReading(date).get();
        ReadingQualityRecord readingQuality = channel.createReadingQuality(new ReadingQualityType("6.1"), readingType, reading);
        readingQuality.save();
        return readingQuality;
    }

}

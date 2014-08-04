package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.IntervalLength;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.kpi.KpiMissEvent;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.pubsub.impl.PublisherImpl;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.assertj.core.api.Assertions;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KpiServiceImplIT {

    public static final String KPI_NAME = "kpiName";
    public static final String READ_METERS = "readMeters";
    public static final String NON_COMMUNICATING_METERS = "nonCommunicatingMeters";
    Date date = new DateTime(2000, 2, 11, 20, 0, 0, 0, DateTimeZone.forID("Europe/Brussels")).toDate();

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    private KpiService kpiService;
    private TransactionService transactionService;


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
        }
    }

    @Before
    public void setUp() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new KpiModule()
        );

        final PublisherImpl pub = (PublisherImpl) injector.getInstance(Publisher.class);
        doAnswer(new Answer<ServiceRegistration<Subscriber>>() {

            @Override
            public ServiceRegistration<Subscriber> answer(InvocationOnMock invocation) throws Throwable {
                pub.addHandler((Subscriber) invocation.getArguments()[1]);
                return null;
            }
        }).
                when(bundleContext).registerService(eq(Subscriber.class), any(Subscriber.class), any(Dictionary.class));

        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                kpiService = injector.getInstance(KpiService.class);
                return null;
            }
        });
    }

    @Test
    public void testCreateKpi() {
        long id = 0;
        try (TransactionContext context = transactionService.getContext()) {
            Kpi kpi = kpiService.newKpi().named(KPI_NAME).interval(IntervalLength.ofDay())
                    .member().named(READ_METERS).withDynamicTarget().asMinimum().add()
                    .member().named(NON_COMMUNICATING_METERS).withTargetSetAt(BigDecimal.valueOf(1, 2)).asMaximum().add()
                    .build();
            kpi.save();

            id = kpi.getId();
            context.commit();
        }

        Optional<Kpi> found = kpiService.getKpi(id);
        assertThat(found).isPresent();

        Kpi kpi = found.get();

        Assertions.assertThat(kpi.getName()).isEqualTo(KPI_NAME);
        Assertions.assertThat(kpi.getMembers()).hasSize(2);
        KpiMember first = kpi.getMembers().get(0);
        Assertions.assertThat(first.getName()).isEqualTo(READ_METERS);
        Assertions.assertThat(first.hasDynamicTarget()).isTrue();
        Assertions.assertThat(first.targetIsMinimum()).isTrue();
        Assertions.assertThat(first.targetIsMaximum()).isFalse();

        KpiMember second = kpi.getMembers().get(1);
        Assertions.assertThat(second.getName()).isEqualTo(NON_COMMUNICATING_METERS);
        Assertions.assertThat(second.hasDynamicTarget()).isFalse();
        Assertions.assertThat(second.getTarget(date)).isEqualTo(BigDecimal.valueOf(1, 2));
        Assertions.assertThat(second.targetIsMinimum()).isFalse();
        Assertions.assertThat(second.targetIsMaximum()).isTrue();

    }

    @Test
    public void testStoreKpiValue() {
        long id = 0;
        try (TransactionContext context = transactionService.getContext()) {
            Kpi kpi = kpiService.newKpi().named(KPI_NAME).interval(IntervalLength.ofDay())
                    .member().named(READ_METERS).withDynamicTarget().asMinimum().add()
                    .member().named(NON_COMMUNICATING_METERS).withTargetSetAt(BigDecimal.valueOf(1, 2)).asMaximum().add()
                    .build();
            kpi.save();

            Date date = new DateMidnight(2013, 7, 31, DateTimeZone.UTC).toDate();

            kpi.getMembers().get(0).score(date, BigDecimal.valueOf(8, 0));
            kpi.getMembers().get(1).score(date, BigDecimal.valueOf(2, 2));

            id = kpi.getId();
            context.commit();
        }


        Optional<Kpi> found = kpiService.getKpi(id);
        assertThat(found).isPresent();

        Kpi kpi = found.get();

        {
            List<? extends KpiEntry> entries =  kpi.getMembers().get(0).getScores(Interval.startAt(date));
            assertThat(entries).hasSize(1);
            assertThat(entries.get(0).getScore()).isEqualTo(BigDecimal.valueOf(8, 0));
            assertThat(entries.get(0).meetsTarget()).isTrue();
        }

        {
            List<? extends KpiEntry> entries =  kpi.getMembers().get(1).getScores(Interval.startAt(date));
            assertThat(entries).hasSize(1);
            KpiEntry entry = entries.get(0);
            assertThat(entry.getScore()).isEqualTo(BigDecimal.valueOf(2, 2));
            assertThat(entry.getTarget()).isEqualTo(BigDecimal.valueOf(1, 2));
            assertThat(entry.meetsTarget()).isFalse();
        }

    }


    @Test
    public void testStoreDynamicTargets() {
        Date date = new DateMidnight(2013, 7, 31, DateTimeZone.UTC).toDate();

        long id = 0;
        try (TransactionContext context = transactionService.getContext()) {
            Kpi kpi = kpiService.newKpi().named(KPI_NAME).interval(IntervalLength.ofDay())
                    .member().named(READ_METERS).withDynamicTarget().asMinimum().add()
                    .member().named(NON_COMMUNICATING_METERS).withTargetSetAt(BigDecimal.valueOf(1, 2)).asMaximum().add()
                    .build();
            kpi.save();

            kpi.getMembers().get(0).getTargetStorer().add(date, BigDecimal.valueOf(85, 2)).execute();

            id = kpi.getId();
            context.commit();
        }


        Optional<Kpi> found = kpiService.getKpi(id);
        assertThat(found).isPresent();

        Kpi kpi = found.get();

        assertThat(kpi.getMembers().get(0).getTarget(date)).isEqualTo(BigDecimal.valueOf(85, 2));
    }

    @Test
    public void testScoreOverDynamicTargets() {
        Date date = new DateMidnight(2013, 7, 31, DateTimeZone.UTC).toDate();

        long id = 0;
        try (TransactionContext context = transactionService.getContext()) {
            Kpi kpi = kpiService.newKpi().named(KPI_NAME).interval(IntervalLength.ofDay())
                    .member().named(READ_METERS).withDynamicTarget().asMinimum().add()
                    .member().named(NON_COMMUNICATING_METERS).withTargetSetAt(BigDecimal.valueOf(1, 2)).asMaximum().add()
                    .build();
            kpi.save();

            kpi.getMembers().get(0).getTargetStorer().add(date, BigDecimal.valueOf(85, 0)).execute();
            kpi.getMembers().get(0).score(date, BigDecimal.valueOf(87, 0));

            id = kpi.getId();
            context.commit();
        }


        Optional<Kpi> found = kpiService.getKpi(id);
        assertThat(found).isPresent();

        Kpi kpi = found.get();

        List<? extends KpiEntry> entries =  kpi.getMembers().get(0).getScores(Interval.startAt(new Date(0)));
        assertThat(entries).hasSize(1);
        KpiEntry entry = entries.get(0);
        assertThat(entry.getScore()).isEqualTo(BigDecimal.valueOf(87, 0));
        assertThat(entry.getTarget()).isEqualTo(BigDecimal.valueOf(85, 0));
        assertThat(entry.meetsTarget()).isTrue();
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateStaticTargetOnDynamicMemberShouldFail() {
        long id = 0;
        try (TransactionContext context = transactionService.getContext()) {
            Kpi kpi = kpiService.newKpi().named(KPI_NAME).interval(IntervalLength.ofDay())
                    .member().named(READ_METERS).withDynamicTarget().asMinimum().add()
                    .member().named(NON_COMMUNICATING_METERS).withTargetSetAt(BigDecimal.valueOf(1, 2)).asMaximum().add()
                    .build();
            kpi.save();

            id = kpi.getId();
            context.commit();
        }

        Optional<Kpi> found = kpiService.getKpi(id);
        assertThat(found).isPresent();

        Kpi kpi = found.get();

        try (TransactionContext context = transactionService.getContext()) {
            kpi.getMembers().get(0).updateTarget(BigDecimal.valueOf(7, 5));
            context.commit();
        }

    }


    @Test
    public void testUpdateStaticTarget() {
        long id = 0;
        try (TransactionContext context = transactionService.getContext()) {
            Kpi kpi = kpiService.newKpi().named(KPI_NAME).interval(IntervalLength.ofDay())
                    .member().named(READ_METERS).withDynamicTarget().asMinimum().add()
                    .member().named(NON_COMMUNICATING_METERS).withTargetSetAt(BigDecimal.valueOf(1, 2)).asMaximum().add()
                    .build();
            kpi.save();

            id = kpi.getId();
            context.commit();
        }

        Optional<Kpi> found = kpiService.getKpi(id);
        assertThat(found).isPresent();

        Kpi kpi = found.get();

        try (TransactionContext context = transactionService.getContext()) {
            kpi.getMembers().get(1).updateTarget(BigDecimal.valueOf(7, 5));
            context.commit();
        }

        found = kpiService.getKpi(id);
        assertThat(found).isPresent();

        kpi = found.get();
        assertThat(kpi.getMembers().get(1).getTarget(new Date(0))).isEqualTo(BigDecimal.valueOf(7, 5));
    }

    @Test
    public void testStoreKpiValueThatNotMeetsTargetTriggersEvent() {
        EventServiceImpl eventService = (EventServiceImpl) injector.getInstance(EventService.class);
        TopicHandler topicHandler = mock(TopicHandler.class);
        when(topicHandler.getTopicMatcher()).thenReturn(EventType.KPI_TARGET_MISSED.topic());

        eventService.addTopicHandler(topicHandler);

        long id = 0;
        try (TransactionContext context = transactionService.getContext()) {
            Kpi kpi = kpiService.newKpi().named(KPI_NAME).interval(IntervalLength.ofDay())
                    .member().named(READ_METERS).withDynamicTarget().asMinimum().add()
                    .member().named(NON_COMMUNICATING_METERS).withTargetSetAt(BigDecimal.valueOf(1, 2)).asMaximum().add()
                    .build();
            kpi.save();

            Date date = new DateMidnight(2013, 7, 31, DateTimeZone.UTC).toDate();

            kpi.getMembers().get(0).score(date, BigDecimal.valueOf(8, 0));
            kpi.getMembers().get(1).score(date, BigDecimal.valueOf(2, 2));

            id = kpi.getId();
            context.commit();
        }

        ArgumentCaptor<LocalEvent> eventCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(topicHandler).handle(eventCaptor.capture());

        LocalEvent localEvent = eventCaptor.getValue();
        assertThat(localEvent.getSource()).isInstanceOf(KpiMissEvent.class);
        KpiMissEvent event = (KpiMissEvent) localEvent.getSource();
        assertThat(event.getMember().getName()).isEqualTo(NON_COMMUNICATING_METERS);
        assertThat(event.getEntry().getScore()).isEqualTo(BigDecimal.valueOf(2, 2));
        assertThat(event.getEntry().getTarget()).isEqualTo(BigDecimal.valueOf(1, 2));

    }



    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

}

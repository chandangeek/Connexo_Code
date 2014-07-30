package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.IntervalLength;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.guava.api.Assertions.assertThat;

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
    public void test() {
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


    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

}

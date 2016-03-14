package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.impl.example.DisconnectHandler;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ServicecCallFinderImplIT {

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private final Instant now = ZonedDateTime.of(2016, 1, 8, 10, 0, 0, 0, ZoneId.of("UTC")).toInstant();
    private Injector injector;

    private TransactionService transactionService;
    private IServiceCallService serviceCallService;

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    @Mock
    private UserService userService;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private MessageInterpolator messageInterpolator;

    private Clock clock;
    private ServiceCallType serviceCallType;
    private ServiceCallType serviceCallTypeTwo;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(MessageInterpolator.class).toInstance(messageInterpolator);
//            bind(ServiceCallTypeOneCustomPropertySet.class).to(ServiceCallTypeOneCustomPropertySet.class);
        }
    }

    @Before
    public void setUp() {
        clock = new ProgrammableClock(ZoneId.of("UTC"), now);
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(clock),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new DataVaultModule(),
                    new FiniteStateMachineModule(),
                    new CustomPropertySetsModule(),
                    new TimeModule(),
                    new BasicPropertiesModule(),
                    new ServiceCallModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(new Transaction<Void>() {

            @Override
            public Void perform() {
                serviceCallService = injector.getInstance(IServiceCallService.class);
                new DisconnectHandler(serviceCallService);

                serviceCallType = serviceCallService.createServiceCallType("primer", "v1")
                        .handler("someHandler")
                        .handler("DisconnectHandler1")
                        .create();

                serviceCallTypeTwo = serviceCallService.createServiceCallType("second", "v2")
                        .handler("someHandler")
                        .handler("DisconnectHandler1")
                        .create();

                return null;
            }
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testServiceCallFinderSorting() {
        ServiceCall serviceCallOne = null;
        ServiceCall serviceCallTwo = null;
        ServiceCall serviceCallThree = null;
        try (TransactionContext context = transactionService.getContext()) {
            serviceCallOne = serviceCallType.newServiceCall()
                    .externalReference("external")
                    .origin("CST")
                    .create();
            serviceCallTwo = serviceCallOne.newChildCall(serviceCallType)
                    .externalReference("external")
                    .origin("CST")
                    .create();
            serviceCallThree = serviceCallType.newServiceCall()
                    .externalReference("external")
                    .origin("CST")
                    .create();

            context.commit();
        }

        List<ServiceCall> result = serviceCallService.getServiceCallFinder().find();
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo(serviceCallOne);
        assertThat(result.get(1)).isEqualTo(serviceCallThree);
        assertThat(result.get(2)).isEqualTo(serviceCallTwo);
    }

    @Test
    public void testServiceCallFinderFiltering() {
        ServiceCall serviceCallOne = null;
        ServiceCall serviceCallTwo = null;
        ServiceCall serviceCallThree = null;
        try (TransactionContext context = transactionService.getContext()) {
            serviceCallOne = serviceCallTypeTwo.newServiceCall()
                    .externalReference("external")
                    .origin("CST")
                    .create();
            serviceCallTwo = serviceCallOne.newChildCall(serviceCallType)
                    .externalReference("external")
                    .origin("CST")
                    .create();
            serviceCallThree = serviceCallTypeTwo.newServiceCall()
                    .externalReference("SAP")
                    .origin("CST")
                    .create();

            context.commit();
        }

        List<ServiceCall> result = serviceCallService.getServiceCallFinder()
                .setParent(serviceCallOne)
                .find();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(serviceCallTwo);

        result = serviceCallService.getServiceCallFinder()
                .setParent(serviceCallThree)
                .find();
        assertThat(result).hasSize(0);

        result = serviceCallService.getServiceCallFinder()
                .setReference("extern*")
                .find();
        assertThat(result).hasSize(2);
        assertThat(result).contains(serviceCallOne, serviceCallTwo);

        List<String> type = new ArrayList<>();
        type.add("second");

        result = serviceCallService.getServiceCallFinder()
                .setType(type)
                .find();
        assertThat(result).hasSize(2);
        assertThat(result).contains(serviceCallOne, serviceCallThree);

        result = serviceCallOne.getChildrenFinder().find();
        assertThat(result).hasSize(1);
        assertThat(result).contains(serviceCallTwo);
    }
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallLog;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.impl.example.DisconnectHandler;
import com.elster.jupiter.servicecall.impl.example.FakeTypeOneCustomPropertySet;
import com.elster.jupiter.servicecall.impl.example.ServiceCallTypeOneCustomPropertySet;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
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
import org.osgi.service.log.LogService;

import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Supplier;

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
public class ServiceCallLogIT {

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;

    private NlsService nlsService;
    private TransactionService transactionService;
    private MessageService messageService;
    private IServiceCallService serviceCallService;

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();
    @Rule
    public TestRule expectedValidationRule = new ExpectedConstraintViolationRule();

    @Mock
    private UserService userService;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private LogService logService;
    @Mock
    private MessageInterpolator messageInterpolator;

    private Clock clock;
    private CustomPropertySetService customPropertySetService;
    private ServiceCallTypeOneCustomPropertySet serviceCallTypeOneCustomPropertySet;
    private FakeTypeOneCustomPropertySet fake;
    private DisconnectHandler disconnectHandler;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(MessageInterpolator.class).toInstance(messageInterpolator);
            bind(SearchService.class).toInstance(mock(SearchService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    class FastClock implements Supplier<Instant> {
        private final Instant now = ZonedDateTime.of(2016, 1, 8, 10, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        private long i;

        @Override
        public Instant get() {
            return now.plusSeconds(i++);
        }
    }

    @Before
    public void setUp() {
        clock = new ProgrammableClock(ZoneId.of("UTC"), new FastClock());
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
                nlsService = injector.getInstance(NlsService.class);
                customPropertySetService = injector.getInstance(CustomPropertySetService.class);
                messageService = injector.getInstance(MessageService.class);
                serviceCallService = injector.getInstance(IServiceCallService.class);
                serviceCallTypeOneCustomPropertySet = injector.getInstance(ServiceCallTypeOneCustomPropertySet.class);
                fake = injector.getInstance(FakeTypeOneCustomPropertySet.class);
                new DisconnectHandler(serviceCallService);
                return null;
            }
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testLogOnServiceCall() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallType serviceCallType = serviceCallService.createServiceCallType("primer", "v1")
                    .logLevel(LogLevel.FINEST)
                    .handler("DisconnectHandler1")
                    .create();
            ServiceCall serviceCall = serviceCallType.newServiceCall().origin("Tests").create();
            serviceCall.log(LogLevel.SEVERE, "Kapot");

            assertThat(serviceCall.getLogs().find()).hasSize(1);
        }
    }

    @Test
    public void testLogsChronologicallySorted() throws Exception {

        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallType serviceCallType = serviceCallService.createServiceCallType("primer", "v1")
                    .logLevel(LogLevel.FINEST)
                    .handler("DisconnectHandler1")
                    .create();
            ServiceCall serviceCall = serviceCallType.newServiceCall().origin("Tests").create();
            serviceCall.log(LogLevel.INFO, "Info 1");
            serviceCall.log(LogLevel.INFO, "Info 2");
            serviceCall.log(LogLevel.INFO, "Info 3");

            List<ServiceCallLog> logs = serviceCall.getLogs().find();
            assertThat(logs).hasSize(3);
            assertThat(logs.get(0).getTime()).isGreaterThan(logs.get(1).getTime());
            assertThat(logs.get(1).getTime()).isGreaterThan(logs.get(2).getTime());
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.REQUIRED_FIELD + "}", property = "message")
    public void testLogOnServiceCallRequiresMessage() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallType serviceCallType = serviceCallService.createServiceCallType("primer", "v1")
                    .logLevel(LogLevel.FINEST)
                    .handler("DisconnectHandler1")
                    .create();
            ServiceCall serviceCall = serviceCallType.newServiceCall().origin("Tests").create();
            serviceCall.log(LogLevel.SEVERE, null);

            assertThat(serviceCall.getLogs().find()).hasSize(1);
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.REQUIRED_FIELD + "}", property = "message")
    public void testLogOnServiceCallRequiresNonEmptyMessage() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallType serviceCallType = serviceCallService.createServiceCallType("primer", "v1")
                    .logLevel(LogLevel.FINEST)
                    .handler("DisconnectHandler1")
                    .create();
            ServiceCall serviceCall = serviceCallType.newServiceCall().origin("Tests").create();
            serviceCall.log(LogLevel.SEVERE, "");

            assertThat(serviceCall.getLogs().find()).hasSize(1);
        }
    }

    @Test
    public void testLogOnServiceCallBelowLevel() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallType serviceCallType = serviceCallService.createServiceCallType("primer", "v1")
                    .logLevel(LogLevel.FINE)
                    .handler("DisconnectHandler1")
                    .create();
            ServiceCall serviceCall = serviceCallType.newServiceCall().origin("Tests").create();
            serviceCall.log(LogLevel.FINE, "Boe");
            serviceCall.log(LogLevel.FINEST, "Baa"); // too low
            serviceCall.log(LogLevel.SEVERE, "Bee");

            assertThat(serviceCall.getLogs().find()).hasSize(2);
        }
    }


}
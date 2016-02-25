package com.elster.jupiter.servicecall;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.impl.FakeTypeOneCustomPropertySet;
import com.elster.jupiter.servicecall.impl.MessageSeeds;
import com.elster.jupiter.servicecall.impl.ServiceCallModule;
import com.elster.jupiter.servicecall.impl.ServiceCallServiceImpl;
import com.elster.jupiter.servicecall.impl.TranslationKeys;
import com.elster.jupiter.servicecall.impl.example.DisconnectHandler;
import com.elster.jupiter.servicecall.impl.example.ServiceCallTypeDomainExtension;
import com.elster.jupiter.servicecall.impl.example.ServiceCallTypeOneCustomPropertySet;
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
import org.osgi.service.log.LogService;

import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;


@RunWith(MockitoJUnitRunner.class)
public class ServiceCallIT {

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private final Instant now = ZonedDateTime.of(2016, 1, 8, 10, 0, 0, 0, ZoneId.of("UTC")).toInstant();
    private Injector injector;

    private NlsService nlsService;
    private TransactionService transactionService;
    private MessageService messageService;
    private ServiceCallService serviceCallService;

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
                nlsService = injector.getInstance(NlsService.class);
                customPropertySetService = injector.getInstance(CustomPropertySetService.class);
                messageService = injector.getInstance(MessageService.class);
                serviceCallService = injector.getInstance(ServiceCallService.class);
                serviceCallTypeOneCustomPropertySet = injector.getInstance(ServiceCallTypeOneCustomPropertySet.class);
                fake = injector.getInstance(FakeTypeOneCustomPropertySet.class);
                new DisconnectHandler((ServiceCallServiceImpl) serviceCallService);
                return null;
            }
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testInitServiceCallCreatesDefaultFSM() {
        try (TransactionContext context = transactionService.getContext()) {
            Optional<ServiceCallLifeCycle> serviceCallLifeCycle = serviceCallService.getServiceCallLifeCycle(TranslationKeys.DEFAULT_SERVICE_CALL_LIFE_CYCLE_NAME.getKey());
            assertThat(serviceCallLifeCycle).isPresent();
        }
    }

    @Test
    public void testInitServiceCallGetDefaultLifeCycle() {
        try (TransactionContext context = transactionService.getContext()) {
            Optional<ServiceCallLifeCycle> serviceCallLifeCycle = serviceCallService.getDefaultServiceCallLifeCycle();
            assertThat(serviceCallLifeCycle).isPresent();
        }
    }

    @Test
    @Expected(value = UnderlyingSQLFailedException.class)
    public void testCanNotCreateDuplicateServiceCallTypes() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            serviceCallService.createServiceCallType("primer", "v1").handler("DisconnectHandler1").logLevel(LogLevel.INFO).add();
            serviceCallService.createServiceCallType("primer", "v1").handler("DisconnectHandler1").logLevel(LogLevel.INFO).add();
            fail("Should have been prevented: duplication");
        }
    }

    @Test
    public void testCanCreateDuplicateServiceCallTypeWithName() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            serviceCallService.createServiceCallType("primer", "v1").handler("DisconnectHandler1").logLevel(LogLevel.INFO).add();
            serviceCallService.createServiceCallType("primer", "v2").handler("DisconnectHandler1").logLevel(LogLevel.INFO).add();
        }
    }

    @Test
    public void testCreateServiceCallTypeWithCustomLogLevel() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallType serviceCallType = serviceCallService.createServiceCallType("primer", "v1").handler("DisconnectHandler1").logLevel(LogLevel.INFO).add();

            List<ServiceCallType> serviceCallTypes = serviceCallService.getServiceCallTypes().find();
            assertThat(serviceCallTypes).hasSize(1);
            assertThat(serviceCallTypes.get(0).getName()).isEqualTo("primer");
            assertThat(serviceCallTypes.get(0).getVersionName()).isEqualTo("v1");
            assertThat(serviceCallTypes.get(0).getLogLevel()).isEqualTo(LogLevel.INFO);
            assertThat(serviceCallTypes.get(0).getStatus()).isEqualTo(Status.ACTIVE);
        }

    }

    @Test
    @ExpectedConstraintViolation(property = "serviceCallHandler", messageId = "{" + MessageSeeds.Constants.REQUIRED_FIELD + "}", strict = false)
    public void testCreateServiceCallTypeWithoutHandler() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallType serviceCallType = serviceCallService.createServiceCallType("primer", "v1").logLevel(LogLevel.INFO).add();
        }
    }

    @Test
    @ExpectedConstraintViolation(property = "serviceCallHandler", messageId = "{" + MessageSeeds.Constants.UNKNOWN_HANDLER + "}")
    public void testCreateServiceCallTypeWithUnknownHandler() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallType serviceCallType = serviceCallService.createServiceCallType("primer", "v1")
                    .handler("BLABLALBA")
                    .logLevel(LogLevel.INFO)
                    .add();
        }
    }

    @Test
    public void testCreateServiceCallTypeWithDefaultLogLevel() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallType serviceCallType = serviceCallService.createServiceCallType("primer", "v1").handler("DisconnectHandler1").add();

            List<ServiceCallType> serviceCallTypes = serviceCallService.getServiceCallTypes().find();
            assertThat(serviceCallTypes).hasSize(1);
            assertThat(serviceCallTypes.get(0).getName()).isEqualTo("primer");
            assertThat(serviceCallTypes.get(0).getVersionName()).isEqualTo("v1");
            assertThat(serviceCallTypes.get(0).getLogLevel()).isEqualTo(LogLevel.WARNING);
            assertThat(serviceCallTypes.get(0).getStatus()).isEqualTo(Status.ACTIVE);
        }
    }

    @Test
    public void testCreateServiceCallTypeWithCustomPropertySet() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySet(ServiceCallTypeDomainExtension.class
                    .getName()).get();
            ServiceCallType serviceCallType = serviceCallService.createServiceCallType("CustomTest", "CustomVersion")
                    .customPropertySet(customPropertySet)
                    .handler("DisconnectHandler1")
                    .add();

            List<ServiceCallType> serviceCallTypes = serviceCallService.getServiceCallTypes().find();
            assertThat(serviceCallTypes).hasSize(1);
            assertThat(serviceCallTypes.get(0).getName()).isEqualTo("CustomTest");
            assertThat(serviceCallTypes.get(0).getVersionName()).isEqualTo("CustomVersion");
            assertThat(serviceCallTypes.get(0).getCustomPropertySets()).hasSize(1);
        }
    }

    @Test
    @Expected(value = InvalidPropertySetDomainTypeException.class)
    public void testCreateServiceCallTypeWithIllegalCustomPropertySet() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            RegisteredCustomPropertySet wrongType = customPropertySetService.findActiveCustomPropertySet("com.elster.jupiter.servicecall.impl.ServiceCallLifeCycleDomainExtension")
                    .get();
            ServiceCallType serviceCallType = serviceCallService
                    .createServiceCallType("CustomTest", "CustomVersion")
                    .customPropertySet(wrongType)
                    .add();
        }
    }

    @Test
    public void testUpdateServiceCallTypeLogLevel() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            serviceCallService.createServiceCallType("primer", "v1").handler("DisconnectHandler1").add();
            Optional<ServiceCallType> serviceCallTypeReloaded = serviceCallService.findServiceCallType("primer", "v1");
            assertThat(serviceCallTypeReloaded.get().getLogLevel()).isEqualTo(LogLevel.WARNING);
            serviceCallTypeReloaded.get().setLogLevel(LogLevel.SEVERE);
            serviceCallTypeReloaded.get().save();

            Optional<ServiceCallType> serviceCallTypeReloadedAgain = serviceCallService.findServiceCallType("primer", "v1");
            assertThat(serviceCallTypeReloadedAgain).isPresent();
            assertThat(serviceCallTypeReloadedAgain.get().getLogLevel()).isEqualTo(LogLevel.SEVERE);
        }
    }

    @Test
    public void testLockServiceCallType() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallType serviceCallType = serviceCallService.createServiceCallType("primer", "v1").handler("DisconnectHandler1").add();
            Optional<ServiceCallType> serviceCallTypeReloaded = serviceCallService.findAndLockServiceCallType(serviceCallType
                    .getId(), serviceCallType.getVersion());
            assertThat(serviceCallTypeReloaded.isPresent()).isTrue();
        }
    }

    @Test
    public void testLockServiceCallTypeIncorrectVersion() throws Exception {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallType serviceCallType = serviceCallService.createServiceCallType("primer", "v1").handler("DisconnectHandler1").add();
            Optional<ServiceCallType> serviceCallTypeReloaded = serviceCallService.findAndLockServiceCallType(serviceCallType
                    .getId(), serviceCallType.getVersion() - 1);
            assertThat(serviceCallTypeReloaded).isEmpty();
        }
    }
}
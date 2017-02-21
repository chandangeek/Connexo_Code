/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fileimport.impl.FileImportServiceImpl;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.fsm.impl.StateTransitionTriggerEventTopicHandler;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Never;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.hamcrest.Matchers;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
//@Ignore // unstable test
public class ServiceCallTransitionIT {

    private static final String IMPORTER_NAME = "someImporter";
    private static final int TIMEOUT_IN_MILLIS = 60_000;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private final Instant now = ZonedDateTime.of(2016, 1, 8, 10, 0, 0, 0, ZoneId.of("UTC")).toInstant();
    private Injector injector;

    private NlsService nlsService;
    private TransactionService transactionService;
    private MessageService messageService;
    private IServiceCallService serviceCallService;
    private PropertySpecService propertySpecService;

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

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
    @Mock
    private ServiceCallHandler serviceCallHandler;
    @Mock
    private FileImporterFactory fileImporterFactory;

    private Clock clock;
    private CustomPropertySetService customPropertySetService;
    private ServiceCallType serviceCallType;
    private MyCustomPropertySet customPropertySet;
    private JsonService jsonService;
    private Thesaurus thesaurus;
    private ImportSchedule importSchedule;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(MessageInterpolator.class).toInstance(messageInterpolator);
            bind(SearchService.class).toInstance(mock(SearchService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
//            bind(ServiceCallTypeOneCustomPropertySet.class).to(ServiceCallTypeOneCustomPropertySet.class);
        }
    }

    @Before
    public void setUp() {
        when(fileImporterFactory.getName()).thenReturn(IMPORTER_NAME);
        when(fileImporterFactory.getApplicationName()).thenReturn("appName");
        when(fileImporterFactory.getDestinationName()).thenReturn("dest");

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
                    new ServiceCallModule(),
                    new FileImportModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(new Transaction<Void>() {


            private FileImportService fileImportService;
            private EventService eventService;

            @Override
            public Void perform() {
                nlsService = injector.getInstance(NlsService.class);
                thesaurus = nlsService.getThesaurus(ServiceCallServiceImpl.COMPONENT_NAME, Layer.DOMAIN);
                customPropertySetService = injector.getInstance(CustomPropertySetService.class);
                messageService = injector.getInstance(MessageService.class);
                serviceCallService = (IServiceCallService) injector.getInstance(ServiceCallService.class);
                propertySpecService = injector.getInstance(PropertySpecService.class);
                jsonService = injector.getInstance(JsonService.class);
                eventService = injector.getInstance(EventService.class);
                fileImportService = injector.getInstance(FileImportService.class);
                ((FileImportServiceImpl) fileImportService).addFileImporter(fileImporterFactory);

                customPropertySet = new MyCustomPropertySet(propertySpecService);
                customPropertySetService.addCustomPropertySet(customPropertySet);

                ((ServiceCallServiceImpl) serviceCallService).addServiceCallHandler(serviceCallHandler, ImmutableMap.of("name", "someHandler"));

                RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(customPropertySet
                        .getId()).get();

                serviceCallType = serviceCallService.createServiceCallType("primer", "v1")
                        .handler("someHandler")
                        .customPropertySet(registeredCustomPropertySet)
                        .create();

                ServiceCallStateChangeTopicHandler serviceCallStateChangeTopicHandler = new ServiceCallStateChangeTopicHandler();
                serviceCallStateChangeTopicHandler.setFiniteStateMachineService(injector.getInstance(FiniteStateMachineService.class));
                serviceCallStateChangeTopicHandler.setJsonService(jsonService);
                serviceCallStateChangeTopicHandler.setServiceCallService(serviceCallService);
                EventServiceImpl eventService = (EventServiceImpl) this.eventService;
                eventService.addTopicHandler(serviceCallStateChangeTopicHandler);
                StateTransitionTriggerEventTopicHandler stateTransitionTriggerEventTopicHandler = new StateTransitionTriggerEventTopicHandler();
                stateTransitionTriggerEventTopicHandler.setEventService(eventService);
                eventService.addTopicHandler(stateTransitionTriggerEventTopicHandler);

                importSchedule = fileImportService.newBuilder()
                        .setImportDirectory(Paths.get("./i"))
                        .setFailureDirectory(Paths.get("./f"))
                        .setProcessingDirectory(Paths.get("./p"))
                        .setSuccessDirectory(Paths.get("./s"))
                        .setScheduleExpression(Never.NEVER)
                        .setImporterName(IMPORTER_NAME)
                        .setName("name")
                        .setPathMatcher("*.*")
                        .setDestination("dest")
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
    public void transitionServiceCall() {
        when(serviceCallHandler.allowStateChange(any(), any(), any())).thenReturn(true);

        MyExtension extension = new MyExtension();
        extension.setValue(BigDecimal.valueOf(65456));

        ServiceCall serviceCall = null;
        try (TransactionContext context = transactionService.getContext()) {
            serviceCall = serviceCallType.newServiceCall()
                    .externalReference("external")
                    .origin("CST")
                    .targetObject(importSchedule)
                    .extendedWith(extension)
                    .create();
            context.commit();
        }

        serviceCall = serviceCallService.getServiceCall(serviceCall.getId()).get();

        try (TransactionContext context = transactionService.getContext()) {
            serviceCall.requestTransition(DefaultState.PENDING);
            context.commit();
        }

        SubscriberSpec messageQueue = messageService.getSubscriberSpec(ServiceCallServiceImpl.SERVICE_CALLS_DESTINATION_NAME, ServiceCallServiceImpl.SERVICE_CALLS_SUBSCRIBER_NAME)
                .get();

        try (TransactionContext context = transactionService.getContext()) {
            Message message = await().atMost(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS)
                    .until(messageQueue::receive, Matchers.any(Message.class));

            ServiceCallMessageHandler serviceCallMessageHandler = new ServiceCallMessageHandler(jsonService, serviceCallService, thesaurus);

            serviceCallMessageHandler.process(message);

            context.commit();
        }

        verify(serviceCallHandler).onStateChange(serviceCall, DefaultState.CREATED, DefaultState.PENDING);

        serviceCall = serviceCallService.getServiceCall(serviceCall.getId()).get();

        assertThat(serviceCall.getState()).isEqualTo(DefaultState.PENDING);

    }

    @Test
    public void forceFailServiceCallOnUnexpectedException() {
        when(serviceCallHandler.allowStateChange(any(), any(), any())).thenReturn(true);
        doThrow(new RuntimeException()).when(serviceCallHandler).onStateChange(any(), any(), any());

        MyExtension extension = new MyExtension();
        extension.setValue(BigDecimal.valueOf(65456));

        ServiceCall serviceCall = null;
        try (TransactionContext context = transactionService.getContext()) {
            serviceCall = serviceCallType.newServiceCall()
                    .externalReference("external")
                    .origin("CST")
                    .targetObject(importSchedule)
                    .extendedWith(extension)
                    .create();
            context.commit();
        }

        serviceCall = serviceCallService.getServiceCall(serviceCall.getId()).get();

        try (TransactionContext context = transactionService.getContext()) {
            serviceCall.requestTransition(DefaultState.PENDING);
            context.commit();
        }

        SubscriberSpec messageQueue = messageService.getSubscriberSpec(ServiceCallServiceImpl.SERVICE_CALLS_DESTINATION_NAME, ServiceCallServiceImpl.SERVICE_CALLS_SUBSCRIBER_NAME)
                .get();

        try (TransactionContext context = transactionService.getContext()) {
            Message message = await().atMost(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS)
                    .until(messageQueue::receive, Matchers.any(Message.class));

            ServiceCallMessageHandler serviceCallMessageHandler = new ServiceCallMessageHandler(jsonService, serviceCallService, thesaurus);

            serviceCallMessageHandler.process(message);

            context.commit();
        }

        verify(serviceCallHandler).onStateChange(serviceCall, DefaultState.CREATED, DefaultState.PENDING);

        serviceCall = serviceCallService.getServiceCall(serviceCall.getId()).get();

        assertThat(serviceCall.getState()).isEqualTo(DefaultState.FAILED);

    }

    @Test
    public void transitionParentServiceCallIfChildFailsOnUnexpectedException() {
        when(serviceCallHandler.allowStateChange(any(), any(), any())).thenReturn(true);
        doThrow(new RuntimeException()).when(serviceCallHandler).onStateChange(any(), any(), any());

        MyExtension extension = new MyExtension();
        extension.setValue(BigDecimal.valueOf(65456));

        ServiceCall parentServiceCall = null;
        ServiceCall serviceCall = null;
        try (TransactionContext context = transactionService.getContext()) {
            parentServiceCall = serviceCallType.newServiceCall()
                    .externalReference("external")
                    .origin("CST")
                    .targetObject(importSchedule)
                    .extendedWith(extension)
                    .create();
            serviceCall = parentServiceCall.newChildCall(serviceCallType)
                    .origin("CST")
                    .targetObject(importSchedule)
                    .extendedWith(extension)
                    .create();
            ((ServiceCallImpl) parentServiceCall).setState(DefaultState.WAITING);
            parentServiceCall.save();
            context.commit();
        }

        serviceCall = serviceCallService.getServiceCall(serviceCall.getId()).get();

        try (TransactionContext context = transactionService.getContext()) {
            serviceCall.requestTransition(DefaultState.PENDING);
            context.commit();
        }

        SubscriberSpec messageQueue = messageService.getSubscriberSpec(ServiceCallServiceImpl.SERVICE_CALLS_DESTINATION_NAME, ServiceCallServiceImpl.SERVICE_CALLS_SUBSCRIBER_NAME)
                .get();

        try (TransactionContext context = transactionService.getContext()) {
            Message message = await().atMost(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS)
                    .until(messageQueue::receive, Matchers.any(Message.class));

            ServiceCallMessageHandler serviceCallMessageHandler = new ServiceCallMessageHandler(jsonService, serviceCallService, thesaurus);

            serviceCallMessageHandler.process(message);

            context.commit();
        }

        verify(serviceCallHandler).onStateChange(serviceCall, DefaultState.CREATED, DefaultState.PENDING);
        verify(serviceCallHandler).onChildStateChange(parentServiceCall, serviceCall, DefaultState.CREATED, DefaultState.FAILED);

        serviceCall = serviceCallService.getServiceCall(serviceCall.getId()).get();

        assertThat(serviceCall.getState()).describedAs("Child should be FAILED").isEqualTo(DefaultState.FAILED);

        // This assumes the parent *should* not change state if a child fails, of course, this decision depends on the actual implementation of the ServiceCallHandler
        assertThat(parentServiceCall.getState()).isEqualTo(DefaultState.WAITING);

    }

    @Test
    public void cancelServiceCall() {
        when(serviceCallHandler.allowStateChange(any(), any(), any())).thenReturn(true);

        MyExtension extension = new MyExtension();
        extension.setValue(BigDecimal.valueOf(65456));

        ServiceCall serviceCall;
        try (TransactionContext context = transactionService.getContext()) {
            serviceCall = serviceCallType.newServiceCall()
                    .externalReference("external")
                    .origin("CST")
                    .targetObject(importSchedule)
                    .extendedWith(extension)
                    .create();
            context.commit();
        }

        serviceCall = serviceCallService.getServiceCall(serviceCall.getId()).get();

        try (TransactionContext context = transactionService.getContext()) {
            serviceCall.requestTransition(DefaultState.PENDING);
            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            serviceCall.requestTransition(DefaultState.CANCELLED);
            context.commit();
        }

        SubscriberSpec messageQueue = messageService.getSubscriberSpec(ServiceCallServiceImpl.SERVICE_CALLS_DESTINATION_NAME, ServiceCallServiceImpl.SERVICE_CALLS_SUBSCRIBER_NAME)
                .get();

        try (TransactionContext context = transactionService.getContext()) {
            Message message = await().atMost(500, TimeUnit.MILLISECONDS)
                    .until(messageQueue::receive, Matchers.any(Message.class));

            ServiceCallMessageHandler serviceCallMessageHandler = new ServiceCallMessageHandler(jsonService, serviceCallService, thesaurus);

            serviceCallMessageHandler.process(message);

            context.commit();
        }

        verify(serviceCallHandler).onStateChange(serviceCall, DefaultState.PENDING, DefaultState.CANCELLED);

        serviceCall = serviceCallService.getServiceCall(serviceCall.getId()).get();

        assertThat(serviceCall.getState()).isEqualTo(DefaultState.CANCELLED);

    }

    @Test
    public void canCancelServiceCall() {
        when(serviceCallHandler.allowStateChange(any(), any(), any())).thenReturn(true);

        MyExtension extension = new MyExtension();
        extension.setValue(BigDecimal.valueOf(65456));

        ServiceCall serviceCall;
        try (TransactionContext context = transactionService.getContext()) {
            serviceCall = serviceCallType.newServiceCall()
                    .externalReference("external")
                    .origin("CST")
                    .targetObject(importSchedule)
                    .extendedWith(extension)
                    .create();
            context.commit();
        }

        serviceCall = serviceCallService.getServiceCall(serviceCall.getId()).get();

        assertThat(serviceCall.canTransitionTo(DefaultState.CANCELLED)).isTrue();

        try (TransactionContext context = transactionService.getContext()) {
            serviceCall.requestTransition(DefaultState.REJECTED);
            context.commit();
        }

        assertThat(serviceCall.canTransitionTo(DefaultState.CANCELLED)).isFalse();
    }

    static class MyExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

        private Reference<ServiceCall> serviceCall = ValueReference.absent();
        private BigDecimal value;

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }

        @Override
        public void copyFrom(ServiceCall domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
            serviceCall.set(domainInstance);
            value = (BigDecimal) propertyValues.getProperty("value");
        }

        @Override
        public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
            propertySetValues.setProperty("value", value);
        }

        @Override
        public void validateDelete() {
        }

        public ServiceCall getServiceCall() {
            return serviceCall.get();
        }
    }

    private static class MyCustomPropertySet implements CustomPropertySet<ServiceCall, MyExtension> {

        private final PropertySpecService propertySpecService;

        @Inject
        private MyCustomPropertySet(PropertySpecService propertySpecService) {
            this.propertySpecService = propertySpecService;
        }

        @Override
        public String getName() {
            return "MyCustomPropertySet";
        }

        @Override
        public Class<ServiceCall> getDomainClass() {
            return ServiceCall.class;
        }

        @Override
        public String getDomainClassDisplayName() {
            return this.getDomainClass().getName();
        }

        @Override
        public PersistenceSupport<ServiceCall, MyExtension> getPersistenceSupport() {
            return new PersistenceSupport<ServiceCall, MyExtension>() {
                @Override
                public String application() {
                    return "Example";
                }

                @Override
                public String componentName() {
                    return "CST";
                }

                @Override
                public String tableName() {
                    return "CST_VALUE";
                }

                @Override
                public String domainFieldName() {
                    return "serviceCall";
                }

                @Override
                public String domainForeignKeyName() {
                    return "CST_FK_001";
                }

                @Override
                public Class<MyExtension> persistenceClass() {
                    return MyExtension.class;
                }

                @Override
                public Optional<Module> module() {
                    return Optional.empty();
                }

                @Override
                public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
                    return Collections.emptyList();
                }

                @Override
                public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
                    table.column("VALUE").number().map("value").add();
                }
            };
        }

        @Override
        public boolean isRequired() {
            return true;
        }

        @Override
        public boolean isVersioned() {
            return false;
        }

        @Override
        public Set<ViewPrivilege> defaultViewPrivileges() {
            return Collections.emptySet();
        }

        @Override
        public Set<EditPrivilege> defaultEditPrivileges() {
            return Collections.emptySet();
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            TranslationKey translationKey = mock(TranslationKey.class);

            PropertySpec spec = propertySpecService.bigDecimalSpec()
                    .named("value", translationKey)
                    .describedAs(translationKey)
                    .fromThesaurus(NlsModule.FakeThesaurus.INSTANCE)
                    .finish();
            return Collections.singletonList(spec);
        }
    }
}

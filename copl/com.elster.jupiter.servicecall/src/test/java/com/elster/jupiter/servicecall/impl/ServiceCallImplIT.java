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
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fileimport.impl.FileImportServiceImpl;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
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
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.impl.example.DisconnectHandler;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.time.Never;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceCallImplIT {

    private static final String IMPORTER_NAME = "someImporter";
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
    private ServiceCallType serviceCallTypeTwo;
    private MyCustomPropertySet customPropertySet;
    private FileImportService fileImportService;
    private ImportSchedule importSchedule;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(MessageInterpolator.class).toInstance(messageInterpolator);
            bind(SearchService.class).toInstance(mock(SearchService.class));
//            bind(ServiceCallTypeOneCustomPropertySet.class).to(ServiceCallTypeOneCustomPropertySet.class);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
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


            @Override
            public Void perform() {
                nlsService = injector.getInstance(NlsService.class);
                customPropertySetService = injector.getInstance(CustomPropertySetService.class);
                messageService = injector.getInstance(MessageService.class);
                serviceCallService = injector.getInstance(IServiceCallService.class);
                propertySpecService = injector.getInstance(PropertySpecService.class);
                fileImportService = injector.getInstance(FileImportService.class);

                ((FileImportServiceImpl) fileImportService).addFileImporter(fileImporterFactory);

                new DisconnectHandler(serviceCallService);


                customPropertySet = new MyCustomPropertySet(propertySpecService);
                customPropertySetService.addCustomPropertySet(customPropertySet);

                ((ServiceCallServiceImpl) serviceCallService).addServiceCallHandler(serviceCallHandler, ImmutableMap.of("name", "someHandler"));

                RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(customPropertySet
                        .getId()).get();

                serviceCallType = serviceCallService.createServiceCallType("primer", "v1")
                        .customPropertySet(registeredCustomPropertySet)
                        .handler("DisconnectHandler1")
                        .create();

                serviceCallTypeTwo = serviceCallService.createServiceCallType("second", "v2")
                        .handler("DisconnectHandler1")
                        .create();

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
    public void createAServiceCall() {
        MyPersistentExtension extension = new MyPersistentExtension();
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

        assertThat(serviceCall.getState()).isEqualTo(DefaultState.CREATED);
        assertThat(serviceCall.getExternalReference()).contains("external");
        assertThat(serviceCall.getOrigin()).contains("CST");
        assertThat((Optional<Object>) serviceCall.getTargetObject()).contains(importSchedule);

        extension = serviceCall.getExtensionFor(customPropertySet).get();

        assertThat(extension.getServiceCall()).isEqualTo(serviceCall);
        assertThat(extension.getValue()).isEqualTo(BigDecimal.valueOf(65456));
    }

    @Test
    public void updateAServiceCall() {
        MyPersistentExtension extension = new MyPersistentExtension();
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

        extension = serviceCall.getExtensionFor(customPropertySet).get();

        extension.setValue(BigDecimal.valueOf(1999));

        try (TransactionContext context = transactionService.getContext()) {
            serviceCall.update(extension);
            context.commit();
        }

        serviceCall = serviceCallService.getServiceCall(serviceCall.getId()).get();

        extension = serviceCall.getExtensionFor(customPropertySet).get();

        assertThat(extension.getValue()).isEqualTo(BigDecimal.valueOf(1999));
    }

    @Test
    public void createAServiceCallWithAChild() {
        MyPersistentExtension parentExtension = new MyPersistentExtension();
        parentExtension.setValue(BigDecimal.valueOf(65456));
        MyPersistentExtension extension = new MyPersistentExtension();
        extension.setValue(BigDecimal.valueOf(65456));

        ServiceCall parentServiceCall = null;
        ServiceCall serviceCall = null;

        try (TransactionContext context = transactionService.getContext()) {
            parentServiceCall = serviceCallType.newServiceCall()
                    .externalReference("external")
                    .origin("CST")
                    .extendedWith(parentExtension)
                    .create();
            serviceCall = parentServiceCall.newChildCall(serviceCallType)
                    .externalReference("externalChild")
                    .origin("CSTchild")
                    .targetObject(importSchedule)
                    .extendedWith(extension)
                    .create();
            context.commit();
        }

        Map<DefaultState, Long> childrenStatus = serviceCallService.getChildrenStatus(parentServiceCall.getId());
        serviceCall = serviceCallService.getServiceCall(serviceCall.getId()).get();
        parentServiceCall = serviceCallService.getServiceCall(parentServiceCall.getId()).get();
        assertThat(serviceCall.getState()).isEqualTo(DefaultState.CREATED);
        assertThat(serviceCall.getExternalReference()).contains("externalChild");
        assertThat(serviceCall.getOrigin()).contains("CSTchild");
        assertThat((Optional<Object>) serviceCall.getTargetObject()).contains(importSchedule);
        assertThat(serviceCall.getParent()).contains(parentServiceCall);
        assertThat(childrenStatus.size()).isEqualTo(1);
        assertThat(childrenStatus.get(DefaultState.CREATED)).isEqualTo(1);

        extension = serviceCall.getExtensionFor(customPropertySet).get();

        assertThat(extension.getServiceCall()).isEqualTo(serviceCall);
        assertThat(extension.getValue()).isEqualTo(BigDecimal.valueOf(65456));
    }

    @Test
    public void createAServiceCallWithoutTargetObject() {
        MyPersistentExtension extension = new MyPersistentExtension();
        extension.setValue(BigDecimal.valueOf(65456));

        ServiceCall serviceCall = null;
        try (TransactionContext context = transactionService.getContext()) {
            serviceCall = serviceCallType.newServiceCall()
                    .externalReference("external")
                    .origin("CST")
                    .extendedWith(extension)
                    .create();
            context.commit();
        }

        serviceCall = serviceCallService.getServiceCall(serviceCall.getId()).get();

        assertThat(serviceCall.getState()).isEqualTo(DefaultState.CREATED);
        assertThat(serviceCall.getExternalReference()).contains("external");
        assertThat(serviceCall.getOrigin()).contains("CST");
        assertThat((Optional<Object>) serviceCall.getTargetObject()).isEmpty();

        extension = serviceCall.getExtensionFor(customPropertySet).get();

        assertThat(extension.getServiceCall()).isEqualTo(serviceCall);
        assertThat(extension.getValue()).isEqualTo(BigDecimal.valueOf(65456));
    }

    @Test
    public void testServiceCallFinderSorting() {
        ServiceCall serviceCallOne = null;
        ServiceCall serviceCallTwo = null;
        ServiceCall serviceCallThree = null;

        MyPersistentExtension extension = new MyPersistentExtension();
        extension.setValue(BigDecimal.valueOf(65456));
        try (TransactionContext context = transactionService.getContext()) {
            serviceCallOne = serviceCallType.newServiceCall()
                    .externalReference("external")
                    .origin("CST")
                    .extendedWith(extension)
                    .create();
            serviceCallTwo = serviceCallOne.newChildCall(serviceCallType)
                    .externalReference("external")
                    .origin("CST")
                    .extendedWith(extension)
                    .create();
            serviceCallThree = serviceCallType.newServiceCall()
                    .externalReference("external")
                    .origin("CST")
                    .extendedWith(extension)
                    .create();

            context.commit();
        }

        List<ServiceCall> result = serviceCallService.getServiceCallFinder().find();
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo(serviceCallOne);
        assertThat(result.get(1)).isEqualTo(serviceCallThree);
        assertThat(result.get(2)).isEqualTo(serviceCallTwo);
    }

   /* @Test
    public void testServiceCallFinderFiltering() {
        ServiceCall serviceCallOne = null;
        ServiceCall serviceCallTwo = null;
        ServiceCall serviceCallThree = null;

        MyPersistentExtension extension = new MyPersistentExtension();
        extension.setValue(BigDecimal.valueOf(65456));

        try (TransactionContext context = transactionService.getContext()) {
            serviceCallOne = serviceCallTypeTwo.newServiceCall()
                    .externalReference("external")
                    .origin("CST")
                    .create();
            serviceCallTwo = serviceCallOne.newChildCall(serviceCallType)
                    .externalReference("external")
                    .origin("CST")
                    .extendedWith(extension)
                    .create();
            serviceCallThree = serviceCallTypeTwo.newServiceCall()
                    .externalReference("SAP")
                    .origin("CST")
                    .create();

            context.commit();
        }

        ServiceCallFilter filter = new ServiceCallFilterImpl();
        filter.setParent(serviceCallOne);

        List<ServiceCall> result = serviceCallService.getServiceCallFinder(filter)
                .find();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(serviceCallTwo);

        filter = new ServiceCallFilterImpl();
        filter.setParent(serviceCallThree);

        result = serviceCallService.getServiceCallFinder(filter)
                .find();
        assertThat(result).hasSize(0);

        filter = new ServiceCallFilterImpl();
        filter.setReference("extern*");

        result = serviceCallService.getServiceCallFinder(filter)
                .find();
        assertThat(result).hasSize(2);
        assertThat(result).contains(serviceCallOne, serviceCallTwo);

        List<String> type = new ArrayList<>();
        type.add("second");

        filter = new ServiceCallFilterImpl();
        filter.setTypes(type);

        result = serviceCallService.getServiceCallFinder(filter)
                .find();
        assertThat(result).hasSize(2);
        assertThat(result).contains(serviceCallOne, serviceCallThree);



        result = serviceCallOne.findChildren(new ServiceCallFilterImpl()).find();
        assertThat(result).hasSize(1);
        assertThat(result).contains(serviceCallTwo);
    }*/

    static class MyPersistentExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

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

    private static class MyCustomPropertySet implements CustomPropertySet<ServiceCall, MyPersistentExtension> {

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
        public PersistenceSupport<ServiceCall, MyPersistentExtension> getPersistenceSupport() {
            return new PersistenceSupport<ServiceCall, MyPersistentExtension>() {
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
                public Class<MyPersistentExtension> persistenceClass() {
                    return MyPersistentExtension.class;
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

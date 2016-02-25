package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
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
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
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
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
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

@RunWith(MockitoJUnitRunner.class)
public class ServiceCallImplIT {

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private final Instant now = ZonedDateTime.of(2016, 1, 8, 10, 0, 0, 0, ZoneId.of("UTC")).toInstant();
    private Injector injector;

    private NlsService nlsService;
    private TransactionService transactionService;
    private MessageService messageService;
    private ServiceCallService serviceCallService;
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

    private Clock clock;
    private CustomPropertySetService customPropertySetService;
    private ServiceCallType serviceCallType;
    private MyCustomPropertySet customPropertySet;
    private Person person;
    private PartyService partyService;

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
                    new ServiceCallModule(),
                    new PartyModule()
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
                propertySpecService = injector.getInstance(PropertySpecService.class);
                partyService = injector.getInstance(PartyService.class);


                customPropertySet = new MyCustomPropertySet(propertySpecService);
                customPropertySetService.addCustomPropertySet(customPropertySet);

                RegisteredCustomPropertySet registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(customPropertySet
                        .getId()).get();

                serviceCallType = serviceCallService.createServiceCallType("primer", "v1")
                        .handler("someHandler")
                        .customPropertySet(registeredCustomPropertySet)
                        .create();

                person = partyService.newPerson("Test", "test")
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
                    .targetObject(person)
                    .extendedWith(extension)
                    .create();
            context.commit();
        }

        serviceCall = serviceCallService.getServiceCall(serviceCall.getId()).get();

        assertThat(serviceCall.getState()).isEqualTo(DefaultState.CREATED);
        assertThat(serviceCall.getExternalReference()).contains("external");
        assertThat(serviceCall.getOrigin()).contains("CST");
        assertThat((Optional<Object>) serviceCall.getTargetObject()).contains(person);

        extension = serviceCall.getExtensionFor(customPropertySet).get();

        assertThat(extension.getServiceCall()).isEqualTo(serviceCall);
        assertThat(extension.getValue()).isEqualTo(BigDecimal.valueOf(65456));
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
                    .targetObject(person)
                    .extendedWith(extension)
                    .create();
            context.commit();
        }

        serviceCall = serviceCallService.getServiceCall(serviceCall.getId()).get();
        parentServiceCall = serviceCallService.getServiceCall(parentServiceCall.getId()).get();

        assertThat(serviceCall.getState()).isEqualTo(DefaultState.CREATED);
        assertThat(serviceCall.getExternalReference()).contains("externalChild");
        assertThat(serviceCall.getOrigin()).contains("CSTchild");
        assertThat((Optional<Object>) serviceCall.getTargetObject()).contains(person);
        assertThat(serviceCall.getParent()).contains(parentServiceCall);

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

    static class MyPersistentExtension implements PersistentDomainExtension<ServiceCall> {

        private Reference<ServiceCall> serviceCall = ValueReference.absent();
        private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = ValueReference.absent();
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
        public PersistenceSupport<ServiceCall, MyPersistentExtension> getPersistenceSupport() {
            return new PersistenceSupport<ServiceCall, MyPersistentExtension>() {
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

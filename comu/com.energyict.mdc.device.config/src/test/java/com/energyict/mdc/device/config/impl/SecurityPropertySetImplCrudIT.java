/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.PkiModule;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.GrantPrivilege;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.common.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigConflictMappingHandler;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ValueType;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLAuthenticationLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLEncryptionLevelAdapter;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.ValueFactory;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.energyict.mdc.common.protocol.security.DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecurityPropertySetImplCrudIT {
    @Rule
    public final TestRule transactional = new TransactionalRule(transactionService);
    @Rule
    public TestRule itWillHitTheFan = new ExpectedConstraintViolationRule();

    private static InMemoryBootstrapModule bootstrapModule;
    private static EventAdmin eventAdmin;
    private static BundleContext bundleContext;
    private static TransactionService transactionService;
    private static ProtocolPluggableService protocolPluggableService;
    private static SecurityManagementService securityManagementService;
    private static DeviceConfigurationServiceImpl deviceConfigurationService;
    private static Injector injector;
    private static SpyEventService eventService;

    @Mock
    private MyDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private AuthenticationDeviceAccessLevel authLevel, authLevel2;
    @Mock
    private EncryptionDeviceAccessLevel encLevel;
    @Mock
    private PropertySpec spec1, spec2, spec3;
    @Mock
    private PropertySpec clientPropertySpec;
    @Mock
    private ValueFactory valueFactory;

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(IdentificationService.class).toInstance(mock(IdentificationService.class));
            bind(CustomPropertySetInstantiatorService.class).toInstance(mock(CustomPropertySetInstantiatorService.class));
            bind(DeviceMessageSpecificationService.class).toInstance(mock(DeviceMessageSpecificationService.class));
            bind(HsmEnergyService.class).toInstance(mock(HsmEnergyService.class));
            bind(HsmEncryptionService.class).toInstance(mock(HsmEncryptionService.class));
            bind(TokenService.class).toInstance(mock(TokenService.class));
        }
    }

    @BeforeClass
    public static void initializeDatabase() {
        initializeStaticMocks();
        User principal = mock(User.class);
        when(principal.getName()).thenReturn(SecurityPropertySetImplCrudIT.class.getSimpleName());
        when(principal.hasPrivilege(anyString(), anyString())).thenReturn(true);
        GrantPrivilege superGrant = mock(GrantPrivilege.class);
        when(superGrant.canGrant(any())).thenReturn(true);
        Group superUser = mock(Group.class);
        when(superUser.getPrivileges()).thenReturn(ImmutableMap.of("", Collections.singletonList(superGrant)));
        when(principal.getGroups()).thenReturn(Collections.singletonList(superUser));
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    bootstrapModule,
                    new ThreadSecurityModule(principal),
                    new EventsModule(),
                    new PubSubModule(),
                    new TransactionModule(false),
                    new UtilModule(),
                    new PkiModule(),
                    new NlsModule(),
                    new DomainUtilModule(),
                    new PartyModule(),
                    new UserModule(),
                    new IdsModule(),
                    new BpmModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new MeteringModule(),
                    new InMemoryMessagingModule(),
                    new EventsModule(),
                    new H2OrmModule(),
                    new DataVaultModule(),
                    new MdcReadingTypeUtilServiceModule(),
                    new MasterDataModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule(),
                    new PluggableModule(),
                    new ProtocolApiModule(),
                    new TasksModule(),
                    new KpiModule(),
                    new ValidationModule(),
                    new EstimationModule(),
                    new FiniteStateMachineModule(),
                    new DeviceLifeCycleConfigurationModule(),
                    new MeteringGroupsModule(),
                    new SearchModule(),
                    new TaskModule(),
                    new DeviceConfigurationModule(),
                    new EngineModelModule(),
                    new IssuesModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule(),
                    new PluggableModule(),
                    new SchedulingModule(),
                    new TimeModule(),
                    new CustomPropertySetsModule(),
                    new CalendarModule(),
                    new WebServicesModule(),
                    new AuditServiceModule(),
                    new FileImportModule());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            eventService = new SpyEventService(injector.getInstance(EventService.class));
            FiniteStateMachineService finiteStateMachineService = injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringService.class);
            injector.getInstance(MasterDataService.class);
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(TaskService.class);
            injector.getInstance(ValidationService.class);
            injector.getInstance(DeviceLifeCycleConfigurationService.class);
            injector.getInstance(PluggableService.class);
            securityManagementService = injector.getInstance(SecurityManagementService.class);
            deviceConfigurationService = new DeviceConfigurationServiceImpl(
                    injector.getInstance(OrmService.class),
                    injector.getInstance(Clock.class),
                    injector.getInstance(ThreadPrincipalService.class),
                    eventService,
                    injector.getInstance(NlsService.class),
                    injector.getInstance(com.elster.jupiter.properties.PropertySpecService.class),
                    injector.getInstance(MeteringService.class),
                    injector.getInstance(MdcReadingTypeUtilService.class),
                    injector.getInstance(UserService.class),
                    injector.getInstance(PluggableService.class),
                    protocolPluggableService,
                    injector.getInstance(EngineConfigurationService.class),
                    injector.getInstance(SchedulingService.class),
                    injector.getInstance(ValidationService.class),
                    injector.getInstance(EstimationService.class),
                    injector.getInstance(MasterDataService.class),
                    finiteStateMachineService,
                    injector.getInstance(DeviceLifeCycleConfigurationService.class),
                    injector.getInstance(CalendarService.class),
                    injector.getInstance(CustomPropertySetService.class),
                    injector.getInstance(DataVaultService.class),
                    UpgradeModule.FakeUpgradeService.getInstance(),
                    injector.getInstance(DeviceMessageSpecificationService.class),
                    securityManagementService,
                    injector.getInstance(MeteringTranslationService.class),
                    injector.getInstance(MessageService.class));
            ctx.commit();
        }
        enhanceEventServiceForConflictCalculation();
    }

    private static void initializeStaticMocks() {
        eventAdmin = mock(EventAdmin.class);
        bundleContext = mock(BundleContext.class);
        bootstrapModule = new InMemoryBootstrapModule();
        protocolPluggableService = mock(ProtocolPluggableService.class);
        when(protocolPluggableService.adapt(any(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return UPLAuthenticationLevelAdapter.adaptTo((com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel) args[0], null);
        });
        when(protocolPluggableService.adapt(any(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return UPLEncryptionLevelAdapter.adaptTo((com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel) args[0], null);
        });
    }

    @AfterClass
    public static void tearDown() {
        bootstrapModule.deactivate();
    }

    private static void enhanceEventServiceForConflictCalculation() {
        doAnswer(invocationOnMock -> {
            LocalEvent localEvent = mock(LocalEvent.class);
            com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
            when(eventType.getTopic()).thenReturn((String) invocationOnMock.getArguments()[0]);
            when(localEvent.getType()).thenReturn(eventType);
            when(localEvent.getSource()).thenReturn(invocationOnMock.getArguments()[1]);
            injector.getInstance(DeviceConfigConflictMappingHandler.class).onEvent(localEvent);
            return null;
        }).when(eventService.getSpy()).postEvent(any(), any());
    }

    @Before
    public void initializeMocks() throws InvalidValueException, PropertyValidationException {
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(protocolPluggableService.findDeviceProtocolPluggableClass(anyLong())).thenReturn(Optional.of(deviceProtocolPluggableClass));
        Mockito.when(valueFactory.fromStringValue(Mockito.any(String.class)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArguments();
                    return args[0];
                });
        when(valueFactory.toStringValue(any(String.class))).thenAnswer(invocation -> invocation.getArgumentAt(0, String.class));
        when(clientPropertySpec.getValueFactory()).thenReturn(valueFactory);
        when(valueFactory.getValueTypeName()).thenReturn(ValueType.INTEGER.getUplClassName());
        Mockito.when(clientPropertySpec.validateValue(Mockito.any(Object.class)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArguments();
                    return args.length == 1 && args[0] != null;
                });
        when(deviceProtocol.getClientSecurityPropertySpec()).thenReturn(Optional.of(clientPropertySpec));
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authLevel, authLevel2));
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Collections.singletonList(encLevel));
        when(spec1.getName()).thenReturn("spec1");
        when(spec2.getName()).thenReturn("spec2");
        when(spec3.getName()).thenReturn("spec3");
        when(spec1.getValueFactory()).thenReturn(valueFactory);
        when(spec2.getValueFactory()).thenReturn(valueFactory);
        when(spec3.getValueFactory()).thenReturn(valueFactory);
        when(authLevel.getSecurityProperties()).thenReturn(Collections.singletonList(spec1));
        when(authLevel.getId()).thenReturn(1);
        when(authLevel2.getId()).thenReturn(2);
        when(authLevel2.getSecurityProperties()).thenReturn(Arrays.asList(spec1, spec2));
        when(encLevel.getId()).thenReturn(2);
        when(encLevel.getSecurityProperties()).thenReturn(Arrays.asList(spec2, spec3));
    }

    @Test
    @Transactional
    public void testCreation() {
        SecurityPropertySet propertySet;
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();

        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());

        assertThat(found.isPresent()).isTrue();

        SecurityPropertySet reloaded = found.get();

        assertThat(reloaded.getName()).isEqualTo("Name");
        assertThat(reloaded.getClient()).isEqualTo("client");
        assertThat(reloaded.getAuthenticationDeviceAccessLevel()).isEqualTo(authLevel);
        assertThat(reloaded.getEncryptionDeviceAccessLevel()).isEqualTo(encLevel);
        assertThat(reloaded.getSecuritySuite().getId()).isEqualTo(NOT_USED_DEVICE_ACCESS_LEVEL_ID);
        assertThat(reloaded.getRequestSecurityLevel().getId()).isEqualTo(NOT_USED_DEVICE_ACCESS_LEVEL_ID);
        assertThat(reloaded.getResponseSecurityLevel().getId()).isEqualTo(NOT_USED_DEVICE_ACCESS_LEVEL_ID);
        assertThat(reloaded.getConfigurationSecurityProperties().size()).isEqualTo(1);
        assertTrue(hasMatchingConfigurationSecurityProperty(reloaded.getConfigurationSecurityProperties(), spec1.getName(), securityAccessorType));
    }

    @Test
    @Transactional
    public void cloneTest() {
        SecurityPropertySet propertySet;
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
        DeviceConfiguration clonedDeviceConfig = deviceType.newConfiguration("Clone").add();
        SecurityPropertySet clonedSecurityPropertySet = ((ServerSecurityPropertySet) propertySet).cloneForDeviceConfig(clonedDeviceConfig);

        assertThat(propertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(clonedSecurityPropertySet.getAuthenticationDeviceAccessLevel());
        assertThat(propertySet.getEncryptionDeviceAccessLevel()).isEqualTo(clonedSecurityPropertySet.getEncryptionDeviceAccessLevel());
        assertThat(propertySet.getSecuritySuite()).isEqualTo(clonedSecurityPropertySet.getSecuritySuite());
        assertThat(propertySet.getRequestSecurityLevel()).isEqualTo(clonedSecurityPropertySet.getRequestSecurityLevel());
        assertThat(propertySet.getResponseSecurityLevel()).isEqualTo(clonedSecurityPropertySet.getResponseSecurityLevel());
        assertThat(propertySet.getName()).isEqualTo(clonedSecurityPropertySet.getName());
        assertThat(propertySet.getClient()).isEqualTo(clonedSecurityPropertySet.getClient());
        assertThat(propertySet.getConfigurationSecurityProperties().size()).isEqualTo(clonedSecurityPropertySet.getConfigurationSecurityProperties().size());
        assertTrue(hasMatchingConfigurationSecurityProperty(clonedSecurityPropertySet.getConfigurationSecurityProperties(), spec1.getName(), securityAccessorType));
        assertThat(clonedSecurityPropertySet.getDeviceConfiguration().getId()).isEqualTo(clonedDeviceConfig.getId());
    }

    @Test
    @Transactional
    public void testDeletion() {
        SecurityPropertySet propertySet;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();

        deviceConfiguration.removeSecurityPropertySet(propertySet);

        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());

        assertThat(found.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testDeleteDeviceTypeDeletesSecuritySets() {
        SecurityPropertySet propertySet;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");
        ComTask testComTask = createTestComTask();
        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
        deviceConfiguration.enableComTask(testComTask, propertySet).add();
        // prepareDelete should delete everything
        deviceType.delete();

        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());

        assertThat(found.isPresent()).isFalse();
    }

    private ComTask createTestComTask() {
        TaskService taskService = injector.getInstance(TaskService.class);
        ComTask testComTask = taskService.newComTask("TestComTask");
        testComTask.save();
        return testComTask;
    }

    @Test
    @Transactional
    public void testUpdate() {
        SecurityPropertySet propertySet;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();

        SecurityPropertySet toUpdate = deviceConfiguration.getSecurityPropertySets().get(0);
        toUpdate.setAuthenticationLevelId(2);
        toUpdate.addConfigurationSecurityProperty(spec2.getName(), securityAccessorType);
        toUpdate.update();

        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());

        assertThat(found.isPresent()).isTrue();

        SecurityPropertySet reloaded = found.get();

        assertThat(reloaded.getName()).isEqualTo("Name");
        assertThat(reloaded.getClient()).isEqualTo("client");
        assertThat(reloaded.getAuthenticationDeviceAccessLevel()).isEqualTo(authLevel2);
        assertThat(reloaded.getEncryptionDeviceAccessLevel()).isEqualTo(encLevel);
        assertThat(reloaded.getSecuritySuite()).isEqualTo(propertySet.getSecuritySuite());
        assertThat(reloaded.getRequestSecurityLevel()).isEqualTo(propertySet.getRequestSecurityLevel());
        assertThat(reloaded.getResponseSecurityLevel()).isEqualTo(propertySet.getResponseSecurityLevel());
        assertThat(reloaded.getConfigurationSecurityProperties().size()).isEqualTo(2);
        assertTrue(hasMatchingConfigurationSecurityProperty(reloaded.getConfigurationSecurityProperties(), spec1.getName(), securityAccessorType));
        assertTrue(hasMatchingConfigurationSecurityProperty(reloaded.getConfigurationSecurityProperties(), spec2.getName(), securityAccessorType));
    }

    @Test
    @Transactional
    public void testUpdateConfigurationSecurityProperties() {
        SecurityPropertySet propertySet;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType1 = securityManagementService.addSecurityAccessorType("GUAK1", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();
        SecurityAccessorType securityAccessorType2 = securityManagementService.addSecurityAccessorType("GUAK2", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();
        SecurityAccessorType securityAccessorType3 = securityManagementService.addSecurityAccessorType("GUAK3", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType1)
                .addConfigurationSecurityProperty(spec2.getName(), securityAccessorType2)
                .build();

        SecurityPropertySet toUpdate = deviceConfiguration.getSecurityPropertySets().get(0);
        toUpdate.setAuthenticationLevelId(2);
        toUpdate.updateConfigurationSecurityProperty(spec1.getName(), securityAccessorType2);
        toUpdate.removeConfigurationSecurityProperty(spec2.getName());
        toUpdate.addConfigurationSecurityProperty(spec3.getName(), securityAccessorType3);
        toUpdate.update();

        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());

        assertThat(found.isPresent()).isTrue();

        SecurityPropertySet reloaded = found.get();

        assertThat(reloaded.getName()).isEqualTo("Name");
        assertThat(reloaded.getClient()).isEqualTo("client");
        assertThat(reloaded.getAuthenticationDeviceAccessLevel()).isEqualTo(authLevel2);
        assertThat(reloaded.getEncryptionDeviceAccessLevel()).isEqualTo(encLevel);
        assertThat(reloaded.getSecuritySuite()).isEqualTo(propertySet.getSecuritySuite());
        assertThat(reloaded.getRequestSecurityLevel()).isEqualTo(propertySet.getRequestSecurityLevel());
        assertThat(reloaded.getResponseSecurityLevel()).isEqualTo(propertySet.getResponseSecurityLevel());
        List<ConfigurationSecurityProperty> configurationSecurityProperties = reloaded.getConfigurationSecurityProperties();
        assertThat(configurationSecurityProperties.size()).isEqualTo(2);
        assertTrue(hasMatchingConfigurationSecurityProperty(configurationSecurityProperties, spec1.getName(), securityAccessorType2));
        assertTrue(hasMatchingConfigurationSecurityProperty(configurationSecurityProperties, spec3.getName(), securityAccessorType3));
    }

    private boolean hasMatchingConfigurationSecurityProperty(List<ConfigurationSecurityProperty> configurationSecurityProperties, String name, SecurityAccessorType securityAccessorType) {
        return configurationSecurityProperties
                .stream()
                .anyMatch(property -> property.getName().equals(name) && property.getSecurityAccessorType().equals(securityAccessorType));
    }

    @Test
    @Transactional
    public void testGetPropertySpecs() {
        SecurityPropertySet propertySet;
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(2)     // Has spec1 and spec2
                .encryptionLevel(2)         // Has spec2 and spec3
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
        Set<com.elster.jupiter.properties.PropertySpec> propertySpecs = propertySet.getPropertySpecs();

        assertThat(propertySpecs.size()).isEqualTo(3);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    public void testCreateWithoutName() {
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.createSecurityPropertySet(null)
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "name")
    public void testCreateWithEmptyName() {
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.createSecurityPropertySet("       ")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "name")
    public void testCreateWithLongName() {
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.createSecurityPropertySet("приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.NAME_UNIQUE + '}')
    public void testCreateWithDuplicateNameInSameConfiguration() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
        deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.NAME_UNIQUE + '}')
    public void testUpdateWithDuplicateNameInSameConfiguration() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("A")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
        SecurityPropertySet securityPropertySet = deviceConfiguration.createSecurityPropertySet("B")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();

        // Business method
        securityPropertySet.setName("A");
        securityPropertySet.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.INVALID_VALUE + "}")
    public void testCreateWithoutClient() {
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.createSecurityPropertySet("withoutClient")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.INVALID_VALUE + "}")
    public void testCreateWithInvalidClient() throws InvalidValueException, PropertyValidationException {
        when(clientPropertySpec.validateValue(Mockito.any())).thenReturn(false);

        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.createSecurityPropertySet("InvalidClient")
                .client("wrong_client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
    }

    @Test
    @Transactional
    public void testCreateWithDuplicateNameInOtherConfiguration() {
        DeviceType deviceType;
        SecurityPropertySet propertySet;
        String expectedName = "Name";
        deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        DeviceConfiguration deviceConfiguration1 = deviceType.newConfiguration("Normal-1").add();
        deviceConfiguration1.save();
        deviceConfiguration1.createSecurityPropertySet(expectedName)
                .client("client1")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
        DeviceConfiguration deviceConfiguration2 = deviceType.newConfiguration("Normal-2").add();
        deviceConfiguration2.save();

        // Business method
        propertySet = deviceConfiguration2.createSecurityPropertySet(expectedName)
                .client("client2")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();

        // Asserts
        assertThat(propertySet).isNotNull();
        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());
        assertThat(found.isPresent()).isTrue();
        SecurityPropertySet reloaded = found.get();
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(reloaded.getClient()).isEqualTo("client2");
    }

    @Test
    @Transactional
    public void testUpdateWithDuplicateNameInOtherConfiguration() {
        DeviceType deviceType;
        SecurityPropertySet propertySet;
        String expectedName = "Name";
        deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        DeviceConfiguration deviceConfiguration1 = deviceType.newConfiguration("Normal-1").add();
        deviceConfiguration1.save();
        deviceConfiguration1.createSecurityPropertySet(expectedName)
                .client("client1")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
        DeviceConfiguration deviceConfiguration2 = deviceType.newConfiguration("Normal-2").add();
        deviceConfiguration2.save();

        propertySet = deviceConfiguration2.createSecurityPropertySet("Other")
                .client("client2")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();

        // Business method
        propertySet.setName(expectedName);
        propertySet.update();

        // Asserts
        assertThat(propertySet).isNotNull();
        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());
        assertThat(found.isPresent()).isTrue();
        SecurityPropertySet reloaded = found.get();
        assertThat(reloaded.getName()).isEqualTo(expectedName);
        assertThat(reloaded.getClient()).isEqualTo("client2");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testAuthenticationLevelIsRequiredWhenProtocolProvidesAtLeastOneAuthenticationLevel() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .encryptionLevel(2)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testAuthenticationLevelShouldNotBeSpecifiedWhenProtocolDoesNotProvideAuthenticationLevels() {
        DeviceConfiguration deviceConfiguration;
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Collections.<AuthenticationDeviceAccessLevel>emptyList());
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testEncryptionLevelIsRequiredWhenProtocolProvidesAtLeastOneEncryptionLevel() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(1)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testEncryptionLevelShouldNotBeSpecifiedWhenProtocolDoesNotProvideEncryptionLevels() {
        DeviceConfiguration deviceConfiguration;
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Collections.<EncryptionDeviceAccessLevel>emptyList());
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.SECURITY_PROPERTY_SET_REQUIRED_PROPERTY_MISSING + "}")
    public void testCreateWithoutRequiredConfigurationSecurityProperty() {
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        when(spec1.isRequired()).thenReturn(true);
        when(spec2.isRequired()).thenReturn(false);

        DeviceConfiguration deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");
        deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec2.getName(), securityAccessorType)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.SECURITY_PROPERTY_SET_PROPERTY_NOT_IN_SPEC + "}")
    public void testCreateWithConfigurationSecurityPropertyNotInSpec() {
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        when(authLevel.getSecurityProperties()).thenReturn(Collections.emptyList());
        when(encLevel.getSecurityProperties()).thenReturn(Collections.emptyList());

        DeviceConfiguration deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");
        deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), securityAccessorType)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    public void testCreateWithInvalidConfigurationSecurityProperty() {
        DeviceType deviceType = createDeviceType("MyType");
        KeyType aes128 = securityManagementService.newSymmetricKeyType("AES128", "AES", 128).add();
        SecurityAccessorType securityAccessorType = securityManagementService.addSecurityAccessorType("GUAK", aes128).keyEncryptionMethod("SSM")
                .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS).description("general use AK").duration(TimeDuration.days(365)).add();

        DeviceConfiguration deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");
        deviceConfiguration.createSecurityPropertySet("Name")
                .client("client")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addConfigurationSecurityProperty(spec1.getName(), null) // Passing null, which will ensure PropertySpec.validateValue() fails
                .build();
    }

    private DeviceType createDeviceType(String name) {
        return deviceConfigurationService.newDeviceType(name, deviceProtocolPluggableClass);
    }

    private DeviceConfiguration createNewInactiveConfiguration(DeviceType deviceType, String name) {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration(name).add();
        deviceConfiguration.save();
        return deviceConfiguration;
    }

    public interface MyDeviceProtocolPluggableClass extends DeviceProtocolPluggableClass {
    }

}

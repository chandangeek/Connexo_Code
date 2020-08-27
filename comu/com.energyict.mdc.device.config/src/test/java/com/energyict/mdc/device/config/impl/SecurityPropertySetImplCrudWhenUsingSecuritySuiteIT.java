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
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.impl.PkiModule;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
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
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ValueType;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLAuthenticationLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLEncryptionLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLRequestSecurityLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLResponseSecurityLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLSecuritySuiteLevelAdapter;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.mdc.upl.properties.ValueFactory;
import com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.security.RequestSecurityLevel;
import com.energyict.mdc.upl.security.ResponseSecurityLevel;
import com.energyict.mdc.upl.security.SecuritySuite;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.fest.assertions.api.Assertions;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
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
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecurityPropertySetImplCrudWhenUsingSecuritySuiteIT {
    @Rule
    public final TestRule transactional = new TransactionalRule(transactionService);
    @Rule
    public TestRule itWillHitTheFan = new ExpectedConstraintViolationRule();

    private static InMemoryBootstrapModule bootstrapModule;
    private static EventAdmin eventAdmin;
    private static BundleContext bundleContext;
    private static TransactionService transactionService;
    private static ProtocolPluggableService protocolPluggableService;
    private static DeviceConfigurationServiceImpl deviceConfigurationService;
    private static Injector injector;
    private static SpyEventService eventService;

    @Mock
    private MyDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock(extraInterfaces = AdvancedDeviceProtocolSecurityCapabilities.class)
    private DeviceProtocol deviceProtocol;
    @Mock
    private AuthenticationDeviceAccessLevel authLevel, authLevel2;
    @Mock
    private EncryptionDeviceAccessLevel encLevel;
    @Mock
    private SecuritySuite securitySuite;
    @Mock
    private RequestSecurityLevel requestSecurityLevel1, requestSecurityLevel2;
    @Mock
    private ResponseSecurityLevel responseSecurityLevel1, responseSecurityLevel2;
    @Mock
    private com.energyict.mdc.upl.properties.PropertySpec spec1, spec2, spec3, spec4, spec5, spec6;
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
            bind(CustomPropertySetInstantiatorService.class).toInstance(mock(CustomPropertySetInstantiatorService.class));
            bind(DeviceMessageSpecificationService.class).toInstance(mock(DeviceMessageSpecificationService.class));
            bind(com.energyict.mdc.upl.io.SocketService.class).toInstance(mock(com.energyict.mdc.upl.io.SocketService.class));
            bind(HsmEnergyService.class).toInstance(mock(HsmEnergyService.class));
            bind(HsmEncryptionService.class).toInstance(mock(HsmEncryptionService.class));
            bind(TokenService.class).toInstance(mock(TokenService.class));
        }
    }

    @BeforeClass
    public static void initializeDatabase() {
        initializeStaticMocks();
        User principal = mock(User.class);
        when(principal.getName()).thenReturn(SecurityPropertySetImplCrudWhenUsingSecuritySuiteIT.class.getSimpleName());
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
                    new BpmModule(),
                    new NlsModule(),
                    new DomainUtilModule(),
                    new PartyModule(),
                    new UserModule(),
                    new IdsModule(),
                    new UsagePointLifeCycleConfigurationModule(),
                    new MeteringModule(),
                    new InMemoryMessagingModule(),
                    new EventsModule(),
                    new H2OrmModule(),
                    new DataVaultModule(),
                    new MdcReadingTypeUtilServiceModule(),
                    new MasterDataModule(),
                    new BpmModule(),
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
                    new FileImportModule()
            );
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
                    injector.getInstance(SecurityManagementService.class),
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
    }

    @AfterClass
    public static void tearDown() {
        bootstrapModule.deactivate();
    }

    @Before
    public void initializeMocks() throws InvalidValueException {
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(protocolPluggableService.findDeviceProtocolPluggableClass(anyLong())).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(protocolPluggableService.adapt(any(SecuritySuite.class))).thenAnswer(invocationOnMock -> UPLSecuritySuiteLevelAdapter.adaptTo(((SecuritySuite) invocationOnMock.getArguments()[0]), null));
        when(protocolPluggableService.adapt(any(AuthenticationDeviceAccessLevel.class))).thenAnswer(invocationOnMock -> UPLAuthenticationLevelAdapter.adaptTo(((AuthenticationDeviceAccessLevel) invocationOnMock
                .getArguments()[0]), null));
        when(protocolPluggableService.adapt(any(EncryptionDeviceAccessLevel.class))).thenAnswer(invocationOnMock -> UPLEncryptionLevelAdapter.adaptTo(((EncryptionDeviceAccessLevel) invocationOnMock.getArguments()[0]), null));
        when(protocolPluggableService.adapt(any(RequestSecurityLevel.class))).thenAnswer(invocationOnMock -> UPLRequestSecurityLevelAdapter.adaptTo(((RequestSecurityLevel) invocationOnMock.getArguments()[0]), null));
        when(protocolPluggableService.adapt(any(ResponseSecurityLevel.class))).thenAnswer(invocationOnMock -> UPLResponseSecurityLevelAdapter.adaptTo(((ResponseSecurityLevel) invocationOnMock.getArguments()[0]), null));
        when(deviceProtocol.getClientSecurityPropertySpec()).thenReturn(Optional.empty());
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authLevel, authLevel2));
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Collections.singletonList(encLevel));
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getSecuritySuites()).thenReturn(Collections.singletonList(securitySuite));
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getRequestSecurityLevels()).thenReturn(Arrays.asList(requestSecurityLevel1, requestSecurityLevel2));
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getResponseSecurityLevels()).thenReturn(Arrays.asList(responseSecurityLevel1, responseSecurityLevel2));

        when(valueFactory.getValueTypeName()).thenReturn(ValueType.INTEGER.getUplClassName());
        when(spec1.getName()).thenReturn("spec1");
        when(spec2.getName()).thenReturn("spec2");
        when(spec3.getName()).thenReturn("spec3");
        when(spec4.getName()).thenReturn("spec4");
        when(spec5.getName()).thenReturn("spec5");
        when(spec6.getName()).thenReturn("spec6");
        when(spec1.getValueFactory()).thenReturn(valueFactory);
        when(spec2.getValueFactory()).thenReturn(valueFactory);
        when(spec3.getValueFactory()).thenReturn(valueFactory);
        when(spec4.getValueFactory()).thenReturn(valueFactory);
        when(spec5.getValueFactory()).thenReturn(valueFactory);
        when(spec6.getValueFactory()).thenReturn(valueFactory);
        when(authLevel.getId()).thenReturn(1);
        when(authLevel.getSecurityProperties()).thenReturn(Collections.singletonList(spec1));
        when(authLevel2.getId()).thenReturn(2);
        when(authLevel2.getSecurityProperties()).thenReturn(Arrays.asList(spec1, spec2));
        when(encLevel.getId()).thenReturn(2);
        when(encLevel.getSecurityProperties()).thenReturn(Arrays.asList(spec2, spec3));
        when(securitySuite.getId()).thenReturn(100);
        when(requestSecurityLevel1.getId()).thenReturn(201);
        when(requestSecurityLevel1.getSecurityProperties()).thenReturn(Collections.singletonList(spec4));
        when(requestSecurityLevel2.getId()).thenReturn(202);
        when(requestSecurityLevel2.getSecurityProperties()).thenReturn(Arrays.asList(spec4, spec5));
        when(responseSecurityLevel1.getId()).thenReturn(301);
        when(responseSecurityLevel1.getSecurityProperties()).thenReturn(Collections.singletonList(spec5));
        when(responseSecurityLevel2.getId()).thenReturn(302);
        when(responseSecurityLevel2.getSecurityProperties()).thenReturn(Arrays.asList(spec5, spec6));

        when(securitySuite.getEncryptionAccessLevels()).thenReturn(Collections.singletonList(encLevel));
        when(securitySuite.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authLevel, authLevel2));
        when(securitySuite.getRequestSecurityLevels()).thenReturn(Arrays.asList(requestSecurityLevel1, requestSecurityLevel2));
        when(securitySuite.getResponseSecurityLevels()).thenReturn(Arrays.asList(responseSecurityLevel1, responseSecurityLevel2));
    }

    @Test
    @Transactional
    public void testCreation() {
        SecurityPropertySet propertySet;
        DeviceType deviceType = createDeviceType("MyType");

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();

        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());

        assertThat(found.isPresent()).isTrue();

        SecurityPropertySet reloaded = found.get();

        assertThat(reloaded.getName()).isEqualTo("Name");
        assertThat(reloaded.getAuthenticationDeviceAccessLevel()).isEqualTo(authLevel);
        assertThat(reloaded.getEncryptionDeviceAccessLevel()).isEqualTo(encLevel);
        assertThat(reloaded.getSecuritySuite()).isEqualTo(securitySuite);
        assertThat(reloaded.getRequestSecurityLevel()).isEqualTo(requestSecurityLevel1);
        assertThat(reloaded.getResponseSecurityLevel()).isEqualTo(responseSecurityLevel2);

    }

    @Test
    @Transactional
    public void cloneTest() {
        SecurityPropertySet propertySet;
        DeviceType deviceType = createDeviceType("MyType");

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
        DeviceConfiguration clonedDeviceConfig = deviceType.newConfiguration("Clone").add();
        SecurityPropertySet clonedSecurityPropertySet = ((ServerSecurityPropertySet) propertySet).cloneForDeviceConfig(clonedDeviceConfig);

        assertThat(propertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(clonedSecurityPropertySet.getAuthenticationDeviceAccessLevel());
        assertThat(propertySet.getEncryptionDeviceAccessLevel()).isEqualTo(clonedSecurityPropertySet.getEncryptionDeviceAccessLevel());
        assertThat(propertySet.getSecuritySuite()).isEqualTo(clonedSecurityPropertySet.getSecuritySuite());
        assertThat(propertySet.getRequestSecurityLevel()).isEqualTo(clonedSecurityPropertySet.getRequestSecurityLevel());
        assertThat(propertySet.getResponseSecurityLevel()).isEqualTo(clonedSecurityPropertySet.getResponseSecurityLevel());
        assertThat(propertySet.getName()).isEqualTo(clonedSecurityPropertySet.getName());
        assertThat(clonedSecurityPropertySet.getDeviceConfiguration().getId()).isEqualTo(clonedDeviceConfig.getId());
    }

    @Test
    @Transactional
    public void testDeletion() {
        SecurityPropertySet propertySet;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
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

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");
        ComTask testComTask = createTestComTask();
        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
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

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(301)
                .build();

        SecurityPropertySet toUpdate = deviceConfiguration.getSecurityPropertySets().get(0);
        toUpdate.setAuthenticationLevelId(2);
        toUpdate.setRequestSecurityLevelId(202);
        toUpdate.setResponseSecurityLevelId(302);
        toUpdate.update();

        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());

        assertThat(found.isPresent()).isTrue();

        SecurityPropertySet reloaded = found.get();

        assertThat(reloaded.getName()).isEqualTo("Name");
        assertThat(reloaded.getAuthenticationDeviceAccessLevel()).isEqualTo(authLevel2);
        assertThat(reloaded.getEncryptionDeviceAccessLevel()).isEqualTo(encLevel);
        assertThat(reloaded.getSecuritySuite()).isEqualTo(securitySuite);
        assertThat(reloaded.getRequestSecurityLevel()).isEqualTo(requestSecurityLevel2);
        assertThat(reloaded.getResponseSecurityLevel()).isEqualTo(responseSecurityLevel2);
    }

    @Test
    @Transactional
    public void testGetPropertySpecs() {
        SecurityPropertySet propertySet;
        DeviceType deviceType = createDeviceType("MyType");

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(2)         // Has spec1 and spec2
                .encryptionLevel(2)             // Has Spec2 and spec3
                .securitySuite(100)
                .requestSecurityLevel(202)      // Has spec4 and spec5
                .responseSecurityLevel(301)     // Has spec5
                .build();
        Set<PropertySpec> propertySpecs = propertySet.getPropertySpecs();

        assertThat(propertySpecs.size()).isEqualTo(5);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    public void testCreateWithoutName() {
        DeviceType deviceType = createDeviceType("MyType");

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.createSecurityPropertySet(null)
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "name")
    public void testCreateWithEmptyName() {
        DeviceType deviceType = createDeviceType("MyType");

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.createSecurityPropertySet("       ")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "name")
    public void testCreateWithLongName() {
        DeviceType deviceType = createDeviceType("MyType");

        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.createSecurityPropertySet("приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.NAME_UNIQUE + '}')
    public void testCreateWithDuplicateNameInSameConfiguration() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.NAME_UNIQUE + '}')
    public void testUpdateWithDuplicateNameInSameConfiguration() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("A")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
        SecurityPropertySet securityPropertySet = deviceConfiguration.createSecurityPropertySet("B")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();

        // Business method
        securityPropertySet.setName("A");
        securityPropertySet.update();
    }

    @Test
    @Transactional
    public void testCreateWithDuplicateNameInOtherConfiguration() {
        DeviceType deviceType;
        SecurityPropertySet propertySet;
        String expectedName = "Name";
        deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        DeviceConfiguration deviceConfiguration1 = deviceType.newConfiguration("Normal-1").add();
        deviceConfiguration1.save();
        deviceConfiguration1.createSecurityPropertySet(expectedName)
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
        DeviceConfiguration deviceConfiguration2 = deviceType.newConfiguration("Normal-2").add();
        deviceConfiguration2.save();

        // Business method
        propertySet = deviceConfiguration2.createSecurityPropertySet(expectedName)
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();

        // Asserts
        assertThat(propertySet).isNotNull();
        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());
        assertThat(found.isPresent()).isTrue();
        SecurityPropertySet reloaded = found.get();
        assertThat(reloaded.getName()).isEqualTo(expectedName);
    }

    @Test
    @Transactional
    public void testUpdateWithDuplicateNameInOtherConfiguration() {
        DeviceType deviceType;
        SecurityPropertySet propertySet;
        String expectedName = "Name";
        deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);

        DeviceConfiguration deviceConfiguration1 = deviceType.newConfiguration("Normal-1").add();
        deviceConfiguration1.save();
        deviceConfiguration1.createSecurityPropertySet(expectedName)
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
        DeviceConfiguration deviceConfiguration2 = deviceType.newConfiguration("Normal-2").add();
        deviceConfiguration2.save();

        propertySet = deviceConfiguration2.createSecurityPropertySet("Other")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
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
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testAuthenticationLevelIsRequiredWhenProtocolProvidesAtLeastOneAuthenticationLevel() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testAuthenticationLevelShouldNotBeSpecifiedWhenProtocolDoesNotProvideAuthenticationLevels() {
        DeviceConfiguration deviceConfiguration;
        when(securitySuite.getAuthenticationAccessLevels()).thenReturn(Collections.<AuthenticationDeviceAccessLevel>emptyList());
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
    }

    @Test
    @Transactional
    public void testUnsupportedAuthenticationLevelWhenProtocolDoesNotProvideAuthenticationLevels() {
        DeviceConfiguration deviceConfiguration;
        when(securitySuite.getAuthenticationAccessLevels()).thenReturn(Collections.<AuthenticationDeviceAccessLevel>emptyList());
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(-1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testEncryptionLevelIsRequiredWhenProtocolProvidesAtLeastOneEncryptionLevel() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testEncryptionLevelShouldNotBeSpecifiedWhenProtocolDoesNotProvideEncryptionLevels() {
        DeviceConfiguration deviceConfiguration;
        when(securitySuite.getEncryptionAccessLevels()).thenReturn(Collections.<EncryptionDeviceAccessLevel>emptyList());
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
    }

    @Test
    @Transactional
    public void testUnsupportedEncryptionLevelWhenProtocolDoesNotProvideEncryptionLevels() {
        DeviceConfiguration deviceConfiguration;
        when(securitySuite.getEncryptionAccessLevels()).thenReturn(Collections.<EncryptionDeviceAccessLevel>emptyList());
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(-1)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testSecuritySuiteIsRequiredWhenProtocolProvidesAtLeastOneEncryptionLevel() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testSecuritySuiteShouldNotBeSpecifiedWhenProtocolDoesNotProvideEncryptionLevels() {
        DeviceConfiguration deviceConfiguration;
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getSecuritySuites()).thenReturn(Collections.<com.energyict.mdc.upl.security.SecuritySuite>emptyList());
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testRequestSecurityLevelIsRequiredWhenProtocolProvidesAtLeastOneEncryptionLevel() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .responseSecurityLevel(302)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testRequestSecurityLevelShouldNotBeSpecifiedWhenProtocolDoesNotProvideEncryptionLevels() {
        DeviceConfiguration deviceConfiguration;
        when(securitySuite.getRequestSecurityLevels()).thenReturn(Collections.<RequestSecurityLevel>emptyList());
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testResponseSecurityLevelIsRequiredWhenProtocolProvidesAtLeastOneEncryptionLevel() {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testResponseSecurityLevelShouldNotBeSpecifiedWhenProtocolDoesNotProvideEncryptionLevels() {
        DeviceConfiguration deviceConfiguration;
        when(securitySuite.getResponseSecurityLevels()).thenReturn(Collections.<ResponseSecurityLevel>emptyList());
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .securitySuite(100)
                .requestSecurityLevel(201)
                .responseSecurityLevel(302)
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

    private DeviceConfiguration createActiveConfiguration(DeviceType deviceType, String name) {
        DeviceConfiguration deviceConfiguration1 = deviceType.newConfiguration(name).add();
        deviceConfiguration1.activate();
        return deviceConfiguration1;
    }

    @Test
    @Transactional
    public void resolveConflictsWhenDeviceConfigBecomesInactiveTest() {
        int authenticationLevel = 1;
        int encryptionLevel = 2;
        int securitySuite = 100;
        int requestSecurityLevel = 201;
        int responseSecurityLevel = 302;
        DeviceType deviceType = createDeviceType("simpleConflictTest");

        DeviceConfiguration deviceConfiguration1 = createActiveConfiguration(deviceType, "FirstConfig");
        SecurityPropertySet securityPropertySet1 = createSecurityPropertySet(deviceConfiguration1, "NoSecurity", authenticationLevel, encryptionLevel, securitySuite, requestSecurityLevel, responseSecurityLevel);

        DeviceConfiguration deviceConfiguration2 = createActiveConfiguration(deviceType, "SecondConfig");
        SecurityPropertySet securityPropertySet2 = createSecurityPropertySet(deviceConfiguration2, "None", authenticationLevel, encryptionLevel, securitySuite, requestSecurityLevel, responseSecurityLevel);

        deviceConfiguration1.deactivate();

        DeviceType reloadedDeviceType = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        Assertions.assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).isEmpty();
    }

    @Test
    @Transactional
    public void resolveSecuritySetConflictWhenRemovalOfSecuritySetTest() {
        int authenticationLevel = 1;
        int encryptionLevel = 2;
        int securitySuite = 100;
        int requestSecurityLevel = 201;
        int responseSecurityLevel = 302;
        DeviceType deviceType = createDeviceType("simpleConflictTest");

        DeviceConfiguration deviceConfiguration1 = createActiveConfiguration(deviceType, "FirstConfig");
        SecurityPropertySet securityPropertySet1 = createSecurityPropertySet(deviceConfiguration1, "NoSecurity", authenticationLevel, encryptionLevel, securitySuite, requestSecurityLevel, responseSecurityLevel);

        DeviceConfiguration deviceConfiguration2 = createActiveConfiguration(deviceType, "SecondConfig");
        SecurityPropertySet securityPropertySet2 = createSecurityPropertySet(deviceConfiguration2, "None", authenticationLevel, encryptionLevel, securitySuite, requestSecurityLevel, responseSecurityLevel);

        deviceConfiguration1.removeSecurityPropertySet(securityPropertySet1);

        DeviceType reloadedDeviceType = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        Assertions.assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).isEmpty();
    }

    private SecurityPropertySet createSecurityPropertySet(DeviceConfiguration deviceConfiguration, String setName, int authenticationLevel, int encryptionLevel, int securitySuite, int requestSecurityLevel, int responseSecurityLevel) {
        return deviceConfiguration.createSecurityPropertySet(setName)
                .authenticationLevel(authenticationLevel)
                .encryptionLevel(encryptionLevel)
                .securitySuite(securitySuite)
                .requestSecurityLevel(requestSecurityLevel)
                .responseSecurityLevel(responseSecurityLevel)
                .build();
    }
}

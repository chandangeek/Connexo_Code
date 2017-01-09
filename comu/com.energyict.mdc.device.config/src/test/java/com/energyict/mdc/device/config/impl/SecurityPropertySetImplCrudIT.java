package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
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
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.ConflictingSecuritySetSolution;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigConflictMappingHandler;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.fest.assertions.api.Assertions;
import org.fest.assertions.core.Condition;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import static com.energyict.mdc.device.config.DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES3;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES3;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Persistence integration test for the {@link SecurityPropertySetImpl} component.
 * <p>
 * Copyrights EnergyICT
 * Date: 10/04/2014
 * Time: 9:59
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityPropertySetImplCrudIT {

    private static InMemoryBootstrapModule bootstrapModule;
    private static EventAdmin eventAdmin;
    private static BundleContext bundleContext;
    private static TransactionService transactionService;
    private static ProtocolPluggableService protocolPluggableService;
    private static DeviceConfigurationServiceImpl deviceConfigurationService;
    private static Injector injector;
    private static SpyEventService eventService;
    @Rule
    public final TestRule transactional = new TransactionalRule(transactionService);
    @Rule
    public TestRule itWillHitTheFan = new ExpectedConstraintViolationRule();
    @Mock
    private MyDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel authLevel, authLevel2;
    @Mock
    private com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel encLevel;

    @BeforeClass
    public static void initializeDatabase() {
        initializeStaticMocks();
        User principal = mock(User.class);
        when(principal.getName()).thenReturn(SecurityPropertySetImplCrudIT.class.getSimpleName());
        when(principal.hasPrivilege(anyString(), anyString())).thenReturn(true);
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    bootstrapModule,
                    new ThreadSecurityModule(principal),
                    new EventsModule(),
                    new PubSubModule(),
                    new TransactionModule(false),
                    new UtilModule(),
                    new NlsModule(),
                    new DomainUtilModule(),
                    new PartyModule(),
                    new UserModule(),
                    new IdsModule(),
                    new MeteringModule(),
                    new InMemoryMessagingModule(),
                    new EventsModule(),
                    new OrmModule(),
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
                    new MdcIOModule(),
                    new EngineModelModule(),
                    new IssuesModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule(),
                    new PluggableModule(),
                    new SchedulingModule(),
                    new TimeModule(),
                    new CustomPropertySetsModule(),
                    new CalendarModule());
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
                    UpgradeModule.FakeUpgradeService.getInstance());
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
    public void initializeMocks() {
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(protocolPluggableService.findDeviceProtocolPluggableClass(anyLong())).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authLevel, authLevel2));
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(encLevel));
        when(authLevel.getId()).thenReturn(1);
        when(authLevel2.getId()).thenReturn(2);
        when(encLevel.getId()).thenReturn(2);
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
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .build();

        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());

        assertThat(found.isPresent()).isTrue();

        SecurityPropertySet reloaded = found.get();

        assertThat(reloaded.getName()).isEqualTo("Name");
        assertThat(reloaded.getAuthenticationDeviceAccessLevel()).isEqualTo(authLevel);
        assertThat(reloaded.getEncryptionDeviceAccessLevel()).isEqualTo(encLevel);
        assertThat(reloaded.getUserActions()).isEqualTo(EnumSet.of(EDITDEVICESECURITYPROPERTIES1, EDITDEVICESECURITYPROPERTIES2));

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
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .build();
        DeviceConfiguration clonedDeviceConfig = deviceType.newConfiguration("Clone").add();
        SecurityPropertySet clonedSecurityPropertySet = ((ServerSecurityPropertySet) propertySet).cloneForDeviceConfig(clonedDeviceConfig);

        assertThat(propertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(clonedSecurityPropertySet.getAuthenticationDeviceAccessLevel());
        assertThat(propertySet.getEncryptionDeviceAccessLevel()).isEqualTo(clonedSecurityPropertySet.getEncryptionDeviceAccessLevel());
        assertThat(propertySet.getName()).isEqualTo(clonedSecurityPropertySet.getName());
        assertThat(clonedSecurityPropertySet.getDeviceConfiguration().getId()).isEqualTo(clonedDeviceConfig.getId());
        assertThat(clonedSecurityPropertySet.getUserActions()).containsOnly(EDITDEVICESECURITYPROPERTIES1, EDITDEVICESECURITYPROPERTIES2);
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
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .build();

        deviceConfiguration.removeSecurityPropertySet(propertySet);

        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());

        assertThat(found.isPresent()).isFalse();
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
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .build();

        SecurityPropertySet toUpdate = deviceConfiguration.getSecurityPropertySets().get(0);
        toUpdate.addUserAction(VIEWDEVICESECURITYPROPERTIES4);
        toUpdate.setAuthenticationLevel(2);
        toUpdate.removeUserAction(EDITDEVICESECURITYPROPERTIES1);
        toUpdate.update();

        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());

        assertThat(found.isPresent()).isTrue();

        SecurityPropertySet reloaded = found.get();

        assertThat(reloaded.getName()).isEqualTo("Name");
        assertThat(reloaded.getAuthenticationDeviceAccessLevel()).isEqualTo(authLevel2);
        assertThat(reloaded.getEncryptionDeviceAccessLevel()).isEqualTo(encLevel);
        assertThat(reloaded.getUserActions()).isEqualTo(EnumSet.of(EDITDEVICESECURITYPROPERTIES2, VIEWDEVICESECURITYPROPERTIES4));


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
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
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
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
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
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
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
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .build();
        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
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
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .build();
        SecurityPropertySet securityPropertySet = deviceConfiguration.createSecurityPropertySet("B")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
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
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .build();
        DeviceConfiguration deviceConfiguration2 = deviceType.newConfiguration("Normal-2").add();
        deviceConfiguration2.save();

        // Business method
        propertySet = deviceConfiguration2.createSecurityPropertySet(expectedName)
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
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
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .build();
        DeviceConfiguration deviceConfiguration2 = deviceType.newConfiguration("Normal-2").add();
        deviceConfiguration2.save();

        propertySet = deviceConfiguration2.createSecurityPropertySet("Other")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
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
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testAuthenticationLevelShouldNotBeSpecifiedWhenProtocolDoesNotProvideAuthenticationLevels() {
        DeviceConfiguration deviceConfiguration;
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Collections.<com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel>emptyList());
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
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
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testEncryptionLevelShouldNotBeSpecifiedWhenProtocolDoesNotProvideEncryptionLevels() {
        DeviceConfiguration deviceConfiguration;
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Collections.<com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel>emptyList());
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .build();
    }


    @Test
    @Transactional
    public void testEditIsNotAllowedWithoutUserActions() {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet;
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .build();

        // Business method && asserts
        assertThat(propertySet.currentUserIsAllowedToEditDeviceProperties()).isFalse();
    }

    @Test
    @Transactional
    public void testEditIsNotAllowedWithOnlyViewUserActions() {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet;
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addUserAction(VIEWDEVICESECURITYPROPERTIES1)
                .addUserAction(VIEWDEVICESECURITYPROPERTIES2)
                .addUserAction(VIEWDEVICESECURITYPROPERTIES3)
                .addUserAction(VIEWDEVICESECURITYPROPERTIES4)
                .build();

        // Business method && asserts
        assertThat(propertySet.currentUserIsAllowedToEditDeviceProperties()).isFalse();
    }

    @Test
    @Transactional
    public void testEditIsAllowedWithAtLeastOneEditUserAction() {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet;
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .build();

        // Business method && asserts
        assertThat(propertySet.currentUserIsAllowedToEditDeviceProperties()).isTrue();
    }

    @Test
    @Transactional
    public void testViewIsNotAllowedWithOnlyEditUserActions() {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet;
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .addUserAction(EDITDEVICESECURITYPROPERTIES3)
                .addUserAction(EDITDEVICESECURITYPROPERTIES4)
                .build();

        assertThat(propertySet.currentUserIsAllowedToViewDeviceProperties()).isFalse();
    }

    @Test
    @Transactional
    public void testViewIsAllowedWithAtLeastOneViewUserAction() {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet;
        DeviceType deviceType = createDeviceType("MyType");

        deviceConfiguration = createNewInactiveConfiguration(deviceType, "Normal");

        propertySet = createSecurityPropertySet(deviceConfiguration, "Name", 1, 2);

        assertThat(propertySet.currentUserIsAllowedToViewDeviceProperties()).isTrue();
    }

    private DeviceType createDeviceType(String name) {
        return deviceConfigurationService.newDeviceType(name, deviceProtocolPluggableClass);
    }

    private DeviceConfiguration createNewInactiveConfiguration(DeviceType deviceType, String name) {
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration(name).add();
        deviceConfiguration.save();
        return deviceConfiguration;
    }

    @Test
    @Transactional
    public void simpleConflictTest() {
        int authenticationLevel = 1;
        int encryptionLevel = 2;
        DeviceType deviceType = createDeviceType("simpleConflictTest");


        DeviceConfiguration deviceConfiguration1 = createActiveConfiguration(deviceType, "FirstConfig");
        SecurityPropertySet securityPropertySet1 = createSecurityPropertySet(deviceConfiguration1, "NoSecurity", authenticationLevel, encryptionLevel);

        DeviceConfiguration deviceConfiguration2 = createActiveConfiguration(deviceType, "SecondConfig");
        SecurityPropertySet securityPropertySet2 = createSecurityPropertySet(deviceConfiguration2, "None", authenticationLevel, encryptionLevel);

        DeviceType reloadedDeviceType = deviceConfigurationService.findDeviceType(deviceType.getId()).get();

        Assertions.assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).hasSize(2);
        Assertions.assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).haveExactly(1, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                return matchConfigs(deviceConfigConflictMapping, deviceConfiguration1, deviceConfiguration2)
                        && deviceConfigConflictMapping.getConflictingSecuritySetSolutions().size() == 1
                        && matchSecurityPropertySets(securityPropertySet1, securityPropertySet2, deviceConfigConflictMapping);
            }
        });
        Assertions.assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).haveExactly(1, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                return matchConfigs(deviceConfigConflictMapping, deviceConfiguration2, deviceConfiguration1)
                        && deviceConfigConflictMapping.getConflictingSecuritySetSolutions().size() == 1
                        && matchSecurityPropertySets(securityPropertySet2, securityPropertySet1, deviceConfigConflictMapping);
            }
        });
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
        DeviceType deviceType = createDeviceType("simpleConflictTest");

        DeviceConfiguration deviceConfiguration1 = createActiveConfiguration(deviceType, "FirstConfig");
        SecurityPropertySet securityPropertySet1 = createSecurityPropertySet(deviceConfiguration1, "NoSecurity", authenticationLevel, encryptionLevel);

        DeviceConfiguration deviceConfiguration2 = createActiveConfiguration(deviceType, "SecondConfig");
        SecurityPropertySet securityPropertySet2 = createSecurityPropertySet(deviceConfiguration2, "None", authenticationLevel, encryptionLevel);

        deviceConfiguration1.deactivate();

        DeviceType reloadedDeviceType = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        Assertions.assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).isEmpty();
    }

    @Test
    @Transactional
    public void resolveSecuritySetConflictWhenRemovalOfSecuritySetTest() {
        int authenticationLevel = 1;
        int encryptionLevel = 2;
        DeviceType deviceType = createDeviceType("simpleConflictTest");

        DeviceConfiguration deviceConfiguration1 = createActiveConfiguration(deviceType, "FirstConfig");
        SecurityPropertySet securityPropertySet1 = createSecurityPropertySet(deviceConfiguration1, "NoSecurity", authenticationLevel, encryptionLevel);

        DeviceConfiguration deviceConfiguration2 = createActiveConfiguration(deviceType, "SecondConfig");
        SecurityPropertySet securityPropertySet2 = createSecurityPropertySet(deviceConfiguration2, "None", authenticationLevel, encryptionLevel);

        deviceConfiguration1.removeSecurityPropertySet(securityPropertySet1);

        DeviceType reloadedDeviceType = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        Assertions.assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).isEmpty();
    }

    @Test
    @Transactional
    public void solvedMappingsAreNotRemovedWhenNewConflictArisesTest() {
        int authenticationLevel = 1;
        int encryptionLevel = 2;
        DeviceType deviceType = createDeviceType("simpleConflictTest");

        DeviceConfiguration deviceConfiguration1 = createActiveConfiguration(deviceType, "FirstConfig");
        SecurityPropertySet securityPropertySet1 = createSecurityPropertySet(deviceConfiguration1, "NoSecurity", authenticationLevel, encryptionLevel);

        DeviceConfiguration deviceConfiguration2 = createActiveConfiguration(deviceType, "SecondConfig");
        SecurityPropertySet securityPropertySet2 = createSecurityPropertySet(deviceConfiguration2, "None", authenticationLevel, encryptionLevel);

        DeviceConfigConflictMapping deviceConfigConflictMapping1 = deviceType.getDeviceConfigConflictMappings().get(0);
        ConflictingSecuritySetSolution conflictingSecuritySetSolution1 = deviceConfigConflictMapping1.getConflictingSecuritySetSolutions().get(0);
        conflictingSecuritySetSolution1.markSolutionAsRemove();
        DeviceConfigConflictMapping deviceConfigConflictMapping2 = deviceType.getDeviceConfigConflictMappings().get(1);
        ConflictingSecuritySetSolution conflictingSecuritySetSolution2 = deviceConfigConflictMapping2.getConflictingSecuritySetSolutions().get(0);
        conflictingSecuritySetSolution2.markSolutionAsRemove();

        DeviceType reloadedDeviceType = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        Assertions.assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).hasSize(2);

        Assertions.assertThat(reloadedDeviceType.getDeviceConfigConflictMappings()).areExactly(2, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                return deviceConfigConflictMapping.getConflictingSecuritySetSolutions()
                        .get(0).getConflictingMappingAction().equals(DeviceConfigConflictMapping.ConflictingMappingAction.REMOVE)
                        && deviceConfigConflictMapping.isSolved();
            }
        });

        // Logic that we want to test: if new SecuritySet is added, new conflicts will be calculated. Existing solved conflicts should still remain
        DeviceConfiguration thirdConfig = createActiveConfiguration(deviceType, "ThirdConfig");
        SecurityPropertySet securityPropertySet3 = createSecurityPropertySet(thirdConfig, "Blablabla", authenticationLevel, encryptionLevel);

        DeviceType finalDeviceType = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        Assertions.assertThat(finalDeviceType.getDeviceConfigConflictMappings()).hasSize(6);

        Assertions.assertThat(finalDeviceType.getDeviceConfigConflictMappings()).areExactly(2, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                return deviceConfigConflictMapping.getConflictingSecuritySetSolutions()
                        .get(0).getConflictingMappingAction().equals(DeviceConfigConflictMapping.ConflictingMappingAction.REMOVE)
                        && deviceConfigConflictMapping.isSolved();
            }
        });

        Assertions.assertThat(finalDeviceType.getDeviceConfigConflictMappings()).areExactly(2, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                return deviceConfigConflictMapping.isSolved();
            }
        });

        Assertions.assertThat(finalDeviceType.getDeviceConfigConflictMappings()).areExactly(4, new Condition<DeviceConfigConflictMapping>() {
            @Override
            public boolean matches(DeviceConfigConflictMapping deviceConfigConflictMapping) {
                return !deviceConfigConflictMapping.isSolved();
            }
        });

    }

    private boolean matchSecurityPropertySets(SecurityPropertySet originSecurityPropertySet, SecurityPropertySet destinationSecurityPropertySet, DeviceConfigConflictMapping deviceConfigConflictMapping) {
        return deviceConfigConflictMapping.getConflictingSecuritySetSolutions().get(0).getOriginDataSource().getId() == originSecurityPropertySet.getId();
//                && deviceConfigConflictMapping.getConflictingSecuritySetSolutions().get(0).getDestinationDataSource().getId() == destinationSecurityPropertySet.getId();
    }

    private boolean matchConfigs(DeviceConfigConflictMapping deviceConfigConflictMapping, DeviceConfiguration originConfig, DeviceConfiguration destinationConfig) {
        return deviceConfigConflictMapping.getOriginDeviceConfiguration().getId() == originConfig.getId()
                && deviceConfigConflictMapping.getDestinationDeviceConfiguration().getId() == destinationConfig.getId();
    }

    private SecurityPropertySet createSecurityPropertySet(DeviceConfiguration deviceConfiguration, String setName, int authenticationLevel, int encryptionLevel) {
        return deviceConfiguration.createSecurityPropertySet(setName)
                .authenticationLevel(authenticationLevel)
                .encryptionLevel(encryptionLevel)
                .addUserAction(VIEWDEVICESECURITYPROPERTIES1)
                .build();
    }


    public interface MyDeviceProtocolPluggableClass extends DeviceProtocolPluggableClass {
    }

    private static class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }

    }
}
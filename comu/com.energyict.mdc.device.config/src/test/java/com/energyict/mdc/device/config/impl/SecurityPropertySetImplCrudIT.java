package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import static com.energyict.mdc.device.config.DeviceSecurityUserAction.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 10/04/2014
 * Time: 9:59
 */
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
    private static DeviceConfigurationServiceImpl deviceConfigurationService;

    @Mock
    private MyDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private AuthenticationDeviceAccessLevel authLevel, authLevel2;
    @Mock
    private EncryptionDeviceAccessLevel encLevel;

    private static class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BpmService.class).toInstance(mock(BpmService.class));
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
        }

    }

    @BeforeClass
    public static void initializeDatabase() {
        initializeStaticMocks();
        User principal = mock(User.class);
        when(principal.getName()).thenReturn(SecurityPropertySetImplCrudIT.class.getSimpleName());
        when(principal.hasPrivilege(anyString(), any(Privilege.class))).thenReturn(true);
        Injector injector = null;
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
                    new MeteringModule(false),
                    new InMemoryMessagingModule(),
                    new EventsModule(),
                    new OrmModule(),
                    new DataVaultModule(),
                    new MdcReadingTypeUtilServiceModule(),
                    new MasterDataModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule(),
                    new ProtocolApiModule(),
                    new TasksModule(),
                    new ValidationModule(),
                    new EstimationModule(),
                    new FiniteStateMachineModule(),
                    new DeviceLifeCycleConfigurationModule(),
                    new MeteringGroupsModule(),
                    new TaskModule(),
                    new DeviceConfigurationModule(),
                    new MdcIOModule(),
                    new EngineModelModule(),
                    new IssuesModule(),
                    new BasicPropertiesModule(),
                    new MdcDynamicModule(),
                    new PluggableModule(),
                    new SchedulingModule(),
                    new TimeModule());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringService.class);
            injector.getInstance(PluggableService.class);
            injector.getInstance(MasterDataService.class);
            injector.getInstance(TaskService.class);
            injector.getInstance(ValidationService.class);
            injector.getInstance(DeviceLifeCycleConfigurationService.class);
            deviceConfigurationService = (DeviceConfigurationServiceImpl) injector.getInstance(DeviceConfigurationService.class);
            ctx.commit();
        }
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
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

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
    public void testDeletion() {
        SecurityPropertySet propertySet;
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

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
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    public void testCreateWithoutName () {
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}", property = "name")
    public void testCreateWithEmptyName () {
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

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
    public void testCreateWithLongName () {
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

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
    public void testCreateWithDuplicateNameInSameConfiguration () {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

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
    public void testCreateWithDuplicateNameInOtherConfiguration () {
        DeviceType deviceType;
        SecurityPropertySet propertySet;
        String expectedName = "Name";
        deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

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

        // Busines method
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
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testAuthenticationLevelIsRequiredWhenProtocolProvidesAtLeastOneAuthenticationLevel () {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.createSecurityPropertySet("Name")
                .encryptionLevel(2)
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testAuthenticationLevelShouldNotBeSpecifiedWhenProtocolDoesNotProvideAuthenticationLevels () {
        DeviceConfiguration deviceConfiguration;
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Collections.<AuthenticationDeviceAccessLevel>emptyList());
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

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
    public void testEncryptionLevelIsRequiredWhenProtocolProvidesAtLeastOneEncryptionLevel () {
        DeviceConfiguration deviceConfiguration;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testEncryptionLevelShouldNotBeSpecifiedWhenProtocolDoesNotProvideEncryptionLevels () {
        DeviceConfiguration deviceConfiguration;
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Collections.<EncryptionDeviceAccessLevel>emptyList());
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                .build();
    }


    @Test
    @Transactional
    public void testEditIsNotAllowedWithoutUserActions () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .build();

        // Business method && asserts
        assertThat(propertySet.currentUserIsAllowedToEditDeviceProperties()).isFalse();
    }

    @Test
    @Transactional
    public void testEditIsNotAllowedWithOnlyViewUserActions () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

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
    public void testEditIsAllowedWithAtLeastOneEditUserAction () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

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
    public void testViewIsNotAllowedWithOnlyEditUserActions () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

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
    public void testViewIsAllowedWithAtLeastOneViewUserAction () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet;
        DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
        deviceType.save();

        deviceConfiguration = deviceType.newConfiguration("Normal").add();
        deviceConfiguration.save();

        propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                .authenticationLevel(1)
                .encryptionLevel(2)
                .addUserAction(VIEWDEVICESECURITYPROPERTIES1)
                .build();

        assertThat(propertySet.currentUserIsAllowedToViewDeviceProperties()).isTrue();
    }

    public interface MyDeviceProtocolPluggableClass extends DeviceProtocolPluggableClass {
    }

}
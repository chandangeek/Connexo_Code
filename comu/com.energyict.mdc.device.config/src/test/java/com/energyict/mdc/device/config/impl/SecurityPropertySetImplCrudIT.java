package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.energyict.protocols.mdc.services.impl.ProtocolsModule;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import static com.energyict.mdc.device.config.DeviceSecurityUserAction.ALLOWCOMTASKEXECUTION1;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES3;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES3;
import static com.energyict.mdc.device.config.DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
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
    public TestRule itWillHitTheFan = new ExpectedConstraintViolationRule();

    private InMemoryBootstrapModule bootstrapModule;
    private Injector injector;

    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private User principal;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private BundleContext bundleContext;
    @Mock
    MyDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private AuthenticationDeviceAccessLevel authLevel, authLevel2;
    @Mock
    private EncryptionDeviceAccessLevel encLevel;


    private TransactionService transactionService;
    private DeviceConfigurationServiceImpl deviceConfigurationService;
    private MeteringService meteringService;

    public void initializeDatabase(boolean showSqlLogging, boolean createMasterData) {
        bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new ThreadSecurityModule(principal),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
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
                new MdcReadingTypeUtilServiceModule(),
                new MasterDataModule(),
                new TasksModule(),
                new DeviceConfigurationModule(),
                new MdcCommonModule(),
                new EngineModelModule(),
                new IssuesModule(),
                new ProtocolsModule(),
                new MdcDynamicModule(),
                new PluggableModule(),
                new SchedulingModule());
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            meteringService = injector.getInstance(MeteringService.class);
//            readingTypeUtilService = injector.getInstance(MdcReadingTypeUtilService.class);
//            engineModelService = injector.getInstance(EngineModelService.class);
//            protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
//            inboundDeviceProtocolService = injector.getInstance(InboundDeviceProtocolService.class);
            injector.getInstance(PluggableService.class);
            injector.getInstance(MasterDataService.class);
            injector.getInstance(TaskService.class);
            deviceConfigurationService = (DeviceConfigurationServiceImpl) injector.getInstance(DeviceConfigurationService.class);
            ctx.commit();
        }
        Environment environment = injector.getInstance(Environment.class);
        environment.put(InMemoryPersistence.JUPITER_BOOTSTRAP_MODULE_COMPONENT_NAME, bootstrapModule, true);
        environment.setApplicationContext(applicationContext);
    }

    @Before
    public void setUp() {
        when(principal.getName()).thenReturn("test");
        Translator translator = mock(Translator.class);
        when(translator.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        when(translator.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(applicationContext.getTranslator()).thenReturn(translator);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(protocolPluggableService.findDeviceProtocolPluggableClass(anyInt())).thenReturn(deviceProtocolPluggableClass);
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authLevel, authLevel2));
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(encLevel));
        when(authLevel.getId()).thenReturn(1);
        when(authLevel2.getId()).thenReturn(2);
        when(encLevel.getId()).thenReturn(2);
        when(principal.hasPrivilege(any(Privilege.class))).thenReturn(true);

//        when(applicationContext.findFactory(5011)).thenReturn(businessObjectFactory);

        initializeDatabase(false, false);
//        protocolPluggableService = injector.getInstance(ProtocolPluggableService.class);
//        engineModelService = injector.getInstance(EngineModelService.class);
    }

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
//            bind(DataModel.class).toProvider(new Provider<DataModel>() {
//                @Override
//                public DataModel get() {
//                    return dataModel;
//                }
//            });
        }

    }

    @After
    public void tearDown() {
        bootstrapModule.deactivate();
    }

    @Test
    public void testCreation() {
        DeviceCommunicationConfiguration communicationConfiguration;
        SecurityPropertySet propertySet = null;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                    .authenticationLevel(1)
                    .encryptionLevel(2)
                    .addUserAction(ALLOWCOMTASKEXECUTION1)
                    .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                    .build();

            context.commit();
        }

        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());

        assertThat(found).isPresent();

        SecurityPropertySet reloaded = found.get();

        assertThat(reloaded.getName()).isEqualTo("Name");
        assertThat(reloaded.getAuthenticationDeviceAccessLevel()).isEqualTo(authLevel);
        assertThat(reloaded.getEncryptionDeviceAccessLevel()).isEqualTo(encLevel);
        assertThat(reloaded.getUserActions()).isEqualTo(EnumSet.of(ALLOWCOMTASKEXECUTION1, EDITDEVICESECURITYPROPERTIES2));

    }

    @Test
    public void testDeletion() {
        DeviceCommunicationConfiguration communicationConfiguration;
        SecurityPropertySet propertySet = null;
        DeviceConfiguration deviceConfiguration = null;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                    .authenticationLevel(1)
                    .encryptionLevel(2)
                    .addUserAction(ALLOWCOMTASKEXECUTION1)
                    .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                    .build();

            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            deviceConfiguration.removeSecurityPropertySet(propertySet);
            context.commit();
        }

        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());

        assertThat(found).isAbsent();

    }

    @Test
    public void testUpdate() {
        DeviceCommunicationConfiguration communicationConfiguration;
        SecurityPropertySet propertySet = null;
        DeviceConfiguration deviceConfiguration = null;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                    .authenticationLevel(1)
                    .encryptionLevel(2)
                    .addUserAction(ALLOWCOMTASKEXECUTION1)
                    .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                    .build();

            context.commit();
        }

        try (TransactionContext context = transactionService.getContext()) {
            SecurityPropertySet toUpdate = deviceConfiguration.getSecurityPropertySets().get(0);
            toUpdate.addUserAction(VIEWDEVICESECURITYPROPERTIES4);
            toUpdate.setAuthenticationLevel(2);
            toUpdate.removeUserAction(ALLOWCOMTASKEXECUTION1);
            toUpdate.update();
            context.commit();
        }

        Optional<SecurityPropertySet> found = deviceConfigurationService.findSecurityPropertySet(propertySet.getId());

        assertThat(found).isPresent();

        SecurityPropertySet reloaded = found.get();

        assertThat(reloaded.getName()).isEqualTo("Name");
        assertThat(reloaded.getAuthenticationDeviceAccessLevel()).isEqualTo(authLevel2);
        assertThat(reloaded.getEncryptionDeviceAccessLevel()).isEqualTo(encLevel);
        assertThat(reloaded.getUserActions()).isEqualTo(EnumSet.of(EDITDEVICESECURITYPROPERTIES2, VIEWDEVICESECURITYPROPERTIES4));


    }

    @Test()
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    public void testCreateWithoutName () {
        DeviceCommunicationConfiguration communicationConfiguration;
        SecurityPropertySet propertySet = null;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            propertySet = deviceConfiguration.createSecurityPropertySet(null)
                    .authenticationLevel(1)
                    .encryptionLevel(2)
                    .addUserAction(ALLOWCOMTASKEXECUTION1)
                    .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                    .build();

            context.commit();
        }
    }

    @Test()
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.INCORRECT_SIZE + "}", property = "name")
    public void testCreateWithEmptyName () {
        DeviceCommunicationConfiguration communicationConfiguration;
        SecurityPropertySet propertySet = null;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            propertySet = deviceConfiguration.createSecurityPropertySet("       ")
                    .authenticationLevel(1)
                    .encryptionLevel(2)
                    .addUserAction(ALLOWCOMTASKEXECUTION1)
                    .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                    .build();

            context.commit();
        }
    }

    @Test()
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.INCORRECT_SIZE + "}", property = "name")
    public void testCreateWithLongName () {
        DeviceCommunicationConfiguration communicationConfiguration;
        SecurityPropertySet propertySet = null;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            propertySet = deviceConfiguration.createSecurityPropertySet("приветик--приветик--приветик--приветик--приветик--приветик--приветик--приветик--+")
                    .authenticationLevel(1)
                    .encryptionLevel(2)
                    .addUserAction(ALLOWCOMTASKEXECUTION1)
                    .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                    .build();

            context.commit();
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = '{' + MessageSeeds.Keys.NAME_UNIQUE + '}')
    public void testCreateWithDuplicateName () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet = null;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                    .authenticationLevel(1)
                    .encryptionLevel(2)
                    .addUserAction(ALLOWCOMTASKEXECUTION1)
                    .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                    .build();

            context.commit();
        }
        try (TransactionContext context = transactionService.getContext()) {
            propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                    .authenticationLevel(1)
                    .encryptionLevel(2)
                    .addUserAction(ALLOWCOMTASKEXECUTION1)
                    .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                    .build();

            context.commit();
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testAuthenticationLevelIsRequiredWhenProtocolProvidesAtLeastOneAuthenticationLevel () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet = null;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                    .encryptionLevel(2)
                    .addUserAction(ALLOWCOMTASKEXECUTION1)
                    .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                    .build();

            context.commit();
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testAuthenticationLevelShouldNotBeSpecifiedWhenProtocolDoesNotProvideAuthenticationLevels () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet = null;
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Collections.<AuthenticationDeviceAccessLevel>emptyList());
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                    .authenticationLevel(1)
                    .encryptionLevel(2)
                    .addUserAction(ALLOWCOMTASKEXECUTION1)
                    .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                    .build();

            context.commit();
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testEncryptionLevelIsRequiredWhenProtocolProvidesAtLeastOneEncryptionLevel () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet = null;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                    .authenticationLevel(1)
                    .addUserAction(ALLOWCOMTASKEXECUTION1)
                    .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                    .build();

            context.commit();
        }
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNSUPPORTED_SECURITY_LEVEL + "}")
    public void testEncryptionLevelShouldNotBeSpecifiedWhenProtocolDoesNotProvideEncryptionLevels () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet = null;
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Collections.<EncryptionDeviceAccessLevel>emptyList());
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                    .authenticationLevel(1)
                    .encryptionLevel(2)
                    .addUserAction(ALLOWCOMTASKEXECUTION1)
                    .addUserAction(EDITDEVICESECURITYPROPERTIES2)
                    .build();

            context.commit();
        }
    }


    @Test
    public void testEditIsNotAllowedWithoutUserActions () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet = null;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                    .authenticationLevel(1)
                    .encryptionLevel(2)
                    .build();

            context.commit();
        }

        // Business method && asserts
        assertThat(propertySet.currentUserIsAllowedToEditDeviceProperties()).isFalse();
    }

    @Test
    public void testEditIsNotAllowedWithOnlyViewUserActions () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet = null;
        try (TransactionContext context = transactionService.getContext()) {
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

            context.commit();
        }

        // Business method && asserts
        assertThat(propertySet.currentUserIsAllowedToEditDeviceProperties()).isFalse();
    }

    @Test
    public void testEditIsAllowedWithAtLeastOneEditUserAction () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet = null;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                    .authenticationLevel(1)
                    .encryptionLevel(2)
                    .addUserAction(EDITDEVICESECURITYPROPERTIES1)
                    .build();

            context.commit();
        }

        // Business method && asserts
        assertThat(propertySet.currentUserIsAllowedToEditDeviceProperties()).isTrue();
    }

    @Test
    public void testViewIsNotAllowedWithOnlyEditUserActions () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet = null;
        try (TransactionContext context = transactionService.getContext()) {
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

            context.commit();
        }

        assertThat(propertySet.currentUserIsAllowedToViewDeviceProperties()).isFalse();
    }

    @Test
    public void testViewIsAllowedWithAtLeastOneViewUserAction () {
        DeviceConfiguration deviceConfiguration;
        SecurityPropertySet propertySet = null;
        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.newDeviceType("MyType", deviceProtocolPluggableClass);
            deviceType.save();

            deviceConfiguration = deviceType.newConfiguration("Normal").add();
            deviceConfiguration.save();

            propertySet = deviceConfiguration.createSecurityPropertySet("Name")
                    .authenticationLevel(1)
                    .encryptionLevel(2)
                    .addUserAction(VIEWDEVICESECURITYPROPERTIES1)
                    .build();

            context.commit();
        }

        assertThat(propertySet.currentUserIsAllowedToViewDeviceProperties()).isTrue();
    }

    public interface MyDeviceProtocolPluggableClass extends DeviceProtocolPluggableClass {
    }


}

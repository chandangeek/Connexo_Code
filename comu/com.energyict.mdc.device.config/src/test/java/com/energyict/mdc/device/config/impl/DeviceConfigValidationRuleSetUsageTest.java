package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.DeviceConfValidationRuleSetUsage;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigValidationRuleSetUsageTest {

    static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private BpmService bpmService;

    static final String DEVICE_TYPE_NAME = DeviceConfigValidationRuleSetUsageTest.class.getSimpleName();

    @Mock
    DeviceProtocolPluggableClass deviceProtocolPluggableClass;

    @Mock
    DeviceProtocol deviceProtocol;

    private InMemoryBootstrapModule bootstrapModule;
    private Injector injector;
    @Mock
    private LicenseService licenseService;
    @Mock
    private IssueService issueService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private RelationService relationService;
    @Mock
    private ConnectionTypeService connectionTypeService;
    @Mock
    private DeviceCacheMarshallingService deviceCacheMarshallingService;
    @Mock
    private DeviceProtocolMessageService deviceProtocolMessageService;
    @Mock
    private DeviceProtocolSecurityService deviceProtocolSecurityService;
    @Mock
    private DeviceProtocolService deviceProtocolService;
    @Mock
    private InboundDeviceProtocolService inboundDeviceProtocolService;
    @Mock
    private LicensedProtocolService licensedProtocolService;
    @Mock
    private UserService userService;

    @Before
    public void setup () {
        when(principal.getName()).thenReturn("Ernie");
        this.bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new MockModule(),
                this.bootstrapModule,
                new ThreadSecurityModule(this.principal),
                new OrmModule(),
                new DataVaultModule(),
                new EventsModule(),
                new PubSubModule(),
                new TransactionModule(),
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new MasterDataModule(),
                new PartyModule(),
                new IdsModule(),
                new FiniteStateMachineModule(),
                new MeteringModule(false),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new ValidationModule(),
                new EstimationModule(),
                new MeteringGroupsModule(),
                new TaskModule(),
                new SchedulingModule(),
                new ProtocolPluggableModule(),
                new EngineModelModule(),
                new MdcReadingTypeUtilServiceModule(),
                new PluggableModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new BasicPropertiesModule(),
                new ProtocolApiModule(),
                new TasksModule(),
                new TimeModule());
        TransactionService transactionService = injector.getInstance(TransactionService.class);

        try (TransactionContext ctx = transactionService.getContext()) {
            injector.getInstance(ThreadPrincipalService.class);
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(ValidationService.class);
            injector.getInstance(MasterDataService.class);
            injector.getInstance(DeviceLifeCycleConfigurationService.class);
            injector.getInstance(DeviceConfigurationService.class);
            ctx.commit();
        }
    }

    @After
    public void cleanupDatabase () {
        this.bootstrapModule.deactivate();
    }

    @Before
    public void setupThesaurus () {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BpmService.class).toInstance(bpmService);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(LicenseService.class).toInstance(licenseService);
            bind(IssueService.class).toInstance(issueService);
            bind(PropertySpecService.class).toInstance(propertySpecService);
            bind(RelationService.class).toInstance(relationService);
            bind(ConnectionTypeService.class).toInstance(connectionTypeService);
            bind(DeviceCacheMarshallingService.class).toInstance(deviceCacheMarshallingService);
            bind(DeviceProtocolMessageService.class).toInstance(deviceProtocolMessageService);
            bind(DeviceProtocolSecurityService.class).toInstance(deviceProtocolSecurityService);
            bind(DeviceProtocolService.class).toInstance(deviceProtocolService);
            bind(InboundDeviceProtocolService.class).toInstance(inboundDeviceProtocolService);
            bind(LicensedProtocolService.class).toInstance(licensedProtocolService);
            bind(UserService.class).toInstance(userService);
            //bind(ValidationService.class).toInstance(validationService);

        }
    }
    @Test
    public void testDeviceConfigValidationRuleSetUsage () {
        TransactionService transactionService = injector.getInstance(TransactionService.class);

        try (TransactionContext context = transactionService.getContext()) {
            ValidationRuleSet validationRuleSet1 = createValidationRuleSet("ruleset1");

            DeviceConfiguration deviceConfiguration = getDeviceConfiguration();
            long deviceConfId = deviceConfiguration.getId();

            //add a ruleset to the device config

            deviceConfiguration.addValidationRuleSet(validationRuleSet1);

            Optional<DeviceConfiguration> foundDeviceConfiguration =
                    injector.getInstance(DeviceConfigurationService.class).findDeviceConfiguration(deviceConfId);
            List<DeviceConfValidationRuleSetUsage> usages = foundDeviceConfiguration.get().getDeviceConfValidationRuleSetUsages();

            assertThat(usages.size() == 1).isTrue();

            DeviceConfValidationRuleSetUsage usage =  usages.get(0);
            assertThat(usage.getDeviceConfiguration().getId() == deviceConfId).isTrue();
            assertThat(usage.getValidationRuleSet().getId() == validationRuleSet1.getId());


            List<ValidationRuleSet> ruleSets = deviceConfiguration.getValidationRuleSets();
            assertThat(ruleSets.size() == 1);

            ValidationRuleSet ruleSet = ruleSets.get(0);
            assertThat(ruleSet.getId() == validationRuleSet1.getId());

            //add a another ruleset to the device config
            ValidationRuleSet validationRuleSet2 = createValidationRuleSet("ruleset2");
            deviceConfiguration.addValidationRuleSet(validationRuleSet2);
            foundDeviceConfiguration =
                    injector.getInstance(DeviceConfigurationService.class).findDeviceConfiguration(deviceConfId);
            usages = foundDeviceConfiguration.get().getDeviceConfValidationRuleSetUsages();
            assertThat(usages.size() == 2).isTrue();

            //remove a ruleset from a device config
            foundDeviceConfiguration.get().removeValidationRuleSet(validationRuleSet2);
            foundDeviceConfiguration =
                    injector.getInstance(DeviceConfigurationService.class).findDeviceConfiguration(deviceConfId);
            usages = foundDeviceConfiguration.get().getDeviceConfValidationRuleSetUsages();
            assertThat(usages.size() == 1).isTrue();
            ruleSets = deviceConfiguration.getValidationRuleSets();
            assertThat(ruleSets.size() == 1);

            ruleSet = ruleSets.get(0);
            assertThat(ruleSet.getId() == validationRuleSet1.getId());
        }

    }

    private ValidationRuleSet createValidationRuleSet(String name) {
        ValidationRuleSet ruleSet = injector.getInstance(ValidationService.class).createValidationRuleSet(name);
        ruleSet.save();
        return ruleSet;
    }

    private DeviceConfiguration getDeviceConfiguration() {
        DeviceType deviceType = injector.getInstance(DeviceConfigurationService.class).newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder = deviceType.newConfiguration("DeviceConfiguration");

        DeviceConfiguration deviceConfiguration = deviceConfigurationBuilder.add();
        deviceType.save();
        return deviceConfiguration;
    }

    @Before
    public void initializeMocks() {
        when(deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(authenticationAccessLevel));
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(0);
        when(this.deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(encryptionAccessLevel));
    }

}



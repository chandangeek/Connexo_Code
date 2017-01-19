package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.impl.PartialScheduledConnectionTaskImpl;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigConflictMappingEngine;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.CannotChangeDeviceConfigStillUnresolvedConflicts;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;
import com.energyict.mdc.device.data.impl.security.BasicAuthenticationSecurityProperties;
import com.energyict.mdc.device.data.impl.tasks.OutboundIpConnectionTypeImpl;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.obis.ObisCode;
import org.assertj.core.api.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 15.09.15
 * Time: 11:58
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigurationChangeIT extends PersistenceIntegrationTest {

    private static final BigDecimal OVERFLOW_VALUE = BigDecimal.valueOf(1000000000);
    private static ConnectionTypePluggableClass outboundIpConnectionTypePluggableClass;
    private final String connectionTaskCreatedTopic = "com/energyict/mdc/device/config/partial(.*)connectiontask/CREATED";
    private final String securitySetCreatedTopic = "com/energyict/mdc/device/config/securitypropertyset/CREATED";

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();

    private String readingTypeMRID1 = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private String readingTypeMRID2 = "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private TimeDuration scheduledConnectionTaskInterval = TimeDuration.minutes(15);

    @BeforeClass
    public static void registerConnectionTypePluggableClasses() {
        inMemoryPersistence.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                outboundIpConnectionTypePluggableClass = registerConnectionTypePluggableClass(OutboundIpConnectionTypeImpl.class);
                registerDeviceProtocol();
                grantAllViewAndEditPrivilegesToPrincipal();
            }
        });
    }

    public static void registerDeviceProtocol() {
        deviceProtocolPluggableClass = registerDeviceProtocol(TestProtocol.class);
    }

    private static <T extends DeviceProtocol> DeviceProtocolPluggableClass registerDeviceProtocol(Class<T> deviceProtocolClass) {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass =
                inMemoryPersistence.getProtocolPluggableService()
                        .newDeviceProtocolPluggableClass(deviceProtocolClass.getSimpleName(), deviceProtocolClass.getName());
        deviceProtocolPluggableClass.save();
        return deviceProtocolPluggableClass;
    }

    @Before
    public void initializeMocks() {
        try (TransactionContext context = getTransactionService().getContext()) {
            super.initializeMocks();
            final List<DeviceConfiguration> configurations = deviceType.getConfigurations();
            configurations.forEach(deviceConfiguration -> {
                deviceConfiguration.deactivate();
                this.deviceType.removeConfiguration(deviceConfiguration);
            });
            context.commit();
        }
    }

    @After
    public void cleanup() {
        try (TransactionContext context = getTransactionService().getContext()) {
            inMemoryPersistence.getDeviceConfigurationService().findAllDeviceTypes().stream()
                    .forEach(dt -> {
                        dt.getConfigurations().forEach(dc -> {
                            inMemoryPersistence.getDeviceService().findDevicesByDeviceConfiguration(dc).stream()
                                    .forEach(device -> {
                                                // remove communications stuff
                                                device.getComTaskExecutions().forEach(device::removeComTaskExecution);
                                                device.getConnectionTasks().forEach(device::removeConnectionTask);
                                                device.delete();
                                            }
                                    );
                            dc.deactivate();
                            DeviceConfigConflictMappingEngine.INSTANCE.reCalculateConflicts(dt);
                        });
                        // we do it in two runs because some obsolete objects refer to different configs
                        dt.getConfigurations().forEach(dc -> {
                            final List<ComTask> comTasks = dc.getComTaskEnablements().stream().map(ComTaskEnablement::getComTask).collect(Collectors.toList());
                            comTasks.forEach(dc::disableComTask);

                            final List<PartialConnectionTask> partialConnectionTasks = dc.getPartialConnectionTasks();
                            for (int i = 0; i < partialConnectionTasks.size(); i++) {
                                dc.remove(partialConnectionTasks.get(i));
                            }
                            final List<SecurityPropertySet> securityPropertySets = dc.getSecurityPropertySets();
                            for (int i = 0; i < securityPropertySets.size(); i++) {
                                dc.removeSecurityPropertySet(securityPropertySets.get(i));
                            }
                            dt.removeConfiguration(dc);
                        });
                        dt.delete();
                    });

            inMemoryPersistence.getMasterDataService().findAllLoadProfileTypes().forEach(LoadProfileType::delete);
            inMemoryPersistence.getMasterDataService().findAllLogBookTypes().stream().forEach(LogBookType::delete);

            inMemoryPersistence.getEngineConfigurationService()
                    .findAllComPortPools()
                    .forEach(ComPortPool::delete);

            inMemoryPersistence.getEngineConfigurationService().findAllComServers().find().forEach(ComServer::delete);
            context.commit();
        }
    }

    private static <T extends ConnectionType> ConnectionTypePluggableClass registerConnectionTypePluggableClass(Class<T> connectionTypeClass) {
        ConnectionTypePluggableClass connectionTypePluggableClass =
                inMemoryPersistence.getProtocolPluggableService()
                        .newConnectionTypePluggableClass(connectionTypeClass.getSimpleName(), connectionTypeClass.getName());
        connectionTypePluggableClass.save();
        return connectionTypePluggableClass;
    }

    private LoadProfileType createLoadProfileType(String loadProfileName, ObisCode obisCode, RegisterType... registerTypes) {
        LoadProfileType loadProfileType = inMemoryPersistence.getMasterDataService().newLoadProfileType(loadProfileName, obisCode, TimeDuration.minutes(15), Arrays.asList(registerTypes));
        loadProfileType.save();
        return loadProfileType;
    }

    private LogBookType createLogBookType(String logBookName, ObisCode obisCode) {
        LogBookType logBookType = inMemoryPersistence.getMasterDataService().newLogBookType(logBookName, obisCode);
        logBookType.save();
        return logBookType;
    }

    /**
     * The event mechanism is not really in place in these tests, so this method allows you to trigger
     * the event itself to update the conflicts
     *
     * @param eventObject the object that initially should have triggered
     * @param topic the topic to filter on
     */
    private void updateConflictsFor(HasId eventObject, String topic) {
        LocalEvent localEvent = mock(LocalEvent.class);
        com.elster.jupiter.events.EventType eventType = mock(com.elster.jupiter.events.EventType.class);
        when(eventType.getTopic()).thenReturn(topic);
        when(localEvent.getType()).thenReturn(eventType);
        when(localEvent.getSource()).thenReturn(eventObject);
        inMemoryPersistence.getDeviceConfigConflictMappingHandler().onEvent(localEvent);
    }

    private OutboundComPortPool createOutboundIpComPortPool(String name) {
        OutboundComPortPool ipComPortPool = inMemoryPersistence.getEngineConfigurationService().newOutboundComPortPool(name, ComPortType.TCP, new TimeDuration(1, TimeDuration.TimeUnit.MINUTES));
        ipComPortPool.setActive(true);
        ipComPortPool.update();
        return ipComPortPool;
    }

    @Test
    public void simpleConfigChangeNoConflictsNoDataSourceTest() {
        DeviceConfiguration secondDeviceConfiguration;
        Device device;
        try (TransactionContext context = getTransactionService().getContext()) {
            DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            context.commit();
        }

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
    }

    @Test(expected = DeviceConfigurationChangeException.class)
    public void changeConfigToSameConfigTest() {
        DeviceConfiguration firstDeviceConfiguration;
        Device device;
        try (TransactionContext context = getTransactionService().getContext()) {
            firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            firstDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            context.commit();
        }
        try {
            inMemoryPersistence.getDeviceService().changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion() , firstDeviceConfiguration.getId(), firstDeviceConfiguration.getVersion());
        } catch (DeviceConfigurationChangeException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.CANNOT_CHANGE_DEVICE_CONFIG_TO_SAME_CONFIG)) {
                fail("Should have gotten an exception indicating that you can not change the config to the same config");
            }
            throw e;
        }
    }

    @Test
    public void lockIsRemovedEvenWhenExceptionOccurs() {
        DeviceConfiguration firstDeviceConfiguration;
        Device device;
        try (TransactionContext context = getTransactionService().getContext()) {
            firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            firstDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            context.commit();
        }
        try {
            inMemoryPersistence.getDeviceService().changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion() , firstDeviceConfiguration.getId(), firstDeviceConfiguration.getVersion());
        } catch (DeviceConfigurationChangeException e) {
            // we expect this :)
        }
        assertThat(inMemoryPersistence.getDeviceService().hasActiveDeviceConfigChangesFor(firstDeviceConfiguration, firstDeviceConfiguration)).isFalse();
    }

    @Test(expected = DeviceConfigurationChangeException.class)
    public void changeConfigToConfigOfOtherDeviceTypeTest() {
        Device device;
        final DeviceConfiguration configOfOtherDeviceType;
        try (TransactionContext context = getTransactionService().getContext()) {
            DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            firstDeviceConfiguration.activate();
            final DeviceType otherDeviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("OtherDeviceType", deviceProtocolPluggableClass);
            configOfOtherDeviceType = otherDeviceType.newConfiguration("ConfigOfOtherDeviceType").add();
            configOfOtherDeviceType.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            context.commit();
        }
        try {
            inMemoryPersistence.getDeviceService().changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion() , configOfOtherDeviceType.getId(), configOfOtherDeviceType.getVersion());
        } catch (DeviceConfigurationChangeException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.CANNOT_CHANGE_DEVICE_CONFIG_TO_OTHER_DEVICE_TYPE)) {
                e.printStackTrace();
                fail("Should have gotten an exception indicating that you can not change the config to the config of another devicetype.");
            }
            throw e;
        }
    }

    @Test
    public void configChangeCreatesNewMeterActivationTest() {
        Instant initialClock = freezeClock(2012, 2, 9, 1, 11, 0, 0);
        Device device;
        DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", initialClock);
            context.commit();
        }

        Instant instant = freezeClock(2015, 9, 18, 11, 30, 0, 0);

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());
        assertThat(modifiedDevice.getCurrentMeterActivation().get().getStart()).isEqualTo(instant);
        assertThat(modifiedDevice.getMeterActivationsMostRecentFirst()).hasSize(2);
    }

    @Test
    public void changeConfigWithSingleSameRegisterSpecTest() {
        Device device;
        DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            RegisterType registerType = getRegisterTypeForReadingType(readingTypeMRID1);
            enhanceDeviceTypeWithRegisterTypes(this.deviceType, registerType);
            DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
            enhanceConfigBuilderWithRegisterTypes(firstConfigBuilder, registerType);
            DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
            firstDeviceConfiguration.activate();
            DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
            enhanceConfigBuilderWithRegisterTypes(secondDeviceConfigBuilder, registerType);
            secondDeviceConfiguration = secondDeviceConfigBuilder.add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            context.commit();
        }

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getRegisters()).hasSize(1);
        assertThat(modifiedDevice.getRegisters().get(0).getRegisterSpec().getId()).isEqualTo(secondDeviceConfiguration.getRegisterSpecs().get(0).getId());
        assertThat(modifiedDevice.getRegisters().get(0).getReadingType().getMRID()).isEqualTo(readingTypeMRID1);
    }

    @Test
    public void changeConfigWithSingleOtherRegisterSpecTest() {
        Device device;
        DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            RegisterType registerType1 = getRegisterTypeForReadingType(readingTypeMRID1);
            RegisterType registerType2 = getRegisterTypeForReadingType(readingTypeMRID2);
            enhanceDeviceTypeWithRegisterTypes(this.deviceType, registerType1, registerType2);
            DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
            enhanceConfigBuilderWithRegisterTypes(firstConfigBuilder, registerType1);
            DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
            firstDeviceConfiguration.activate();
            DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
            enhanceConfigBuilderWithRegisterTypes(secondDeviceConfigBuilder, registerType2);
            secondDeviceConfiguration = secondDeviceConfigBuilder.add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            context.commit();
        }

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        // registerType1 will NOT exist anymore on the device

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getRegisters()).hasSize(1);
        assertThat(modifiedDevice.getRegisters().get(0).getRegisterSpec().getId()).isEqualTo(secondDeviceConfiguration.getRegisterSpecs().get(0).getId());
        assertThat(modifiedDevice.getRegisters().get(0).getReadingType().getMRID()).isEqualTo(readingTypeMRID2);
    }

    @Test
    public void changeConfigWithSingleRegisterSpecToTwoRegisterSpecsTest() {
        Device device;
        DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            RegisterType registerType1 = getRegisterTypeForReadingType(readingTypeMRID1);
            RegisterType registerType2 = getRegisterTypeForReadingType(readingTypeMRID2);
            enhanceDeviceTypeWithRegisterTypes(this.deviceType, registerType1, registerType2);
            DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
            enhanceConfigBuilderWithRegisterTypes(firstConfigBuilder, registerType1);
            DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
            firstDeviceConfiguration.activate();
            DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
            enhanceConfigBuilderWithRegisterTypes(secondDeviceConfigBuilder, registerType1, registerType2);
            secondDeviceConfiguration = secondDeviceConfigBuilder.add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            context.commit();
        }

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getRegisters()).hasSize(2);
        assertThat(modifiedDevice.getRegisters()).haveExactly(1, new Condition<Register>() {
            @Override
            public boolean matches(Register register) {
                return register.getRegisterSpecId() == secondDeviceConfiguration.getRegisterSpecs().get(0).getId() &&
                        register.getReadingType().getMRID().equals(secondDeviceConfiguration.getRegisterSpecs().get(0).getReadingType().getMRID());
            }
        });
        assertThat(modifiedDevice.getRegisters()).haveExactly(1, new Condition<Register>() {
            @Override
            public boolean matches(Register register) {
                return register.getRegisterSpecId() == secondDeviceConfiguration.getRegisterSpecs().get(1).getId() &&
                        register.getReadingType().getMRID().equals(secondDeviceConfiguration.getRegisterSpecs().get(1).getReadingType().getMRID());
            }
        });
    }

    @Test
    public void changeConfigWithTwoRegisterSpecsToOneRegisterSpecTest() {
        DeviceConfiguration secondDeviceConfiguration;
        Device device;
        try (TransactionContext context = getTransactionService().getContext()) {
            RegisterType registerType1 = getRegisterTypeForReadingType(readingTypeMRID1);
            RegisterType registerType2 = getRegisterTypeForReadingType(readingTypeMRID2);
            enhanceDeviceTypeWithRegisterTypes(this.deviceType, registerType1, registerType2);
            DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
            enhanceConfigBuilderWithRegisterTypes(firstConfigBuilder, registerType1, registerType2);
            DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
            firstDeviceConfiguration.activate();
            DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
            enhanceConfigBuilderWithRegisterTypes(secondDeviceConfigBuilder, registerType1);
            secondDeviceConfiguration = secondDeviceConfigBuilder.add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            context.commit();
        }

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getRegisters()).hasSize(1);
        assertThat(modifiedDevice.getRegisters()).haveExactly(1, new Condition<Register>() {
            @Override
            public boolean matches(Register register) {
                return register.getRegisterSpecId() == secondDeviceConfiguration.getRegisterSpecs().get(0).getId() &&
                        register.getReadingType().getMRID().equals(secondDeviceConfiguration.getRegisterSpecs().get(0).getReadingType().getMRID());
            }
        });
    }

    @Test
    public void changeConfigWithSingleOtherLoadProfileSpecTest() {
        Device device;
        DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            RegisterType registerType1 = getRegisterTypeForReadingType(readingTypeMRID1);
            RegisterType registerType2 = getRegisterTypeForReadingType(readingTypeMRID2);
            LoadProfileType loadProfileType1 = createLoadProfileType("MyLoadProfile", ObisCode.fromString("1.0.99.1.0.255"), registerType1, registerType2);
            LoadProfileType loadProfileType2 = createLoadProfileType("MySecondLoadProfile", ObisCode.fromString("1.0.99.1.0.255"), registerType1);

            enhanceDeviceTypeWithLoadProfileTypes(this.deviceType, loadProfileType1, loadProfileType2);
            DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
            enhanceConfigBuilderWithLoadProfileTypes(firstConfigBuilder, loadProfileType1);
            DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
            firstDeviceConfiguration.activate();
            DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
            enhanceConfigBuilderWithLoadProfileTypes(secondDeviceConfigBuilder, loadProfileType2);
            secondDeviceConfiguration = secondDeviceConfigBuilder.add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            context.commit();
        }

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getLoadProfiles()).hasSize(1);
        assertThat(modifiedDevice.getLoadProfiles().get(0).getLoadProfileSpec().getId()).isEqualTo(secondDeviceConfiguration.getLoadProfileSpecs().get(0).getId());
        assertThat(modifiedDevice.getLoadProfiles().get(0).getChannels()).hasSize(1);
        assertThat(modifiedDevice.getLoadProfiles().get(0).getChannels()).haveExactly(1, new Condition<Channel>() {
            @Override
            public boolean matches(Channel channel) {
                return channel.getChannelSpec().getId() == secondDeviceConfiguration.getChannelSpecs().get(0).getId();
            }
        });
    }

    @Test
    public void changeConfigWithSingleSameLoadProfileSpecTest() {
        Device device;
        DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            RegisterType registerType1 = getRegisterTypeForReadingType(readingTypeMRID1);
            RegisterType registerType2 = getRegisterTypeForReadingType(readingTypeMRID2);
            LoadProfileType loadProfileType = createLoadProfileType("MyLoadProfile", ObisCode.fromString("1.0.99.1.0.255"), registerType1, registerType2);

            enhanceDeviceTypeWithLoadProfileTypes(this.deviceType, loadProfileType);
            DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
            enhanceConfigBuilderWithLoadProfileTypes(firstConfigBuilder, loadProfileType);
            DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
            firstDeviceConfiguration.activate();
            DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
            enhanceConfigBuilderWithLoadProfileTypes(secondDeviceConfigBuilder, loadProfileType);
            secondDeviceConfiguration = secondDeviceConfigBuilder.add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            context.commit();
        }

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getLoadProfiles()).hasSize(1);
        assertThat(modifiedDevice.getLoadProfiles().get(0).getLoadProfileSpec().getId()).isEqualTo(secondDeviceConfiguration.getLoadProfileSpecs().get(0).getId());
        assertThat(modifiedDevice.getLoadProfiles().get(0).getChannels()).hasSize(2);
        assertThat(modifiedDevice.getLoadProfiles().get(0).getChannels()).haveExactly(1, new Condition<Channel>() {
            @Override
            public boolean matches(Channel channel) {
                return channel.getChannelSpec().getId() == secondDeviceConfiguration.getChannelSpecs().get(0).getId();
            }
        });
        assertThat(modifiedDevice.getLoadProfiles().get(0).getChannels()).haveExactly(1, new Condition<Channel>() {
            @Override
            public boolean matches(Channel channel) {
                return channel.getChannelSpec().getId() == secondDeviceConfiguration.getChannelSpecs().get(1).getId();
            }
        });
    }

    @Test
    public void changeConfigWithSingleOtherLogBookSpecTest() {

        Device device;
        DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            final LogBookType logBookType1 = createLogBookType("MyFirstLogBookType", ObisCode.fromString("0.0.99.98.0.255"));
            final LogBookType logBookType2 = createLogBookType("MySecondLogBookType", ObisCode.fromString("1.0.99.98.0.255"));
            enhanceDeviceTypeWithLogBookTypes(this.deviceType, logBookType1, logBookType2);
            DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
            enhanceConfigBuilderWithLogBookTypes(firstConfigBuilder, logBookType1);
            DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
            firstDeviceConfiguration.activate();
            DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
            enhanceConfigBuilderWithLogBookTypes(secondDeviceConfigBuilder, logBookType2);
            secondDeviceConfiguration = secondDeviceConfigBuilder.add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            context.commit();
        }

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getLogBooks()).hasSize(1);
        assertThat(modifiedDevice.getLogBooks().get(0).getLogBookSpec().getId()).isEqualTo(secondDeviceConfiguration.getLogBookSpecs().get(0).getId());
    }

    @Test
    public void changeConfigWithSingleSameLogBookSpecTest() {
        Device device;
        DeviceConfiguration secondDeviceConfiguration;

        try (TransactionContext context = getTransactionService().getContext()) {
            final LogBookType logBookType1 = createLogBookType("MyFirstLogBookType", ObisCode.fromString("0.0.99.98.0.255"));
            enhanceDeviceTypeWithLogBookTypes(this.deviceType, logBookType1);
            DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
            enhanceConfigBuilderWithLogBookTypes(firstConfigBuilder, logBookType1);
            DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
            firstDeviceConfiguration.activate();
            DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
            enhanceConfigBuilderWithLogBookTypes(secondDeviceConfigBuilder, logBookType1);
            secondDeviceConfiguration = secondDeviceConfigBuilder.add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            context.commit();
        }

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getLogBooks()).hasSize(1);
        assertThat(modifiedDevice.getLogBooks().get(0).getLogBookSpec().getId()).isEqualTo(secondDeviceConfiguration.getLogBookSpecs().get(0).getId());
    }

    @Test(expected = CannotChangeDeviceConfigStillUnresolvedConflicts.class)
    public void changeConfigWhileThereAreStillConflictingMappingsTest() {
        Device device;
        final DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            final OutboundComPortPool outboundIpPool = createOutboundIpComPortPool("OutboundIpPool");
            final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").isDirectlyAddressable(true).add();
            final PartialScheduledConnectionTaskImpl myFirstConnectionTask = createPartialConnectionTask(firstDeviceConfiguration, "MyDefaultConnectionTaskName", outboundIpPool);
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").isDirectlyAddressable(true).add();
            final PartialScheduledConnectionTaskImpl mySecondConnectionTask = createPartialConnectionTask(secondDeviceConfiguration, "MySecondConnectionTask", outboundIpPool);
            secondDeviceConfiguration.activate();

            updateConflictsFor(mySecondConnectionTask, connectionTaskCreatedTopic);

            assertThat(deviceType.getDeviceConfigConflictMappings()).hasSize(2);

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            context.commit();
        }
        inMemoryPersistence.getDeviceService().changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion() , secondDeviceConfiguration.getId(), secondDeviceConfiguration.getVersion());
    }

    @Test
    public void changeConfigWithNoConflictConnectionMethodsTest() {
        final String connectionTaskName = "MyDefaultConnectionTaskName";
        final PartialScheduledConnectionTaskImpl mySecondConnectionTask;
        Device device;
        final DeviceConfiguration secondDeviceConfiguration;

        try (TransactionContext context = getTransactionService().getContext()) {
            final OutboundComPortPool outboundIpPool = createOutboundIpComPortPool("OutboundIpPool");
            final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").isDirectlyAddressable(true).add();
            final PartialScheduledConnectionTaskImpl myFirstConnectionTask = createPartialConnectionTask(firstDeviceConfiguration, connectionTaskName, outboundIpPool);
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").isDirectlyAddressable(true).add();
            mySecondConnectionTask = createPartialConnectionTask(secondDeviceConfiguration, connectionTaskName, outboundIpPool);
            secondDeviceConfiguration.activate();

            updateConflictsFor(mySecondConnectionTask, connectionTaskCreatedTopic);
            assertThat(deviceType.getDeviceConfigConflictMappings()).isEmpty();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            device.getScheduledConnectionTaskBuilder(myFirstConnectionTask).setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE).add();

            context.commit();
        }
        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getConnectionTasks().get(0).getPartialConnectionTask().getId()).isEqualTo(mySecondConnectionTask.getId());
    }

    @Test
    public void changeConfigWithNoConflictsRemoveAndMapConnectionMethodsTest() {
        final String firstConnectionTaskName = "myFirstConnectionTaskName";
        final String secondConnectionTaskName = "mySecondConnectionTaskName";
        final PartialScheduledConnectionTaskImpl otherSecondConnectionTask;
        Device device;
        final DeviceConfiguration secondDeviceConfiguration;

        try (TransactionContext context = getTransactionService().getContext()) {
            final OutboundComPortPool outboundIpPool = createOutboundIpComPortPool("OutboundIpPool");
            final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").isDirectlyAddressable(true).add();
            final PartialScheduledConnectionTaskImpl myFirstConnectionTask = createPartialConnectionTask(firstDeviceConfiguration, firstConnectionTaskName, outboundIpPool);
            final PartialScheduledConnectionTaskImpl mySecondConnectionTask = createPartialConnectionTask(firstDeviceConfiguration, secondConnectionTaskName, outboundIpPool);
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").isDirectlyAddressable(true).add();
            otherSecondConnectionTask = createPartialConnectionTask(secondDeviceConfiguration, firstConnectionTaskName, outboundIpPool);
            secondDeviceConfiguration.activate();

            updateConflictsFor(otherSecondConnectionTask, connectionTaskCreatedTopic);
            assertThat(deviceType.getDeviceConfigConflictMappings()).isEmpty();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            final ScheduledConnectionTask originalScheduledConnectionTask = device.getScheduledConnectionTaskBuilder(myFirstConnectionTask)
                    .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE)
                    .add();
            final ScheduledConnectionTask originalSecondScheduledConnectionTask = device.getScheduledConnectionTaskBuilder(mySecondConnectionTask)
                    .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE)
                    .add();

            context.commit();
        }
        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getConnectionTasks()).hasSize(1);
        assertThat(modifiedDevice.getConnectionTasks().get(0).getPartialConnectionTask().getId()).isEqualTo(otherSecondConnectionTask.getId());
    }

    @Test
    public void changeConfigWithConflictAndResolvedRemoveActionTest() {
        Device device;
        final DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            final OutboundComPortPool outboundIpPool = createOutboundIpComPortPool("OutboundIpPool");
            final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").isDirectlyAddressable(true).add();
            final PartialScheduledConnectionTaskImpl myFirstConnectionTask = createPartialConnectionTask(firstDeviceConfiguration, "MyDefaultConnectionTaskName", outboundIpPool);
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").isDirectlyAddressable(true).add();
            final PartialScheduledConnectionTaskImpl mySecondConnectionTask = createPartialConnectionTask(secondDeviceConfiguration, "MySecondConnectionTask", outboundIpPool);
            secondDeviceConfiguration.activate();

            updateConflictsFor(mySecondConnectionTask, connectionTaskCreatedTopic);
            final DeviceConfigConflictMapping deviceConfigConflictMapping = getDeviceConfigConflictMapping(firstDeviceConfiguration, secondDeviceConfiguration);
            deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().get(0).markSolutionAsRemove();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            final ScheduledConnectionTask originalScheduledConnectionTask = device.getScheduledConnectionTaskBuilder(myFirstConnectionTask)
                    .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE)
                    .add();

            context.commit();
        }
        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getConnectionTasks()).isEmpty();
    }

    @Test
    public void changeConfigWithConflictAndResolvedMapActionTest() {
        Device device;
        final PartialScheduledConnectionTaskImpl mySecondConnectionTask;
        final DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            final OutboundComPortPool outboundIpPool = createOutboundIpComPortPool("OutboundIpPool");
            final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").isDirectlyAddressable(true).add();
            final PartialScheduledConnectionTaskImpl myFirstConnectionTask = createPartialConnectionTask(firstDeviceConfiguration, "MyDefaultConnectionTaskName", outboundIpPool);
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").isDirectlyAddressable(true).add();
            mySecondConnectionTask = createPartialConnectionTask(secondDeviceConfiguration, "MySecondConnectionTask", outboundIpPool);
            secondDeviceConfiguration.activate();

            updateConflictsFor(mySecondConnectionTask, connectionTaskCreatedTopic);
            final DeviceConfigConflictMapping deviceConfigConflictMapping = getDeviceConfigConflictMapping(firstDeviceConfiguration, secondDeviceConfiguration);
            deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().get(0).markSolutionAsMap(mySecondConnectionTask);

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            final ScheduledConnectionTask originalScheduledConnectionTask = device.getScheduledConnectionTaskBuilder(myFirstConnectionTask)
                    .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE)
                    .add();

            context.commit();
        }
        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getConnectionTasks().get(0).getPartialConnectionTask().getId()).isEqualTo(mySecondConnectionTask.getId());
    }

    @Test
    public void changeConfigWithConflictAndResolvedMapWithPropertiesTest() {
        Device device;
        final PartialScheduledConnectionTaskImpl mySecondConnectionTask;
        final DeviceConfiguration secondDeviceConfiguration;
        final String ipAddressValue = "10.0.66.99";
        final BigDecimal portNumberValue = new BigDecimal(1235L);
        final String ipAddressPropertyName = "ipAddress";
        final String portNumberPropertyName = "port";

        try (TransactionContext context = getTransactionService().getContext()) {
            final OutboundComPortPool outboundIpPool = createOutboundIpComPortPool("OutboundIpPool");
            final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").isDirectlyAddressable(true).add();
            final PartialScheduledConnectionTaskImpl myFirstConnectionTask = createPartialConnectionTask(firstDeviceConfiguration, "MyDefaultConnectionTaskName", outboundIpPool);
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").isDirectlyAddressable(true).add();
            mySecondConnectionTask = createPartialConnectionTask(secondDeviceConfiguration, "MySecondConnectionTask", outboundIpPool);
            secondDeviceConfiguration.activate();

            updateConflictsFor(mySecondConnectionTask, connectionTaskCreatedTopic);
            final DeviceConfigConflictMapping deviceConfigConflictMapping = getDeviceConfigConflictMapping(firstDeviceConfiguration, secondDeviceConfiguration);
            deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().get(0).markSolutionAsMap(mySecondConnectionTask);
            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            final ScheduledConnectionTask originalScheduledConnectionTask = device.getScheduledConnectionTaskBuilder(myFirstConnectionTask)
                    .setProperty(ipAddressPropertyName, ipAddressValue)
                    .setProperty(portNumberPropertyName, portNumberValue)
                    .add();
            context.commit();
        }
        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getConnectionTasks().get(0).getPartialConnectionTask().getId()).isEqualTo(mySecondConnectionTask.getId());
        assertThat(modifiedDevice.getConnectionTasks().get(0).getProperty(ipAddressPropertyName).getValue()).isEqualTo(ipAddressValue);
        assertThat(modifiedDevice.getConnectionTasks().get(0).getProperty(portNumberPropertyName).getValue()).isEqualTo(portNumberValue);
    }


    @Test(expected = CannotChangeDeviceConfigStillUnresolvedConflicts.class)
    public void changeConfigWhileThereAreStillSecuritySetConflictingMappingsTest() {
        Device device;
        final DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            final SecurityPropertySet firstSecurityPropertySet = firstDeviceConfiguration.createSecurityPropertySet("NoSecurity").encryptionLevel(0).authenticationLevel(0).build();
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
            final SecurityPropertySet secondSecurityPropertySet = secondDeviceConfiguration.createSecurityPropertySet("None").encryptionLevel(0).authenticationLevel(0).build();
            secondDeviceConfiguration.activate();

            updateConflictsFor(secondSecurityPropertySet, securitySetCreatedTopic);

            assertThat(deviceType.getDeviceConfigConflictMappings()).hasSize(2);

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            context.commit();
        }
        inMemoryPersistence.getDeviceService().changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion() , secondDeviceConfiguration.getId(), secondDeviceConfiguration.getVersion());
    }

    @Test
    public void changeConfigWithNoConflictingSecurityPropertySetsAndValidPropertiesTest() {
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());
        final SecurityPropertySet firstSecurityPropertySet;
        final SecurityPropertySet secondSecurityPropertySet;
        Device device;
        final DeviceConfiguration secondDeviceConfiguration;

        try (TransactionContext context = getTransactionService().getContext()) {
            final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            final String securityPropertySetName = "NoSecurity";
            firstSecurityPropertySet = createSecurityPropertySet(firstDeviceConfiguration, securityPropertySetName);
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
            secondSecurityPropertySet = createSecurityPropertySet(secondDeviceConfiguration, securityPropertySetName);
            secondDeviceConfiguration.activate();

            updateConflictsFor(secondSecurityPropertySet, securitySetCreatedTopic);
            assertThat(deviceType.getDeviceConfigConflictMappings()).isEmpty();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            TypedProperties securityProperties = TypedProperties.empty();
            securityProperties.setProperty(BasicAuthenticationSecurityProperties.ActualFields.PASSWORD.javaName(), "12345678");
            securityProperties.setProperty(BasicAuthenticationSecurityProperties.ActualFields.USER_NAME.javaName(), "C3P0");
            device.setSecurityProperties(firstSecurityPropertySet, securityProperties);
            device.save();
            context.commit();
        }
        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        try (TransactionContext context = getTransactionService().getContext()) {
            assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
            assertThat(modifiedDevice.getSecurityProperties(secondSecurityPropertySet).get(0).getName()).isEqualTo(BasicAuthenticationSecurityProperties.ActualFields.PASSWORD.javaName());
            assertThat(modifiedDevice.getSecurityProperties(firstSecurityPropertySet)).isEmpty();
            context.commit();
        }
    }

    @Test
    public void changeConfigWithNoConflictingSecurityPropertySetsAndValidPropertiesRemoveAndMapTest() {
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());
        final SecurityPropertySet firstSecurityPropertySet;
        final SecurityPropertySet secondSecurityPropertySet;
        final SecurityPropertySet otherSecurityPropertySet;
        Device device;
        final DeviceConfiguration secondDeviceConfiguration;

        try (TransactionContext context = getTransactionService().getContext()) {
            final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            final String firstSecurityPropertySetName = "NoSecurity";
            final String secondSecurityPropertySetName = "OtherSecurity";
            firstSecurityPropertySet = createSecurityPropertySet(firstDeviceConfiguration, firstSecurityPropertySetName);
            secondSecurityPropertySet = createSecurityPropertySet(firstDeviceConfiguration, secondSecurityPropertySetName);
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
            otherSecurityPropertySet = createSecurityPropertySet(secondDeviceConfiguration, firstSecurityPropertySetName);
            secondDeviceConfiguration.activate();

            updateConflictsFor(otherSecurityPropertySet, securitySetCreatedTopic);
            assertThat(deviceType.getDeviceConfigConflictMappings()).isEmpty();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            TypedProperties securityProperties = TypedProperties.empty();
            securityProperties.setProperty(BasicAuthenticationSecurityProperties.ActualFields.PASSWORD.javaName(), "12345678");
            securityProperties.setProperty(BasicAuthenticationSecurityProperties.ActualFields.USER_NAME.javaName(), "C3P0");
            device.setSecurityProperties(firstSecurityPropertySet, securityProperties);
            device.setSecurityProperties(secondSecurityPropertySet, securityProperties);
            device.save();

            context.commit();
        }
        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        try (TransactionContext context = getTransactionService().getContext()) {
            assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
            assertThat(modifiedDevice.getSecurityProperties(otherSecurityPropertySet).get(0).getName()).isEqualTo(BasicAuthenticationSecurityProperties.ActualFields.PASSWORD.javaName());
            assertThat(modifiedDevice.getSecurityProperties(firstSecurityPropertySet)).isEmpty();
            assertThat(modifiedDevice.getSecurityProperties(secondSecurityPropertySet)).isEmpty();
            context.commit();
        }
    }

    @Test
    public void changeConfigWithConflictingSecurityPropertySetsAndMapSolutionTest() {
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());

        Device device;
        final DeviceConfiguration secondDeviceConfiguration;
        final SecurityPropertySet secondSecurityPropertySet;
        final SecurityPropertySet firstSecurityPropertySet;
        try (TransactionContext context = getTransactionService().getContext()) {
            final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            final String firstSecurityPropertySetName = "NoSecurity";
            final String secondSecurityPropertySetName = "AnotherSecurityPropertySetName";
            firstSecurityPropertySet = createSecurityPropertySet(firstDeviceConfiguration, firstSecurityPropertySetName);
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
            secondSecurityPropertySet = createSecurityPropertySet(secondDeviceConfiguration, secondSecurityPropertySetName);
            secondDeviceConfiguration.activate();

            updateConflictsFor(secondSecurityPropertySet, securitySetCreatedTopic);
            final DeviceConfigConflictMapping deviceConfigConflictMapping = getDeviceConfigConflictMapping(firstDeviceConfiguration, secondDeviceConfiguration);
            deviceConfigConflictMapping.getConflictingSecuritySetSolutions().get(0).markSolutionAsMap(secondSecurityPropertySet);

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            TypedProperties securityProperties = TypedProperties.empty();
            securityProperties.setProperty(BasicAuthenticationSecurityProperties.ActualFields.PASSWORD.javaName(), "12345678");
            securityProperties.setProperty(BasicAuthenticationSecurityProperties.ActualFields.USER_NAME.javaName(), "C3P0");
            device.setSecurityProperties(firstSecurityPropertySet, securityProperties);
            device.getSecurityProperties(firstSecurityPropertySet);
            device.save();
            context.commit();
        }


        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        try (TransactionContext context = getTransactionService().getContext()) {
            assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
            assertThat(modifiedDevice.getSecurityProperties(secondSecurityPropertySet).get(0).getName()).isEqualTo(BasicAuthenticationSecurityProperties.ActualFields.PASSWORD.javaName());
            assertThat(modifiedDevice.getSecurityProperties(firstSecurityPropertySet)).isEmpty();
            context.commit();
        }
    }

    @Test
    public void changeConfigWithConflictingSecurityPropertySetsAndRemoveSolutionTest() {
        Device device;
        final SecurityPropertySet secondSecurityPropertySet;
        final DeviceConfiguration secondDeviceConfiguration;
        final SecurityPropertySet firstSecurityPropertySet;
        try (TransactionContext context = getTransactionService().getContext()) {
            final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            final String firstSecurityPropertySetName = "NoSecurity";
            final String secondSecurityPropertySetName = "AnotherSecurityPropertySetName";
            firstSecurityPropertySet = createSecurityPropertySet(firstDeviceConfiguration, firstSecurityPropertySetName);
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
            secondSecurityPropertySet = createSecurityPropertySet(secondDeviceConfiguration, secondSecurityPropertySetName);
            secondDeviceConfiguration.activate();

            updateConflictsFor(secondSecurityPropertySet, securitySetCreatedTopic);
            final DeviceConfigConflictMapping deviceConfigConflictMapping = getDeviceConfigConflictMapping(firstDeviceConfiguration, secondDeviceConfiguration);
            deviceConfigConflictMapping.getConflictingSecuritySetSolutions().get(0).markSolutionAsRemove();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            TypedProperties securityProperties = TypedProperties.empty();
            securityProperties.setProperty(BasicAuthenticationSecurityProperties.ActualFields.PASSWORD.javaName(), "12345678");
            securityProperties.setProperty(BasicAuthenticationSecurityProperties.ActualFields.USER_NAME.javaName(), "C3P0");
            device.setSecurityProperties(firstSecurityPropertySet, securityProperties);
            device.save();
            context.commit();
        }
        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        try (TransactionContext context = getTransactionService().getContext()) {
            assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
            assertThat(modifiedDevice.getSecurityProperties(secondSecurityPropertySet)).isEmpty();
            assertThat(modifiedDevice.getSecurityProperties(firstSecurityPropertySet)).isEmpty();
            context.commit();
        }
    }

    @Test
    public void changeConfigRemovesNonExistingManuallyScheduledComTaskExecutions() {
        Device device;
        final DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            final ComTask comTaskForTesting = inMemoryPersistence.getTaskService().newComTask("ComTaskForTesting");
            comTaskForTesting.createClockTask(ClockTaskType.FORCECLOCK).add();
            comTaskForTesting.save();
            final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            final String securityPropertySetName = "NoSecurity";
            final SecurityPropertySet firstSecurityPropertySet = createSecurityPropertySet(firstDeviceConfiguration, securityPropertySetName);
            final ComTaskEnablement comTaskEnablement1 = createComTaskEnablement(comTaskForTesting, firstDeviceConfiguration, firstSecurityPropertySet);
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
            final SecurityPropertySet secondSecurityPropertySet = createSecurityPropertySet(secondDeviceConfiguration, securityPropertySetName);
            secondDeviceConfiguration.activate();

            updateConflictsFor(secondSecurityPropertySet, securitySetCreatedTopic);
            assertThat(deviceType.getDeviceConfigConflictMappings()).isEmpty();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            final ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = device.newAdHocComTaskExecution(comTaskEnablement1).add();
            device.save();
            context.commit();
        }

        assertThat(device.getComTaskExecutions()).hasSize(1);

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());
        assertThat(modifiedDevice.getComTaskExecutions()).isEmpty();
    }

    @Test
    public void changeConfigRemovesNonExistingComScheduleScheduledComTaskExecutionsTest() {
        Device device;
        final DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            final ComTask clockComTask = inMemoryPersistence.getTaskService().newComTask("ClockComTask");
            clockComTask.createClockTask(ClockTaskType.FORCECLOCK).add();
            clockComTask.save();
            final ComScheduleBuilder mySchedule = inMemoryPersistence.getSchedulingService().newComSchedule("MySchedule", new TemporalExpression(TimeDuration.days(1)), Instant.now());
            mySchedule.addComTask(clockComTask);
            ComSchedule comSchedule = mySchedule.build();

            final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            final String securityPropertySetName = "NoSecurity";
            final SecurityPropertySet firstSecurityPropertySet = createSecurityPropertySet(firstDeviceConfiguration, securityPropertySetName);
            final ComTaskEnablement comTaskEnablement1 = createComTaskEnablement(clockComTask, firstDeviceConfiguration, firstSecurityPropertySet);
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
            final SecurityPropertySet secondSecurityPropertySet = createSecurityPropertySet(secondDeviceConfiguration, securityPropertySetName);
            secondDeviceConfiguration.activate();

            updateConflictsFor(secondSecurityPropertySet, securitySetCreatedTopic);
            assertThat(deviceType.getDeviceConfigConflictMappings()).isEmpty();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            final ScheduledComTaskExecution scheduledComTaskExecution = device.newScheduledComTaskExecution(comSchedule).add();
            device.save();
            context.commit();
        }

        assertThat(device.getComTaskExecutions()).hasSize(1);

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());
        assertThat(modifiedDevice.getComTaskExecutions()).isEmpty();
    }

    @Test
    public void changeConfigWithConfiguredDialectPropertyTest() {
        final String myPropertyValue = "MyPropertyValue";
        Device device;
        DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            firstDeviceConfiguration.activate();
            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.setProtocolDialectProperty(ProtocolDialectPropertiesImplIT.DIALECT_1_NAME, ProtocolDialectPropertiesImplIT.REQUIRED_PROPERTY_NAME, myPropertyValue);
            device.save();

            final ProtocolDialectProperties protocolDialectProperties = device.getProtocolDialectProperties(ProtocolDialectPropertiesImplIT.DIALECT_1_NAME).get();
            assertThat(protocolDialectProperties.getTypedProperties().getProperty(ProtocolDialectPropertiesImplIT.REQUIRED_PROPERTY_NAME)).isEqualTo(myPropertyValue);
            assertThat(protocolDialectProperties.getProtocolDialectConfigurationProperties().getDeviceConfiguration().getId()).isEqualTo(firstDeviceConfiguration.getId());
            context.commit();
        }

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        try (TransactionContext context = getTransactionService().getContext()) {
            assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
            final ProtocolDialectProperties protocolDialectProperties1 = modifiedDevice.getProtocolDialectProperties(ProtocolDialectPropertiesImplIT.DIALECT_1_NAME).get();
            assertThat(protocolDialectProperties1.getTypedProperties().getProperty(ProtocolDialectPropertiesImplIT.REQUIRED_PROPERTY_NAME)).isEqualTo(myPropertyValue);
            assertThat(protocolDialectProperties1.getProtocolDialectConfigurationProperties().getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
            context.commit();
        }
    }

    @Test
    public void generalPropertiesWithConfigurationChangeTest() {
        final String configValue = "myConfigValueForTheOptionalProperty";
        final String propertyName = TestProtocol.MYOPTIONALPROPERTY;
        Device device;
        DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            firstDeviceConfiguration.activate();
            firstDeviceConfiguration.getDeviceProtocolProperties().setProperty(propertyName, configValue);
            firstDeviceConfiguration.save();

            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            assertThat(device.getDeviceProtocolProperties().getProperty(propertyName)).isEqualTo(configValue);
            context.commit();
        }

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());
        assertThat(modifiedDevice.getDeviceProtocolProperties().getProperty(propertyName)).isNull();
    }

    @Test
    public void generalPropertyOnDeviceWithConfigChangeTest() {
        Device device;
        DeviceConfiguration secondDeviceConfiguration;
        final String deviceValue = "MyDeviceValueForTheOptionalProperty";
        final String propertyName = TestProtocol.MYOPTIONALPROPERTY;
        try (TransactionContext context = getTransactionService().getContext()) {
            DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            firstDeviceConfiguration.activate();

            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.setProtocolProperty(propertyName, deviceValue);
            device.save();
            assertThat(device.getDeviceProtocolProperties().getProperty(propertyName)).isEqualTo(deviceValue);
            context.commit();
        }

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());
        assertThat(modifiedDevice.getDeviceProtocolProperties().getProperty(propertyName)).isEqualTo(deviceValue);
    }

    @Test
    public void generalPropertyOnDeviceAndOnConfigRemoveOnDeviceAfterConfigChangeTest() {
        Device device;
        DeviceConfiguration secondDeviceConfiguration;
        final String deviceValue = "MyDeviceValueForTheOptionalProperty";
        final String configValue = "myConfigValueForTheOptionalProperty";
        final String propertyName = TestProtocol.MYOPTIONALPROPERTY;
        try (TransactionContext context = getTransactionService().getContext()) {
            DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            firstDeviceConfiguration.activate();

            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
            secondDeviceConfiguration.activate();
            secondDeviceConfiguration.getDeviceProtocolProperties().setProperty(propertyName, configValue);
            secondDeviceConfiguration.save();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.setProtocolProperty(propertyName, deviceValue);
            device.save();
            assertThat(device.getDeviceProtocolProperties().getProperty(propertyName)).isEqualTo(deviceValue);
            context.commit();
        }

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());
        assertThat(modifiedDevice.getDeviceProtocolProperties().getProperty(propertyName)).isEqualTo(deviceValue);

        try (TransactionContext context = getTransactionService().getContext()) {
            modifiedDevice.removeProtocolProperty(propertyName);
            modifiedDevice.save();
            context.commit();
        }

        final Device reloadedDevice = inMemoryPersistence.getDeviceService().findDeviceById(modifiedDevice.getId()).get();
        assertThat(reloadedDevice.getDeviceProtocolProperties().getProperty(propertyName)).isEqualTo(configValue);
    }

    @Test
    public void configChangeToConfigWithNoMessagesAllowedTest() {
        Device device;
        DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
            firstDeviceConfiguration.activate();

            secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
            secondDeviceConfiguration.activate();
            secondDeviceConfiguration.save();

            final List<DeviceMessageId> deviceMessageIds = secondDeviceConfiguration.getDeviceMessageEnablements().stream().map(DeviceMessageEnablement::getDeviceMessageId).collect(Collectors.toList());
            deviceMessageIds.forEach(secondDeviceConfiguration::removeDeviceMessageEnablement);
            secondDeviceConfiguration.save();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID", Instant.now());
            device.save();
            device.newDeviceMessage(DeviceMessageId.CONTACTOR_CLOSE).setReleaseDate(Instant.now()).add(); // should not fail!
            context.commit();
        }

        Device modifiedDevice = inMemoryPersistence.getDeviceService()
                .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                        .getId(), secondDeviceConfiguration.getVersion());

        boolean ok = false; // keeping track of a boolean because we need to validate that the message was called due to the below method
        try (TransactionContext context = getTransactionService().getContext()) {
            try {
                modifiedDevice.newDeviceMessage(DeviceMessageId.CONTACTOR_CLOSE).setReleaseDate(Instant.now()).add(); // should fail!
            } catch (ConstraintViolationException e) {
                if (!e.getMessage().contains("deviceMessage.not.allowed.config")) {
                    fail("Should have gotten an exception indicating that we could not create the message");
                } else {
                    ok = true;
                }
            }
            context.commit();
        }

        if (!ok) {
            fail("Damn, we should have landed in the constraintviolationexception ...");
        }
    }

    @Test(expected = DeviceConfigurationChangeException.class)
    public void cannotChangeConfigOfDataloggerSlaveTest() {
        Device device;

        DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            RegisterType registerType = getRegisterTypeForReadingType(readingTypeMRID1);
            DeviceType dataloggerSlaveDeviceType = inMemoryPersistence.getDeviceConfigurationService()
                    .newDataloggerSlaveDeviceTypeBuilder("DataLoggerSlave", getDefaultDeviceLifeCycle())
                    .withRegisterTypes(Collections.singletonList(registerType))
                    .create();
            DeviceType.DeviceConfigurationBuilder configBuilder1 = dataloggerSlaveDeviceType.newConfiguration("cannotChangeConfigOfDataloggerSlaveTest1");
            configBuilder1.newNumericalRegisterSpec(registerType)
                    .numberOfFractionDigits(0)
                    .overflowValue(BigDecimal.valueOf(100000000L));
            DeviceConfiguration firstDeviceConfiguration = configBuilder1.add();
            firstDeviceConfiguration.activate();
            DeviceType.DeviceConfigurationBuilder configBuilder2 = dataloggerSlaveDeviceType.newConfiguration("cannotChangeConfigOfDataloggerSlaveTest2");
            configBuilder2.newNumericalRegisterSpec(registerType)
                    .numberOfFractionDigits(0)
                    .overflowValue(BigDecimal.valueOf(100000000L));
            secondDeviceConfiguration = configBuilder2.add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "DataloggerSlave", inMemoryPersistence.getClock()
                            .instant());
            device.save();
            context.commit();
        }
        try {
            Device modifiedDevice = inMemoryPersistence.getDeviceService()
                    .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                            .getId(), secondDeviceConfiguration.getVersion());
        } catch (DeviceConfigurationChangeException e) {
            if (e.getMessageSeed().equals(MessageSeeds.CANNOT_CHANGE_CONFIG_DATALOGGER_SLAVE)) {
                throw e;
            } else {
                fail("Should have gotten an exception indicating that you can not change the config of a datalogger slave.");
            }
        }
    }

    @Test(expected = DeviceConfigurationChangeException.class)
    public void cannotChangeToADataLoggerEnabledConfig() {
        Device device;

        DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            RegisterType registerType = getRegisterTypeForReadingType(readingTypeMRID1);
            enhanceDeviceTypeWithRegisterTypes(this.deviceType, registerType);
            DeviceType.DeviceConfigurationBuilder configBuilder1 = deviceType.newConfiguration("cannotChangeToADataLoggerEnabledConfig1");
            configBuilder1.newNumericalRegisterSpec(registerType)
                    .numberOfFractionDigits(0)
                    .overflowValue(BigDecimal.valueOf(100000000L));
            DeviceConfiguration firstDeviceConfiguration = configBuilder1.add();
            firstDeviceConfiguration.activate();
            DeviceType.DeviceConfigurationBuilder configBuilder2 = deviceType.newConfiguration("cannotChangeToADataLoggerEnabledConfig2");
            configBuilder2.newNumericalRegisterSpec(registerType)
                    .numberOfFractionDigits(0)
                    .overflowValue(BigDecimal.valueOf(100000000L));
            configBuilder2.dataloggerEnabled(true);
            secondDeviceConfiguration = configBuilder2.add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "RegularDevice", inMemoryPersistence.getClock()
                            .instant());
            device.save();
            context.commit();
        }
        try {
            Device modifiedDevice = inMemoryPersistence.getDeviceService()
                    .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                            .getId(), secondDeviceConfiguration.getVersion());
        } catch (DeviceConfigurationChangeException e) {
            if (e.getMessageSeed().equals(MessageSeeds.CANNOT_CHANGE_CONFIG_TO_DATALOGGER_ENABLED)) {
                throw e;
            } else {
                fail("Should have gotten an exception indicating that you can not change the config to a datalogger enabled config.");
            }
        }
    }

    @Test(expected = DeviceConfigurationChangeException.class)
    public void cannotChangeFromADataLoggerEnabledConfig() {
        Device device;

        DeviceConfiguration secondDeviceConfiguration;
        try (TransactionContext context = getTransactionService().getContext()) {
            RegisterType registerType = getRegisterTypeForReadingType(readingTypeMRID1);
            enhanceDeviceTypeWithRegisterTypes(this.deviceType, registerType);
            DeviceType.DeviceConfigurationBuilder configBuilder1 = deviceType.newConfiguration("cannotChangeToADataLoggerEnabledConfig1");
            configBuilder1.newNumericalRegisterSpec(registerType)
                    .numberOfFractionDigits(0)
                    .overflowValue(BigDecimal.valueOf(100000000L));
            configBuilder1.dataloggerEnabled(true);
            DeviceConfiguration firstDeviceConfiguration = configBuilder1.add();
            firstDeviceConfiguration.activate();
            DeviceType.DeviceConfigurationBuilder configBuilder2 = deviceType.newConfiguration("cannotChangeToADataLoggerEnabledConfig2");
            configBuilder2.newNumericalRegisterSpec(registerType)
                    .numberOfFractionDigits(0)
                    .overflowValue(BigDecimal.valueOf(100000000L));
            secondDeviceConfiguration = configBuilder2.add();
            secondDeviceConfiguration.activate();

            device = inMemoryPersistence.getDeviceService()
                    .newDevice(firstDeviceConfiguration, "DeviceName", "AlreadyDataLoggerEnabled", inMemoryPersistence.getClock()
                            .instant());
            device.save();
            context.commit();
        }
        try {
            Device modifiedDevice = inMemoryPersistence.getDeviceService()
                    .changeDeviceConfigurationForSingleDevice(device.getId(), device.getVersion(), secondDeviceConfiguration
                            .getId(), secondDeviceConfiguration.getVersion());
        } catch (DeviceConfigurationChangeException e) {
            if (e.getMessageSeed().equals(MessageSeeds.CANNOT_CHANGE_CONFIG_FROM_DATALOGGER_ENABLED)) {
                throw e;
            } else {
                fail("Should have gotten an exception indicating that you can not change the config from a datalogger enabled config.");
            }
        }
    }

    private ComTaskEnablement createComTaskEnablement(ComTask comTaskForTesting, DeviceConfiguration firstDeviceConfiguration, SecurityPropertySet firstSecurityPropertySet) {
        return firstDeviceConfiguration.enableComTask(comTaskForTesting, firstSecurityPropertySet, firstDeviceConfiguration
                .getProtocolDialectConfigurationPropertiesList()
                .get(0))
                .useDefaultConnectionTask(true)
                .add();
    }

    private SecurityPropertySet createSecurityPropertySet(DeviceConfiguration deviceConfiguration, String securityPropertySetName) {
        return deviceConfiguration.createSecurityPropertySet(securityPropertySetName)
                .encryptionLevel(0)
                .authenticationLevel(0)
                .addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1)
                .addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1)
                .build();
    }

    private DeviceConfigConflictMapping getDeviceConfigConflictMapping(DeviceConfiguration firstDeviceConfiguration, DeviceConfiguration secondDeviceConfiguration) {
        return deviceType.getDeviceConfigConflictMappings().stream().filter(getDeviceConfigConflictMappingPredicate(firstDeviceConfiguration, secondDeviceConfiguration)).findFirst().get();
    }

    private Predicate<DeviceConfigConflictMapping> getDeviceConfigConflictMappingPredicate(DeviceConfiguration firstDeviceConfiguration, DeviceConfiguration secondDeviceConfiguration) {
        return deviceConfigConflictMapping -> deviceConfigConflictMapping.getOriginDeviceConfiguration()
                .getId() == firstDeviceConfiguration.getId() && deviceConfigConflictMapping.getDestinationDeviceConfiguration()
                .getId() == secondDeviceConfiguration.getId();
    }

    private PartialScheduledConnectionTaskImpl createPartialConnectionTask(DeviceConfiguration deviceConfiguration, String connectionTaskName, OutboundComPortPool comPortPool) {
        final PartialScheduledConnectionTaskBuilder partialScheduledConnectionTaskBuilder = deviceConfiguration.newPartialScheduledConnectionTask(connectionTaskName, outboundIpConnectionTypePluggableClass, scheduledConnectionTaskInterval, ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        partialScheduledConnectionTaskBuilder.comPortPool(comPortPool);
        return partialScheduledConnectionTaskBuilder.build();
    }

    private RegisterType getRegisterTypeForReadingType(String readingTypeMRID) {
        return inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(inMemoryPersistence.getMeteringService().getReadingType(readingTypeMRID).get()).get();
    }

    private void enhanceDeviceTypeWithRegisterTypes(DeviceType deviceType, RegisterType... registerType) {
        Stream.of(registerType).forEach(deviceType::addRegisterType);
    }

    private void enhanceDeviceTypeWithLogBookTypes(DeviceType deviceType, LogBookType... logBookTypes) {
        Stream.of(logBookTypes).forEach(deviceType::addLogBookType);
    }

    private void enhanceDeviceTypeWithLoadProfileTypes(DeviceType deviceType, LoadProfileType... loadProfileTypes) {
        Stream.of(loadProfileTypes).forEach(deviceType::addLoadProfileType);
    }

    private void enhanceConfigBuilderWithRegisterTypes(DeviceType.DeviceConfigurationBuilder deviceConfigBuilder, RegisterType... registerType) {
        Stream.of(registerType).forEach(getNewNumericalRegisterSpec(deviceConfigBuilder));
    }

    private void enhanceConfigBuilderWithLoadProfileTypes(DeviceType.DeviceConfigurationBuilder deviceConfigBuilder, LoadProfileType... loadProfileTypes) {
        Stream.of(loadProfileTypes).forEach(getLoadProfileSpec(deviceConfigBuilder));
    }

    private void enhanceConfigBuilderWithLogBookTypes(DeviceType.DeviceConfigurationBuilder deviceConfigBuilder, LogBookType... logBookTypes) {
        Stream.of(logBookTypes).forEach(getLogBookSpec(deviceConfigBuilder));
    }

    private Consumer<LogBookType> getLogBookSpec(DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder) {
        return deviceConfigurationBuilder::newLogBookSpec;
    }

    private Consumer<LoadProfileType> getLoadProfileSpec(DeviceType.DeviceConfigurationBuilder deviceConfigurationBuilder) {
        return loadProfileType -> {
            LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder = deviceConfigurationBuilder.newLoadProfileSpec(loadProfileType);
            loadProfileType.getChannelTypes().forEach(channelType -> deviceConfigurationBuilder.newChannelSpec(channelType, loadProfileSpecBuilder).overflow(OVERFLOW_VALUE).nbrOfFractionDigits(3));
        };
    }

    private Consumer<RegisterType> getNewNumericalRegisterSpec(DeviceType.DeviceConfigurationBuilder deviceConfigBuilder) {
        return registerType -> {
            NumericalRegisterSpec.Builder builder = deviceConfigBuilder.newNumericalRegisterSpec(registerType);
            builder.overflowValue(OVERFLOW_VALUE);
            builder.numberOfFractionDigits(3);
        };
    }

    private static void grantAllViewAndEditPrivilegesToPrincipal() {
        Set<Privilege> privileges = new HashSet<>();
        Privilege editPrivilege = mock(Privilege.class);
        when(editPrivilege.getName()).thenReturn(EditPrivilege.LEVEL_1.getPrivilege());
        privileges.add(editPrivilege);
        Privilege viewPrivilege = mock(Privilege.class);
        when(viewPrivilege.getName()).thenReturn(ViewPrivilege.LEVEL_1.getPrivilege());
        privileges.add(viewPrivilege);
        when(inMemoryPersistence.getMockedUser().getPrivileges()).thenReturn(privileges);
    }

    private DeviceLifeCycle getDefaultDeviceLifeCycle() {
        return inMemoryPersistence
                .getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
    }
}
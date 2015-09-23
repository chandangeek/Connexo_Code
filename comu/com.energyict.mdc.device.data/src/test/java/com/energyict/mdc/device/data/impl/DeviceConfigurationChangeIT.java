package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.VoidTransaction;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.config.impl.PartialScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.CannotChangeDeviceConfigStillUnresolvedConflicts;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;
import com.energyict.mdc.device.data.impl.tasks.OutboundIpConnectionTypeImpl;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.tasks.ClockTaskType;
import com.energyict.mdc.tasks.ComTask;
import org.assertj.core.api.Condition;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
            }
        });
    }

    @Before
    public void setup() {
        final List<DeviceConfiguration> configurations = this.deviceType.getConfigurations();
        configurations.stream().forEach(deviceConfiguration -> {
            deviceConfiguration.deactivate();
            this.deviceType.removeConfiguration(deviceConfiguration);
        });
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
     * @param topic       the topic to filter on
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
        ipComPortPool.save();
        return ipComPortPool;
    }

    @Test
    @Transactional
    public void simpleConfigChangeNoConflictsNoDataSourceTest() {
        DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
        firstDeviceConfiguration.activate();
        DeviceConfiguration secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
    }

    @Test(expected = DeviceConfigurationChangeException.class)
    @Transactional
    public void changeConfigToSameConfigTest() {
        DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
        firstDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();
        try {
            Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, firstDeviceConfiguration);
        } catch (DeviceConfigurationChangeException e) {
            if(!e.getMessageSeed().equals(MessageSeeds.CANNOT_CHANGE_DEVICE_CONFIG_TO_SAME_CONFIG)){
                fail("Should have gotten an exception indicating that you can not change the config to the same config");
            }
            throw e;
        }
    }

    @Test(expected = DeviceConfigurationChangeException.class)
    @Transactional
    public void changeConfigToConfigOfOtherDeviceTypeTest() {
        DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
        firstDeviceConfiguration.activate();
        final DeviceType otherDeviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType("OtherDeviceType", deviceProtocolPluggableClass);
        otherDeviceType.save();
        final DeviceConfiguration configOfOtherDeviceType = otherDeviceType.newConfiguration("ConfigOfOtherDeviceType").add();
        configOfOtherDeviceType.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();
        try {
            Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, configOfOtherDeviceType);
        } catch (DeviceConfigurationChangeException e) {
            if(!e.getMessageSeed().equals(MessageSeeds.CANNOT_CHANGE_DEVICE_CONFIG_TO_OTHER_DEVICE_TYPE)){
                fail("Should have gotten an exception indicating that you can not change the config to the config of another devicetype");
            }
            throw e;
        }
    }

    @Test
    @Transactional
    public void configChangeCreatesNewMeterActivationTest() {
        Instant initialClock = freezeClock(2012, 2, 9, 1, 11, 0, 0);
        DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
        firstDeviceConfiguration.activate();
        DeviceConfiguration secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();
        device.activate(initialClock);

        Instant instant = freezeClock(2015, 9, 18, 11, 30, 0, 0);

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);
        assertThat(modifiedDevice.getCurrentMeterActivation().get().getStart()).isEqualTo(instant);
        assertThat(modifiedDevice.getMeterActivationsMostRecentFirst()).hasSize(2);
    }

    @Test
    @Transactional
    public void changeConfigWithSingleSameRegisterSpecTest() {
        RegisterType registerType = getRegisterTypeForReadingType(readingTypeMRID1);
        enhanceDeviceTypeWithRegisterTypes(this.deviceType, registerType);
        DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(firstConfigBuilder, registerType);
        DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
        firstDeviceConfiguration.activate();
        DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(secondDeviceConfigBuilder, registerType);
        DeviceConfiguration secondDeviceConfiguration = secondDeviceConfigBuilder.add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getRegisters()).hasSize(1);
        assertThat(modifiedDevice.getRegisters().get(0).getRegisterSpec().getId()).isEqualTo(secondDeviceConfiguration.getRegisterSpecs().get(0).getId());
        assertThat(modifiedDevice.getRegisters().get(0).getReadingType().getMRID()).isEqualTo(readingTypeMRID1);
    }

    @Test
    @Transactional
    public void changeConfigWithSingleOtherRegisterSpecTest() {
        RegisterType registerType1 = getRegisterTypeForReadingType(readingTypeMRID1);
        RegisterType registerType2 = getRegisterTypeForReadingType(readingTypeMRID2);
        enhanceDeviceTypeWithRegisterTypes(this.deviceType, registerType1, registerType2);
        DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(firstConfigBuilder, registerType1);
        DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
        firstDeviceConfiguration.activate();
        DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(secondDeviceConfigBuilder, registerType2);
        DeviceConfiguration secondDeviceConfiguration = secondDeviceConfigBuilder.add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        // registerType1 will NOT exist anymore on the device

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getRegisters()).hasSize(1);
        assertThat(modifiedDevice.getRegisters().get(0).getRegisterSpec().getId()).isEqualTo(secondDeviceConfiguration.getRegisterSpecs().get(0).getId());
        assertThat(modifiedDevice.getRegisters().get(0).getReadingType().getMRID()).isEqualTo(readingTypeMRID2);
    }

    @Test
    @Transactional
    public void changeConfigWithSingleRegisterSpecToTwoRegisterSpecsTest() {
        RegisterType registerType1 = getRegisterTypeForReadingType(readingTypeMRID1);
        RegisterType registerType2 = getRegisterTypeForReadingType(readingTypeMRID2);
        enhanceDeviceTypeWithRegisterTypes(this.deviceType, registerType1, registerType2);
        DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(firstConfigBuilder, registerType1);
        DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
        firstDeviceConfiguration.activate();
        DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(secondDeviceConfigBuilder, registerType1, registerType2);
        DeviceConfiguration secondDeviceConfiguration = secondDeviceConfigBuilder.add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

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
    @Transactional
    public void changeConfigWithTwoRegisterSpecsToOneRegisterSpecTest() {
        RegisterType registerType1 = getRegisterTypeForReadingType(readingTypeMRID1);
        RegisterType registerType2 = getRegisterTypeForReadingType(readingTypeMRID2);
        enhanceDeviceTypeWithRegisterTypes(this.deviceType, registerType1, registerType2);
        DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(firstConfigBuilder, registerType1, registerType2);
        DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
        firstDeviceConfiguration.activate();
        DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
        enhanceConfigBuilderWithRegisterTypes(secondDeviceConfigBuilder, registerType1);
        DeviceConfiguration secondDeviceConfiguration = secondDeviceConfigBuilder.add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

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
    @Transactional
    public void changeConfigWithSingleOtherLoadProfileSpecTest() {
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
        DeviceConfiguration secondDeviceConfiguration = secondDeviceConfigBuilder.add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

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
    @Transactional
    public void changeConfigWithSingleSameLoadProfileSpecTest() {
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
        DeviceConfiguration secondDeviceConfiguration = secondDeviceConfigBuilder.add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

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
    @Transactional
    public void changeConfigWithSingleOtherLogBookSpecTest() {
        final LogBookType logBookType1 = createLogBookType("MyFirstLogBookType", ObisCode.fromString("0.0.99.98.0.255"));
        final LogBookType logBookType2 = createLogBookType("MySecondLogBookType", ObisCode.fromString("1.0.99.98.0.255"));

        enhanceDeviceTypeWithLogBookTypes(this.deviceType, logBookType1, logBookType2);
        DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
        enhanceConfigBuilderWithLogBookTypes(firstConfigBuilder, logBookType1);
        DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
        firstDeviceConfiguration.activate();
        DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
        enhanceConfigBuilderWithLogBookTypes(secondDeviceConfigBuilder, logBookType2);
        DeviceConfiguration secondDeviceConfiguration = secondDeviceConfigBuilder.add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getLogBooks()).hasSize(1);
        assertThat(modifiedDevice.getLogBooks().get(0).getLogBookSpec().getId()).isEqualTo(secondDeviceConfiguration.getLogBookSpecs().get(0).getId());
    }

    @Test
    @Transactional
    public void changeConfigWithSingleSameLogBookSpecTest() {
        final LogBookType logBookType1 = createLogBookType("MyFirstLogBookType", ObisCode.fromString("0.0.99.98.0.255"));

        enhanceDeviceTypeWithLogBookTypes(this.deviceType, logBookType1);
        DeviceType.DeviceConfigurationBuilder firstConfigBuilder = deviceType.newConfiguration("FirstDeviceConfiguration");
        enhanceConfigBuilderWithLogBookTypes(firstConfigBuilder, logBookType1);
        DeviceConfiguration firstDeviceConfiguration = firstConfigBuilder.add();
        firstDeviceConfiguration.activate();
        DeviceType.DeviceConfigurationBuilder secondDeviceConfigBuilder = deviceType.newConfiguration("SecondDeviceConfiguration");
        enhanceConfigBuilderWithLogBookTypes(secondDeviceConfigBuilder, logBookType1);
        DeviceConfiguration secondDeviceConfiguration = secondDeviceConfigBuilder.add();
        secondDeviceConfiguration.activate();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getLogBooks()).hasSize(1);
        assertThat(modifiedDevice.getLogBooks().get(0).getLogBookSpec().getId()).isEqualTo(secondDeviceConfiguration.getLogBookSpecs().get(0).getId());
    }

    @Test(expected = CannotChangeDeviceConfigStillUnresolvedConflicts.class)
    @Transactional
    public void changeConfigWhileThereAreStillConflictingMappingsTest() {
        final OutboundComPortPool outboundIpPool = createOutboundIpComPortPool("OutboundIpPool");
        final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").isDirectlyAddressable(true).add();
        final PartialScheduledConnectionTaskImpl myFirstConnectionTask = createPartialConnectionTask(firstDeviceConfiguration, "MyDefaultConnectionTaskName", outboundIpPool);
        firstDeviceConfiguration.activate();
        final DeviceConfiguration secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").isDirectlyAddressable(true).add();
        final PartialScheduledConnectionTaskImpl mySecondConnectionTask = createPartialConnectionTask(secondDeviceConfiguration, "MySecondConnectionTask", outboundIpPool);
        secondDeviceConfiguration.activate();

        updateConflictsFor(mySecondConnectionTask, connectionTaskCreatedTopic);

        assertThat(deviceType.getDeviceConfigConflictMappings()).hasSize(2);

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();
        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);
    }

    @Test
    @Transactional
    public void changeConfigWithNoConflictConnectionMethods() {
        final String connectionTaskName = "MyDefaultConnectionTaskName";

        final OutboundComPortPool outboundIpPool = createOutboundIpComPortPool("OutboundIpPool");
        final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").isDirectlyAddressable(true).add();
        final PartialScheduledConnectionTaskImpl myFirstConnectionTask = createPartialConnectionTask(firstDeviceConfiguration, connectionTaskName, outboundIpPool);
        firstDeviceConfiguration.activate();
        final DeviceConfiguration secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").isDirectlyAddressable(true).add();
        final PartialScheduledConnectionTaskImpl mySecondConnectionTask = createPartialConnectionTask(secondDeviceConfiguration, connectionTaskName, outboundIpPool);
        secondDeviceConfiguration.activate();

        updateConflictsFor(mySecondConnectionTask, connectionTaskCreatedTopic);
        assertThat(deviceType.getDeviceConfigConflictMappings()).isEmpty();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();
        final ScheduledConnectionTask originalScheduledConnectionTask = device.getScheduledConnectionTaskBuilder(myFirstConnectionTask).setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE).add();
        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getConnectionTasks().get(0).getPartialConnectionTask().getId()).isEqualTo(mySecondConnectionTask.getId());
    }

    @Test
    @Transactional
    public void changeConfigWithConflictAndResolvedRemoveActionTest() {
        final OutboundComPortPool outboundIpPool = createOutboundIpComPortPool("OutboundIpPool");
        final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").isDirectlyAddressable(true).add();
        final PartialScheduledConnectionTaskImpl myFirstConnectionTask = createPartialConnectionTask(firstDeviceConfiguration, "MyDefaultConnectionTaskName", outboundIpPool);
        firstDeviceConfiguration.activate();
        final DeviceConfiguration secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").isDirectlyAddressable(true).add();
        final PartialScheduledConnectionTaskImpl mySecondConnectionTask = createPartialConnectionTask(secondDeviceConfiguration, "MySecondConnectionTask", outboundIpPool);
        secondDeviceConfiguration.activate();

        updateConflictsFor(mySecondConnectionTask, connectionTaskCreatedTopic);
        final DeviceConfigConflictMapping deviceConfigConflictMapping = getDeviceConfigConflictMapping(firstDeviceConfiguration, secondDeviceConfiguration);
        deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().get(0).setSolution(DeviceConfigConflictMapping.ConflictingMappingAction.REMOVE, mySecondConnectionTask);

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();
        final ScheduledConnectionTask originalScheduledConnectionTask = device.getScheduledConnectionTaskBuilder(myFirstConnectionTask).setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE).add();
        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getConnectionTasks()).isEmpty();
    }

    @Test
    @Transactional
    public void changeConfigWithConflictAndResolvedMapActionTest() {
        final OutboundComPortPool outboundIpPool = createOutboundIpComPortPool("OutboundIpPool");
        final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").isDirectlyAddressable(true).add();
        final PartialScheduledConnectionTaskImpl myFirstConnectionTask = createPartialConnectionTask(firstDeviceConfiguration, "MyDefaultConnectionTaskName", outboundIpPool);
        firstDeviceConfiguration.activate();
        final DeviceConfiguration secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").isDirectlyAddressable(true).add();
        final PartialScheduledConnectionTaskImpl mySecondConnectionTask = createPartialConnectionTask(secondDeviceConfiguration, "MySecondConnectionTask", outboundIpPool);
        secondDeviceConfiguration.activate();

        updateConflictsFor(mySecondConnectionTask, connectionTaskCreatedTopic);
        final DeviceConfigConflictMapping deviceConfigConflictMapping = getDeviceConfigConflictMapping(firstDeviceConfiguration, secondDeviceConfiguration);
        deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().get(0).setSolution(DeviceConfigConflictMapping.ConflictingMappingAction.MAP, mySecondConnectionTask);

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();
        final ScheduledConnectionTask originalScheduledConnectionTask = device.getScheduledConnectionTaskBuilder(myFirstConnectionTask).setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE).add();
        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getConnectionTasks().get(0).getPartialConnectionTask().getId()).isEqualTo(mySecondConnectionTask.getId());
    }

    @Test
    @Transactional
    public void changeConfigWithConflictAndResolvedMapWithPropertiesTest() {
        final OutboundComPortPool outboundIpPool = createOutboundIpComPortPool("OutboundIpPool");
        final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").isDirectlyAddressable(true).add();
        final PartialScheduledConnectionTaskImpl myFirstConnectionTask = createPartialConnectionTask(firstDeviceConfiguration, "MyDefaultConnectionTaskName", outboundIpPool);
        firstDeviceConfiguration.activate();
        final DeviceConfiguration secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").isDirectlyAddressable(true).add();
        final PartialScheduledConnectionTaskImpl mySecondConnectionTask = createPartialConnectionTask(secondDeviceConfiguration, "MySecondConnectionTask", outboundIpPool);
        secondDeviceConfiguration.activate();

        updateConflictsFor(mySecondConnectionTask, connectionTaskCreatedTopic);
        final DeviceConfigConflictMapping deviceConfigConflictMapping = getDeviceConfigConflictMapping(firstDeviceConfiguration, secondDeviceConfiguration);
        deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().get(0).setSolution(DeviceConfigConflictMapping.ConflictingMappingAction.MAP, mySecondConnectionTask);

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();
        final String ipAddressValue = "10.0.66.99";
        final BigDecimal portNumberValue = new BigDecimal(1235L);
        final String ipAddressPropertyName = "ipAddress";
        final String portNumberPropertyName = "port";
        final ScheduledConnectionTask originalScheduledConnectionTask = device.getScheduledConnectionTaskBuilder(myFirstConnectionTask)
                .setProperty(ipAddressPropertyName, ipAddressValue)
                .setProperty(portNumberPropertyName, portNumberValue)
                .add();
        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getConnectionTasks().get(0).getPartialConnectionTask().getId()).isEqualTo(mySecondConnectionTask.getId());
        assertThat(modifiedDevice.getConnectionTasks().get(0).getProperty(ipAddressPropertyName).getValue()).isEqualTo(ipAddressValue);
        assertThat(modifiedDevice.getConnectionTasks().get(0).getProperty(portNumberPropertyName).getValue()).isEqualTo(portNumberValue);
    }


    @Test(expected = CannotChangeDeviceConfigStillUnresolvedConflicts.class)
    @Transactional
    public void changeConfigWhileThereAreStillSecuritySetConflictingMappingsTest() {
        final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
        final SecurityPropertySet firstSecurityPropertySet = firstDeviceConfiguration.createSecurityPropertySet("NoSecurity").encryptionLevel(0).authenticationLevel(0).build();
        firstDeviceConfiguration.activate();
        final DeviceConfiguration secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
        final SecurityPropertySet secondSecurityPropertySet = secondDeviceConfiguration.createSecurityPropertySet("None").encryptionLevel(0).authenticationLevel(0).build();
        secondDeviceConfiguration.activate();

        updateConflictsFor(secondSecurityPropertySet, securitySetCreatedTopic);

        assertThat(deviceType.getDeviceConfigConflictMappings()).hasSize(2);

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();
        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);
    }

    @Test
    @Transactional
    public void changeConfigWithNoConflictingSecurityPropertySetsAndValidProperties() {
        final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
        final String securityPropertySetName = "NoSecurity";
        final SecurityPropertySet firstSecurityPropertySet = createSecurityPropertySet(firstDeviceConfiguration, securityPropertySetName);
        firstDeviceConfiguration.activate();
        final DeviceConfiguration secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
        final SecurityPropertySet secondSecurityPropertySet = createSecurityPropertySet(secondDeviceConfiguration, securityPropertySetName);
        secondDeviceConfiguration.activate();

        updateConflictsFor(secondSecurityPropertySet, securitySetCreatedTopic);
        assertThat(deviceType.getDeviceConfigConflictMappings()).isEmpty();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("Password", "12345678");
        device.setSecurityProperties(firstSecurityPropertySet, securityProperties);
        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getSecurityProperties(secondSecurityPropertySet).get(0).getName()).isEqualTo("Password");
        assertThat(modifiedDevice.getSecurityProperties(firstSecurityPropertySet)).isEmpty();
    }

    @Test
    @Transactional
    public void changeConfigWithConflictingSecurityPropertySetsAndMapSolutionTest() {
        final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
        final String firstSecurityPropertySetName = "NoSecurity";
        final String secondSecurityPropertySetName = "AnotherSecurityPropertySetName";
        final SecurityPropertySet firstSecurityPropertySet = createSecurityPropertySet(firstDeviceConfiguration, firstSecurityPropertySetName);
        firstDeviceConfiguration.activate();
        final DeviceConfiguration secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
        final SecurityPropertySet secondSecurityPropertySet = createSecurityPropertySet(secondDeviceConfiguration, secondSecurityPropertySetName);
        secondDeviceConfiguration.activate();

        updateConflictsFor(secondSecurityPropertySet, securitySetCreatedTopic);
        final DeviceConfigConflictMapping deviceConfigConflictMapping = getDeviceConfigConflictMapping(firstDeviceConfiguration, secondDeviceConfiguration);
        deviceConfigConflictMapping.getConflictingSecuritySetSolutions().get(0).setSolution(DeviceConfigConflictMapping.ConflictingMappingAction.MAP, secondSecurityPropertySet);

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("Password", "12345678");
        device.setSecurityProperties(firstSecurityPropertySet, securityProperties);
        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getSecurityProperties(secondSecurityPropertySet).get(0).getName()).isEqualTo("Password");
        assertThat(modifiedDevice.getSecurityProperties(firstSecurityPropertySet)).isEmpty();
    }

    @Test
    @Transactional
    public void changeConfigWithConflictingSecurityPropertySetsAndRemoveSolutionTest() {
        final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
        final String firstSecurityPropertySetName = "NoSecurity";
        final String secondSecurityPropertySetName = "AnotherSecurityPropertySetName";
        final SecurityPropertySet firstSecurityPropertySet = createSecurityPropertySet(firstDeviceConfiguration, firstSecurityPropertySetName);
        firstDeviceConfiguration.activate();
        final DeviceConfiguration secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
        final SecurityPropertySet secondSecurityPropertySet = createSecurityPropertySet(secondDeviceConfiguration, secondSecurityPropertySetName);
        secondDeviceConfiguration.activate();

        updateConflictsFor(secondSecurityPropertySet, securitySetCreatedTopic);
        final DeviceConfigConflictMapping deviceConfigConflictMapping = getDeviceConfigConflictMapping(firstDeviceConfiguration, secondDeviceConfiguration);
        deviceConfigConflictMapping.getConflictingSecuritySetSolutions().get(0).setSolution(DeviceConfigConflictMapping.ConflictingMappingAction.REMOVE);

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        device.save();
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("Password", "12345678");
        device.setSecurityProperties(firstSecurityPropertySet, securityProperties);
        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);

        assertThat(modifiedDevice.getDeviceConfiguration().getId()).isEqualTo(secondDeviceConfiguration.getId());
        assertThat(modifiedDevice.getSecurityProperties(secondSecurityPropertySet)).isEmpty();
        assertThat(modifiedDevice.getSecurityProperties(firstSecurityPropertySet)).isEmpty();
    }

    @Test
    @Transactional
    public void changeConfigRemovesNonExistingComTaskExecutions() {
        final ComTask comTaskForTesting = inMemoryPersistence.getTaskService().newComTask("ComTaskForTesting");
        comTaskForTesting.createClockTask(ClockTaskType.FORCECLOCK).add();
        comTaskForTesting.save();
        final DeviceConfiguration firstDeviceConfiguration = deviceType.newConfiguration("FirstDeviceConfiguration").add();
        final String securityPropertySetName = "NoSecurity";
        final SecurityPropertySet firstSecurityPropertySet = createSecurityPropertySet(firstDeviceConfiguration, securityPropertySetName);
        final ComTaskEnablement comTaskEnablement1 = createComTaskEnablement(comTaskForTesting, firstDeviceConfiguration, firstSecurityPropertySet);
        firstDeviceConfiguration.activate();
        final DeviceConfiguration secondDeviceConfiguration = deviceType.newConfiguration("SecondDeviceConfiguration").add();
        final SecurityPropertySet secondSecurityPropertySet = createSecurityPropertySet(secondDeviceConfiguration, securityPropertySetName);
        secondDeviceConfiguration.activate();

        updateConflictsFor(secondSecurityPropertySet, securitySetCreatedTopic);
        assertThat(deviceType.getDeviceConfigConflictMappings()).isEmpty();

        Device device = inMemoryPersistence.getDeviceService().newDevice(firstDeviceConfiguration, "DeviceName", "DeviceMRID");
        final ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = device.newAdHocComTaskExecution(comTaskEnablement1).add();
        device.save();

        assertThat(device.getComTaskExecutions()).hasSize(1);

        Device modifiedDevice = inMemoryPersistence.getDeviceService().changeDeviceConfiguration(device, secondDeviceConfiguration);
        assertThat(device.getComTaskExecutions()).isEmpty();
    }

    // TODO update protocoldialecconfigproperties

    private ComTaskEnablement createComTaskEnablement(ComTask comTaskForTesting, DeviceConfiguration firstDeviceConfiguration, SecurityPropertySet firstSecurityPropertySet) {
        return firstDeviceConfiguration.enableComTask(comTaskForTesting, firstSecurityPropertySet, firstDeviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0)).useDefaultConnectionTask(true).add();
    }

    private SecurityPropertySet createSecurityPropertySet(DeviceConfiguration deviceConfiguration, String securityPropertySetName) {
        return deviceConfiguration.createSecurityPropertySet(securityPropertySetName).encryptionLevel(0).authenticationLevel(0).addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1).addUserAction(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1).build();
    }

    private DeviceConfigConflictMapping getDeviceConfigConflictMapping(DeviceConfiguration firstDeviceConfiguration, DeviceConfiguration secondDeviceConfiguration) {
        return deviceType.getDeviceConfigConflictMappings().stream().filter(getDeviceConfigConflictMappingPredicate(firstDeviceConfiguration, secondDeviceConfiguration)).findFirst().get();
    }

    private Predicate<DeviceConfigConflictMapping> getDeviceConfigConflictMappingPredicate(DeviceConfiguration firstDeviceConfiguration, DeviceConfiguration secondDeviceConfiguration) {
        return deviceConfigConflictMapping -> deviceConfigConflictMapping.getOriginDeviceConfiguration().getId() == firstDeviceConfiguration.getId() && deviceConfigConflictMapping.getDestinationDeviceConfiguration().getId() == secondDeviceConfiguration.getId();
    }

    private PartialScheduledConnectionTaskImpl createPartialConnectionTask(DeviceConfiguration firstDeviceConfiguration, String connectionTaskName, OutboundComPortPool comPortPool) {
        final PartialScheduledConnectionTaskBuilder partialScheduledConnectionTaskBuilder = firstDeviceConfiguration.newPartialScheduledConnectionTask(connectionTaskName, outboundIpConnectionTypePluggableClass, scheduledConnectionTaskInterval, ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        partialScheduledConnectionTaskBuilder.comPortPool(comPortPool);
        return partialScheduledConnectionTaskBuilder.build();
    }

    private RegisterType getRegisterTypeForReadingType(String readingTypeMRID) {
        return inMemoryPersistence.getMasterDataService().findRegisterTypeByReadingType(inMemoryPersistence.getMeteringService().getReadingType(readingTypeMRID).get()).get();
    }

    private void enhanceDeviceTypeWithRegisterTypes(DeviceType deviceType, RegisterType... registerType) {
        Stream.of(registerType).forEach(deviceType::addRegisterType);
        deviceType.save();
    }

    private void enhanceDeviceTypeWithLogBookTypes(DeviceType deviceType, LogBookType... logBookTypes) {
        Stream.of(logBookTypes).forEach(deviceType::addLogBookType);
        deviceType.save();
    }

    private void enhanceDeviceTypeWithLoadProfileTypes(DeviceType deviceType, LoadProfileType... loadProfileTypes) {
        Stream.of(loadProfileTypes).forEach(deviceType::addLoadProfileType);
        deviceType.save();
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
            loadProfileType.getChannelTypes().stream().forEach(channelType -> deviceConfigurationBuilder.newChannelSpec(channelType, loadProfileSpecBuilder));
        };
    }

    private Consumer<RegisterType> getNewNumericalRegisterSpec(DeviceType.DeviceConfigurationBuilder deviceConfigBuilder) {
        return registerType -> {
            NumericalRegisterSpec.Builder builder = deviceConfigBuilder.newNumericalRegisterSpec(registerType);
            builder.setNumberOfDigits(9);
            builder.setNumberOfFractionDigits(3);
        };
    }
}
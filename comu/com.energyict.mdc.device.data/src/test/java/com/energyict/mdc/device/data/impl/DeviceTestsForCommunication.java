package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.NextExecutionSpecBuilder;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialConnectionInitiationTaskBuilder;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.device.config.PartialOutboundConnectionTaskBuilder;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.CannotDeleteConnectionTaskWhichIsNotFromThisDevice;
import com.energyict.mdc.device.data.impl.tasks.NoParamsConnectionType;
import com.energyict.mdc.device.data.impl.tasks.SimpleDiscoveryProtocol;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import org.fest.assertions.core.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 04/04/14
 * Time: 12:06
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceTestsForCommunication extends PersistenceIntegrationTest {

    private static final ComWindow COM_WINDOW = new ComWindow();

    private OutboundComPortPool outboundComPortPool;
    private InboundComPortPool inboundComPortPool;
    private ConnectionTypePluggableClass connectionTypePluggableClass;
    private InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass;
    private PartialScheduledConnectionTask partialOutboundConnectionTask;
    private PartialInboundConnectionTask partialInboundConnectionTask;
    private PartialConnectionInitiationTask partialConnectionInitiationTask;
    private TimeDuration frequency = TimeDuration.hours(1);

    private DeviceConfiguration createDeviceConfigWithPartialOutboundConnectionTask() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithPartialOutboundConnectionTask");
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        DeviceCommunicationConfiguration communicationConfiguration = inMemoryPersistence.getDeviceConfigurationService().newDeviceCommunicationConfiguration(deviceConfiguration);
        communicationConfiguration.save();
        addPartialOutboundConnectionTask(communicationConfiguration);
        deviceType.save();
        return deviceConfiguration;
    }

    private DeviceConfiguration createDeviceConfigWithPartialInboundConnectionTask() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithPartialInboundConnectionTask");
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        DeviceCommunicationConfiguration communicationConfiguration = inMemoryPersistence.getDeviceConfigurationService().newDeviceCommunicationConfiguration(deviceConfiguration);
        communicationConfiguration.save();
        addPartialInboundConnectionTask(communicationConfiguration);
        deviceType.save();
        return deviceConfiguration;
    }

    private DeviceConfiguration createDeviceConfigWithPartiaConnectionInitiationTask() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithPartialConnectionInitiationTask");
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        DeviceCommunicationConfiguration communicationConfiguration = inMemoryPersistence.getDeviceConfigurationService().newDeviceCommunicationConfiguration(deviceConfiguration);
        communicationConfiguration.save();
        addPartialConnectionInitiationConnectionTask(communicationConfiguration);
        deviceType.save();
        return deviceConfiguration;
    }

    private DeviceConfiguration createDeviceConfigWithThreeTypesOfPartialsTask() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithThreePartials");
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        DeviceCommunicationConfiguration communicationConfiguration = inMemoryPersistence.getDeviceConfigurationService().newDeviceCommunicationConfiguration(deviceConfiguration);
        communicationConfiguration.save();
        addPartialOutboundConnectionTask(communicationConfiguration);
        addPartialInboundConnectionTask(communicationConfiguration);
        addPartialConnectionInitiationConnectionTask(communicationConfiguration);
        deviceType.save();
        return deviceConfiguration;
    }

    private void addPartialOutboundConnectionTask(DeviceCommunicationConfiguration communicationConfiguration) {
        PartialOutboundConnectionTaskBuilder partialOutboundConnectionTaskBuilder = communicationConfiguration.createPartialOutboundConnectionTask()
                .name("MyOutbound")
                .comPortPool(outboundComPortPool)
                .pluggableClass(connectionTypePluggableClass)
                .comWindow(COM_WINDOW)
                .rescheduleDelay(TimeDuration.seconds(60))
                .connectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .asDefault(true);
        NextExecutionSpecBuilder<PartialOutboundConnectionTaskBuilder> nextExecutionSpecBuilder = partialOutboundConnectionTaskBuilder.nextExecutionSpec();
        nextExecutionSpecBuilder.temporalExpression(frequency);
        nextExecutionSpecBuilder.set();
        partialOutboundConnectionTask = partialOutboundConnectionTaskBuilder.build();
        communicationConfiguration.save();
    }

    private void addPartialInboundConnectionTask(DeviceCommunicationConfiguration communicationConfiguration) {
        PartialInboundConnectionTaskBuilder partialInboundConnectionTaskBuilder = communicationConfiguration.createPartialInboundConnectionTask()
                .name("MyInboundConnectionTask")
                .comPortPool(inboundComPortPool)
                .pluggableClass(connectionTypePluggableClass)
                .asDefault(false);
        partialInboundConnectionTask = partialInboundConnectionTaskBuilder.build();
        communicationConfiguration.save();
    }

    private void addPartialConnectionInitiationConnectionTask(DeviceCommunicationConfiguration communicationConfiguration) {
        PartialConnectionInitiationTaskBuilder partialConnectionInitiationTaskBuilder = communicationConfiguration.createPartialConnectionInitiationTask()
                .name("MyConnectionInitiationTask")
                .comPortPool(outboundComPortPool)
                .pluggableClass(connectionTypePluggableClass)
                .rescheduleDelay(TimeDuration.seconds(60));
        partialConnectionInitiationTask = partialConnectionInitiationTaskBuilder.build();
        communicationConfiguration.save();
    }

    @Before
    public void initBefore() {
        connectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().newConnectionTypePluggableClass("NoParamsConnectionType", NoParamsConnectionType.class.getName());
        connectionTypePluggableClass.save();
        outboundComPortPool = inMemoryPersistence.getEngineModelService().newOutboundComPortPool();
        outboundComPortPool.setActive(true);
        outboundComPortPool.setComPortType(ComPortType.TCP);
        outboundComPortPool.setName("OutboundComPortPool");
        outboundComPortPool.setTaskExecutionTimeout(TimeDuration.minutes(15));
        outboundComPortPool.save();
        inboundDeviceProtocolPluggableClass = inMemoryPersistence.getProtocolPluggableService().newInboundDeviceProtocolPluggableClass("MyInboundDeviceProtocolPluggableClass", SimpleDiscoveryProtocol.class.getName());
        inboundDeviceProtocolPluggableClass.save();
        inboundComPortPool = inMemoryPersistence.getEngineModelService().newInboundComPortPool();
        inboundComPortPool.setName("InboundComPortPool");
        inboundComPortPool.setActive(true);
        inboundComPortPool.setComPortType(ComPortType.TCP);
        inboundComPortPool.setDiscoveryProtocolPluggableClass(inboundDeviceProtocolPluggableClass);
        inboundComPortPool.save();
    }

    @Test
    @Transactional
    public void createDeviceWithoutConnectionTasksTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithConnectionType, "DeviceWithoutConnectionTasks");
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask> connectionTasks = reloadedDevice.getConnectionTasks();

        assertThat(connectionTasks).isEmpty();
    }

    @Test
    @Transactional
    public void createDeviceWithScheduledConnectionTaskTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithConnectionType, "DeviceWithConnectionTasks");
        ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilderFor(partialOutboundConnectionTask).add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialOutboundConnectionTask.getId());
        assertThat(connectionTasks.get(0).getId()).isEqualTo(scheduledConnectionTask.getId());
    }

    @Test
    @Transactional
    public void createScheduledConnectionTaskAfterDeviceCreationTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithConnectionType, "AddConnectionTasksAfterDeviceCreation");
        device.save();
        ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilderFor(partialOutboundConnectionTask).add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialOutboundConnectionTask.getId());
        assertThat(connectionTasks.get(0).getId()).isEqualTo(scheduledConnectionTask.getId());
    }

    @Test
    @Transactional
    public void createDeviceWithInboundConnectionTaskTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialInboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeviceWithConnectionTasks");
        InboundConnectionTask inboundConnectionTask = device.getInboundConnectionTaskBuilderFor(partialInboundConnectionTask).add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialInboundConnectionTask.getId());
        assertThat(connectionTasks.get(0).getId()).isEqualTo(inboundConnectionTask.getId());
    }

    @Test
    @Transactional
    public void createInboundConnectionTaskAfterDeviceCreationTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialInboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeviceWithConnectionTasks");
        device.save();
        InboundConnectionTask inboundConnectionTask = device.getInboundConnectionTaskBuilderFor(partialInboundConnectionTask).add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialInboundConnectionTask.getId());
        assertThat(connectionTasks.get(0).getId()).isEqualTo(inboundConnectionTask.getId());
    }

    @Test
    @Transactional
    public void createDeviceWithConnectionInitiationTaskTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartiaConnectionInitiationTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Device");
        ConnectionInitiationTask connectionInitiationTask = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask).add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialConnectionInitiationTask.getId());
        assertThat(connectionTasks.get(0).getId()).isEqualTo(connectionInitiationTask.getId());
    }

    @Test
    @Transactional
    public void createConnectionInitiationTaskTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartiaConnectionInitiationTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Device");
        device.save();
        ConnectionInitiationTask connectionInitiationTask = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask).add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialConnectionInitiationTask.getId());
        assertThat(connectionTasks.get(0).getId()).isEqualTo(connectionInitiationTask.getId());
    }

    @Test
    @Transactional
    public void createDeviceWithMultipleConnectionTasksTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithThreeTypesOfPartialsTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Device");
        final ConnectionInitiationTask connectionInitiationTask = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask).add();
        final ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilderFor(partialOutboundConnectionTask).add();
        final InboundConnectionTask inboundConnectionTask = device.getInboundConnectionTaskBuilderFor(partialInboundConnectionTask).add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(3);
        assertThat(connectionTasks).has(new Condition<List<ConnectionTask>>() {
            @Override
            public boolean matches(List<ConnectionTask> value) {
                int tripleMatch = 0b0000;
                for (ConnectionTask connectionTask : value) {
                    if (connectionTask.getId() == scheduledConnectionTask.getId() && connectionTask.getPartialConnectionTask().getId() == partialOutboundConnectionTask.getId()) {
                        tripleMatch |= 0b0001;
                    }

                    if (connectionTask.getId() == inboundConnectionTask.getId() && connectionTask.getPartialConnectionTask().getId() == partialInboundConnectionTask.getId()) {
                        tripleMatch |= 0b0010;
                    }

                    if (connectionTask.getId() == connectionInitiationTask.getId() && connectionTask.getPartialConnectionTask().getId() == partialConnectionInitiationTask.getId()) {
                        tripleMatch |= 0b0100;
                    }
                }
                return tripleMatch == 0b0111;
            }
        });
    }

    @Test
    @Transactional
    public void deleteDeviceDeletesConnectionTasksTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithThreeTypesOfPartialsTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Device");
        final ConnectionInitiationTask connectionInitiationTask = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask).add();
        final ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilderFor(partialOutboundConnectionTask).add();
        final InboundConnectionTask inboundConnectionTask = device.getInboundConnectionTaskBuilderFor(partialInboundConnectionTask).add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.delete();

        assertThat(inMemoryPersistence.getDeviceDataService().findConnectionTask(connectionInitiationTask.getId()).orNull()).isNull();
        assertThat(inMemoryPersistence.getDeviceDataService().findConnectionTask(scheduledConnectionTask.getId()).orNull()).isNull();
        assertThat(inMemoryPersistence.getDeviceDataService().findConnectionTask(inboundConnectionTask.getId()).orNull()).isNull();
    }

    @Test
    @Transactional
    public void deleteAConnectionTaskTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithConnectionType, "DeviceWithConnectionTasks");
        ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilderFor(partialOutboundConnectionTask).add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.removeConnectionTask(scheduledConnectionTask);

        Device deviceWithoutConnectionTasks = getReloadedDevice(reloadedDevice);

        assertThat(deviceWithoutConnectionTasks.getConnectionTasks()).isEmpty();
    }

    @Test(expected = CannotDeleteConnectionTaskWhichIsNotFromThisDevice.class)
    @Transactional
    public void deleteAConnectionTaskWhichIsNotFromThatDeviceTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithConnectionType, "DeviceWithConnectionTasks");
        ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilderFor(partialOutboundConnectionTask).add();
        device.save();

        Device otherDevice = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithConnectionType, "OtherDevice");

        // should throw exception
        otherDevice.removeConnectionTask(scheduledConnectionTask);
    }
}
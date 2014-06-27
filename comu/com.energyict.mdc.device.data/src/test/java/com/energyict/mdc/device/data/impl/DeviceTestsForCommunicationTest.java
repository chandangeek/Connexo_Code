package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialConnectionInitiationTaskBuilder;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.CannotDeleteConnectionTaskWhichIsNotFromThisDevice;
import com.energyict.mdc.device.data.impl.tasks.*;
import com.energyict.mdc.device.data.tasks.*;
;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.scheduling.TemporalExpression;
import org.fest.assertions.core.Condition;
import org.junit.Before;
import org.junit.Ignore;
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
public class DeviceTestsForCommunicationTest extends PersistenceIntegrationTest {

    private static final ComWindow COM_WINDOW = new ComWindow();
    private static final String MRID = "MRID";

    private OutboundComPortPool outboundComPortPool;
    private OutboundComPortPool otherOutboundComPortPool;
    private InboundComPortPool inboundComPortPool;
    private InboundComPortPool otherInboundComPortPool;
    private ConnectionTypePluggableClass connectionTypePluggableClass;
    private ConnectionTypePluggableClass ipConnectionTypePluggableClass;
    private ConnectionTypePluggableClass inbConnectionTypePluggableClass;
    private InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass;
    private PartialScheduledConnectionTask partialScheduledConnectionTask;
    private PartialInboundConnectionTask partialInboundConnectionTask;
    private PartialConnectionInitiationTask partialConnectionInitiationTask;
    private TimeDuration frequency = TimeDuration.hours(1);

    private DeviceConfiguration createDeviceConfigWithPartialOutboundConnectionTask() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithPartialOutboundConnectionTask");
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        deviceConfiguration.save();

        addPartialOutboundConnectionTask(deviceConfiguration);
        deviceType.save();
        return deviceConfiguration;
    }

    private DeviceConfiguration createDeviceConfigWithPartialIpOutboundConnectionTask() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithPartialIpOutboundConnectionTask");
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        deviceConfiguration.save();

        PartialScheduledConnectionTaskBuilder partialScheduledConnectionTaskBuilder = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", ipConnectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .asDefault(true);
        partialScheduledConnectionTaskBuilder.build();
        deviceType.save();
        return deviceConfiguration;
    }

    private DeviceConfiguration createDeviceConfigWithPartialInboundConnectionTask() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithPartialInboundConnectionTask");
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        deviceConfiguration.save();
        addPartialInboundConnectionTask(deviceConfiguration);
        deviceType.save();
        return deviceConfiguration;
    }

    private DeviceConfiguration createDeviceConfigWithPartialIpInboundConnectionTask() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithPartialInboundConnectionTask");
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        DeviceCommunicationConfiguration communicationConfiguration = inMemoryPersistence.getDeviceConfigurationService().newDeviceCommunicationConfiguration(deviceConfiguration);
        communicationConfiguration.save();
        addPartialInboundConnectionTask(communicationConfiguration);
        deviceType.save();
        return deviceConfiguration;
    }

    private DeviceConfiguration createDeviceConfigWithPartialConnectionInitiationTask() {
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
        addPartialOutboundConnectionTask(deviceConfiguration);
        addPartialInboundConnectionTask(deviceConfiguration);
        PartialConnectionInitiationTaskBuilder partialConnectionInitiationTaskBuilder = deviceConfiguration.newPartialConnectionInitiationTask("MyConnectionInitiationTask", connectionTypePluggableClass, TimeDuration.seconds(60))
                .comPortPool(outboundComPortPool);
        partialConnectionInitiationTask = partialConnectionInitiationTaskBuilder.build();
        deviceType.save();
        return deviceConfiguration;
    }

    private void addPartialInboundConnectionTask(DeviceConfiguration deviceConfiguration) {
        PartialInboundConnectionTaskBuilder partialInboundConnectionTaskBuilder = deviceConfiguration.newPartialInboundConnectionTask("MyInboundConnectionTask", inbConnectionTypePluggableClass)
                .comPortPool(inboundComPortPool)
                .asDefault(false);
        partialInboundConnectionTask = partialInboundConnectionTaskBuilder.build();
    }

    private void addPartialOutboundConnectionTask(DeviceConfiguration deviceConfiguration) {
        PartialScheduledConnectionTaskBuilder partialScheduledConnectionTaskBuilder = deviceConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .asDefault(true);
        partialScheduledConnectionTask = partialScheduledConnectionTaskBuilder.build();
    }

    private void addPartialOutboundConnectionTaskFor(DeviceCommunicationConfiguration communicationConfiguration, ConnectionTypePluggableClass connectionTypePluggableClass) {
        PartialScheduledConnectionTaskBuilder partialScheduledConnectionTaskBuilder = communicationConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .asDefault(true);
        partialScheduledConnectionTask = partialScheduledConnectionTaskBuilder.build();
        communicationConfiguration.save();
    }

    private void addPartialInboundConnectionTask(DeviceCommunicationConfiguration communicationConfiguration) {
        addPartialInboundConnectionTaskFor(communicationConfiguration, this.connectionTypePluggableClass);
    }

    private void addPartialIpInboundConnectionTask(DeviceCommunicationConfiguration communicationConfiguration) {
        addPartialInboundConnectionTaskFor(communicationConfiguration, this.ipConnectionTypePluggableClass);
    }

    private void addPartialInboundConnectionTaskFor(DeviceCommunicationConfiguration communicationConfiguration, ConnectionTypePluggableClass connectionTypePluggableClass) {
        PartialInboundConnectionTaskBuilder partialInboundConnectionTaskBuilder = communicationConfiguration.newPartialInboundConnectionTask("MyInboundConnectionTask", connectionTypePluggableClass)
                .comPortPool(inboundComPortPool)
                .asDefault(false);
        partialInboundConnectionTask = partialInboundConnectionTaskBuilder.build();
        communicationConfiguration.save();
    }

    private void addPartialConnectionInitiationConnectionTask(DeviceCommunicationConfiguration communicationConfiguration) {
        PartialConnectionInitiationTaskBuilder partialConnectionInitiationTaskBuilder = communicationConfiguration.newPartialConnectionInitiationTask("MyConnectionInitiationTask", connectionTypePluggableClass, TimeDuration.seconds(60))
                .comPortPool(outboundComPortPool);
        partialConnectionInitiationTask = partialConnectionInitiationTaskBuilder.build();
        communicationConfiguration.save();
    }

    @Before
    public void initBefore() {
        inbConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().newConnectionTypePluggableClass("NoParamsInbConnectionType", InboundNoParamsConnectionTypeImpl.class.getName());
        connectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().newConnectionTypePluggableClass("NoParamsConnectionType", OutboundNoParamsConnectionTypeImpl.class.getName());
        connectionTypePluggableClass.save();
//        ipConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().newConnectionTypePluggableClass("IPConnectionType", IpConnectionType.class.getName());
//        ipConnectionTypePluggableClass.save(); // TODO enable again once JP-1123 is completely finished

        outboundComPortPool = inMemoryPersistence.getEngineModelService().newOutboundComPortPool();
        outboundComPortPool.setActive(true);
        outboundComPortPool.setComPortType(ComPortType.TCP);
        outboundComPortPool.setName("OutboundComPortPool");
        outboundComPortPool.setTaskExecutionTimeout(TimeDuration.minutes(15));
        outboundComPortPool.save();

        otherOutboundComPortPool = inMemoryPersistence.getEngineModelService().newOutboundComPortPool();
        otherOutboundComPortPool.setActive(true);
        otherOutboundComPortPool.setComPortType(ComPortType.TCP);
        otherOutboundComPortPool.setName("OtherPool");
        otherOutboundComPortPool.setTaskExecutionTimeout(TimeDuration.minutes(30));
        otherOutboundComPortPool.save();

        inboundDeviceProtocolPluggableClass = inMemoryPersistence.getProtocolPluggableService().newInboundDeviceProtocolPluggableClass("MyInboundDeviceProtocolPluggableClass", SimpleDiscoveryProtocol.class.getName());
        inboundDeviceProtocolPluggableClass.save();
        inboundComPortPool = inMemoryPersistence.getEngineModelService().newInboundComPortPool();
        inboundComPortPool.setName("InboundComPortPool");
        inboundComPortPool.setActive(true);
        inboundComPortPool.setComPortType(ComPortType.TCP);
        inboundComPortPool.setDiscoveryProtocolPluggableClass(inboundDeviceProtocolPluggableClass);
        inboundComPortPool.save();

        otherInboundComPortPool = inMemoryPersistence.getEngineModelService().newInboundComPortPool();
        otherInboundComPortPool.setName("OtherInboundPool");
        otherInboundComPortPool.setActive(true);
        otherInboundComPortPool.setComPortType(ComPortType.TCP);
        otherInboundComPortPool.setDiscoveryProtocolPluggableClass(inboundDeviceProtocolPluggableClass);
        otherInboundComPortPool.save();
    }

    @Test
    @Transactional
    public void createDeviceWithScheduledConnectionTaskTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithConnectionType, "DeviceWithConnectionTasks", MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialScheduledConnectionTask.getId());

        assertThat(reloadedDevice.getInboundConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getScheduledConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
    }

    @Test
    @Transactional
    public void createScheduledConnectionTaskAfterDeviceCreationTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithConnectionType, "AddConnectionTasksAfterDeviceCreation", MRID);
        device.save();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialScheduledConnectionTask.getId());

        assertThat(reloadedDevice.getInboundConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getScheduledConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
    }

    @Test
    @Transactional
    public void createDeviceWithInboundConnectionTaskTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialInboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeviceWithConnectionTasks", MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialInboundConnectionTask.getId());

        assertThat(reloadedDevice.getInboundConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getScheduledConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
    }

    @Test
    @Transactional
    public void createInboundConnectionTaskAfterDeviceCreationTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialInboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeviceWithConnectionTasks", MRID);
        device.save();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialInboundConnectionTask.getId());

        assertThat(reloadedDevice.getInboundConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getScheduledConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
    }

    @Test
    @Transactional
    public void createDeviceWithConnectionInitiationTaskTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialConnectionInitiationTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Device", MRID);
        ConnectionInitiationTask connectionInitiationTask = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask).add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialConnectionInitiationTask.getId());
        assertThat(connectionTasks.get(0).getId()).isEqualTo(connectionInitiationTask.getId());

        assertThat(reloadedDevice.getInboundConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getScheduledConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getConnectionInitiationTasks()).hasSize(1);
    }

    @Test
    @Transactional
    public void createConnectionInitiationTaskTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialConnectionInitiationTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Device", MRID);
        device.save();
        ConnectionInitiationTask connectionInitiationTask = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask).add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialConnectionInitiationTask.getId());
        assertThat(connectionTasks.get(0).getId()).isEqualTo(connectionInitiationTask.getId());

        assertThat(reloadedDevice.getInboundConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getScheduledConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getConnectionInitiationTasks()).hasSize(1);
    }

    @Test
    @Transactional
    public void createDeviceWithMultipleConnectionTasksTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithThreeTypesOfPartialsTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Device", MRID);
        final ConnectionInitiationTask connectionInitiationTask = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask).add();
//        final ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask).add();
//        final InboundConnectionTask inboundConnectionTask = device.getInboundConnectionTaskBuilder(partialInboundConnectionTask).add();
        final ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTasks().get(0);
        final InboundConnectionTask inboundConnectionTask = device.getInboundConnectionTasks().get(0);
        device.save();
        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(3);
        assertThat(connectionTasks).has(new Condition<List<ConnectionTask<?, ?>>>() {
            @Override
            public boolean matches(List<ConnectionTask<?, ?>> value) {
                int tripleMatch = 0b0000;
                for (ConnectionTask<?,?> connectionTask : value) {
                    if (connectionTask.getId() == scheduledConnectionTask.getId() && connectionTask.getPartialConnectionTask().getId() == partialScheduledConnectionTask.getId()) {
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

        assertThat(reloadedDevice.getInboundConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getScheduledConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getConnectionInitiationTasks()).hasSize(1);
    }

    @Test
    @Transactional
    public void deleteDeviceDeletesConnectionTasksTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithThreeTypesOfPartialsTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Device", MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();
        reloadedDevice.delete();
        for (ConnectionTask<?, ?> connectionTask : connectionTasks) {
            assertThat(inMemoryPersistence.getDeviceDataService().findConnectionTask(connectionTask.getId()).orNull()).isNull();
        }
    }

    @Test
    @Transactional
    public void deleteAConnectionTaskTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithConnectionType, "DeviceWithConnectionTasks", MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.removeConnectionTask(reloadedDevice.getConnectionTasks().get(0));

        Device deviceWithoutConnectionTasks = getReloadedDevice(reloadedDevice);

        assertThat(deviceWithoutConnectionTasks.getConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getInboundConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getScheduledConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
    }

    @Test(expected = CannotDeleteConnectionTaskWhichIsNotFromThisDevice.class)
    @Transactional
    public void deleteAConnectionTaskWhichIsNotFromThatDeviceTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithConnectionType, "DeviceWithConnectionTasks", MRID);
        device.save();

        Device otherDevice = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithConnectionType, "OtherDevice", MRID);

        // should throw exception
        otherDevice.removeConnectionTask(device.getConnectionTasks().get(0));
    }

    @Test
    @Transactional
    public void updateAConnectionTaskThroughHisDeviceTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithConnectionType, "DeviceWithConnectionTasks", MRID);
//        ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask).add();
        device.save();

        device.getScheduledConnectionTasks().get(0).setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        TemporalExpression newTemporalExpression = new TemporalExpression(TimeDuration.minutes(5));
        device.getScheduledConnectionTasks().get(0).setNextExecutionSpecsFrom(newTemporalExpression);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(((ScheduledConnectionTask) connectionTasks.get(0)).getConnectionStrategy()).isEqualTo(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        assertThat(((ScheduledConnectionTask) connectionTasks.get(0)).getNextExecutionSpecs().getTemporalExpression()).isEqualTo(newTemporalExpression);
    }

    @Test
    @Transactional
    public void scheduledConnectionTaskBuilderTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithConnectionType, "DeviceWithConnectionTasks", MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getConnectionTasks().get(0).getComPortPool().getId()).isEqualTo(outboundComPortPool.getId());
        assertThat(((ScheduledConnectionTask) reloadedDevice.getConnectionTasks().get(0)).getNextExecutionSpecs()).isEqualTo(null);
        assertThat(((ScheduledConnectionTask) reloadedDevice.getConnectionTasks().get(0)).getCommunicationWindow()).isEqualTo(COM_WINDOW);
        assertThat(((ScheduledConnectionTask) reloadedDevice.getConnectionTasks().get(0)).getConnectionStrategy()).isEqualTo( ConnectionStrategy.AS_SOON_AS_POSSIBLE);
    }



    @Ignore // TODO enable again once JP-1123 is completely finished
    @Test
    @Transactional
    public void scheduledConnectionTaskBuilderPropertyTest() {
        final String ipAddress = "10.0.1.12";
        final int portNumber = 4059;
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialIpOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeviceWithConnectionProps", MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getConnectionTasks().get(0).getProperties()).has(new Condition<List<ConnectionTaskProperty>>() {
            @Override
            public boolean matches(List<ConnectionTaskProperty> value) {
                int bothMatch = 0b0000;
                for (ConnectionTaskProperty connectionTaskProperty : value) {
                    if (connectionTaskProperty.getName().equals(IpConnectionType.IP_ADDRESS_PROPERTY_NAME) && connectionTaskProperty.getValue().toString().equals(ipAddress)) {
                        bothMatch |= 0b0001;
                    }
                    if (connectionTaskProperty.getName().equals(IpConnectionType.PORT_PROPERTY_NAME) && connectionTaskProperty.getValue().equals(portNumber)) {
                        bothMatch |= 0b0010;
                    }
                }
                return bothMatch == 0b0011;
            }
        });

        assertThat(reloadedDevice.getInboundConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getScheduledConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
    }

    @Test
    @Transactional
    public void inboundConnectionTaskBuilderTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialInboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeviceConfigWithInboundConnection", MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getConnectionTasks().get(0).getComPortPool().getId()).isEqualTo(inboundComPortPool.getId());

        assertThat(reloadedDevice.getInboundConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getScheduledConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
    }

    @Ignore // TODO enable again once JP-1123 is completely finished
    @Test
    @Transactional
    public void inboundConnectionTaskBuilderPropertyTest() {
        final String ipAddress = "10.0.1.12";
        final int portNumber = 4059;
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialIpInboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeviceWithConnectionProps", MRID);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getConnectionTasks().get(0).getProperties()).has(new Condition<List<ConnectionTaskProperty>>() {
            @Override
            public boolean matches(List<ConnectionTaskProperty> value) {
                int bothMatch = 0b0000;
                for (ConnectionTaskProperty connectionTaskProperty : value) {
                    if (connectionTaskProperty.getName().equals(IpConnectionType.IP_ADDRESS_PROPERTY_NAME) && connectionTaskProperty.getValue().toString().equals(ipAddress)) {
                        bothMatch |= 0b0001;
                    }
                    if (connectionTaskProperty.getName().equals(IpConnectionType.PORT_PROPERTY_NAME) && connectionTaskProperty.getValue().equals(portNumber)) {
                        bothMatch |= 0b0010;
                    }
                }
                return bothMatch == 0b0011;
            }
        });

        assertThat(reloadedDevice.getInboundConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getScheduledConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
    }

    @Test
    @Transactional
    public void connectionInitiationTaskBuilderTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialConnectionInitiationTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeviceWithConnectionProps", MRID);
        Device.ConnectionInitiationTaskBuilder connectionInitiationTaskBuilder = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask);
        connectionInitiationTaskBuilder.setComPortPool(otherOutboundComPortPool);
        ConnectionInitiationTask connectionInitiationTask = connectionInitiationTaskBuilder.add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getConnectionTasks().get(0).getComPortPool().getId()).isEqualTo(otherOutboundComPortPool.getId());
    }

    @Ignore // TODO enable again once JP-1123 is completely finished
    @Test
    @Transactional
    public void connectionInitiationTaskBuilderPropertyTest() {
        final String ipAddress = "10.0.1.12";
        final int portNumber = 4059;
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialConnectionInitiationTask();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeviceWithConnectionProps", MRID);
        Device.ConnectionInitiationTaskBuilder connectionInitiationTaskBuilder = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask);
        connectionInitiationTaskBuilder.setProperty(IpConnectionType.IP_ADDRESS_PROPERTY_NAME, ipAddress);
        connectionInitiationTaskBuilder.setProperty(IpConnectionType.PORT_PROPERTY_NAME, portNumber);
        ConnectionInitiationTask connectionInitiationTask = connectionInitiationTaskBuilder.add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getConnectionTasks().get(0).getProperties()).has(new Condition<List<ConnectionTaskProperty>>() {
            @Override
            public boolean matches(List<ConnectionTaskProperty> value) {
                int bothMatch = 0b0000;
                for (ConnectionTaskProperty connectionTaskProperty : value) {
                    if (connectionTaskProperty.getName().equals(IpConnectionType.IP_ADDRESS_PROPERTY_NAME) && connectionTaskProperty.getValue().toString().equals(ipAddress)) {
                        bothMatch |= 0b0001;
                    }
                    if (connectionTaskProperty.getName().equals(IpConnectionType.PORT_PROPERTY_NAME) && connectionTaskProperty.getValue().equals(portNumber)) {
                        bothMatch |= 0b0010;
                    }
                }
                return bothMatch == 0b0011;
            }
        });
    }
}
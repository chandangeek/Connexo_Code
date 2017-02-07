/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.Privilege;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialConnectionInitiationTaskBuilder;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.tasks.InboundNoParamsConnectionTypeImpl;
import com.energyict.mdc.device.data.impl.tasks.IpConnectionProperties;
import com.energyict.mdc.device.data.impl.tasks.OutboundIpConnectionTypeImpl;
import com.energyict.mdc.device.data.impl.tasks.OutboundNoParamsConnectionTypeImpl;
import com.energyict.mdc.device.data.impl.tasks.SimpleDiscoveryProtocol;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;

import com.google.common.base.Strings;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.assertj.core.api.Condition;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceCommunicationTest extends PersistenceIntegrationTest {

    private static final ComWindow COM_WINDOW = new ComWindow();
    private static final String MRID = "MRID";

    private static ConnectionTypePluggableClass ipConnectionTypePluggableClass;
    private static ConnectionTypePluggableClass outboundNoParamsPluggableClass;
    private static ConnectionTypePluggableClass inboundNoParamsPluggableClass;

    private OutboundComPortPool outboundComPortPool;
    private OutboundComPortPool otherOutboundComPortPool;
    private InboundComPortPool inboundComPortPool;
    private InboundComPortPool otherInboundComPortPool;
    private PartialScheduledConnectionTask partialScheduledConnectionTask;
    private PartialInboundConnectionTask partialInboundConnectionTask;
    private PartialConnectionInitiationTask partialConnectionInitiationTask;

    @BeforeClass
    public static void initializePluggableClasses() {
        try (TransactionContext context = getTransactionService().getContext()) {
            outboundNoParamsPluggableClass = inMemoryPersistence.getProtocolPluggableService().newConnectionTypePluggableClass("NoParamsOutboundConnectionType", OutboundNoParamsConnectionTypeImpl.class.getName());
            outboundNoParamsPluggableClass.save();
            inboundNoParamsPluggableClass = inMemoryPersistence.getProtocolPluggableService().newConnectionTypePluggableClass("NoParamsInboundConnectionType", InboundNoParamsConnectionTypeImpl.class.getName());
            inboundNoParamsPluggableClass.save();
            ipConnectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService().newConnectionTypePluggableClass("IPConnectionType", OutboundIpConnectionTypeImpl.class.getName());
            ipConnectionTypePluggableClass.save();
            deviceProtocolPluggableClass = inMemoryPersistence.getProtocolPluggableService().newDeviceProtocolPluggableClass("MyTestProtocol", TestProtocol.class.getName());
            deviceProtocolPluggableClass.save();
            context.commit();
        }
    }

    @AfterClass
    public static void deletePluggableClasses() throws SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            outboundNoParamsPluggableClass.delete();
            inboundNoParamsPluggableClass.delete();
            ipConnectionTypePluggableClass.delete();
            context.commit();
        }
        inMemoryPersistence.cleanUpDataBase();
    }

    @Before
    public void initBefore() {
        outboundComPortPool = inMemoryPersistence.getEngineConfigurationService().newOutboundComPortPool("OutboundComPortPool", ComPortType.TCP, TimeDuration.minutes(15));
        outboundComPortPool.setActive(true);
        outboundComPortPool.update();

        otherOutboundComPortPool = inMemoryPersistence.getEngineConfigurationService().newOutboundComPortPool("OtherPool", ComPortType.TCP, TimeDuration.minutes(30));
        otherOutboundComPortPool.setActive(true);
        otherOutboundComPortPool.update();

        InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass = inMemoryPersistence.getProtocolPluggableService()
                .newInboundDeviceProtocolPluggableClass("MyInboundDeviceProtocolPluggableClass", SimpleDiscoveryProtocol.class.getName());
        inboundDeviceProtocolPluggableClass.save();
        inboundComPortPool = inMemoryPersistence.getEngineConfigurationService().newInboundComPortPool("InboundComPortPool", ComPortType.TCP, inboundDeviceProtocolPluggableClass);
        inboundComPortPool.setActive(true);
        inboundComPortPool.update();

        otherInboundComPortPool = inMemoryPersistence.getEngineConfigurationService().newInboundComPortPool("OtherInboundPool", ComPortType.TCP, inboundDeviceProtocolPluggableClass);
        otherInboundComPortPool.setActive(true);
        otherInboundComPortPool.update();
        IssueStatus wontFix = mock(IssueStatus.class);
        when(inMemoryPersistence.getIssueService().findStatus(IssueStatus.WONT_FIX)).thenReturn(Optional.of(wontFix));
    }

    private DeviceConfiguration createDeviceConfigWithPartialOutboundConnectionTask() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithPartialOutboundConnectionTask");
        configurationWithConnectionType.isDirectlyAddressable(true);
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        addPartialOutboundConnectionTask(deviceConfiguration);
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    private DeviceConfiguration createDeviceConfigWithPartialIpOutboundConnectionTask() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithPartialIpOutboundConnectionTask");
        configurationWithConnectionType.isDirectlyAddressable(true);
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        addPartialIpOutboundConnectionTask(deviceConfiguration);
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    private DeviceConfiguration createDeviceConfigWithPartialInboundConnectionTask() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithPartialInboundConnectionTask");
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        addPartialInboundConnectionTask(deviceConfiguration);
        return deviceConfiguration;
    }

    private DeviceConfiguration createDeviceConfigWithPartialIpInboundConnectionTask() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithPartialInboundConnectionTask");
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        addPartialInboundConnectionTask(deviceConfiguration);
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    private DeviceConfiguration createDeviceConfigWithPartialConnectionInitiationTask() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithPartialConnectionInitiationTask");
        configurationWithConnectionType.isDirectlyAddressable(true);
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        addPartialConnectionInitiationConnectionTask(deviceConfiguration);
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    private DeviceConfiguration createDeviceConfigWithThreeTypesOfPartialsTask() {
        DeviceType.DeviceConfigurationBuilder configurationWithConnectionType = deviceType.newConfiguration("ConfigurationWithThreePartials");
        configurationWithConnectionType.isDirectlyAddressable(true);
        DeviceConfiguration deviceConfiguration = configurationWithConnectionType.add();
        addPartialOutboundConnectionTask(deviceConfiguration);
        addPartialInboundConnectionTask(deviceConfiguration);
        addPartialConnectionInitiationConnectionTask(deviceConfiguration);
        return deviceConfiguration;
    }

    private void addPartialOutboundConnectionTask(DeviceConfiguration communicationConfiguration) {
        addPartialOutboundConnectionTaskFor(communicationConfiguration, outboundNoParamsPluggableClass);
    }

    private void addPartialIpOutboundConnectionTask(DeviceConfiguration communicationConfiguration) {
        addPartialOutboundConnectionTaskFor(communicationConfiguration, ipConnectionTypePluggableClass);
    }

    private void addPartialOutboundConnectionTaskFor(DeviceConfiguration communicationConfiguration, ConnectionTypePluggableClass connectionTypePluggableClass) {
        PartialScheduledConnectionTaskBuilder partialScheduledConnectionTaskBuilder = communicationConfiguration.newPartialScheduledConnectionTask("MyOutbound", connectionTypePluggableClass, TimeDuration.seconds(60), ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .comPortPool(outboundComPortPool)
                .comWindow(COM_WINDOW)
                .asDefault(true);
        partialScheduledConnectionTask = partialScheduledConnectionTaskBuilder.build();
        communicationConfiguration.save();
    }

    private void addPartialInboundConnectionTask(DeviceConfiguration deviceConfiguration) {
        addPartialInboundConnectionTaskFor(deviceConfiguration, inboundNoParamsPluggableClass);
    }

    private void addPartialInboundConnectionTaskFor(DeviceConfiguration deviceConfiguration, ConnectionTypePluggableClass connectionTypePluggableClass) {
        PartialInboundConnectionTaskBuilder partialInboundConnectionTaskBuilder = deviceConfiguration.newPartialInboundConnectionTask("MyInboundConnectionTask", connectionTypePluggableClass)
                .comPortPool(inboundComPortPool)
                .asDefault(false);
        partialInboundConnectionTask = partialInboundConnectionTaskBuilder.build();
        deviceConfiguration.save();
    }

    private void addPartialConnectionInitiationConnectionTask(DeviceConfiguration deviceConfiguration) {
        PartialConnectionInitiationTaskBuilder partialConnectionInitiationTaskBuilder = deviceConfiguration.newPartialConnectionInitiationTask("MyConnectionInitiationTask", outboundNoParamsPluggableClass, TimeDuration
                .seconds(60))
                .comPortPool(outboundComPortPool);
        partialConnectionInitiationTask = partialConnectionInitiationTaskBuilder.build();
        deviceConfiguration.save();
    }

    @Test
    @Transactional
    public void createDeviceWithoutConnectionTasksTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfigurationWithConnectionType, "DeviceWithoutConnectionTasks", MRID, Instant.now());
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();

        assertThat(connectionTasks).isEmpty();
        assertThat(reloadedDevice.getInboundConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getScheduledConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
    }

    @Test
    @Transactional
    public void createDeviceWithScheduledConnectionTaskTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        deviceConfigurationWithConnectionType.activate();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfigurationWithConnectionType, "DeviceWithConnectionTasks", MRID, Instant.now());
        device.save();
        ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask).add();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialScheduledConnectionTask.getId());
        assertThat(connectionTasks.get(0).getId()).isEqualTo(scheduledConnectionTask.getId());

        assertThat(reloadedDevice.getInboundConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getScheduledConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
    }

    @Test
    @Transactional
    public void createScheduledConnectionTaskAfterDeviceCreationTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfigurationWithConnectionType, "AddConnectionTasksAfterDeviceCreation", MRID, Instant
                        .now());
        device.save();
        ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask).add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialScheduledConnectionTask.getId());
        assertThat(connectionTasks.get(0).getId()).isEqualTo(scheduledConnectionTask.getId());

        assertThat(reloadedDevice.getInboundConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getScheduledConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
    }

    @Test
    @Transactional
    public void createDeviceWithInboundConnectionTaskTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialInboundConnectionTask();
        deviceConfiguration.activate();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "DeviceWithConnectionTasks", MRID, Instant.now());
        device.save();
        InboundConnectionTask inboundConnectionTask = device.getInboundConnectionTaskBuilder(partialInboundConnectionTask).add();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialInboundConnectionTask.getId());
        assertThat(connectionTasks.get(0).getId()).isEqualTo(inboundConnectionTask.getId());

        assertThat(reloadedDevice.getInboundConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getScheduledConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
    }

    @Test
    @Transactional
    public void createInboundConnectionTaskAfterDeviceCreationTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialInboundConnectionTask();
        deviceConfiguration.activate();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "DeviceWithConnectionTasks", MRID, Instant.now());
        device.save();
        InboundConnectionTask inboundConnectionTask = device.getInboundConnectionTaskBuilder(partialInboundConnectionTask).add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        assertThat(connectionTasks.get(0).getPartialConnectionTask().getId()).isEqualTo(partialInboundConnectionTask.getId());
        assertThat(connectionTasks.get(0).getId()).isEqualTo(inboundConnectionTask.getId());

        assertThat(reloadedDevice.getInboundConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getScheduledConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
    }

    @Test
    @Transactional
    public void createDeviceWithConnectionInitiationTaskTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialConnectionInitiationTask();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "Device", MRID, Instant.now());
        device.save();
        ConnectionInitiationTask connectionInitiationTask = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask).add();

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
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "Device", MRID, Instant.now());
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
        deviceConfiguration.activate();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "Device", MRID, Instant.now());
        device.save();
        final ConnectionInitiationTask connectionInitiationTask = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask).add();
        final ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask).add();
        final InboundConnectionTask inboundConnectionTask = device.getInboundConnectionTaskBuilder(partialInboundConnectionTask).add();

        Device reloadedDevice = getReloadedDevice(device);
        List<ConnectionTask<?, ?>> connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(3);
        assertThat(connectionTasks).has(new Condition<List<? extends ConnectionTask<?, ?>>>() {
            @Override
            public boolean matches(List<? extends ConnectionTask<?, ?>> value) {
                int tripleMatch = 0b0000;
                for (ConnectionTask<?, ?> connectionTask : value) {
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
        deviceConfiguration.activate();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "Device", MRID, Instant.now());
        device.save();
        final ConnectionInitiationTask connectionInitiationTask = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask).add();
        final ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask).add();
        final InboundConnectionTask inboundConnectionTask = device.getInboundConnectionTaskBuilder(partialInboundConnectionTask).add();

        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.delete();

        assertThat(inMemoryPersistence.getConnectionTaskService().findConnectionTask(connectionInitiationTask.getId()).orElse(null)).isNull();
        assertThat(inMemoryPersistence.getConnectionTaskService().findConnectionTask(scheduledConnectionTask.getId()).orElse(null)).isNull();
        assertThat(inMemoryPersistence.getConnectionTaskService().findConnectionTask(inboundConnectionTask.getId()).orElse(null)).isNull();
    }

    @Test
    @Transactional
    public void deleteAConnectionTaskTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        deviceConfigurationWithConnectionType.activate();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfigurationWithConnectionType, "DeviceWithConnectionTasks", MRID, Instant.now());

        ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask).add();

        ScheduledConnectionTask reloaded = inMemoryPersistence.getConnectionTaskService().findScheduledConnectionTask(scheduledConnectionTask.getId()).get();
        Device reloadedDevice = getReloadedDevice(device);
        reloadedDevice.removeConnectionTask(reloaded);
        assertThat(reloadedDevice.getInboundConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getScheduledConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();

        Device deviceWithoutConnectionTasks = getReloadedDevice(reloadedDevice);

        assertThat(deviceWithoutConnectionTasks.getConnectionTasks()).isEmpty();
        assertThat(deviceWithoutConnectionTasks.getInboundConnectionTasks()).isEmpty();
        assertThat(deviceWithoutConnectionTasks.getScheduledConnectionTasks()).isEmpty();
        assertThat(deviceWithoutConnectionTasks.getConnectionInitiationTasks()).isEmpty();
    }

    @Test
    @Transactional
    public void updateAConnectionTaskThroughHisDeviceTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfigurationWithConnectionType, "DeviceWithConnectionTasks", MRID, Instant.now());
        device.save();

        TemporalExpression newTemporalExpression = new TemporalExpression(TimeDuration.minutes(5));
        device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask)
                .setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .setNextExecutionSpecsFrom(newTemporalExpression).add();

        List<ConnectionTask<?, ?>> connectionTasks = device.getConnectionTasks();
        assertThat(((ScheduledConnectionTask) connectionTasks.get(0)).getNextExecutionSpecs()).isNotNull();

        Device reloadedDevice = getReloadedDevice(device);
        connectionTasks = reloadedDevice.getConnectionTasks();
        assertThat(connectionTasks).hasSize(1);
        ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) connectionTasks.get(0);
        assertThat(scheduledConnectionTask.getConnectionStrategy()).isEqualTo(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        assertThat(scheduledConnectionTask.getNextExecutionSpecs()).isNotNull();
        assertThat(scheduledConnectionTask.getNextExecutionSpecs().getTemporalExpression()).isEqualTo(newTemporalExpression);
    }

    @Test
    @Transactional
    public void scheduledConnectionTaskBuilderTest() {
        TemporalExpression nextExecutionSpecTempExpression = new TemporalExpression(TimeDuration.hours(1));
        ConnectionStrategy minimizeConnectionStrategy = ConnectionStrategy.MINIMIZE_CONNECTIONS;
        ComWindow communicationWindow = new ComWindow(PartialTime.fromHours(0), PartialTime.fromHours(12));

        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithPartialOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfigurationWithConnectionType, "DeviceWithConnectionTasks", MRID, Instant.now());
        device.save();
        Device.ScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask);
        scheduledConnectionTaskBuilder.setConnectionStrategy(minimizeConnectionStrategy);
        scheduledConnectionTaskBuilder.setNextExecutionSpecsFrom(nextExecutionSpecTempExpression);
        scheduledConnectionTaskBuilder.setCommunicationWindow(communicationWindow);
        scheduledConnectionTaskBuilder.setComPortPool(otherOutboundComPortPool);
        scheduledConnectionTaskBuilder.add();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getConnectionTasks().get(0).getComPortPool().getId()).isEqualTo(otherOutboundComPortPool.getId());
        ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) reloadedDevice.getConnectionTasks().get(0);
        assertThat(scheduledConnectionTask.getNextExecutionSpecs().getTemporalExpression()).isEqualTo(nextExecutionSpecTempExpression);
        assertThat(scheduledConnectionTask.getCommunicationWindow()).isEqualTo(communicationWindow);
        assertThat(scheduledConnectionTask.getConnectionStrategy()).isEqualTo(minimizeConnectionStrategy);
    }

    @Test
    @Transactional
    public void scheduledConnectionTaskBuilderWithConnectionInitiatorTest() {
        DeviceConfiguration deviceConfigurationWithConnectionType = createDeviceConfigWithThreeTypesOfPartialsTask();
        deviceConfigurationWithConnectionType.activate();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfigurationWithConnectionType, "DeviceWithConnectionTasks", MRID, Instant.now());
        device.save();
        final ConnectionInitiationTask connectionInitiationTask = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask).add();
        Device.ScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask);
        scheduledConnectionTaskBuilder.setInitiatorTask(connectionInitiationTask);
        final ScheduledConnectionTask scheduledConnectionTask = scheduledConnectionTaskBuilder.add();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getConnectionTasks()).has(new Condition<List<? extends ConnectionTask<?, ?>>>() {
            @Override
            public boolean matches(List<? extends ConnectionTask<?, ?>> value) {
                for (ConnectionTask connectionTask : value) {
                    if (connectionTask.getId() == scheduledConnectionTask.getId()) {
                        return ((ScheduledConnectionTask) connectionTask).getInitiatorTask().getId() == connectionInitiationTask.getId();
                    }
                }
                return false;
            }
        });
    }

    @Test
    @Transactional
    public void scheduledConnectionTaskBuilderPropertyTest() {
        String ipAddress = "10.0.1.12";
        BigDecimal portNumber = new BigDecimal("4059");
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialIpOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "DeviceWithConnectionProps", MRID, Instant.now());
        device.save();
        this.grantAllViewAndEditPrivilegesToPrincipal();
        Device.ScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask);
        scheduledConnectionTaskBuilder.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), ipAddress);
        scheduledConnectionTaskBuilder.setProperty(IpConnectionProperties.PORT.propertyName(), portNumber);
        scheduledConnectionTaskBuilder.add();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getConnectionTasks().get(0).getProperties()).has(new Condition<List<? extends ConnectionTaskProperty>>() {
            @Override
            public boolean matches(List<? extends ConnectionTaskProperty> value) {
                int bothMatch = 0b0000;
                for (ConnectionTaskProperty connectionTaskProperty : value) {
                    if (connectionTaskProperty.getName().equals(IpConnectionProperties.IP_ADDRESS.propertyName()) && connectionTaskProperty.getValue().toString().equals(ipAddress)) {
                        bothMatch |= 0b0001;
                    }
                    if (connectionTaskProperty.getName().equals(IpConnectionProperties.PORT.propertyName()) && connectionTaskProperty.getValue().equals(portNumber)) {
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

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    public void scheduledConnectionTaskBuilderLargePropertyTest() {
        String ipAddress = Strings.repeat("10.0.1.12", StringFactory.MAX_SIZE);
        BigDecimal portNumber = new BigDecimal("4059");
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialIpOutboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "DeviceWithConnectionProps", MRID, Instant.now());

        Device.ScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask);
        scheduledConnectionTaskBuilder.setProperty(IpConnectionProperties.PORT.propertyName(), portNumber);

        // Business method
        scheduledConnectionTaskBuilder.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), ipAddress);
        scheduledConnectionTaskBuilder.add();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void inboundConnectionTaskBuilderTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialInboundConnectionTask();
        deviceConfiguration.activate();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "DeviceConfigWithInboundConnection", MRID, Instant.now());
        device.save();
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = device.getInboundConnectionTaskBuilder(partialInboundConnectionTask);
        inboundConnectionTaskBuilder.setComPortPool(otherInboundComPortPool);
        inboundConnectionTaskBuilder.add();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getConnectionTasks().get(0).getComPortPool().getId()).isEqualTo(otherInboundComPortPool.getId());

        assertThat(reloadedDevice.getInboundConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getScheduledConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
    }

    @Test
    @Transactional
    public void inboundConnectionTaskBuilderPropertyTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialIpInboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "DeviceWithConnectionProps", MRID, Instant.now());
        device.save();
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = device.getInboundConnectionTaskBuilder(partialInboundConnectionTask);
        inboundConnectionTaskBuilder.add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getInboundConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getScheduledConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getConnectionInitiationTasks()).isEmpty();
        InboundConnectionTask inboundConnectionTask = reloadedDevice.getInboundConnectionTasks().get(0);
        assertThat(inboundConnectionTask.getProperties()).isEmpty();

    }

    @Test(expected = ConstraintViolationException.class)
    @Transactional
    public void inboundConnectionTaskBuilderLargePropertyTest() {
        String ipAddress = Strings.repeat("10.0.1.12", StringFactory.MAX_SIZE);
        BigDecimal portNumber = new BigDecimal("4059");
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialIpInboundConnectionTask();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "DeviceWithConnectionProps", MRID, Instant.now());
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = device.getInboundConnectionTaskBuilder(partialInboundConnectionTask);
        inboundConnectionTaskBuilder.setProperty(IpConnectionProperties.PORT.propertyName(), portNumber);

        // Business method
        inboundConnectionTaskBuilder.setProperty(IpConnectionProperties.IP_ADDRESS.propertyName(), ipAddress);
        inboundConnectionTaskBuilder.add();
        device.save();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void connectionInitiationTaskBuilderTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialConnectionInitiationTask();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "DeviceWithConnectionProps", MRID, Instant.now());
        device.save();
        Device.ConnectionInitiationTaskBuilder connectionInitiationTaskBuilder = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask);
        connectionInitiationTaskBuilder.setComPortPool(otherOutboundComPortPool);
        connectionInitiationTaskBuilder.add();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getConnectionTasks().get(0).getComPortPool().getId()).isEqualTo(otherOutboundComPortPool.getId());
    }

    @Test
    @Transactional
    public void connectionInitiationTaskBuilderPropertyTest() {
        DeviceConfiguration deviceConfiguration = createDeviceConfigWithPartialConnectionInitiationTask();
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "DeviceWithConnectionProps", MRID, Instant.now());
        device.save();
        Device.ConnectionInitiationTaskBuilder connectionInitiationTaskBuilder = device.getConnectionInitiationTaskBuilder(partialConnectionInitiationTask);
        connectionInitiationTaskBuilder.add();
        device.save();

        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getConnectionTasks()).hasSize(1);
        assertThat(reloadedDevice.getConnectionInitiationTasks()).hasSize(1);
        assertThat(reloadedDevice.getInboundConnectionTasks()).isEmpty();
        assertThat(reloadedDevice.getScheduledConnectionTasks()).isEmpty();
        ConnectionInitiationTask connectionInitiationTask = reloadedDevice.getConnectionInitiationTasks().get(0);
        assertThat(connectionInitiationTask.getProperties()).isEmpty();
    }

    private void grantAllViewAndEditPrivilegesToPrincipal() {
        Set<Privilege> privileges = new HashSet<>();
        Privilege editPrivilege = mock(Privilege.class);
        when(editPrivilege.getName()).thenReturn(EditPrivilege.LEVEL_1.getPrivilege());
        privileges.add(editPrivilege);
        Privilege viewPrivilege = mock(Privilege.class);
        when(viewPrivilege.getName()).thenReturn(ViewPrivilege.LEVEL_1.getPrivilege());
        privileges.add(viewPrivilege);
        when(inMemoryPersistence.getMockedUser().getPrivileges()).thenReturn(privileges);
        when(inMemoryPersistence.getMockedUser().getPrivileges(anyString())).thenReturn(privileges);
    }

}
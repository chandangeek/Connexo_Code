/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.upl.properties.PropertySpec;

import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.junit.Before;

import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provides code reuse opportunities for ComTaskExecutionImpl component test cases.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-01 (13:44)
 */
public abstract class AbstractComTaskExecutionImplTest extends PersistenceIntegrationTest {

    protected String COM_TASK_NAME = "TheNameOfMyComTask";
    protected int maxNrOfTries = 27;
    protected int comTaskEnablementPriority = 213;
    protected ConnectionFunction connectionFunction1, connectionFunction2, connectionFunction3;

    protected static OutboundComPortPool createOutboundIpComPortPool(String name) {
        OutboundComPortPool ipComPortPool = inMemoryPersistence.getEngineConfigurationService().newOutboundComPortPool(name, ComPortType.TCP, new TimeDuration(1, TimeDuration.TimeUnit.MINUTES));
        ipComPortPool.setActive(true);
        ipComPortPool.update();
        return ipComPortPool;
    }

    protected static InboundComPortPool createInboundComPortPool(String name) {
        InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass = mock(InboundDeviceProtocolPluggableClass.class);
        when(inboundDeviceProtocolPluggableClass.getId()).thenReturn(1L);
        InboundComPortPool inboundComPortPool = inMemoryPersistence.getEngineConfigurationService().newInboundComPortPool(name, ComPortType.TCP, inboundDeviceProtocolPluggableClass, Collections.emptyMap());
        inboundComPortPool.setActive(true);
        inboundComPortPool.update();
        return inboundComPortPool;
    }

    @Before
    @Override
    public void initializeMocks() {
        super.initializeMocks();
        connectionFunction1 = mockConnectionFunction(1, "CF1", "CF 1");
        connectionFunction2 = mockConnectionFunction(2, "CF2", "CF 2");
        connectionFunction3 = mockConnectionFunction(3, "CF3", "CF 3");
        when(deviceProtocolPluggableClass.getConsumableConnectionFunctions()).thenReturn(Arrays.asList(connectionFunction1, connectionFunction2));
    }

    @Before
    public void getFirstProtocolDialectConfigurationPropertiesFromDeviceConfiguration() {
        IssueStatus wontFix = mock(IssueStatus.class);
        when(inMemoryPersistence.getIssueService().findStatus(IssueStatus.WONT_FIX)).thenReturn(Optional.of(wontFix));
    }

    protected ComTask createComTaskWithBasicCheck(String comTaskName) {
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask(comTaskName);
        comTask.setStoreData(true);
        comTask.setMaxNrOfTries(maxNrOfTries);
        comTask.createBasicCheckTask().add();
        comTask.save();
        return inMemoryPersistence.getTaskService().findComTask(comTask.getId()).get(); // to make sure all elements in the composition are properly loaded
    }

    protected ComTaskEnablement enableComTask(boolean useDefault) {
        return enableComTask(useDefault, COM_TASK_NAME);
    }

    protected ComTaskEnablement enableComTask(ConnectionFunction connectionFunction) {
        return enableComTask(false, connectionFunction, COM_TASK_NAME);
    }

    protected ComTaskEnablement enableComTask(boolean useDefault, String comTaskName) {
        return enableComTask(useDefault, null, comTaskName);
    }

    protected ComTaskEnablement enableComTask(boolean useDefault, ConnectionFunction connectionFunction, String comTaskName) {
        ComTask comTaskWithBasicCheck = createComTaskWithBasicCheck(comTaskName);
        ComTaskEnablementBuilder builder = this.deviceConfiguration.enableComTask(comTaskWithBasicCheck, this.securityPropertySet);
        builder.useDefaultConnectionTask(useDefault);
        if (!useDefault && connectionFunction != null) {
            builder.setConnectionFunction(connectionFunction);
        }
        builder.setPriority(this.comTaskEnablementPriority);
        return builder.add();
    }

    protected ComTaskExecution reloadComTaskExecution(Device device, ComTaskExecution comTaskExecution) {
        Device reloadedDevice = getReloadedDevice(device);
        for (ComTaskExecution taskExecution : reloadedDevice.getComTaskExecutions()) {
            if (comTaskExecution.getId() == taskExecution.getId()) {
                return taskExecution;
            }
        }
        fail("ComTaskExecution with id " + comTaskExecution.getId() + " not found after reloading device " + device.getName());
        return null;
    }

    protected ScheduledConnectionTaskImpl createAsapWithNoPropertiesWithoutViolations(String name, Device device, PartialScheduledConnectionTask partialConnectionTask, OutboundComPortPool outboundTcpipComPortPool) {
        partialConnectionTask.setName(name);
        partialConnectionTask.save();

        return (ScheduledConnectionTaskImpl) device.getScheduledConnectionTaskBuilder(partialConnectionTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .add();
    }

    protected InboundConnectionTaskImpl createInboundWithNoPropertiesWithoutViolations(String name, Device device, PartialInboundConnectionTask partialConnectionTask, InboundComPortPool inboundComPortPool) {
        partialConnectionTask.setName(name);
        partialConnectionTask.save();

        return (InboundConnectionTaskImpl) device.getInboundConnectionTaskBuilder(partialConnectionTask)
                .setComPortPool(inboundComPortPool)
                .add();
    }

    protected PartialScheduledConnectionTask createPartialScheduledConnectionTask() {
        return this.createPartialScheduledConnectionTask(TimeDuration.minutes(5));
    }

    protected PartialScheduledConnectionTask createPartialScheduledConnectionTask(TimeDuration frequency) {
        ProtocolDialectConfigurationProperties configDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new PartialConnectionTaskProtocolDialect());
        deviceConfiguration.save();
        ConnectionTypePluggableClass connectionTypePluggableClass =
                inMemoryPersistence.getProtocolPluggableService()
                        .newConnectionTypePluggableClass(
                                OutboundNoParamsConnectionTypeImpl.class.getSimpleName(),
                                OutboundNoParamsConnectionTypeImpl.class.getName());
        connectionTypePluggableClass.save();
        return deviceConfiguration.
                newPartialScheduledConnectionTask(
                        "Outbound (1)",
                        connectionTypePluggableClass,
                        frequency,
                        ConnectionStrategy.AS_SOON_AS_POSSIBLE,
                        configDialect).
                comWindow(new ComWindow(0, 7200)).
                build();
    }

    protected ScheduledConnectionTaskImpl createASAPConnectionStandardTask(Device device) {
        return this.createASAPConnectionStandardTask(device, TimeDuration.minutes(5));
    }

    protected ScheduledConnectionTaskImpl createASAPConnectionStandardTask(Device device, TimeDuration frequency) {
        PartialScheduledConnectionTask partialScheduledConnectionTask = createPartialScheduledConnectionTask(frequency);
        OutboundComPortPool outboundPool = createOutboundIpComPortPool("MyOutboundPool");
        return createAsapWithNoPropertiesWithoutViolations("MyConnectionTask", device, partialScheduledConnectionTask, outboundPool);
    }

    protected ScheduledConnectionTaskImpl createMinimizeOneDayConnectionStandardTask(Device device) {
        PartialScheduledConnectionTask partialOutboundConnectionTask = createPartialScheduledConnectionTask();
        OutboundComPortPool outboundPool = createOutboundIpComPortPool("MyOutboundPool");
        partialOutboundConnectionTask.setName("Minimize");
        partialOutboundConnectionTask.save();
        return (ScheduledConnectionTaskImpl) device.getScheduledConnectionTaskBuilder(partialOutboundConnectionTask)
                .setComPortPool(outboundPool)
                .setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .setNextExecutionSpecsFrom(new TemporalExpression(TimeDuration.days(1)))
                .add();
    }

    protected InboundConnectionTaskImpl createInboundConnectionStandardTask(Device device) {
        PartialInboundConnectionTask partialInboundConnectionTask = createPartialInboundConnectionTask();
        InboundComPortPool inboundComPortPool = createInboundComPortPool("MyInboundPool");
        return createInboundWithNoPropertiesWithoutViolations("MyInboundConnectionTask", device, partialInboundConnectionTask, inboundComPortPool);
    }

    protected PartialInboundConnectionTask createPartialInboundConnectionTask() {
        ProtocolDialectConfigurationProperties configDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new PartialConnectionTaskProtocolDialect());
        deviceConfiguration.save();
        ConnectionTypePluggableClass connectionTypePluggableClass =
                inMemoryPersistence.getProtocolPluggableService()
                        .newConnectionTypePluggableClass(
                                InboundNoParamsConnectionTypeImpl.class.getSimpleName(),
                                InboundNoParamsConnectionTypeImpl.class.getName());
        connectionTypePluggableClass.save();
        return deviceConfiguration.
                newPartialInboundConnectionTask(
                        "Inbound (1)",
                        connectionTypePluggableClass,
                        configDialect)
                .build();
    }

    protected ComSchedule createComSchedule(ComTask comTask) {
        return createComSchedule(comTask, new TemporalExpression(TimeDuration.days(1)));
    }

    protected ComSchedule createComSchedule(String name, ComTask comTask) {
        return createComSchedule(name, comTask, new TemporalExpression(TimeDuration.days(1)));
    }

    protected ComSchedule createComSchedule(ComTask comTask, TemporalExpression temporalExpression) {
        return this.createComSchedule("MyComSchedule", comTask, temporalExpression);
    }

    protected ComSchedule createComSchedule(String name, ComTask comTask, TemporalExpression temporalExpression) {
        return inMemoryPersistence.getSchedulingService()
                .newComSchedule(name, temporalExpression, Instant.now())
                .addComTask(comTask)
                .build();
    }

    protected OutboundComPort createOutboundComPort() {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = inMemoryPersistence.getEngineConfigurationService().newOnlineComServerBuilder();
        String name = "ComServer";
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.storeTaskQueueSize(1);
        onlineComServerBuilder.storeTaskThreadPriority(1);
        onlineComServerBuilder.changesInterPollDelay(TimeDuration.minutes(5));
        onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.schedulingInterPollDelay(TimeDuration.minutes(1));
        onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.serverName(name);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        onlineComServerBuilder.numberOfStoreTaskThreads(2);
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();
        OutboundComPort.OutboundComPortBuilder outboundComPortBuilder = onlineComServer.newOutboundComPort("ComPort", 1);
        outboundComPortBuilder.comPortType(ComPortType.TCP);
        return outboundComPortBuilder.add();
    }

    protected InboundComPort createInboundComPort() {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = inMemoryPersistence.getEngineConfigurationService().newOnlineComServerBuilder();
        String name = "ComServer";
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.storeTaskQueueSize(1);
        onlineComServerBuilder.storeTaskThreadPriority(1);
        onlineComServerBuilder.changesInterPollDelay(TimeDuration.minutes(5));
        onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.schedulingInterPollDelay(TimeDuration.minutes(1));
        onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.numberOfStoreTaskThreads(2);
        onlineComServerBuilder.serverName(name);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();
        InboundComPort.InboundComPortBuilder inboundComPortBuilder = onlineComServer.newTCPBasedInboundComPort("ComPort", 1, 80);
        return (InboundComPort) inboundComPortBuilder.add();
    }

    protected Instant createFixedTimeStamp(int years, int months, int days, int hours, int minutes, int seconds, int millis) {
        return createFixedTimeStamp(years, months, days, hours, minutes, seconds, millis, null);
    }

    protected Instant createFixedTimeStamp(int years, int months, int days, int hours, int minutes, int seconds, int millis, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone == null ? utcTimeZone : timeZone);
        calendar.set(years, months, days, hours, minutes, seconds);
        calendar.set(Calendar.MILLISECOND, millis);
        return calendar.getTime().toInstant();
    }

    private ConnectionFunction mockConnectionFunction(long id, String name, String localizedName) {
        ConnectionFunction connectionFunction = mock(ConnectionFunction.class);
        when(connectionFunction.getId()).thenReturn(id);
        when(connectionFunction.getConnectionFunctionName()).thenReturn(name);
        when(connectionFunction.getConnectionFunctionDisplayName()).thenReturn(localizedName);
        return connectionFunction;
    }

    class PartialConnectionTaskProtocolDialect implements DeviceProtocolDialect {

        @Override
        public String getDeviceProtocolDialectName() {
            return Property.DEVICE_PROTOCOL_DIALECT.getName();
        }

        @Override
        public List<PropertySpec> getUPLPropertySpecs() {
            return getPropertySpecs().stream().map(ConnexoToUPLPropertSpecAdapter::adaptTo).collect(Collectors.toList());
        }

        @Override
        public String getDeviceProtocolDialectDisplayName() {
            return "It's a Dell Display";
        }

        @Override
        public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
            return Optional.empty();
        }
    }
}
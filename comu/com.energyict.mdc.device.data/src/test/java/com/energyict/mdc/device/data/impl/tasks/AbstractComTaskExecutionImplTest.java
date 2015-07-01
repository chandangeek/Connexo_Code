package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import org.junit.Before;

import java.time.Instant;
import java.util.*;

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

    protected static final String DEVICE_PROTOCOL_DIALECT_NAME = "Limbueregs";
    protected static final String OTHER_DEVICE_PROTOCOL_DIALECT_NAME = "WestVloams";

    protected String COM_TASK_NAME = "TheNameOfMyComTask";
    protected int maxNrOfTries = 27;
    protected int comTaskEnablementPriority = 213;
    protected ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties;
    private ConnectionTask.ConnectionTaskLifecycleStatus status = ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE;

    @Before
    public void getFirstProtocolDialectConfigurationPropertiesFromDeviceConfiguration () {
        deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new ComTaskExecutionDialect());
        deviceConfiguration.save();
        this.protocolDialectConfigurationProperties = this.deviceConfiguration.getProtocolDialectConfigurationPropertiesList().get(0);
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
        ProtocolDialectConfigurationProperties configDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new ComTaskExecutionDialect());
        deviceConfiguration.save();
        return enableComTask(useDefault, configDialect, COM_TASK_NAME);
    }

    protected ComTaskEnablement enableComTask(boolean useDefault, ProtocolDialectConfigurationProperties configDialect, String comTaskName) {
        ComTask comTaskWithBasicCheck = createComTaskWithBasicCheck(comTaskName);
        ComTaskEnablementBuilder builder = this.deviceConfiguration.enableComTask(comTaskWithBasicCheck, this.securityPropertySet, configDialect);
        builder.useDefaultConnectionTask(useDefault);
        builder.setPriority(this.comTaskEnablementPriority);
        return builder.add();
    }

    protected ScheduledComTaskExecution reloadScheduledComTaskExecution(Device device, ScheduledComTaskExecution comTaskExecution) {
        Device reloadedDevice = getReloadedDevice(device);
        for (ComTaskExecution taskExecution : reloadedDevice.getComTaskExecutions()) {
            if (comTaskExecution.getId() == taskExecution.getId()) {
                return (ScheduledComTaskExecution) taskExecution;
            }
        }
        fail("ScheduledComTaskExecution with id " + comTaskExecution.getId() + " not found after reloading device " + device.getName());
        return null;
    }

    protected ManuallyScheduledComTaskExecution reloadManuallyScheduledComTaskExecution(Device device, ManuallyScheduledComTaskExecution comTaskExecution) {
        Device reloadedDevice = getReloadedDevice(device);
        for (ComTaskExecution taskExecution : reloadedDevice.getComTaskExecutions()) {
            if (comTaskExecution.getId() == taskExecution.getId()) {
                return (ManuallyScheduledComTaskExecution) taskExecution;
            }
        }
        fail("ManuallyScheduledComTaskExecution with id " + comTaskExecution.getId() + " not found after reloading device " + device.getName());
        return null;
    }

    protected static OutboundComPortPool createOutboundIpComPortPool(String name) {
        OutboundComPortPool ipComPortPool = inMemoryPersistence.getEngineConfigurationService().newOutboundComPortPool(name, ComPortType.TCP, new TimeDuration(1, TimeDuration.TimeUnit.MINUTES));
        ipComPortPool.setActive(true);
        ipComPortPool.save();
        return ipComPortPool;
    }

    protected ScheduledConnectionTaskImpl createAsapWithNoPropertiesWithoutViolations(String name, Device device, PartialScheduledConnectionTask partialConnectionTask, OutboundComPortPool outboundTcpipComPortPool) {
        partialConnectionTask.setName(name);
        partialConnectionTask.save();

        ScheduledConnectionTaskImpl scheduledConnectionTask = (ScheduledConnectionTaskImpl) device.getScheduledConnectionTaskBuilder(partialConnectionTask)
                .setComPortPool(outboundTcpipComPortPool)
                .setConnectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE)
                .add();
        device.save();
        return scheduledConnectionTask;
    }

    protected PartialScheduledConnectionTask createPartialScheduledConnectionTask() {
        return this.createPartialScheduledConnectionTask(TimeDuration.minutes(5));
    }

    protected PartialScheduledConnectionTask createPartialScheduledConnectionTask(TimeDuration frequency) {
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
                        ConnectionStrategy.AS_SOON_AS_POSSIBLE).
                comWindow(new ComWindow(0, 7200)).
                build();
    }

    protected ScheduledConnectionTaskImpl createASAPConnectionStandardTask(Device device) {
        return this.createASAPConnectionStandardTask(device, TimeDuration.minutes(5));
    }

    protected ScheduledConnectionTaskImpl createASAPConnectionStandardTask(Device device, TimeDuration frequency) {
        PartialScheduledConnectionTask partialScheduledConnectionTask = createPartialScheduledConnectionTask(frequency);
        OutboundComPortPool outboundPool = createOutboundIpComPortPool("MyOutboundPool");
        ScheduledConnectionTaskImpl myConnectionTask = createAsapWithNoPropertiesWithoutViolations("MyConnectionTask", device, partialScheduledConnectionTask, outboundPool);
        myConnectionTask.save();
        return myConnectionTask;
    }

    protected ScheduledConnectionTaskImpl createMinimizeOneDayConnectionStandardTask(Device device) {
        PartialScheduledConnectionTask partialOutboundConnectionTask = createPartialScheduledConnectionTask();
        OutboundComPortPool outboundPool = createOutboundIpComPortPool("MyOutboundPool");
        partialOutboundConnectionTask.setName("Minimize");
        partialOutboundConnectionTask.save();
        ScheduledConnectionTaskImpl scheduledConnectionTask = (ScheduledConnectionTaskImpl) device.getScheduledConnectionTaskBuilder(partialOutboundConnectionTask)
                .setComPortPool(outboundPool)
                .setConnectionStrategy(ConnectionStrategy.MINIMIZE_CONNECTIONS)
                .setNextExecutionSpecsFrom(new TemporalExpression(TimeDuration.days(1)))
                .add();
        device.save();

        return scheduledConnectionTask;
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
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule(name, temporalExpression, Instant.now()).build();
        comSchedule.addComTask(comTask);
        comSchedule.save();
        return comSchedule;
    }

    protected OutboundComPort createOutboundComPort() {
        OnlineComServer onlineComServer = inMemoryPersistence.getEngineConfigurationService().newOnlineComServerInstance();
        onlineComServer.setName("ComServer");
        onlineComServer.setStoreTaskQueueSize(1);
        onlineComServer.setStoreTaskThreadPriority(1);
        onlineComServer.setChangesInterPollDelay(TimeDuration.minutes(5));
        onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.setSchedulingInterPollDelay(TimeDuration.minutes(1));
        onlineComServer.setServerLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.setNumberOfStoreTaskThreads(2);
        OutboundComPort.OutboundComPortBuilder outboundComPortBuilder = onlineComServer.newOutboundComPort("ComPort", 1);
        outboundComPortBuilder.comPortType(ComPortType.TCP);
        OutboundComPort outboundComPort = outboundComPortBuilder.add();
        onlineComServer.save();
        return outboundComPort;
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

    protected class ComTaskExecutionDialect implements DeviceProtocolDialect {

        @Override
        public String getDeviceProtocolDialectName() {
            return DEVICE_PROTOCOL_DIALECT_NAME;
        }

        @Override
        public String getDisplayName() {
            return "It's a Dell Display";
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }

        @Override
        public PropertySpec getPropertySpec(String name) {
            return null;
        }
    }

    protected class OtherComTaskExecutionDialect extends ComTaskExecutionDialect {

        @Override
        public String getDeviceProtocolDialectName() {
            return OTHER_DEVICE_PROTOCOL_DIALECT_NAME;
        }
    }

}
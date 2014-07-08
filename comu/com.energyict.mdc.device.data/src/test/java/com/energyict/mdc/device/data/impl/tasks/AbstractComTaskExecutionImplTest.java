package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceDataServiceImpl;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.tasks.AdHocComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.*;

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
        this.protocolDialectConfigurationProperties = this.deviceConfiguration.getCommunicationConfiguration().getProtocolDialectConfigurationPropertiesList().get(0);
    }

    protected ComTask createComTaskWithBasicCheck() {
        return createComTaskWithBasicCheck(COM_TASK_NAME);
    }

    protected ComTask createComTaskWithBasicCheck(String comTaskName) {
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask(comTaskName);
        comTask.setStoreData(true);
        comTask.setMaxNrOfTries(maxNrOfTries);
        comTask.createBasicCheckTask().add();
        comTask.save();
        return inMemoryPersistence.getTaskService().findComTask(comTask.getId()); // to make sure all elements in the composition are properly loaded
    }

    protected ComTaskEnablement createMockedComTaskEnablement(boolean useDefault) {
        ProtocolDialectConfigurationProperties configDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new ComTaskExecutionDialect());
        deviceConfiguration.save();
        return createMockedComTaskEnablement(useDefault, configDialect, COM_TASK_NAME);
    }

    protected ComTaskEnablement createMockedComTaskEnablement(boolean useDefault, ProtocolDialectConfigurationProperties configDialect, String comTaskName) {
        Optional<ProtocolDialectConfigurationProperties> optionalConfigDialect = Optional.fromNullable(configDialect);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        ComTask comTaskWithBasicCheck = createComTaskWithBasicCheck(comTaskName);
        when(comTaskEnablement.getComTask()).thenReturn(comTaskWithBasicCheck);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(optionalConfigDialect);
        when(comTaskEnablement.usesDefaultConnectionTask()).thenReturn(useDefault);
        when(comTaskEnablement.getPriority()).thenReturn(comTaskEnablementPriority);
        return comTaskEnablement;
    }

    protected AdHocComTaskExecution reloadAdHocComTaskExecution(Device device, AdHocComTaskExecution comTaskExecution) {
        Device reloadedDevice = getReloadedDevice(device);
        for (ComTaskExecution taskExecution : reloadedDevice.getComTaskExecutions()) {
            if (comTaskExecution.getId() == taskExecution.getId()) {
                return (AdHocComTaskExecution) taskExecution;
            }
        }
        fail("AdHocComTaskExecution with id " + comTaskExecution.getId() + " not found after reloading device " + device.getName());
        return null;
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
        OutboundComPortPool ipComPortPool = inMemoryPersistence.getEngineModelService().newOutboundComPortPool();
        ipComPortPool.setActive(true);
        ipComPortPool.setComPortType(ComPortType.TCP);
        ipComPortPool.setName(name);
        ipComPortPool.setTaskExecutionTimeout(new TimeDuration(1, TimeDuration.MINUTES));
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
        return deviceConfiguration.getCommunicationConfiguration().
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
        DeviceDataServiceImpl deviceDataService = inMemoryPersistence.getDeviceDataService();
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

    protected ComSchedule createComSchedule(ComTask comTask, TemporalExpression temporalExpression) {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("MyComSchedule", temporalExpression, new UtcInstant(new Date())).build();
        comSchedule.addComTask(comTask);
        return comSchedule;
    }

    protected OutboundComPort createOutboundComPort() {
        OnlineComServer onlineComServer = inMemoryPersistence.getEngineModelService().newOnlineComServerInstance();
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

    protected Date createFixedTimeStamp(int years, int months, int days, int hours, int minutes, int seconds, int millis) {
        return createFixedTimeStamp(years, months, days, hours, minutes, seconds, millis, null);
    }


    protected Date createFixedTimeStamp(int years, int months, int days, int hours, int minutes, int seconds, int millis, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone == null ? utcTimeZone : timeZone);
        calendar.set(years, months, days, hours, minutes, seconds);
        calendar.set(Calendar.MILLISECOND, millis);
        return calendar.getTime();
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
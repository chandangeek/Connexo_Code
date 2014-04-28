package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.NextExecutionSpecs;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.config.TemporalExpression;
import com.energyict.mdc.device.data.ComTaskEnablement;
import com.energyict.mdc.device.data.ComTaskExecutionDependant;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComTaskExecutionWhichIsNotFromThisDevice;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsAlreadyObsoleteException;
import com.energyict.mdc.device.data.exceptions.ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.DeviceDataServiceImpl;
import com.energyict.mdc.device.data.impl.InMemoryIntegrationPersistence;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.tasks.ComTask;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.fest.assertions.core.Condition;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the ComTaskExecutionImpl component
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/04/14
 * Time: 14:40
 */
public class ComTaskExecutionImplTest extends PersistenceIntegrationTest {

    private static final String DEVICE_PROTOCOL_DIALECT_NAME = "Limbueregs";
    private static final String OTHER_DEVICE_PROTOCOL_DIALECT_NAME = "WestVloams";

    private String COM_TASK_NAME = "TheNameOfMyComTask";
    private int maxNrOfTries = 27;
    private int comTaskEnablementPriority = 213;

    private ComTask createComTaskWithBasicCheck() {
        ComTask comTask = inMemoryPersistence.getTaskService().createComTask();
        comTask.setName(COM_TASK_NAME);
        comTask.setStoreData(true);
        comTask.setMaxNrOfTries(maxNrOfTries);
        comTask.createBasicCheckTask().add();
        comTask.save();
        return inMemoryPersistence.getTaskService().findComTask(comTask.getId()); // to make sure all elements in the composition are properly loaded
    }

    private class ComTaskExecutionDialect implements DeviceProtocolDialect {

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

    private class OtherComTaskExecutionDialect extends ComTaskExecutionDialect {

        @Override
        public String getDeviceProtocolDialectName() {
            return OTHER_DEVICE_PROTOCOL_DIALECT_NAME;
        }
    }

    private ComTaskEnablement createMockedComTaskEnablement(boolean useDefault) {
        ProtocolDialectConfigurationProperties configDialect = deviceConfiguration.findOrCreateProtocolDialectConfigurationProperties(new ComTaskExecutionDialect());
        deviceConfiguration.save();
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        ComTask comTaskWithBasicCheck = createComTaskWithBasicCheck();
        when(comTaskEnablement.getComTask()).thenReturn(comTaskWithBasicCheck);
        when(comTaskEnablement.getProtocolDialectConfigurationProperties()).thenReturn(configDialect);
        when(comTaskEnablement.useDefaultConnectionTask()).thenReturn(useDefault);
        when(comTaskEnablement.getPriority()).thenReturn(comTaskEnablementPriority);
        return comTaskEnablement;
    }

    private ComTaskExecution getReloadedComTaskExecution(Device device) {
        Device reloadedDevice = getReloadedDevice(device);
        return reloadedDevice.getComTaskExecutions().get(0);
    }


    private static OutboundComPortPool createOutboundIpComPortPool(String name) {
        OutboundComPortPool ipComPortPool = inMemoryPersistence.getEngineModelService().newOutboundComPortPool();
        ipComPortPool.setActive(true);
        ipComPortPool.setComPortType(ComPortType.TCP);
        ipComPortPool.setName(name);
        ipComPortPool.setTaskExecutionTimeout(new TimeDuration(1, TimeDuration.MINUTES));
        ipComPortPool.save();
        return ipComPortPool;
    }

    private ScheduledConnectionTaskImpl createAsapWithNoPropertiesWithoutViolations(String name, Device device, PartialOutboundConnectionTask partialConnectionTask, OutboundComPortPool outboundTcpipComPortPool) {
        DeviceDataServiceImpl deviceDataService = inMemoryPersistence.getDeviceDataService();
        partialConnectionTask.setName(name);
        partialConnectionTask.save();
        return ((ScheduledConnectionTaskImpl) deviceDataService.newAsapConnectionTask(device, partialConnectionTask, outboundTcpipComPortPool));
    }

    private PartialOutboundConnectionTask createPartialOutboundConnectionTask() {
        ConnectionTypePluggableClass connectionTypePluggableClass = inMemoryPersistence.getProtocolPluggableService()
                .newConnectionTypePluggableClass(NoParamsConnectionType.class.getSimpleName(), NoParamsConnectionType.class.getName());
        connectionTypePluggableClass.save();
        return deviceConfiguration.getCommunicationConfiguration().newPartialScheduledConnectionTask("Outbound (1)", connectionTypePluggableClass, TimeDuration.minutes(5), ConnectionStrategy.AS_SOON_AS_POSSIBLE).
                comWindow(new ComWindow(0, 7200)).
                build();
    }

    private ScheduledConnectionTaskImpl createASAPConnectionStandardTask(Device device) {
        PartialOutboundConnectionTask partialOutboundConnectionTask = createPartialOutboundConnectionTask();
        OutboundComPortPool outboundPool = createOutboundIpComPortPool("MyOutboundPool");
        ScheduledConnectionTaskImpl myConnectionTask = createAsapWithNoPropertiesWithoutViolations("MyConnectionTask", device, partialOutboundConnectionTask, outboundPool);
        myConnectionTask.save();
        return myConnectionTask;
    }

    private ScheduledConnectionTaskImpl createMinimizeOneDayConnectionStandardTask(Device device) {
        PartialOutboundConnectionTask partialOutboundConnectionTask = createPartialOutboundConnectionTask();
        OutboundComPortPool outboundPool = createOutboundIpComPortPool("MyOutboundPool");
        DeviceDataServiceImpl deviceDataService = inMemoryPersistence.getDeviceDataService();
        partialOutboundConnectionTask.setName("Minimize");
        partialOutboundConnectionTask.save();
        ScheduledConnectionTaskImpl scheduledConnectionTask = (ScheduledConnectionTaskImpl) deviceDataService.newMinimizeConnectionTask(device, partialOutboundConnectionTask, outboundPool, new TemporalExpression(TimeDuration.days(1)));
        scheduledConnectionTask.save();
        return scheduledConnectionTask;
    }

    @Test
    @Transactional
    public void createWithoutViolationsTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);

        assertThat(reloadedComTaskExecution).isNotNull();
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs()).isNull();
        assertThat(reloadedComTaskExecution.getDevice().getId()).isEqualTo(device.getId());
        assertThat(reloadedComTaskExecution.getComTask().getId()).isEqualTo(comTaskEnablement.getComTask().getId());
        assertThat(reloadedComTaskExecution.getConnectionTask()).isNull();
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isTrue();
        assertThat(reloadedComTaskExecution.getExecutingComPort()).isNull();
        assertThat(reloadedComTaskExecution.getCurrentTryCount()).isEqualTo(1);
        assertThat(reloadedComTaskExecution.getExecutionStartedTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getLastExecutionStartTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getMaxNumberOfTries()).isEqualTo(maxNrOfTries);
        assertThat(reloadedComTaskExecution.getObsoleteDate()).isNull();
        assertThat(reloadedComTaskExecution.getPlannedPriority()).isEqualTo(comTaskEnablementPriority);
        assertThat(reloadedComTaskExecution.getProtocolDialectConfigurationProperties().getId()).isEqualTo(deviceConfiguration.getCommunicationConfiguration().getProtocolDialectConfigurationPropertiesList().get(0).getId());
        assertThat(reloadedComTaskExecution.isAdhoc()).isTrue();
        assertThat(reloadedComTaskExecution.isScheduled()).isFalse();
        assertThat(reloadedComTaskExecution.isExecuting()).isFalse();
        assertThat(reloadedComTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isFalse();
    }

    @Test
    @Transactional
    public void createWithMyNextExecutionSpecTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithMyNextExecSpec");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.createNextExecutionSpec(myTemporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().getId()).isEqualTo(comTaskExecution.getNextExecutionSpecs().getId());
        assertThat(reloadedComTaskExecution.isAdhoc()).isFalse();
        assertThat(reloadedComTaskExecution.isScheduled()).isTrue();
    }

    @Test
    @Transactional
    public void updateWithMyNextExecutionSpecsTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithMyNextExecSpec");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.createOrUpdateNextExecutionSpec(myTemporalExpression);
        ComTaskExecution updatedComTaskExecution = comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().getId()).isEqualTo(updatedComTaskExecution.getNextExecutionSpecs().getId());
        assertThat(reloadedComTaskExecution.isAdhoc()).isFalse();
        assertThat(reloadedComTaskExecution.isScheduled()).isTrue();
    }

    @Test
    @Transactional
    public void createWithMasterScheduleNextExecutionSpecTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        NextExecutionSpecs masterScheduleNextExecutionSpec = inMemoryPersistence.getDeviceConfigurationService().newNextExecutionSpecs(temporalExpression);
        masterScheduleNextExecutionSpec.save();
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithMasterNextExecSpec");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setMasterNextExecutionSpec(masterScheduleNextExecutionSpec);
        comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().getId()).isEqualTo(masterScheduleNextExecutionSpec.getId());
        assertThat(reloadedComTaskExecution.isAdhoc()).isFalse();
        assertThat(reloadedComTaskExecution.isScheduled()).isTrue();
    }

    @Test
    @Transactional
    public void updateWithMasterScheduleNextExecutionSpecTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        NextExecutionSpecs masterScheduleNextExecutionSpec = inMemoryPersistence.getDeviceConfigurationService().newNextExecutionSpecs(temporalExpression);
        masterScheduleNextExecutionSpec.save();
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithMasterNextExecSpec");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setMasterNextExecutionSpec(masterScheduleNextExecutionSpec);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs().getId()).isEqualTo(masterScheduleNextExecutionSpec.getId());
        assertThat(reloadedComTaskExecution.isAdhoc()).isFalse();
        assertThat(reloadedComTaskExecution.isScheduled()).isTrue();
    }

    @Test
    @Transactional
    public void comTaskExecutionDeletedWhenDeviceDeletedTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeletionTest");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        long comTaskExecId = reloadedComTaskExecution.getId();

        device.delete();

        assertThat(inMemoryPersistence.getDeviceDataService().findComTaskExecution(comTaskExecId)).isNull();
    }

    @Test
    @Transactional
    public void myNextExecSpecIsDeletedAfterComTaskExecutionDeletedTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "NextExecSpecDelete");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.createNextExecutionSpec(myTemporalExpression);
        comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        long nextExecutionSpecId = reloadedComTaskExecution.getNextExecutionSpecs().getId();
        ((ComTaskExecutionImpl) reloadedComTaskExecution).delete();

        assertThat(inMemoryPersistence.getDeviceConfigurationService().findNextExecutionSpecs(nextExecutionSpecId)).isNull();
    }

    @Test
    @Transactional
    public void masterScheduleNextExecSpecNotDeletedWhenComTaskExecutionDeletedTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(3));
        NextExecutionSpecs masterScheduleNextExecutionSpec = inMemoryPersistence.getDeviceConfigurationService().newNextExecutionSpecs(temporalExpression);
        masterScheduleNextExecutionSpec.save();
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "MasterNextExecSpecNotDeleted");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setMasterNextExecutionSpec(masterScheduleNextExecutionSpec);
        comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        long nextExecutionSpecId = reloadedComTaskExecution.getNextExecutionSpecs().getId();
        device.removeComTaskExecution(reloadedComTaskExecution);
        device.save();

        assertThat(inMemoryPersistence.getDeviceConfigurationService().findNextExecutionSpecs(nextExecutionSpecId)).isNotNull();
    }

    @Test
    @Transactional
    public void removeComTaskTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        device.removeComTaskExecution(reloadedComTaskExecution);
        device.save();

        Device reloadedDevice = getReloadedDevice(device);

        assertThat(reloadedDevice.getComTaskExecutions()).isEmpty();
    }

    @Test(expected = CannotDeleteComTaskExecutionWhichIsNotFromThisDevice.class)
    @Transactional
    public void removeComTaskWhichIsNotFromDeviceXTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device deviceWithoutComTaskExecutions = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeviceWithoutComTaskExecutions");
        Device deviceWithComTaskExecutions = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeviceWithComTaskExecutions");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = deviceWithComTaskExecutions.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        deviceWithComTaskExecutions.save();

        deviceWithoutComTaskExecutions.removeComTaskExecution(comTaskExecution);
    }

    @Test
    @Transactional
    public void useDefaultConnectionTaskOnBuilderTest() {
        boolean originalDefaultValue = false;
        boolean testUseDefault = !originalDefaultValue;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(originalDefaultValue);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setUseDefaultConnectionTask(testUseDefault);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isEqualTo(testUseDefault);
    }

    @Test
    @Transactional
    public void useDefaultConnectionTaskOnUpdaterTest() {
        boolean originalDefaultValue = false;
        boolean testUseDefault = !originalDefaultValue;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(originalDefaultValue);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setConnectionTask(createASAPConnectionStandardTask(device));
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setUseDefaultConnectionTaskFlag(testUseDefault);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isEqualTo(testUseDefault);
    }

    @Test
    @Transactional
    public void setConnectionTaskOnBuilderTest() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setConnectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getConnectionTask().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void setConnectionTaskOnBuilderSetsUseDefaultToFalseTest() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setConnectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isFalse();
    }

    @Test
    @Transactional
    public void setUseDefaultConnectionTaskClearsConnectionTaskTest() {
        boolean useDefaultTrue = true;
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setConnectionTask(connectionTask);
        comTaskExecutionBuilder.setUseDefaultConnectionTask(useDefaultTrue);    // this call should clear the connectionTask
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isTrue();
        assertThat(reloadedComTaskExecution.getConnectionTask()).isNull();
    }

    @Test
    @Transactional
    public void setConnectionTaskOnUpdaterTest() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setConnectionTask(connectionTask);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getConnectionTask().getId()).isEqualTo(connectionTask.getId());
    }

    @Test
    @Transactional
    public void setConnectionTaskOnUpdaterSetsUseDefaultToFalseTest() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setConnectionTask(connectionTask);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isFalse();
    }

    @Test
    @Transactional
    public void setUseDefaultOnUpdaterClearsConnectionTaskTest() {
        boolean useDefaultTrue = true;
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setUseDefaultConnectionTask(false);
        comTaskExecutionBuilder.setConnectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setUseDefaultConnectionTaskFlag(useDefaultTrue);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isTrue();
        assertThat(reloadedComTaskExecution.getConnectionTask()).isNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.CONNECTION_TASK_REQUIRED_WHEN_NOT_USING_DEFAULT + "}")
    public void setNotToUseDefaultAndNoConnectionTaskSetTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setUseDefaultConnectionTask(false);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
    }

    @Test
    @Transactional
    public void setPriorityOnBuilderTest() {
        int myPriority = 514;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "PriorityTester");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setPriority(myPriority);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getPriority()).isEqualTo(myPriority);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.PRIORITY_NOT_IN_RANGE + "}")
    public void negativePriorityOnBuilderTest() {
        int myPriority = -123;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setPriority(myPriority);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.PRIORITY_NOT_IN_RANGE + "}")
    public void setPriorityOutOfRangeTest() {
        int myPriority = TaskPriorityConstants.LOWEST_PRIORITY + 1;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setPriority(myPriority);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
    }

    @Test
    @Transactional
    public void setPriorityOnUpdaterTest() {
        int myPriority = 231;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "PriorityUpdater");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setPriority(myPriority);
        comTaskExecutionUpdater.update();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getPriority()).isEqualTo(myPriority);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.PRIORITY_NOT_IN_RANGE + "}")
    public void negativePriorityOnUpdaterTest() {
        int myPriority = -7859;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setPriority(myPriority);
        comTaskExecutionUpdater.update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.PRIORITY_NOT_IN_RANGE + "}")
    public void updatePriorityOutOfRangeTest() {
        int myPriority = TaskPriorityConstants.LOWEST_PRIORITY + 1;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setPriority(myPriority);
        comTaskExecutionUpdater.update();
    }

    @Test
    @Transactional
    public void ignoreNextForInboundOnBuilderTest() {
        boolean ignoreOnInbound = true;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setIgnoreNextExecutionSpecForInbound(ignoreOnInbound);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isEqualTo(ignoreOnInbound);
    }

    @Test
    @Transactional
    public void ignoreNextForInboundOnUpdaterTest() {
        boolean ignoreOnInbound = true;
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithValidationError");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setIgnoreNextExecutionSpecForInbound(ignoreOnInbound);
        comTaskExecutionUpdater.update();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.isIgnoreNextExecutionSpecsForInbound()).isEqualTo(ignoreOnInbound);
    }

    @Test
    @Transactional
    public void setProtocolDialectOnBuilderTest() {
        ProtocolDialectConfigurationProperties otherDialect = deviceConfiguration.createProtocolDialectConfigurationProperties("MyDialect", new OtherComTaskExecutionDialect());
        deviceConfiguration.save();
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Dialect");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setProtocolDialectConfigurationProperties(otherDialect);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getProtocolDialectConfigurationProperties().getId()).isEqualTo(otherDialect.getId());
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ARE_REQUIRED + "}")
    @Transactional
    public void setNullProtocolDialectTest() {
        deviceConfiguration.save();
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Dialect");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setProtocolDialectConfigurationProperties(null);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
    }

    @Test
    @Transactional
    public void setProtocolDialectOnUpdaterTest() {
        ProtocolDialectConfigurationProperties otherDialect = deviceConfiguration.createProtocolDialectConfigurationProperties("MyDialect", new OtherComTaskExecutionDialect());
        deviceConfiguration.save();
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Dialect");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setProtocolDialectConfigurationProperties(otherDialect);
        comTaskExecutionUpdater.update();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getProtocolDialectConfigurationProperties().getId()).isEqualTo(otherDialect.getId());
    }

    @Test
    @Transactional
    public void removeNextExecutionSpecInUpdaterTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(3));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithMyNextExecSpec");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.createNextExecutionSpec(myTemporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.removeNextExecutionSpec();
        comTaskExecutionUpdater.update();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getNextExecutionSpecs()).isNull();
    }

    @Test
    @Transactional
    public void makeSuccessfulObsoleteTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithMyNextExecSpec");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        comTaskExecution.makeObsolete();
        Device reloadedDevice = getReloadedDevice(device);
        assertThat(reloadedDevice.getComTaskExecutions()).isEmpty();
    }

    @Test(expected = ComTaskExecutionIsAlreadyObsoleteException.class)
    @Transactional
    public void makeObsoleteTwiceTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "ObsoleteTest");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        comTaskExecution.makeObsolete();
        comTaskExecution.makeObsolete();
    }

    private OutboundComPort createOutboundComPort() {
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

    @Test(expected = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenComPortIsFilledInTest() {
        OutboundComPort outboundComPort = createOutboundComPort();

        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "ObsoleteTest");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        InMemoryIntegrationPersistence.update("update MDCCOMTASKEXEC set comport = " + outboundComPort.getId() + " where id = " + comTaskExecution.getId());

        comTaskExecution.makeObsolete();
    }

    @Test(expected = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenConnectionTaskHasComServerFilledInTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "ObsoleteTest");
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setConnectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        InMemoryIntegrationPersistence.update("update mdcconnectiontask set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());

        comTaskExecution.makeObsolete();
    }

    @Test(expected = ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException.class)
    @Transactional
    public void makeObsoleteWhenDefaultConnectionTaskHasComServerFilledInTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "ObsoleteTest");
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setUseDefaultConnectionTask(true);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        inMemoryPersistence.getDeviceDataService().setDefaultConnectionTask(connectionTask);

        InMemoryIntegrationPersistence.update("update mdcconnectiontask set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());

        comTaskExecution.makeObsolete();
    }

    @Test
    @Transactional
    public void isScheduledTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.createNextExecutionSpec(temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.isScheduled()).isTrue();
        assertThat(reloadedComTaskExecution.isAdhoc()).isFalse();
    }

    @Test
    @Transactional
    public void isScheduledAfterUpdateTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        assertThat(comTaskExecution.isScheduled()).isFalse();
        assertThat(comTaskExecution.isAdhoc()).isTrue();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.createOrUpdateNextExecutionSpec(temporalExpression);
        comTaskExecutionUpdater.update();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.isScheduled()).isTrue();
        assertThat(reloadedComTaskExecution.isAdhoc()).isFalse();
    }

    @Test
    @Transactional
    public void isNotScheduledAfterUpdateTest() {
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.createNextExecutionSpec(temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        assertThat(comTaskExecution.isScheduled()).isTrue();
        assertThat(comTaskExecution.isAdhoc()).isFalse();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.removeNextExecutionSpec();
        comTaskExecutionUpdater.update();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.isScheduled()).isFalse();
        assertThat(reloadedComTaskExecution.isAdhoc()).isTrue();
    }

    @Test
    @Transactional
    public void isExecutingByComPortTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        TemporalExpression temporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.createNextExecutionSpec(temporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        // Making the comPort filled in
        ((ServerComTaskExecution) comTaskExecution).executionStarted(outboundComPort);

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.isExecuting()).isTrue();
    }

    @Test
    @Transactional
    public void isExecutingByComServerOnConnectionTaskTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        comTaskExecutionBuilder.setConnectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        InMemoryIntegrationPersistence.update("update mdccomtaskexec set " + ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName() + " = 1 where id = " + comTaskExecution.getId());
        InMemoryIntegrationPersistence.update("update mdcconnectiontask set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());
        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.isExecuting()).isTrue();
    }

    @Test
    @Transactional
    public void isNotExecutionBecauseComServeIsFilledInOnConnectionTaskButNextIsNotPassedYetTest() {
        OutboundComPort outboundComPort = createOutboundComPort();
        ComServer comServer = outboundComPort.getComServer();
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        comTaskExecutionBuilder.setConnectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        long future = inMemoryPersistence.getClock().now().getTime() + 1000000000000L;  // let's just hope it won't take that long until this test is finished
        InMemoryIntegrationPersistence.update("update mdccomtaskexec set " + ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName() + " = " + future + " where id = " + comTaskExecution.getId());
        InMemoryIntegrationPersistence.update("update mdcconnectiontask set comserver = " + comServer.getId() + " where id = " + connectionTask.getId());
        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.isExecuting()).isFalse();
    }

    @Test
    @Transactional
    public void notifyConnectionTaskRemovedTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "WithoutViolations");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ScheduledConnectionTaskImpl connectionTask = createASAPConnectionStandardTask(device);
        comTaskExecutionBuilder.setConnectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ((ComTaskExecutionImpl) comTaskExecution).connectionTaskRemoved();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getConnectionTask()).isNull();
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Constants.UNIQUE_COMTASKS_PER_DEVICE + "}", strict = false)
    public void duplicateComTaskOnDeviceTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Duplicate");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder1 = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution1 = comTaskExecutionBuilder1.add();
        device.save();
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder2 = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution2 = comTaskExecutionBuilder2.add();
        device.save();
    }

    @Test
    @Transactional
    public void duplicateComTaskOnDeviceAfterRemoveTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Duplicate");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder1 = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution1 = comTaskExecutionBuilder1.add();
        device.save();
        device.removeComTaskExecution(comTaskExecution1);

        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder2 = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution2 = comTaskExecutionBuilder2.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(device.getComTaskExecutions()).hasSize(1);
        assertThat(reloadedComTaskExecution.getId()).isEqualTo(comTaskExecution2.getId());
    }

    @Test
    @Transactional
    public void isObsoleteTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "IsObsolete");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder1 = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution1 = comTaskExecutionBuilder1.add();
        device.save();
        device.removeComTaskExecution(comTaskExecution1);

        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder2 = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution2 = comTaskExecutionBuilder2.add();
        device.save();

        List<ComTaskExecution> allComTaskExecutions = inMemoryPersistence.getDeviceDataService().findAllComTaskExecutionsIncludingObsoleteForDevice(device);
        assertThat(allComTaskExecutions).hasSize(2);
        assertThat(allComTaskExecutions).areExactly(1, new Condition<ComTaskExecution>() {
            @Override
            public boolean matches(ComTaskExecution value) {
                return value.isObsolete();
            }
        });
    }

    private Date createFixedTimeStamp(int years, int months, int days, int hours, int minutes, int seconds, int millis) {
        return createFixedTimeStamp(years, months, days, hours, minutes, seconds, millis, null);
    }


    private Date createFixedTimeStamp(int years, int months, int days, int hours, int minutes, int seconds, int millis, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone == null ? utcTimeZone : timeZone);
        calendar.set(years, months, days, hours, minutes, seconds);
        calendar.set(Calendar.MILLISECOND, millis);
        return calendar.getTime();
    }

    @Test
    @Transactional
    public void updateNextExecutionTimeStampTest() {
        Date creationTime = freezeClock(2014, 4, 4, 10, 12, 32, 123);
        Date updateTime = freezeClock(2014, 4, 4, 13, 44, 10, 3);
        Date fixedTimeStamp = createFixedTimeStamp(2014, 4, 4, 14, 0, 0, 0);
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "UpdateNextExecSpec");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.createNextExecutionSpec(myTemporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        comTaskExecution.updateNextExecutionTimestamp();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
    }

    @Test
    @Transactional
    public void updateNextExecutionTimeStampWhenAdHocTest() {
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "UpdateNextExecSpec");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        comTaskExecution.updateNextExecutionTimestamp();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isNull();
    }

    @Test
    @Transactional
    public void checkCorrectTimeStampsForScheduledComTaskExecutionWithAsapConnectionTaskTest() {
        Date currentTime = freezeClock(2014, 4, 4, 10, 12, 32, 123);
        Date fixedTimeStamp = createFixedTimeStamp(2014, 4, 4, 11, 0, 0, 0);
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "TimeChecks");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.createNextExecutionSpec(myTemporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
        assertThat(reloadedComTaskExecution.getPlannedNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
        assertThat(reloadedComTaskExecution.getLastExecutionStartTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getLastSuccessfulCompletionTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getExecutionStartedTimestamp()).isNull();
    }

    @Test
    @Transactional
    public void checkCorrectTimeStampsForScheduledComTaskExecutionWithMinimizeConnectionTaskTest() {
        Date nextFromComTask = createFixedTimeStamp(2014, 4, 4, 11, 0, 0, 0);
        Date nextFromConnectionTask = createFixedTimeStamp(2014, 4, 5, 0, 0, 0, 0, TimeZone.getTimeZone("UTC"));
        Date currentTime = freezeClock(2014, 4, 4, 10, 12, 32, 123);
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "TimeChecks");
        ScheduledConnectionTaskImpl connectionTask = createMinimizeOneDayConnectionStandardTask(device);
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setConnectionTask(connectionTask);
        comTaskExecutionBuilder.createNextExecutionSpec(myTemporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(nextFromConnectionTask);
        assertThat(reloadedComTaskExecution.getPlannedNextExecutionTimestamp()).isEqualTo(nextFromComTask);
        assertThat(reloadedComTaskExecution.getLastExecutionStartTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getLastSuccessfulCompletionTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getExecutionStartedTimestamp()).isNull();
    }

    @Test
    @Transactional
    public void putOnHoldTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "PutOnHold");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.createNextExecutionSpec(myTemporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        reloadedComTaskExecution.putOnHold();

        ComTaskExecution onHoldComTaskExecution = getReloadedComTaskExecution(device);

        assertThat(onHoldComTaskExecution.isOnHold()).isTrue();
        assertThat(onHoldComTaskExecution.getPlannedNextExecutionTimestamp()).isNotNull();
    }

    @Test
    @Transactional
    public void nextExecutionSpecWithOffsetTest() {
        Date currentTime = freezeClock(2014, 4, 4, 10, 12, 32, 123);
        Date fixedTimeStamp = createFixedTimeStamp(2014, 4, 5, 3, 30, 0, 0, TimeZone.getTimeZone("UTC"));
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.days(1), TimeDuration.minutes(210));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "TimeChecks");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.createNextExecutionSpec(myTemporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
        assertThat(reloadedComTaskExecution.getPlannedNextExecutionTimestamp()).isEqualTo(fixedTimeStamp);
        assertThat(reloadedComTaskExecution.getLastExecutionStartTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getLastSuccessfulCompletionTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getExecutionStartedTimestamp()).isNull();
    }

    @Test
    @Transactional
    public void removeNextExecutionSpecClearsTimestampsTest() {
        Date currentTime = freezeClock(2014, 4, 4, 10, 12, 32, 123);
        Date fixedTimeStamp = createFixedTimeStamp(2014, 4, 4, 11, 0, 0, 0);
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "Removecheck");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.createNextExecutionSpec(myTemporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.removeNextExecutionSpec();
        comTaskExecutionUpdater.update();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.getNextExecutionTimestamp()).isNull();
        assertThat(reloadedComTaskExecution.getPlannedNextExecutionTimestamp()).isNull();
    }

    @Test
    @Transactional
    public void deleteComTaskExecutionDeletesComTaskExecutionSessionsTest() {
        ComTaskExecutionDependant comTaskExecutionDependant = mock(ComTaskExecutionDependant.class);
        when(Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(ComTaskExecutionDependant.class)).thenReturn(Arrays.asList(comTaskExecutionDependant));

        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeletionTest");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        long comTaskExecId = reloadedComTaskExecution.getId();

        device.delete();

        verify(comTaskExecutionDependant).comTaskExecutionDeleted(any(ComTaskExecution.class));
    }

    @Test
    @Transactional
    public void lockComTaskExecutionTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "LockTest");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.createNextExecutionSpec(myTemporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        OutboundComPort outboundComPort = createOutboundComPort();

        ComTaskExecution lockComTaskExecution = inMemoryPersistence.getDeviceDataService().attemptLockComTaskExecution(comTaskExecution, outboundComPort);

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);

        assertThat(lockComTaskExecution).isNotNull();
        assertThat(reloadedComTaskExecution.isExecuting()).isTrue();
        assertThat(reloadedComTaskExecution.getExecutingComPort().getId()).isEqualTo(outboundComPort.getId());
    }

    @Test
    @Transactional
    public void cantLockTwiceTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "LockTest");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.createNextExecutionSpec(myTemporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        OutboundComPort outboundComPort = createOutboundComPort();

        ComTaskExecution lockComTaskExecution = inMemoryPersistence.getDeviceDataService().attemptLockComTaskExecution(comTaskExecution, outboundComPort);
        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        ComTaskExecution notLockedComTaskExecution = inMemoryPersistence.getDeviceDataService().attemptLockComTaskExecution(reloadedComTaskExecution, outboundComPort);

        assertThat(notLockedComTaskExecution).isNull();
    }

    @Test
    @Transactional
    public void unlockComTaskExecutionTest() {
        TemporalExpression myTemporalExpression = new TemporalExpression(TimeDuration.hours(1));
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "LockTest");
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.createNextExecutionSpec(myTemporalExpression);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        OutboundComPort outboundComPort = createOutboundComPort();

        ComTaskExecution lockComTaskExecution = inMemoryPersistence.getDeviceDataService().attemptLockComTaskExecution(comTaskExecution, outboundComPort);
        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        inMemoryPersistence.getDeviceDataService().unlockComTaskExecution(reloadedComTaskExecution);

        ComTaskExecution notLockedComTaskExecution = getReloadedComTaskExecution(device);

        assertThat(notLockedComTaskExecution.isExecuting()).isFalse();
        assertThat(notLockedComTaskExecution.getExecutingComPort()).isNull();
    }
}
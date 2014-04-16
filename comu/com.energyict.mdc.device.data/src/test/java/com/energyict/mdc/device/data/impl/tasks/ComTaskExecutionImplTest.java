package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.NextExecutionSpecs;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.TaskPriorityConstants;
import com.energyict.mdc.device.config.TemporalExpression;
import com.energyict.mdc.device.data.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.CannotDeleteComTaskExecutionWhichIsNotFromThisDevice;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.DeviceDataServiceImpl;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.tasks.ComTask;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
        ProtocolDialectConfigurationProperties configDialect = deviceConfiguration.createProtocolDialectConfigurationProperties("MyConfigDialect", new ComTaskExecutionDialect());
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
        return deviceConfiguration.getCommunicationConfiguration().createPartialOutboundConnectionTask().
                name("Outbound (1)").
                comWindow(new ComWindow(0, 7200)).
                rescheduleDelay(TimeDuration.minutes(5)).
                connectionStrategy(ConnectionStrategy.AS_SOON_AS_POSSIBLE).
                pluggableClass(connectionTypePluggableClass).
                build();
    }

    private ScheduledConnectionTaskImpl createConnectionStandardTask(Device device) {
        PartialOutboundConnectionTask partialOutboundConnectionTask = createPartialOutboundConnectionTask();
        OutboundComPortPool outboundPool = createOutboundIpComPortPool("MyOutboundPool");
        ScheduledConnectionTaskImpl myConnectionTask = createAsapWithNoPropertiesWithoutViolations("MyConnectionTask", device, partialOutboundConnectionTask, outboundPool);
        myConnectionTask.save();
        return myConnectionTask;
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
        device.removeComTaskExecution(reloadedComTaskExecution);
        device.save();

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
        comTaskExecutionBuilder.setConnectionTask(createConnectionStandardTask(device));
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setUseDefaultConnectionTask(testUseDefault);
        comTaskExecutionUpdater.update();
        device.save();

        ComTaskExecution reloadedComTaskExecution = getReloadedComTaskExecution(device);
        assertThat(reloadedComTaskExecution.useDefaultConnectionTask()).isEqualTo(testUseDefault);
    }

    @Test
    @Transactional
    public void setConnectionTaskOnBuilderTest() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "BuilderTest");
        ScheduledConnectionTaskImpl connectionTask = createConnectionStandardTask(device);
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
        ScheduledConnectionTaskImpl connectionTask = createConnectionStandardTask(device);
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
        ScheduledConnectionTaskImpl connectionTask = createConnectionStandardTask(device);
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
        ScheduledConnectionTaskImpl connectionTask = createConnectionStandardTask(device);
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
        ScheduledConnectionTaskImpl connectionTask = createConnectionStandardTask(device);
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
        ScheduledConnectionTaskImpl connectionTask = createConnectionStandardTask(device);
        ComTaskEnablement comTaskEnablement = createMockedComTaskEnablement(true);
        ComTaskExecution.ComTaskExecutionBuilder comTaskExecutionBuilder = device.getComTaskExecutionBuilder(comTaskEnablement);
        comTaskExecutionBuilder.setUseDefaultConnectionTask(false);
        comTaskExecutionBuilder.setConnectionTask(connectionTask);
        ComTaskExecution comTaskExecution = comTaskExecutionBuilder.add();
        device.save();

        ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = device.getComTaskExecutionUpdater(comTaskExecution);
        comTaskExecutionUpdater.setUseDefaultConnectionTask(useDefaultTrue);
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
        int myPriority = TaskPriorityConstants.LOWEST_PRIORITY+1;
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
        int myPriority = TaskPriorityConstants.LOWEST_PRIORITY+1;
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
}
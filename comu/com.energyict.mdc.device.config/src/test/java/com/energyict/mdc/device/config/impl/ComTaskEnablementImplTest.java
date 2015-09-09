package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.config.exceptions.CannotDisableComTaskThatWasNotEnabledException;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.time.TimeDuration;

import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.mockito.ArgumentCaptor;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComTaskEnablementImpl} component.
 * More specifically, this class will focus on:
 * <ul>
 * <li>Suspending</li>
 * <li>Resuming</li>
 * <li>Removing next execution specs</li>
 * <li>Adding next execution specs</li>
 * <li>Updating next execution specs</li>
 * <li>Switching back and forth from default connection task to specific connection task</li>
 * <li>Switching to another specific connection task</li>
 * <li>Changing the preferred priority</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-02-11 (13:01)
 */
public class ComTaskEnablementImplTest extends PersistenceWithRealProtocolPluggableServiceTest {

    private static final String DEVICE_TYPE_NAME = ComTaskEnablementImplTest.class.getSimpleName() + "Type";

    private TopicHandler topicHandler;
    private ConnectionTypePluggableClass noParamsConnectionTypePluggableClass;
    private DeviceType deviceType;
    private ComTask comTask1;
    private ComTask comTask2;
    private DeviceConfiguration deviceConfiguration1;
    private DeviceConfiguration deviceConfiguration2;
    private SecurityPropertySet securityPropertySet1;
    private SecurityPropertySet securityPropertySet2;
    private PartialScheduledConnectionTask partialConnectionTask1;

    private ProtocolDialectSharedData sharedData;
    ProtocolDialectConfigurationProperties properties;

    @Before
    public void setup () {
        this.registerNoParamsConnectionType();
        this.createDeviceType();
        this.createComTasks();
        this.createConfigurations();
        this.createSecurityPropertySets();
        this.createPartialConnectionTasks();
        sharedData = new ProtocolDialectSharedData();
        properties = deviceConfiguration1.findOrCreateProtocolDialectConfigurationProperties(sharedData.getProtocolDialect());
    }

    private void createDeviceType() {
        deviceType = inMemoryPersistence.getDeviceConfigurationService().newDeviceType(DEVICE_TYPE_NAME, deviceProtocolPluggableClass);
        deviceType.save();
    }

    private void registerNoParamsConnectionType() {
        this.noParamsConnectionTypePluggableClass =
                inMemoryPersistence.getProtocolPluggableService()
                        .newConnectionTypePluggableClass(
                                OutboundNoParamsConnectionTypeImpl.class.getSimpleName(),
                                OutboundNoParamsConnectionTypeImpl.class.getName());
        this.noParamsConnectionTypePluggableClass.save();
    }

    private void createComTasks() {
        this.comTask1 = this.createComTask("ComTask1");
        this.comTask2 = this.createComTask("ComTask2");
    }

    private ComTask createComTask (String name) {
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask(name);
        comTask.setMaxNrOfTries(1);
        comTask.createBasicCheckTask().add();
        comTask.save();
        return comTask;
    }

    private void createConfigurations() {
        this.deviceConfiguration1 = this.deviceType.newConfiguration("Config 1").add();
        this.deviceConfiguration1.setDirectlyAddressable(true);
        this.deviceConfiguration2 = this.deviceType.newConfiguration("Config 2").add();
        this.deviceConfiguration2.setDirectlyAddressable(true);
        this.deviceType.save();
    }

    private void createSecurityPropertySets () {
        this.securityPropertySet1 = this.createSecurityPropertySet(this.deviceConfiguration1, "SPPS-Config-1");
        this.securityPropertySet2 = this.createSecurityPropertySet(this.deviceConfiguration2, "SPPS-Config-2");
    }

    private SecurityPropertySet createSecurityPropertySet(DeviceConfiguration configuration, String name) {
        return configuration.
                    createSecurityPropertySet(name).
                        authenticationLevel(0).
                        encryptionLevel(0).
                        addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1).
                        build();
    }

    private void createPartialConnectionTasks () {
        this.partialConnectionTask1 =
                this.deviceConfiguration1.newPartialScheduledConnectionTask(
                        ComTaskEnablementImplTest.class.getSimpleName(),
                        this.noParamsConnectionTypePluggableClass,
                        TimeDuration.minutes(5),
                        ConnectionStrategy.AS_SOON_AS_POSSIBLE).
                    build();
    }

    private void registerSubscriber() {
        this.topicHandler = mock(TopicHandler.class);
        when(this.topicHandler.getTopicMatcher()).thenReturn("*");
        inMemoryPersistence.registerTopicHandler(this.topicHandler);
    }

    @After
    public void unregisterSubscriberIfAny () {
        if (this.topicHandler != null) {
            inMemoryPersistence.unregisterSubscriber(this.topicHandler);
        }
        sharedData.invalidate();
    }

    @Test(expected = IllegalStateException.class)
    @Transactional
    public void testModifyAfterBuild () {
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);

        ComTaskEnablementBuilder builder = comTaskEnablementBuilder.setIgnoreNextExecutionSpecsForInbound(true);
        builder.add();

        // Business method
        builder.setPriority(ComTaskEnablement.HIGHEST_PRIORITY);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalStateException.class)
    @Transactional
    public void testCompleteBuilderTwice () {
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);

        ComTaskEnablementBuilder builder = comTaskEnablementBuilder.setIgnoreNextExecutionSpecsForInbound(true);
        builder.add();

        // Business method
        builder.add();

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void testCreate () {
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);

        // Business method
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.setIgnoreNextExecutionSpecsForInbound(true).add();

        // Asserts
        assertThat(comTaskEnablement.isIgnoreNextExecutionSpecsForInbound()).isTrue();
        assertThat(comTaskEnablement.getComTask()).isNotNull();
        assertThat(comTaskEnablement.getComTask().getId()).isEqualTo(this.comTask1.getId());
        assertThat(comTaskEnablement.getDeviceConfiguration()).isNotNull();
        assertThat(comTaskEnablement.getDeviceConfiguration().getId()).isEqualTo(this.deviceConfiguration1.getId());
        assertThat(comTaskEnablement.getSecurityPropertySet()).isNotNull();
        assertThat(comTaskEnablement.getSecurityPropertySet().getId()).isEqualTo(this.securityPropertySet1.getId());
        assertThat(comTaskEnablement.getPriority()).isEqualTo(ComTaskEnablement.DEFAULT_PRIORITY);
    }

    @Test
    @Transactional
    public void testCreateWithNextExecutionSpecs () {
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);

        // Business method
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.setIgnoreNextExecutionSpecsForInbound(true).add();

        // Asserts
        assertThat(comTaskEnablement.isIgnoreNextExecutionSpecsForInbound()).isTrue();
        assertThat(comTaskEnablement.getComTask()).isNotNull();
        assertThat(comTaskEnablement.getComTask().getId()).isEqualTo(this.comTask1.getId());
        assertThat(comTaskEnablement.getDeviceConfiguration()).isNotNull();
        assertThat(comTaskEnablement.getDeviceConfiguration().getId()).isEqualTo(this.deviceConfiguration1.getId());
        assertThat(comTaskEnablement.getSecurityPropertySet()).isNotNull();
        assertThat(comTaskEnablement.getSecurityPropertySet().getId()).isEqualTo(this.securityPropertySet1.getId());
        assertThat(comTaskEnablement.getPriority()).isEqualTo(ComTaskEnablement.DEFAULT_PRIORITY);
    }

    @Test
    @Transactional
    public void testUpdateNextExecutionSpecs() {
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        // Business method
        comTaskEnablement.save();

        // Assert that none of the other attributes have changed
        assertThat(comTaskEnablement.getComTask().getId()).isEqualTo(this.comTask1.getId());
        assertThat(comTaskEnablement.getDeviceConfiguration().getId()).isEqualTo(this.deviceConfiguration1.getId());
        assertThat(comTaskEnablement.getSecurityPropertySet().getId()).isEqualTo(this.securityPropertySet1.getId());
        assertThat(comTaskEnablement.getPriority()).isEqualTo(ComTaskEnablement.DEFAULT_PRIORITY);
    }

    @Test
    @Transactional
    public void testRemoveNextExecutionSpecs() {
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();


        // Assert that none of the other attributes have changed
        assertThat(comTaskEnablement.getComTask().getId()).isEqualTo(this.comTask1.getId());
        assertThat(comTaskEnablement.getDeviceConfiguration().getId()).isEqualTo(this.deviceConfiguration1.getId());
        assertThat(comTaskEnablement.getSecurityPropertySet().getId()).isEqualTo(this.securityPropertySet1.getId());
        assertThat(comTaskEnablement.getPriority()).isEqualTo(ComTaskEnablement.DEFAULT_PRIORITY);
    }

    @Test
    @Transactional
    public void testCreateWithPriority() {
        int expectedPriority = ComTaskEnablement.HIGHEST_PRIORITY + 100;
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.setPriority(expectedPriority);

        // Business method
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        // Asserts
        assertThat(comTaskEnablement.getPriority()).isEqualTo(expectedPriority);
        assertThat(comTaskEnablement.getComTask()).isNotNull();
        assertThat(comTaskEnablement.getComTask().getId()).isEqualTo(this.comTask1.getId());
        assertThat(comTaskEnablement.getDeviceConfiguration()).isNotNull();
        assertThat(comTaskEnablement.getDeviceConfiguration().getId()).isEqualTo(this.deviceConfiguration1.getId());
        assertThat(comTaskEnablement.getSecurityPropertySet()).isNotNull();
        assertThat(comTaskEnablement.getSecurityPropertySet().getId()).isEqualTo(this.securityPropertySet1.getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.COM_TASK_ENABLEMENT_PRIORITY_RANGE + "}")
    public void testCreateWithInvalidPriority() {
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.setPriority(ComTaskEnablement.LOWEST_PRIORITY + 100);

        // Business method
        comTaskEnablementBuilder.add();

        // Asserts: see ExpectedConstraintViolation
    }

    @Test
    @Transactional
    public void testUpdatePriority() {
        int initialPriority = ComTaskEnablement.HIGHEST_PRIORITY + 100;
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.setPriority(initialPriority).add();

        // Business method
        int updatedPriority = ComTaskEnablement.HIGHEST_PRIORITY;
        comTaskEnablement.setPriority(updatedPriority);
        comTaskEnablement.save();

        // Asserts
        assertThat(comTaskEnablement.getPriority()).isEqualTo(updatedPriority);
        assertThat(comTaskEnablement.getComTask()).isNotNull();
        assertThat(comTaskEnablement.getComTask().getId()).isEqualTo(this.comTask1.getId());
        assertThat(comTaskEnablement.getDeviceConfiguration()).isNotNull();
        assertThat(comTaskEnablement.getDeviceConfiguration().getId()).isEqualTo(this.deviceConfiguration1.getId());
        assertThat(comTaskEnablement.getSecurityPropertySet()).isNotNull();
        assertThat(comTaskEnablement.getSecurityPropertySet().getId()).isEqualTo(this.securityPropertySet1.getId());
    }

    /**
     * Tests that saving a {@link ComTask} without protocol dialect properties, produces a constraint violation.
     */
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "protocolDialectConfigurationProperties")
    public void testEnableWithoutProtocolDialect() {
        // Business method
        this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, null).add();

        // Asserts: see ExpectedConstraintViolation
    }

    /**
     * Tests that enabling the same {@link ComTask} twice, produces a constraint violation.
     */
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.COM_TASK_CAN_ONLY_BE_ENABLED_ONCE + "}")
    public void testEnableTwice () {
        this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties).add();

        // Business method
        this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties).add();

        // Asserts: see ExpectedConstraintViolation
    }

    /**
     * Tests that enabling the same {@link ComTask} twice, even with another {@link SecurityPropertySet},
     * produces a constraint violation.
     */
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.COM_TASK_CAN_ONLY_BE_ENABLED_ONCE + "}")
    public void testEnableTwiceWithAnotherSecuritySet () {
        this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties).add();
        SecurityPropertySet anotherSecurityPropertySet = this.createSecurityPropertySet(this.deviceConfiguration1, "SPPS-Config-1 Bis");

        // Business method
        this.deviceConfiguration1.enableComTask(this.comTask1, anotherSecurityPropertySet, properties).add();

        // Asserts: see ExpectedConstraintViolation
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.COM_TASK_ENABLEMENT_SECURITY_PROPERTY_SET_MUST_BE_FROM_SAME_CONFIGURATION + "}")
    public void testCreateWithASecurityPropertySetFromAnotherConfiguration() {
        // Business method
        this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet2, properties).add();

        // Asserts: see ExpectedConstraintViolation
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.COM_TASK_ENABLEMENT_CANNOT_USE_DEFAULT_AND_PARTIAL_CONNECTION_TASK + "}")
    public void testCreateWithPartialConnectionTaskAndWithDefault() {
        // Business method
        this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties).
                setPartialConnectionTask(this.partialConnectionTask1).
                useDefaultConnectionTask(true).
                add();

        // Asserts: see ExpectedConstraintViolation
    }

    @Test
    @Transactional
    public void testRemoveScheduling() {
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        comTaskEnablement.save();

        // Assert that none of the other attributes have changed
        assertThat(comTaskEnablement.getComTask().getId()).isEqualTo(this.comTask1.getId());
        assertThat(comTaskEnablement.getDeviceConfiguration().getId()).isEqualTo(this.deviceConfiguration1.getId());
        assertThat(comTaskEnablement.getSecurityPropertySet().getId()).isEqualTo(this.securityPropertySet1.getId());
        assertThat(comTaskEnablement.getPriority()).isEqualTo(ComTaskEnablement.DEFAULT_PRIORITY);
    }

    @Test
    @Transactional
    public void testSwitchFromDefaultToConnectionTask() {
        this.registerSubscriber();

        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.useDefaultConnectionTask(true);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        // Business method
        comTaskEnablement.setPartialConnectionTask(this.partialConnectionTask1);
        comTaskEnablement.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(this.topicHandler, times(2)).handle(eventArgumentCaptor.capture());
        List<LocalEvent> localEvents = eventArgumentCaptor.getAllValues();
        assertThat(localEvents.get(0).getSource()).isInstanceOf(SwitchFromDefaultConnectionToPartialConnectionTaskEventData.class);
        SwitchFromDefaultConnectionToPartialConnectionTaskEventData eventData = (SwitchFromDefaultConnectionToPartialConnectionTaskEventData) localEvents.get(0).getSource();
        assertThat(eventData.getPartialConnectionTaskId()).isEqualTo(this.partialConnectionTask1.getId());
        assertThat(localEvents.get(1).getSource()).isEqualTo(comTaskEnablement);
        assertThat(localEvents.get(1).getType().getTopic()).isEqualTo(UpdateEventType.COMTASKENABLEMENT.topic());

        // Assert that none of the other attributes have changed
        assertThat(comTaskEnablement.getComTask().getId()).isEqualTo(this.comTask1.getId());
        assertThat(comTaskEnablement.getDeviceConfiguration().getId()).isEqualTo(this.deviceConfiguration1.getId());
        assertThat(comTaskEnablement.getSecurityPropertySet().getId()).isEqualTo(this.securityPropertySet1.getId());
        assertThat(comTaskEnablement.getPriority()).isEqualTo(ComTaskEnablement.DEFAULT_PRIORITY);
    }

    @Test
    @Transactional
    public void testSwitchFromDefaultToNoDefaultWithoutConnectionTask() {
        this.registerSubscriber();

        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.useDefaultConnectionTask(true);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        // Business method
        comTaskEnablement.useDefaultConnectionTask(false);
        comTaskEnablement.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(this.topicHandler, times(2)).handle(eventArgumentCaptor.capture());
        List<LocalEvent> localEvents = eventArgumentCaptor.getAllValues();
        assertThat(localEvents.get(0).getSource()).isInstanceOf(SwitchOffUsingDefaultConnectionEventData.class);
        assertThat(localEvents.get(1).getSource()).isEqualTo(comTaskEnablement);
        assertThat(localEvents.get(1).getType().getTopic()).isEqualTo(UpdateEventType.COMTASKENABLEMENT.topic());

        // Assert that none of the other attributes have changed
        assertThat(comTaskEnablement.getComTask().getId()).isEqualTo(this.comTask1.getId());
        assertThat(comTaskEnablement.getDeviceConfiguration().getId()).isEqualTo(this.deviceConfiguration1.getId());
        assertThat(comTaskEnablement.getSecurityPropertySet().getId()).isEqualTo(this.securityPropertySet1.getId());
        assertThat(comTaskEnablement.getPriority()).isEqualTo(ComTaskEnablement.DEFAULT_PRIORITY);
    }

    @Test
    @Transactional
    public void testSwitchOnDefaultWithoutPriorConnectionTask() {
        this.registerSubscriber();

        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.useDefaultConnectionTask(false);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        // Business method
        comTaskEnablement.useDefaultConnectionTask(true);
        comTaskEnablement.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(this.topicHandler, times(2)).handle(eventArgumentCaptor.capture());
        List<LocalEvent> localEvents = eventArgumentCaptor.getAllValues();
        assertThat(localEvents.get(0).getSource()).isInstanceOf(SwitchOnUsingDefaultConnectionEventData.class);
        assertThat(localEvents.get(1).getSource()).isEqualTo(comTaskEnablement);
        assertThat(localEvents.get(1).getType().getTopic()).isEqualTo(UpdateEventType.COMTASKENABLEMENT.topic());

        // Assert that none of the other attributes have changed
        assertThat(comTaskEnablement.getComTask().getId()).isEqualTo(this.comTask1.getId());
        assertThat(comTaskEnablement.getDeviceConfiguration().getId()).isEqualTo(this.deviceConfiguration1.getId());
        assertThat(comTaskEnablement.getSecurityPropertySet().getId()).isEqualTo(this.securityPropertySet1.getId());
        assertThat(comTaskEnablement.getPriority()).isEqualTo(ComTaskEnablement.DEFAULT_PRIORITY);
    }

    @Test
    @Transactional
    public void testSwitchFromConnectionTaskToDefault() {
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.setPartialConnectionTask(this.partialConnectionTask1);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        this.registerSubscriber();

        // Business method
        comTaskEnablement.useDefaultConnectionTask(true);
        comTaskEnablement.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(this.topicHandler, times(2)).handle(eventArgumentCaptor.capture());    //Once for the connection strategy change and once for the update of the object itself
        List<LocalEvent> localEvents = eventArgumentCaptor.getAllValues();
        assertThat(localEvents.get(0).getSource()).isInstanceOf(SwitchFromPartialConnectionTaskToDefaultConnectionEventData.class);
        SwitchFromPartialConnectionTaskToDefaultConnectionEventData eventData = (SwitchFromPartialConnectionTaskToDefaultConnectionEventData) localEvents.get(0).getSource();
        assertThat(eventData.getPartialConnectionTaskId()).isEqualTo(this.partialConnectionTask1.getId());
        assertThat(localEvents.get(1).getSource()).isEqualTo(comTaskEnablement);
        assertThat(localEvents.get(1).getType().getTopic()).isEqualTo(UpdateEventType.COMTASKENABLEMENT.topic());

        // Assert that none of the other attributes have changed
        assertThat(comTaskEnablement.getComTask().getId()).isEqualTo(this.comTask1.getId());
        assertThat(comTaskEnablement.getDeviceConfiguration().getId()).isEqualTo(this.deviceConfiguration1.getId());
        assertThat(comTaskEnablement.getSecurityPropertySet().getId()).isEqualTo(this.securityPropertySet1.getId());
        assertThat(comTaskEnablement.getPriority()).isEqualTo(ComTaskEnablement.DEFAULT_PRIORITY);
    }

    @Test
    @Transactional
    public void testRemoveConnectionTaskWithoutSwitchingToDefault() {
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.setPartialConnectionTask(this.partialConnectionTask1);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        this.registerSubscriber();

        // Business method
        comTaskEnablement.setPartialConnectionTask(null);
        comTaskEnablement.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(this.topicHandler, times(2)).handle(eventArgumentCaptor.capture());    //Once for the connection strategy change and once for the update of the object itself
        List<LocalEvent> localEvents = eventArgumentCaptor.getAllValues();
        assertThat(localEvents.get(0).getSource()).isInstanceOf(RemovePartialConnectionTaskEventData.class);
        RemovePartialConnectionTaskEventData eventData = (RemovePartialConnectionTaskEventData) localEvents.get(0).getSource();
        assertThat(eventData.getPartialConnectionTaskId()).isEqualTo(this.partialConnectionTask1.getId());
        assertThat(localEvents.get(1).getSource()).isEqualTo(comTaskEnablement);
        assertThat(localEvents.get(1).getType().getTopic()).isEqualTo(UpdateEventType.COMTASKENABLEMENT.topic());

        // Assert that none of the other attributes have changed
        assertThat(comTaskEnablement.getComTask().getId()).isEqualTo(this.comTask1.getId());
        assertThat(comTaskEnablement.getDeviceConfiguration().getId()).isEqualTo(this.deviceConfiguration1.getId());
        assertThat(comTaskEnablement.getSecurityPropertySet().getId()).isEqualTo(this.securityPropertySet1.getId());
        assertThat(comTaskEnablement.getPriority()).isEqualTo(ComTaskEnablement.DEFAULT_PRIORITY);
    }

    @Test
    @Transactional
    public void testSwitchFromOneConnectionTaskToAnother() {
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.setPartialConnectionTask(this.partialConnectionTask1);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        ConnectionTypePluggableClass anotherNoParamsConnectionTypePluggableClass =
                inMemoryPersistence.getProtocolPluggableService().
                        newConnectionTypePluggableClass(
                                "testSwitchFromOneConnectionTaskToAnother", OutboundNoParamsConnectionTypeImpl.class.getName());
        anotherNoParamsConnectionTypePluggableClass.save();
        PartialScheduledConnectionTask anotherPartialScheduledConnectionTask =
                this.deviceConfiguration1.
                        newPartialScheduledConnectionTask(
                                "testSwitchFromOneConnectionTaskToAnother",
                                anotherNoParamsConnectionTypePluggableClass,
                                TimeDuration.minutes(5),
                                ConnectionStrategy.MINIMIZE_CONNECTIONS).
                        nextExecutionSpec().temporalExpression(TimeDuration.hours(1)).set().
                        build();

        this.registerSubscriber();

        // Business method
        comTaskEnablement.setPartialConnectionTask(anotherPartialScheduledConnectionTask);
        comTaskEnablement.save();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(this.topicHandler, times(2)).handle(eventArgumentCaptor.capture());
        List<LocalEvent> localEvents = eventArgumentCaptor.getAllValues();
        assertThat(localEvents.get(0).getSource()).isInstanceOf(SwitchBetweenPartialConnectionTasksEventData.class);
        SwitchBetweenPartialConnectionTasksEventData eventData = (SwitchBetweenPartialConnectionTasksEventData) localEvents.get(0).getSource();
        assertThat(eventData.getOldPartialConnectionTaskId()).isEqualTo(this.partialConnectionTask1.getId());
        assertThat(eventData.getNewPartialConnectionTaskId()).isEqualTo(anotherPartialScheduledConnectionTask.getId());
        assertThat(localEvents.get(1).getSource()).isEqualTo(comTaskEnablement);
        assertThat(localEvents.get(1).getType().getTopic()).isEqualTo(UpdateEventType.COMTASKENABLEMENT.topic());

        // Assert that none of the other attributes have changed
        assertThat(comTaskEnablement.getComTask().getId()).isEqualTo(this.comTask1.getId());
        assertThat(comTaskEnablement.getDeviceConfiguration().getId()).isEqualTo(this.deviceConfiguration1.getId());
        assertThat(comTaskEnablement.getSecurityPropertySet().getId()).isEqualTo(this.securityPropertySet1.getId());
        assertThat(comTaskEnablement.getPriority()).isEqualTo(ComTaskEnablement.DEFAULT_PRIORITY);
    }

    @Test
    @Transactional
    public void testSuspend() {
        this.registerSubscriber();

        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.useDefaultConnectionTask(true).setPriority(ComTaskEnablement.LOWEST_PRIORITY);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        // Business method
        comTaskEnablement.suspend();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(this.topicHandler).handle(eventArgumentCaptor.capture());
        LocalEvent localEvent = eventArgumentCaptor.getValue();
        assertThat(localEvent.getType().getTopic()).isEqualTo(EventType.COMTASKENABLEMENT_SUSPEND.topic());
        assertThat(localEvent.getSource()).isEqualTo(comTaskEnablement);
    }

    @Test
    @Transactional
    public void testResume() {
        this.registerSubscriber();

        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.useDefaultConnectionTask(true).setPriority(ComTaskEnablement.LOWEST_PRIORITY);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        // Business method
        comTaskEnablement.resume();

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(this.topicHandler).handle(eventArgumentCaptor.capture());
        LocalEvent localEvent = eventArgumentCaptor.getValue();
        assertThat(localEvent.getType().getTopic()).isEqualTo(EventType.COMTASKENABLEMENT_RESUME.topic());
        assertThat(localEvent.getSource()).isEqualTo(comTaskEnablement);
    }

    @Test
    @Transactional
    public void testDisable () {
        this.registerSubscriber();

        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.useDefaultConnectionTask(true).setPriority(ComTaskEnablement.LOWEST_PRIORITY);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        // Business method
        this.deviceConfiguration1.disableComTask(this.comTask1);

        // Asserts
        ArgumentCaptor<LocalEvent> eventArgumentCaptor = ArgumentCaptor.forClass(LocalEvent.class);
        verify(this.topicHandler).handle(eventArgumentCaptor.capture());
        LocalEvent localEvent = eventArgumentCaptor.getValue();
        assertThat(localEvent.getType().getTopic()).isEqualTo(EventType.COMTASKENABLEMENT_VALIDATEDELETE.topic());
        assertThat(localEvent.getSource()).isEqualTo(comTaskEnablement);
    }

    @Test(expected = CannotDisableComTaskThatWasNotEnabledException.class)
    @Transactional
    public void testDisableWhenNoneEnabled () {
        // Business method
        this.deviceConfiguration1.disableComTask(this.comTask1);

        // Asserts: see expected exception rule
    }

    @Test(expected = CannotDisableComTaskThatWasNotEnabledException.class)
    @Transactional
    public void testDisableWhenNotEnabled () {
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.useDefaultConnectionTask(true).setPriority(ComTaskEnablement.LOWEST_PRIORITY).add();

        // Business method
        this.deviceConfiguration1.disableComTask(this.comTask2);

        // Asserts: see expected exception rule
    }


    @Test
    @Transactional
    public void setDefaultConnectionTaskUpdatesComTaskEnablementsTest() {
        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.useDefaultConnectionTask(true);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        Optional<ComTaskEnablement> reloadedComTaskEnablement = getReloadedComTaskEnablement(comTaskEnablement);
        assertThat(reloadedComTaskEnablement.get().getPartialConnectionTask().isPresent()).isFalse();

        this.partialConnectionTask1.setDefault(true);
        this.partialConnectionTask1.save();

        Optional<ComTaskEnablement> enabledWithDefault = getReloadedComTaskEnablement(comTaskEnablement);
        assertThat(enabledWithDefault.get().getPartialConnectionTask().get().getId()).isEqualTo(this.partialConnectionTask1.getId());
    }

    @Test
    @Transactional
    public void unSetDefaultConnectionTaskUpdatesComTaskEnablementsTest() {
        this.partialConnectionTask1.setDefault(true);
        this.partialConnectionTask1.save();

        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.useDefaultConnectionTask(true);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        Optional<ComTaskEnablement> reloadedComTaskEnablement = getReloadedComTaskEnablement(comTaskEnablement);
        assertThat(reloadedComTaskEnablement.get().getPartialConnectionTask().isPresent()).isTrue();


        this.partialConnectionTask1.setDefault(false);
        this.partialConnectionTask1.save();

        Optional<ComTaskEnablement> usingNonExistingDefaultConnectionTaskEnablement = getReloadedComTaskEnablement(comTaskEnablement);
        assertThat(usingNonExistingDefaultConnectionTaskEnablement.get().getPartialConnectionTask().isPresent()).isFalse();
    }

    private Optional<ComTaskEnablement> getReloadedComTaskEnablement(ComTaskEnablement comTaskEnablement) {
        return inMemoryPersistence.getDeviceConfigurationService().findComTaskEnablement(comTaskEnablement.getId());
    }

    @Test
    @Transactional
    public void cloneTest() {
        this.partialConnectionTask1.setDefault(true);
        this.partialConnectionTask1.save();
        prepareDeviceConfigForCloning();

        ComTaskEnablementBuilder comTaskEnablementBuilder = this.deviceConfiguration1.enableComTask(this.comTask1, this.securityPropertySet1, properties);
        comTaskEnablementBuilder.useDefaultConnectionTask(true);
        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();

        ComTaskEnablement clonedComTaskEnablement = ((ServerComTaskEnablement) comTaskEnablement).cloneForDeviceConfig(deviceConfiguration2);

        // Asserts
        assertThat(clonedComTaskEnablement.getComTask()).isNotNull();
        assertThat(clonedComTaskEnablement.getComTask().getId()).isEqualTo(this.comTask1.getId());
        assertThat(clonedComTaskEnablement.getDeviceConfiguration()).isNotNull();
        assertThat(clonedComTaskEnablement.getDeviceConfiguration().getId()).isEqualTo(this.deviceConfiguration2.getId());
        assertThat(clonedComTaskEnablement.getSecurityPropertySet().getId()).isNotEqualTo(securityPropertySet1.getId());
        assertThat(clonedComTaskEnablement.getSecurityPropertySet().getAuthenticationDeviceAccessLevel()).isEqualTo(securityPropertySet1.getAuthenticationDeviceAccessLevel());
        assertThat(clonedComTaskEnablement.getSecurityPropertySet().getEncryptionDeviceAccessLevel()).isEqualTo(securityPropertySet1.getEncryptionDeviceAccessLevel());
        assertThat(clonedComTaskEnablement.getPriority()).isEqualTo(ComTaskEnablement.DEFAULT_PRIORITY);
    }

    private void prepareDeviceConfigForCloning() {
        ((ServerPartialConnectionTask) partialConnectionTask1).cloneForDeviceConfig(deviceConfiguration2);
        ((ServerSecurityPropertySet) securityPropertySet1).cloneForDeviceConfig(deviceConfiguration2);
        deviceConfiguration2.findOrCreateProtocolDialectConfigurationProperties(properties.getDeviceProtocolDialect());
        properties.getPropertySpecs().stream().forEach(propertySpec -> {
            Object propertyValue = properties.getProperty(propertySpec.getName());
            if(propertyValue != null){
                deviceConfiguration2.getDeviceProtocolProperties().setProperty(propertySpec.getName(), propertyValue);
            }
        });
        deviceConfiguration2.save();
    }
}
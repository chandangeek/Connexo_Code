package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.tasks.ScheduledComTaskExecutionImpl;
import com.energyict.mdc.tasks.ComTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests teh {@link ComTasksInComScheduleMustHaveSameConfigurationSettingsValidator} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-05 (16:58)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTasksInComScheduleMustHaveSameConfigurationSettingsValidatorTest {

    private static final long PARTIAL_CONNECTION_TASK_ID = 97L;
    private static final long PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ID = 101L;
    private static final long SECURITY_PROPERTY_SET_ID = 103L;
    private static final int PRIORITY = 107;

    @Mock
    private DeviceConfiguration configuration;
    @Mock
    private Device device;
    @Mock
    private ComTask comTask1;
    @Mock
    private ComTaskEnablement comTaskEnablement1;
    @Mock
    private ComTask comTask2;
    @Mock
    private ComTaskEnablement comTaskEnablement2;
    @Mock
    private ScheduledComTaskExecutionImpl scheduledComTaskExecution;
    @Mock
    private ComTasksInComScheduleMustHaveSameConfigurationSettings validatorSettings;
    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    private ComTasksInComScheduleMustHaveSameConfigurationSettingsValidator validator;

    @Before
    public void initializeMocks() {
        when(this.comTask1.getName()).thenReturn("CT1");
        when(this.comTask2.getName()).thenReturn("CT2");
        when(this.configuration.getComTaskEnablementFor(this.comTask1)).thenReturn(Optional.of(this.comTaskEnablement1));
        when(this.configuration.getComTaskEnablementFor(this.comTask2)).thenReturn(Optional.of(this.comTaskEnablement2));
        when(this.device.getDeviceConfiguration()).thenReturn(this.configuration);
        when(this.scheduledComTaskExecution.getDevice()).thenReturn(this.device);
    }

    @Before
    public void initializeValidator() {
        this.validator = new ComTasksInComScheduleMustHaveSameConfigurationSettingsValidator();
        this.validator.initialize(this.validatorSettings);
    }

    @Test
    public void testComTaskThatIsNotEnabledIsValidToo() {
        ComTask comTask = mock(ComTask.class);
        when(this.configuration.getComTaskEnablementFor(comTask)).thenReturn(Optional.<ComTaskEnablement>empty());
        when(this.scheduledComTaskExecution.getComTasks()).thenReturn(Arrays.asList(comTask));

        // Business method
        boolean valid = this.validator.isValid(scheduledComTaskExecution, this.constraintValidatorContext);

        // Asserts
        assertThat(valid).isTrue();
    }

    @Test
    public void testSingleComTaskIsValid() {
        when(this.scheduledComTaskExecution.getComTasks()).thenReturn(Arrays.asList(this.comTask1));
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID);
        when(this.comTaskEnablement1.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(protocolDialectConfigurationProperties.getId()).thenReturn(PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ID);
        when(this.comTaskEnablement1.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(SECURITY_PROPERTY_SET_ID);
        when(this.comTaskEnablement1.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(this.comTaskEnablement1.getPriority()).thenReturn(PRIORITY);

        // Business method
        boolean valid = this.validator.isValid(scheduledComTaskExecution, this.constraintValidatorContext);

        // Asserts
        assertThat(valid).isTrue();
    }

    @Test
    public void testTwoComTasksWithTheSameSettingsAreValid() {
        when(this.scheduledComTaskExecution.getComTasks()).thenReturn(Arrays.asList(this.comTask1, this.comTask2));
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID);
        when(this.comTaskEnablement1.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask));
        when(this.comTaskEnablement2.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(protocolDialectConfigurationProperties.getId()).thenReturn(PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ID);
        when(this.comTaskEnablement1.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        when(this.comTaskEnablement2.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(SECURITY_PROPERTY_SET_ID);
        when(this.comTaskEnablement1.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(this.comTaskEnablement2.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(this.comTaskEnablement1.getPriority()).thenReturn(PRIORITY);
        when(this.comTaskEnablement2.getPriority()).thenReturn(PRIORITY);

        // Business method
        boolean valid = this.validator.isValid(scheduledComTaskExecution, this.constraintValidatorContext);

        // Asserts
        assertThat(valid).isTrue();
    }

    @Test
    public void testTwoComTasksWithDifferentPriorityAreNotValid() {
        when(this.scheduledComTaskExecution.getComTasks()).thenReturn(Arrays.asList(this.comTask1, this.comTask2));
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID);
        when(this.comTaskEnablement1.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask));
        when(this.comTaskEnablement2.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(protocolDialectConfigurationProperties.getId()).thenReturn(PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ID);
        when(this.comTaskEnablement1.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        when(this.comTaskEnablement2.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(SECURITY_PROPERTY_SET_ID);
        when(this.comTaskEnablement1.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(this.comTaskEnablement2.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(this.comTaskEnablement1.getPriority()).thenReturn(PRIORITY);
        when(this.comTaskEnablement2.getPriority()).thenReturn(PRIORITY + 1);

        // Business method
        boolean valid = this.validator.isValid(scheduledComTaskExecution, this.constraintValidatorContext);

        // Asserts
        assertThat(valid).isFalse();
    }

    @Test
    public void testTwoComTasksWithDifferentSecuritySetAreNotValid() {
        when(this.scheduledComTaskExecution.getComTasks()).thenReturn(Arrays.asList(this.comTask1, this.comTask2));
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID);
        when(this.comTaskEnablement1.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask));
        when(this.comTaskEnablement2.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(protocolDialectConfigurationProperties.getId()).thenReturn(PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ID);
        when(this.comTaskEnablement1.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        when(this.comTaskEnablement2.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        SecurityPropertySet securityPropertySet1 = mock(SecurityPropertySet.class);
        when(securityPropertySet1.getId()).thenReturn(SECURITY_PROPERTY_SET_ID);
        when(this.comTaskEnablement1.getSecurityPropertySet()).thenReturn(securityPropertySet1);
        SecurityPropertySet securityPropertySet2 = mock(SecurityPropertySet.class);
        when(securityPropertySet2.getId()).thenReturn(SECURITY_PROPERTY_SET_ID + 1);
        when(this.comTaskEnablement2.getSecurityPropertySet()).thenReturn(securityPropertySet2);
        when(this.comTaskEnablement1.getPriority()).thenReturn(PRIORITY);
        when(this.comTaskEnablement2.getPriority()).thenReturn(PRIORITY);

        // Business method
        boolean valid = this.validator.isValid(scheduledComTaskExecution, this.constraintValidatorContext);

        // Asserts
        assertThat(valid).isFalse();
    }

    @Test
    public void testTwoComTasksWithDifferentProtocolDialectConfigurationPropertiesAreNotValid() {
        when(this.scheduledComTaskExecution.getComTasks()).thenReturn(Arrays.asList(this.comTask1, this.comTask2));
        PartialConnectionTask partialConnectionTask = mock(PartialConnectionTask.class);
        when(partialConnectionTask.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID);
        when(this.comTaskEnablement1.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask));
        when(this.comTaskEnablement2.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties1 = mock(ProtocolDialectConfigurationProperties.class);
        when(protocolDialectConfigurationProperties1.getId()).thenReturn(PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ID);
        when(this.comTaskEnablement1.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties1);
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties2 = mock(ProtocolDialectConfigurationProperties.class);
        when(protocolDialectConfigurationProperties2.getId()).thenReturn(PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ID + 1);
        when(this.comTaskEnablement2.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties2);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(SECURITY_PROPERTY_SET_ID);
        when(this.comTaskEnablement1.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(this.comTaskEnablement2.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(this.comTaskEnablement1.getPriority()).thenReturn(PRIORITY);
        when(this.comTaskEnablement2.getPriority()).thenReturn(PRIORITY);

        // Business method
        boolean valid = this.validator.isValid(scheduledComTaskExecution, this.constraintValidatorContext);

        // Asserts
        assertThat(valid).isFalse();
    }

    @Test
    public void testTwoComTasksWithDifferentPartialConnectionTasksAreNotValid() {
        when(this.scheduledComTaskExecution.getComTasks()).thenReturn(Arrays.asList(this.comTask1, this.comTask2));
        PartialConnectionTask partialConnectionTask1 = mock(PartialConnectionTask.class);
        when(partialConnectionTask1.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID);
        when(this.comTaskEnablement1.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask1));
        PartialConnectionTask partialConnectionTask2 = mock(PartialConnectionTask.class);
        when(partialConnectionTask2.getId()).thenReturn(PARTIAL_CONNECTION_TASK_ID + 1);
        when(this.comTaskEnablement2.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask2));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(protocolDialectConfigurationProperties.getId()).thenReturn(PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_ID);
        when(this.comTaskEnablement1.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        when(this.comTaskEnablement2.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(SECURITY_PROPERTY_SET_ID);
        when(this.comTaskEnablement1.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(this.comTaskEnablement2.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(this.comTaskEnablement1.getPriority()).thenReturn(PRIORITY);
        when(this.comTaskEnablement2.getPriority()).thenReturn(PRIORITY);

        // Business method
        boolean valid = this.validator.isValid(scheduledComTaskExecution, this.constraintValidatorContext);

        // Asserts
        assertThat(valid).isFalse();
    }

}
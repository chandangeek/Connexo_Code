package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.tasks.ComTask;

import com.google.common.base.Optional;

import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComTasksMustBeEnabledByDeviceConfigurationValidator} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-07 (13:10)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComTasksMustBeEnabledByDeviceConfigurationValidatorTest {

    private static final long COMTASK_1_ID = 97L;
    private static final long COMTASK_2_ID = 101L;
    @Mock
    private DeviceConfiguration deviceConfiguration;
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
    private ComTaskExecutionImpl comTaskExecution;
    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ComTasksMustBeEnabledByDeviceConfiguration validatorSettings;

    private ComTasksMustBeEnabledByDeviceConfigurationValidator validator;

    @Before
    public void initializeMocks() {
        when(this.comTask1.getId()).thenReturn(COMTASK_1_ID);
        when(this.comTask1.getName()).thenReturn("CT1");
        when(this.comTaskEnablement1.getComTask()).thenReturn(this.comTask1);
        when(this.comTask2.getId()).thenReturn(COMTASK_2_ID);
        when(this.comTask2.getName()).thenReturn("CT2");
        when(this.comTaskEnablement2.getComTask()).thenReturn(this.comTask2);
        when(this.device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.comTaskExecution.getDevice()).thenReturn(this.device);
    }

    @Before
    public void initializeValidator() {
        this.validator = new ComTasksMustBeEnabledByDeviceConfigurationValidator();
        this.validator.initialize(this.validatorSettings);
    }

    @Test
    public void testSingleComTaskThatIsAlsoEnabled() {
        this.enableAllMockedComTasksOnMockedConfiguration();
        when(this.comTaskExecution.getComTasks()).thenReturn(Arrays.asList(this.comTask1));

        // Business method
        boolean valid = this.validator.isValid(this.comTaskExecution, this.context);

        // Asserts
        assertThat(valid).isTrue();
    }

    @Test
    public void testMultipleComTasksThatAreAlsoEnabled() {
        this.enableAllMockedComTasksOnMockedConfiguration();
        when(this.comTaskExecution.getComTasks()).thenReturn(Arrays.asList(this.comTask1, this.comTask2));

        // Business method
        boolean valid = this.validator.isValid(this.comTaskExecution, this.context);

        // Asserts
        assertThat(valid).isTrue();
    }

    @Test
    public void testSingleComTaskWhenNoneAreEnabled() {
        // No ComTasks are enabled on the configuration
        when(this.comTaskExecution.getComTasks()).thenReturn(Arrays.asList(this.comTask1));

        // Business method
        boolean valid = this.validator.isValid(this.comTaskExecution, this.context);

        // Asserts
        assertThat(valid).isFalse();
    }

    @Test
    public void testMultipleComTasksWhenNoneAreEnabled() {
        // No ComTasks are enabled on the configuration
        when(this.comTaskExecution.getComTasks()).thenReturn(Arrays.asList(this.comTask1, this.comTask2));

        // Business method
        boolean valid = this.validator.isValid(this.comTaskExecution, this.context);

        // Asserts
        assertThat(valid).isFalse();
    }

    @Test
    public void testSingleComTaskThatIsNotEnabled() {
        this.enableAllMockedComTasksOnMockedConfiguration();
        ComTask other = mock(ComTask.class);
        when(other.getId()).thenReturn(COMTASK_2_ID + 1);
        when(other.getName()).thenReturn("Not enabled");
        when(this.comTaskExecution.getComTasks()).thenReturn(Arrays.asList(other));

        // Business method
        boolean valid = this.validator.isValid(this.comTaskExecution, this.context);

        // Asserts
        assertThat(valid).isFalse();
    }

    @Test
    public void testMultipleComTaskThatAreNotEnabled() {
        this.enableAllMockedComTasksOnMockedConfiguration();
        ComTask other1 = mock(ComTask.class);
        when(other1.getId()).thenReturn(COMTASK_2_ID + 1);
        when(other1.getName()).thenReturn("Not enabled-1");
        ComTask other2 = mock(ComTask.class);
        when(other2.getId()).thenReturn(COMTASK_2_ID + 2);
        when(other2.getName()).thenReturn("Not enabled-2");
        when(this.comTaskExecution.getComTasks()).thenReturn(Arrays.asList(other1, other2));

        // Business method
        boolean valid = this.validator.isValid(this.comTaskExecution, this.context);

        // Asserts
        assertThat(valid).isFalse();
    }

    private void enableAllMockedComTasksOnMockedConfiguration() {
        when(this.deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(this.comTaskEnablement1, this.comTaskEnablement2));
        when(this.deviceConfiguration.getComTaskEnablementFor(this.comTask1)).thenReturn(Optional.of(this.comTaskEnablement1));
        when(this.deviceConfiguration.getComTaskEnablementFor(this.comTask2)).thenReturn(Optional.of(this.comTaskEnablement2));
    }

}
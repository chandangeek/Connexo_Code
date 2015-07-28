package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ActivateConnectionTasks} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-05 (12:59)
 */
@RunWith(MockitoJUnitRunner.class)
public class ActivateConnectionTasksTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ComTaskEnablement comTaskEnablement1;
    @Mock
    private ComTaskEnablement comTaskEnablement2;
    @Mock
    private ComTaskEnablement comTaskEnablement3;
    @Mock
    private ComTaskEnablement comTaskEnablement4;
    @Mock
    private PartialConnectionTask partialConnectionTask1;
    @Mock
    private PartialConnectionTask partialConnectionTask2;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setup() {
        when(partialConnectionTask1.getId()).thenReturn(112321L);
        when(partialConnectionTask2.getId()).thenReturn(325145L);
    }

    @Test
    public void testGetPropertySpecs() {
        ActivateConnectionTasks microAction = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = microAction.getPropertySpecs(this.propertySpecService);

        // Asserts
        assertThat(propertySpecs).isEmpty();
    }

    @Test
    public void executesActivatesAllConnectionTasks() {
        ConnectionTask connectionTask1 = mock(ConnectionTask.class);
        when(connectionTask1.getPartialConnectionTask()).thenReturn(partialConnectionTask1);
        ConnectionTask connectionTask2 = mock(ConnectionTask.class);
        when(connectionTask2.getPartialConnectionTask()).thenReturn(partialConnectionTask2);
        when(this.device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(this.deviceConfiguration.getComTaskEnablements()).thenReturn(Arrays.asList(comTaskEnablement1, comTaskEnablement2, comTaskEnablement3, comTaskEnablement4));
        when(comTaskEnablement1.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask1));
        when(comTaskEnablement2.getPartialConnectionTask()).thenReturn(Optional.<PartialConnectionTask>empty());
        when(comTaskEnablement3.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask2));
        when(comTaskEnablement4.getPartialConnectionTask()).thenReturn(Optional.of(partialConnectionTask1));
        when(this.device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask1, connectionTask2));

        ActivateConnectionTasks microAction = this.getTestInstance();

        // Business method
        microAction.execute(this.device, Instant.now(), Collections.emptyList());

        // Asserts
        verify(connectionTask1).activate();
        verify(connectionTask2).activate();
    }

    private ActivateConnectionTasks getTestInstance() {
        return new ActivateConnectionTasks(thesaurus);
    }

}
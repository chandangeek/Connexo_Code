package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.StateTransitionTriggerEvent;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the creation of a {@link StateTransitionTriggerEvent}
 * from a {@link CustomStateTransitionEventTypeImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (17:09)
 */
@RunWith(MockitoJUnitRunner.class)
public class StateTransitionTriggerEventCreationTest {

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private FiniteStateMachine stateMachine;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ServerFiniteStateMachineService stateMachineService;

    @Before
    public void initializeMocks() {
        when(this.dataModel.getInstance(StateTransitionTriggerEventImpl.class)).thenReturn(new StateTransitionTriggerEventImpl(this.eventService));
    }

    @Test
    public void newInstanceCopiesAllProperties() {
        CustomStateTransitionEventTypeImpl eventType = new CustomStateTransitionEventTypeImpl(this.dataModel, this.thesaurus, this.stateMachineService);
        String expectedSourceId = "Test1";
        Map<String, Object> expectedProperties = new HashMap<>();
        expectedProperties.put("firstName", "Rudi");
        expectedProperties.put("lastName", "Vankeirsbilck");

        // Business method
        String expectedSourceCurrentStateName = "Current";
        StateTransitionTriggerEvent triggerEvent = eventType.newInstance(this.stateMachine, expectedSourceId, expectedSourceCurrentStateName, Instant.now(), expectedProperties);

        // Asserts
        assertThat(triggerEvent).isNotNull();
        assertThat(triggerEvent.getType()).isEqualTo(eventType);
        assertThat(triggerEvent.getFiniteStateMachine()).isEqualTo(this.stateMachine);
        assertThat(triggerEvent.getSourceId()).isEqualTo(expectedSourceId);
        assertThat(triggerEvent.getSourceCurrentStateName()).isEqualTo(expectedSourceCurrentStateName);
        Map<String, Object> actualProperties = triggerEvent.getProperties();
        assertThat(actualProperties).isNotEmpty();
        assertThat(actualProperties.get("firstName")).isEqualTo("Rudi");
        assertThat(actualProperties.get("lastName")).isEqualTo("Vankeirsbilck");
    }

    @Test
    public void newInstanceWithoutProperties() {
        CustomStateTransitionEventTypeImpl eventType = new CustomStateTransitionEventTypeImpl(this.dataModel, this.thesaurus, this.stateMachineService);
        String expectedSourceId = "Test2";

        // Business method
        String expectedSourceCurrentStateName = "Current";
        StateTransitionTriggerEvent triggerEvent = eventType.newInstance(this.stateMachine, expectedSourceId, expectedSourceCurrentStateName, Instant.now(), new HashMap<>());

        // Asserts
        assertThat(triggerEvent).isNotNull();
        assertThat(triggerEvent.getType()).isEqualTo(eventType);
        assertThat(triggerEvent.getFiniteStateMachine()).isEqualTo(this.stateMachine);
        assertThat(triggerEvent.getSourceId()).isEqualTo(expectedSourceId);
        assertThat(triggerEvent.getSourceCurrentStateName()).isEqualTo(expectedSourceCurrentStateName);
        Map<String, Object> actualProperties = triggerEvent.getProperties();
        assertThat(actualProperties).isEmpty();
    }

}
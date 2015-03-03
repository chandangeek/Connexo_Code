package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.StateTransitionEvent;
import com.elster.jupiter.orm.DataModel;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link StateTransitionEventTypeImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (17:09)
 */
@RunWith(MockitoJUnitRunner.class)
public class StateTransitionEventTypeImplTest extends TestCase {

    @Mock
    private DataModel dataModel;

    @Before
    public void initializeMocks() {
        when(this.dataModel.getInstance(StateTransitionEventImpl.class)).thenReturn(new StateTransitionEventImpl());
    }

    @Test
    public void newInstanceCopiesAllProperties() {
        StateTransitionEventTypeImpl eventType = new StateTransitionEventTypeImpl(this.dataModel);
        String expectedSourceId = "Test1";
        Map<String, Object> expectedProperties = new HashMap<>();
        expectedProperties.put("firstName", "Rudi");
        expectedProperties.put("lastName", "Vankeirsbilck");

        // Business method
        StateTransitionEvent stateTransitionEvent = eventType.newInstance(expectedSourceId, expectedProperties);

        // Asserts
        assertThat(stateTransitionEvent).isNotNull();
        assertThat(stateTransitionEvent.getType()).isEqualTo(eventType);
        assertThat(stateTransitionEvent.getSourceId()).isEqualTo(expectedSourceId);
        Map<String, Object> actualProperties = stateTransitionEvent.getProperties();
        assertThat(actualProperties).isNotEmpty();
        assertThat(actualProperties.get("firstName")).isEqualTo("Rudi");
        assertThat(actualProperties.get("lastName")).isEqualTo("Vankeirsbilck");
    }

    @Test
    public void newInstanceWithoutProperties() {
        StateTransitionEventTypeImpl eventType = new StateTransitionEventTypeImpl(this.dataModel);
        String expectedSourceId = "Test2";

        // Business method
        StateTransitionEvent stateTransitionEvent = eventType.newInstance(expectedSourceId, new HashMap<>());

        // Asserts
        assertThat(stateTransitionEvent).isNotNull();
        assertThat(stateTransitionEvent.getType()).isEqualTo(eventType);
        assertThat(stateTransitionEvent.getSourceId()).isEqualTo(expectedSourceId);
        Map<String, Object> actualProperties = stateTransitionEvent.getProperties();
        assertThat(actualProperties).isEmpty();
    }

}
package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;

import java.util.Optional;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link TransitionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (11:25)
 */
public class TransitionTypeTest {

    @Test
    public void allTransitionTypesHaveAFrom() {
        for (TransitionType transitionType : TransitionType.values()) {
            assertThat(transitionType.getFrom()).as(transitionType.name() + " does not have a from").isNotNull();
        }
    }

    @Test
    public void allTransitionTypesHaveATo() {
        for (TransitionType transitionType : TransitionType.values()) {
            assertThat(transitionType.getTo()).as(transitionType.name() + " does not have a to").isNotNull();
        }
    }

    @Test
    public void testWithCustomStateTransition() {
        State from = mock(State.class);
        when(from.isCustom()).thenReturn(true);
        State to = mock(State.class);
        when(to.isCustom()).thenReturn(true);
        StateTransition transition = mock(StateTransition.class);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);

        // Business method
        Optional<TransitionType> transitionType = TransitionType.from(transition);

        // Asserts
        assertThat(transitionType.isPresent()).isFalse();
    }

    @Test
    public void testWithCustomFromAndStandardTo() {
        State from = mock(State.class);
        when(from.isCustom()).thenReturn(true);
        when(from.getName()).thenReturn("testWithCustomFromAndStandardTo");
        State to = mock(State.class);
        when(to.isCustom()).thenReturn(false);
        StateTransition transition = mock(StateTransition.class);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);

        // Business method
        Optional<TransitionType> transitionType = TransitionType.from(transition);

        // Asserts
        assertThat(transitionType.isPresent()).isFalse();
    }

    @Test
    public void testWithStandardFromAndCustomTo() {
        State from = mock(State.class);
        when(from.isCustom()).thenReturn(false);
        State to = mock(State.class);
        when(to.isCustom()).thenReturn(true);
        when(to.getName()).thenReturn("testWithStandardFromAndCustomTo");
        StateTransition transition = mock(StateTransition.class);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);

        // Business method
        Optional<TransitionType> transitionType = TransitionType.from(transition);

        // Asserts
        assertThat(transitionType.isPresent()).isFalse();
    }

    @Test
    public void testWithStandardTransition() {
        this.testStandardTransition(DefaultState.IN_STOCK, DefaultState.COMMISSIONING, TransitionType.COMMISSION);
        this.testStandardTransition(DefaultState.IN_STOCK, DefaultState.ACTIVE, TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING);
        this.testStandardTransition(DefaultState.IN_STOCK, DefaultState.INACTIVE, TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING);
        this.testStandardTransition(DefaultState.COMMISSIONING, DefaultState.ACTIVE, TransitionType.INSTALL_AND_ACTIVATE);
        this.testStandardTransition(DefaultState.COMMISSIONING, DefaultState.INACTIVE, TransitionType.INSTALL_INACTIVE);
        this.testStandardTransition(DefaultState.ACTIVE, DefaultState.INACTIVE, TransitionType.DEACTIVATE);
        this.testStandardTransition(DefaultState.INACTIVE, DefaultState.ACTIVE, TransitionType.ACTIVATE);
        this.testStandardTransition(DefaultState.INACTIVE, DefaultState.DECOMMISSIONED, TransitionType.DECOMMISSION);
        this.testStandardTransition(DefaultState.ACTIVE, DefaultState.DECOMMISSIONED, TransitionType.DEACTIVATE_AND_DECOMMISSION);
        this.testStandardTransition(DefaultState.DECOMMISSIONED, DefaultState.REMOVED, TransitionType.DELETE_FROM_DECOMMISSIONED);
        this.testStandardTransition(DefaultState.IN_STOCK, DefaultState.REMOVED, TransitionType.DELETE_FROM_IN_STOCK);

    }
    @Test
    public void testMandatoryPreTransitionChecksActivate(){
        assertThat(TransitionType.ACTIVATE.requiredChecks()).hasSize(4);
        assertThat(TransitionType.ACTIVATE.requiredChecks()).contains(MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.ACTIVATE.requiredChecks()).contains(MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.ACTIVATE.requiredChecks()).contains(MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.ACTIVATE.requiredChecks()).contains(MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
    }
    @Test
    public void testOptionalPreTransitionChecksActivate(){
        assertThat(TransitionType.ACTIVATE.optionalChecks()).hasSize(6);
        assertThat(TransitionType.ACTIVATE.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE);
        assertThat(TransitionType.ACTIVATE.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE);
        assertThat(TransitionType.ACTIVATE.optionalChecks()).contains(MicroCheck.SLAVE_DEVICE_HAS_GATEWAY);
        assertThat(TransitionType.ACTIVATE.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE);
        assertThat(TransitionType.ACTIVATE.optionalChecks()).contains(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        assertThat(TransitionType.ACTIVATE.optionalChecks()).contains(MicroCheck.LINKED_WITH_USAGE_POINT);
    }

    @Test
    public void testMandatoryPreTransitionActionsActivate(){
        assertThat(TransitionType.ACTIVATE.requiredActions()).hasSize(3);
        assertThat(TransitionType.ACTIVATE.requiredActions()).contains(MicroAction.SET_LAST_READING);
        assertThat(TransitionType.ACTIVATE.requiredActions()).contains(MicroAction.CREATE_METER_ACTIVATION);
        assertThat(TransitionType.ACTIVATE.requiredActions()).contains(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
    }
    @Test
    public void testOptionalPreTransitionActionsActivate(){
        assertThat(TransitionType.ACTIVATE.optionalActions()).hasSize(4);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.START_COMMUNICATION);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.START_RECURRING_COMMUNICATION);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.ENABLE_ESTIMATION);
        assertThat(TransitionType.ACTIVATE.optionalActions()).contains(MicroAction.ENABLE_VALIDATION);
    }
    @Test
    public void testMandatoryPreTransitionChecksDeactivate(){
        assertThat(TransitionType.DEACTIVATE.requiredChecks()).hasSize(4);
        assertThat(TransitionType.DEACTIVATE.requiredChecks()).contains(MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.DEACTIVATE.requiredChecks()).contains(MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.DEACTIVATE.requiredChecks()).contains(MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.DEACTIVATE.requiredChecks()).contains(MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
    }
    @Test
    public void testOptionalPreTransitionChecksDeactivate(){
        assertThat(TransitionType.DEACTIVATE.optionalChecks()).hasSize(3);
        assertThat(TransitionType.DEACTIVATE.optionalChecks()).contains(MicroCheck.ALL_LOAD_PROFILE_DATA_COLLECTED);
        assertThat(TransitionType.DEACTIVATE.optionalChecks()).contains(MicroCheck.ALL_DATA_VALIDATED);
        assertThat(TransitionType.DEACTIVATE.optionalChecks()).contains(MicroCheck.ALL_DATA_VALID);
    }
    @Test
    public void testMandatoryPreTransitionActionsDeActivate(){
        assertThat(TransitionType.DEACTIVATE.requiredActions()).isEmpty();
    }
    @Test
    public void testOptionalPreTransitionActionsDeactivate(){
        assertThat(TransitionType.DEACTIVATE.optionalActions()).hasSize(6);
        assertThat(TransitionType.DEACTIVATE.optionalActions()).contains(MicroAction.FORCE_VALIDATION_AND_ESTIMATION );
        assertThat(TransitionType.DEACTIVATE.optionalActions()).contains(MicroAction.START_COMMUNICATION);
        assertThat(TransitionType.DEACTIVATE.optionalActions()).contains(MicroAction.DISABLE_VALIDATION);
        assertThat(TransitionType.DEACTIVATE.optionalActions()).contains(MicroAction.DISABLE_ESTIMATION);
        assertThat(TransitionType.DEACTIVATE.optionalActions()).contains(MicroAction.START_RECURRING_COMMUNICATION);
        assertThat(TransitionType.DEACTIVATE.optionalActions()).contains(MicroAction.DISABLE_COMMUNICATION);
    }
    @Test
    public void testMandatoryPreTransitionChecksDeactivateAndDecommission(){
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredChecks()).isEmpty();
    }
    @Test
    public void testOptionalPreTransitionChecksDeactivateAndDecommission(){
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.optionalChecks()).hasSize(4);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.optionalChecks()).contains(MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.optionalChecks()).contains(MicroCheck.ALL_LOAD_PROFILE_DATA_COLLECTED);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.optionalChecks()).contains(MicroCheck.ALL_DATA_VALIDATED);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.optionalChecks()).contains(MicroCheck.ALL_DATA_VALID);
    }
    @Test
    public void testMandatoryPreTransitionActionsDeActivateAndDecommission(){
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).hasSize(6);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).contains(MicroAction.DETACH_SLAVE_FROM_MASTER);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).contains(MicroAction.CLOSE_ALL_ISSUES);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).contains(MicroAction.CLOSE_METER_ACTIVATION);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).contains(MicroAction.DISABLE_VALIDATION);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).contains(MicroAction.DISABLE_ESTIMATION);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.requiredActions()).contains(MicroAction.DISABLE_COMMUNICATION);
    }
    @Test
    public void testOptionalPreTransitionActionsDeactivateAndDecommission(){
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.optionalActions()).hasSize(2);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.optionalActions()).contains(MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS);
        assertThat(TransitionType.DEACTIVATE_AND_DECOMMISSION.optionalActions()).contains(MicroAction.FORCE_VALIDATION_AND_ESTIMATION);
    }
    @Test
    public void testMandatoryPreTransitionChecksDecommission(){
        assertThat(TransitionType.DECOMMISSION.requiredChecks()).isEmpty();
    }
    @Test
    public void testOptionalPreTransitionChecksDecommission(){
        assertThat(TransitionType.DECOMMISSION.optionalChecks()).hasSize(4);
        assertThat(TransitionType.DECOMMISSION.optionalChecks()).contains(MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED);
        assertThat(TransitionType.DECOMMISSION.optionalChecks()).contains(MicroCheck.ALL_LOAD_PROFILE_DATA_COLLECTED);
        assertThat(TransitionType.DECOMMISSION.optionalChecks()).contains(MicroCheck.ALL_DATA_VALIDATED);
        assertThat(TransitionType.DECOMMISSION.optionalChecks()).contains(MicroCheck.ALL_DATA_VALID);
    }
    @Test
    public void testOptionalPreTransitionActionsDecommission(){
        assertThat(TransitionType.DECOMMISSION.optionalActions()).hasSize(2);
        assertThat(TransitionType.DECOMMISSION.optionalActions()).contains(MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS);
        assertThat(TransitionType.DECOMMISSION.optionalActions()).contains(MicroAction.FORCE_VALIDATION_AND_ESTIMATION);
    }
    @Test
    public void testMandatoryPreTransitionActionsDecommission(){
        assertThat(TransitionType.DECOMMISSION.requiredActions()).hasSize(6);
        assertThat(TransitionType.DECOMMISSION.requiredActions()).contains(MicroAction.DETACH_SLAVE_FROM_MASTER);
        assertThat(TransitionType.DECOMMISSION.requiredActions()).contains(MicroAction.CLOSE_ALL_ISSUES);
        assertThat(TransitionType.DECOMMISSION.requiredActions()).contains(MicroAction.CLOSE_METER_ACTIVATION);
        assertThat(TransitionType.DECOMMISSION.requiredActions()).contains(MicroAction.DISABLE_VALIDATION);
        assertThat(TransitionType.DECOMMISSION.requiredActions()).contains(MicroAction.DISABLE_ESTIMATION);
        assertThat(TransitionType.DECOMMISSION.requiredActions()).contains(MicroAction.DISABLE_COMMUNICATION);
    }
    @Test
    public void testMandatoryPreTransitionChecksInstallAndActivate(){
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.requiredChecks()).hasSize(4);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.requiredChecks()).contains(MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.requiredChecks()).contains(MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.requiredChecks()).contains(MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.requiredChecks()).contains(MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
    }
    @Test
    public void testOptionalPreTransitionChecksInstallAndActivate(){
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalChecks()).hasSize(6);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalChecks()).contains(MicroCheck.SLAVE_DEVICE_HAS_GATEWAY);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalChecks()).contains(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalChecks()).contains(MicroCheck.LINKED_WITH_USAGE_POINT);
    }
    @Test
    public void testOptionalPreTransitionActionsInstallAndActivate(){
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).hasSize(4);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).contains(MicroAction.START_COMMUNICATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).contains(MicroAction.START_RECURRING_COMMUNICATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).contains(MicroAction.ENABLE_ESTIMATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.optionalActions()).contains(MicroAction.ENABLE_VALIDATION);
    }
    @Test
    public void testMandatoryPreTransitionActionsInstallAndActivate(){
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.requiredActions()).hasSize(3);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.requiredActions()).contains(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.requiredActions()).contains(MicroAction.SET_LAST_READING);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE.requiredActions()).contains(MicroAction.CREATE_METER_ACTIVATION);
    }
    @Test
    public void testMandatoryPreTransitionChecksInstallAndActivateWithoutCommissioning(){
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.requiredChecks()).hasSize(4);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.requiredChecks()).contains(MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.requiredChecks()).contains(MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.requiredChecks()).contains(MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.requiredChecks()).contains(MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
    }
    @Test
    public void testOptionalPreTransitionChecksInstallAndActivateWithoutCommissioning(){
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalChecks()).hasSize(6);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalChecks()).contains(MicroCheck.SLAVE_DEVICE_HAS_GATEWAY);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalChecks()).contains(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalChecks()).contains(MicroCheck.LINKED_WITH_USAGE_POINT);
    }

    @Test
    public void testMandatoryPreTransitionActionsInstallAndActivateWithoutCommissioning(){
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.requiredActions()).hasSize(3);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.requiredActions()).contains(MicroAction.SET_LAST_READING);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.requiredActions()).contains(MicroAction.CREATE_METER_ACTIVATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.requiredActions()).contains(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
    }
    @Test
    public void testOptionalPreTransitionActionsInstallAndActivateWithoutCommissioning(){
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).hasSize(4);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.START_COMMUNICATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.START_RECURRING_COMMUNICATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.ENABLE_ESTIMATION);
        assertThat(TransitionType.INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.ENABLE_VALIDATION);
    }
    @Test
    public void testMandatoryPreTransitionChecksInstallInactive(){
        assertThat(TransitionType.INSTALL_INACTIVE.requiredChecks()).hasSize(4);
        assertThat(TransitionType.INSTALL_INACTIVE.requiredChecks()).contains(MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.INSTALL_INACTIVE.requiredChecks()).contains(MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.INSTALL_INACTIVE.requiredChecks()).contains(MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.INSTALL_INACTIVE.requiredChecks()).contains(MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
    }
    @Test
    public void testOptionalPreTransitionChecksInstallInactive(){
        assertThat(TransitionType.INSTALL_INACTIVE.optionalChecks()).hasSize(6);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalChecks()).contains(MicroCheck.SLAVE_DEVICE_HAS_GATEWAY);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalChecks()).contains(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalChecks()).contains(MicroCheck.LINKED_WITH_USAGE_POINT);
    }
    @Test
    public void testMandatoryPreTransitionActionsInstallInactive(){
        assertThat(TransitionType.INSTALL_INACTIVE.requiredActions()).hasSize(3);
        assertThat(TransitionType.INSTALL_INACTIVE.requiredActions()).contains(MicroAction.SET_LAST_READING);
        assertThat(TransitionType.INSTALL_INACTIVE.requiredActions()).contains(MicroAction.CREATE_METER_ACTIVATION);
        assertThat(TransitionType.INSTALL_INACTIVE.requiredActions()).contains(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
    }
    @Test
    public void testOptionalPreTransitionActionsInstallInactive(){
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).hasSize(7);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.START_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.DISABLE_VALIDATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.DISABLE_ESTIMATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.START_RECURRING_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.ENABLE_ESTIMATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.DISABLE_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE.optionalActions()).contains(MicroAction.ENABLE_VALIDATION);
    }
    @Test
    public void testMandatoryPreTransitionChecksInstallInactiveFromInStock(){
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.requiredChecks()).hasSize(4);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.requiredChecks()).contains(MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.requiredChecks()).contains(MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.requiredChecks()).contains(MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.requiredChecks()).contains(MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
    }
    @Test
    public void testOptionalPreTransitionChecksInstallInactiveFromInStock(){
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalChecks()).hasSize(6);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalChecks()).contains(MicroCheck.SLAVE_DEVICE_HAS_GATEWAY);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalChecks()).contains(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalChecks()).contains(MicroCheck.LINKED_WITH_USAGE_POINT);
    }
    @Test
    public void testMandatoryPreTransitionActionsInstallInactiveFromInStock(){
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.requiredActions()).hasSize(3);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.requiredActions()).contains(MicroAction.SET_LAST_READING);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.requiredActions()).contains(MicroAction.CREATE_METER_ACTIVATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.requiredActions()).contains(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
    }
    @Test
    public void testOptionalPreTransitionActionsInstallInactiveFromInStock(){
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).hasSize(7);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.START_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.DISABLE_VALIDATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.DISABLE_ESTIMATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.START_RECURRING_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.ENABLE_ESTIMATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.DISABLE_COMMUNICATION);
        assertThat(TransitionType.INSTALL_INACTIVE_WITHOUT_COMMISSIONING.optionalActions()).contains(MicroAction.ENABLE_VALIDATION);
    }
    @Test
    public void testMandatoryPreTransitionChecksRemoveFromDecommissioned(){
        assertThat(TransitionType.DELETE_FROM_DECOMMISSIONED.requiredChecks()).isEmpty();
    }
    @Test
    public void testOptionalPreTransitionChecksRemoveFromDecommissioned(){
        assertThat(TransitionType.DELETE_FROM_DECOMMISSIONED.optionalChecks()).isEmpty();
    }
    @Test
    public void testMandatoryPreTransitionActionsRemoveFromDecommissioned(){
        assertThat(TransitionType.DELETE_FROM_DECOMMISSIONED.requiredActions()).hasSize(1);
        assertThat(TransitionType.DELETE_FROM_DECOMMISSIONED.requiredActions()).contains(MicroAction.REMOVE_DEVICE);
    }
    @Test
    public void testOptionalPreTransitionActionsInstallRemoveFromDecommissioned(){
        assertThat(TransitionType.DELETE_FROM_DECOMMISSIONED.optionalActions()).isEmpty();
    }
    @Test
    public void testMandatoryPreTransitionChecksRemoveFromStock(){
        assertThat(TransitionType.DELETE_FROM_IN_STOCK.requiredChecks()).isEmpty();
    }
    @Test
    public void testOptionalPreTransitionChecksRemoveFromStock(){
        assertThat(TransitionType.DELETE_FROM_IN_STOCK.optionalChecks()).isEmpty();
    }
    @Test
    public void testMandatoryPreTransitionActionsRemoveFromStock(){
        assertThat(TransitionType.DELETE_FROM_IN_STOCK.requiredActions()).hasSize(1);
        assertThat(TransitionType.DELETE_FROM_IN_STOCK.requiredActions()).contains(MicroAction.REMOVE_DEVICE);
    }
    @Test
    public void testOptionalPreTransitionActionsInstallRemoveStock(){
        assertThat(TransitionType.DELETE_FROM_IN_STOCK.optionalActions()).isEmpty();
    }
    @Test
    public void testMandatoryPreTransitionChecksCommission(){
        assertThat(TransitionType.COMMISSION.requiredChecks()).hasSize(4);
        assertThat(TransitionType.COMMISSION.requiredChecks()).contains(MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.COMMISSION.requiredChecks()).contains(MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.COMMISSION.requiredChecks()).contains(MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID);
        assertThat(TransitionType.COMMISSION.requiredChecks()).contains(MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID);
    }
    @Test
    public void testOptionalPreTransitionChecksCommission(){
        assertThat(TransitionType.COMMISSION.optionalChecks()).hasSize(5);
        assertThat(TransitionType.COMMISSION.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE);
        assertThat(TransitionType.COMMISSION.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE);
        assertThat(TransitionType.COMMISSION.optionalChecks()).contains(MicroCheck.SLAVE_DEVICE_HAS_GATEWAY);
        assertThat(TransitionType.COMMISSION.optionalChecks()).contains(MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE);
        assertThat(TransitionType.COMMISSION.optionalChecks()).contains(MicroCheck.DEFAULT_CONNECTION_AVAILABLE);
    }
    @Test
    public void testMandatoryPreTransitionActionsCommission(){
        assertThat(TransitionType.COMMISSION.requiredActions()).hasSize(3);
        assertThat(TransitionType.COMMISSION.requiredActions()).contains(MicroAction.CREATE_METER_ACTIVATION);
        assertThat(TransitionType.COMMISSION.requiredActions()).contains(MicroAction.SET_LAST_READING);
        assertThat(TransitionType.COMMISSION.requiredActions()).contains(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
    }
    @Test
    public void testOptionalPreTransitionActionsCommission(){
        assertThat(TransitionType.COMMISSION.optionalActions()).hasSize(3);
        assertThat(TransitionType.COMMISSION.optionalActions()).contains(MicroAction.START_COMMUNICATION);
        assertThat(TransitionType.COMMISSION.optionalActions()).contains(MicroAction.START_RECURRING_COMMUNICATION);
        assertThat(TransitionType.COMMISSION.optionalActions()).contains(MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE);
    }
    private void testStandardTransition(DefaultState defaultFrom, DefaultState defaultTo, TransitionType expectedTransitionType) {
        State from = mock(State.class);
        when(from.isCustom()).thenReturn(false);
        when(from.getName()).thenReturn(defaultFrom.getKey());
        State to = mock(State.class);
        when(to.isCustom()).thenReturn(false);
        when(to.getName()).thenReturn(defaultTo.getKey());
        StateTransition transition = mock(StateTransition.class);
        when(transition.getFrom()).thenReturn(from);
        when(transition.getTo()).thenReturn(to);

        // Business method
        Optional<TransitionType> transitionType = TransitionType.from(transition);

        // Asserts
        assertThat(transitionType.isPresent())
                .as("TransitionType#from for default state " + defaultFrom + " and " + defaultTo + " was not found")
                .isTrue();
        assertThat(transitionType.get()).isEqualTo(expectedTransitionType);
    }

}
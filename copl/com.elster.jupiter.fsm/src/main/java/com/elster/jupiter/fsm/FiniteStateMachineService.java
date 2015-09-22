package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.events.EventType;

import java.util.List;
import java.util.Optional;

/**
 * Provides services to manage {@link FiniteStateMachine}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:25)
 */
@ProviderType
public interface FiniteStateMachineService {

    String COMPONENT_NAME = "FSM";

    /**
     * Returns the topic onto which {@link StateTransitionChangeEvent}s are published.
     *
     * @return The topic
     */
    String stateTransitionChangeEventTopic();

    void addStandardEventPredicate(StandardEventPredicate predicate);

    /**
     * Gets the List of {@link StateChangeBusinessProcess}es.
     *
     * @return The List of StateChangeBusinessProcess
     */
    List<StateChangeBusinessProcess> findStateChangeBusinessProcesses();

    /**
     * Gets the {@link StateChangeBusinessProcess} with given id.
     * @param id to find
     * @return The Optional StateChangeBusinessProcess with given id
     */
    Optional<StateChangeBusinessProcess> findStateChangeBusinessProcessById(long id);

    /**
     * Enables the external business process identified by the specified
     * deploymentId and processId to be executed when a {@link State}
     * is entered or exited.
     *
     * @param name of the external process
     * @param deploymentId The deployment id of the external process
     * @param processId The process id of the external process
     * @return The StateChangeBusinessProcess
     */
    StateChangeBusinessProcess enableAsStateChangeBusinessProcess(String name,String deploymentId, String processId);

    /**
     * Disables the external business process identified by the specified
     * deploymentId and processId to be executed when a {@link State}
     * is entered or exited. This will fail when there is at least
     * one State on which this external process is configured
     * to be executed on entry or on exit.
     *
     * @param deploymentId The deployment id of the external process
     * @param processId The process id of the external process
     */
    void disableAsStateChangeBusinessProcess(String deploymentId, String processId);

    /**
     * Creates a new {@link CustomStateTransitionEventType} with the specified symbol.
     * Note that you are responsible for saving the CustomStateTransitionEventType.
     *
     * @param symbol The symbolic representation of the event
     * @return The CustomStateTransitionEventType
     */
    CustomStateTransitionEventType newCustomStateTransitionEventType(String symbol);

    /**
     * Creates a new {@link StandardStateTransitionEventType} with the specified symbol.
     * Note that you are responsible for saving the StandardStateTransitionEventType.
     *
     * @param eventType The standard EventType
     * @return The StandardStateTransitionEventType
     */
    StandardStateTransitionEventType newStandardStateTransitionEventType(EventType eventType);

    /**
     * Finds the {@link CustomStateTransitionEventType} with the specified symbolic representation
     * if such a CustomStateTransitionEventType exists.
     *
     * @param symbol The symbolic representation
     * @return The CustomStateTransitionEventType
     */
    Optional<CustomStateTransitionEventType> findCustomStateTransitionEventType(String symbol);

    /**
     * Finds the {@link StandardStateTransitionEventType} for the specified {@link EventType}
     * if such a StandardStateTransitionEventType exists.
     *
     * @param eventType The standard EventType
     * @return The StandardStateTransitionEventType
     */
    Optional<StandardStateTransitionEventType> findStandardStateTransitionEventType(EventType eventType);

    /**
     * Finds the {@link StateTransitionEventType} with the specified unique symbolic representation.
     *
     * @param symbol The symbolic representation
     * @see StateTransitionEventType#getSymbol()
     * @return The StateTransitionEventType
     */
    Optional<StateTransitionEventType> findStateTransitionEventTypeBySymbol(String symbol);

    /**
     * Gets all the {@link StateTransitionEventType} that have been defined in the system.
     *
     * @return The List of StateTransitionEventType
     */
    List<StateTransitionEventType> getStateTransitionEventTypes();

    /**
     * Starts the building process for a new {@link FiniteStateMachine}
     * with the specified name. The name must be unique across the system,
     * i.e. no two FiniteStateMachines can have the same name.
     *
     * @param name The name
     * @return The FiniteStateMachineBuilder
     */
    FiniteStateMachineBuilder newFiniteStateMachine(String name);

    /**
     * Clones the specified {@link FiniteStateMachine} with the specified name.
     * The name must be unique across the system,
     * i.e. no two FiniteStateMachines can have the same name.
     * Note that the cloned FiniteStateMachine has already been saved.
     *
     * @param source The FiniteStateMachine that is being cloned
     * @param name The name
     * @return The cloned FiniteStateMachine
     */
    FiniteStateMachine cloneFiniteStateMachine(FiniteStateMachine source, String name);

    /**
     * Finds the {@link FiniteStateMachine} that is uniquely identified
     * by the specified id.
     *
     * @param id The unique identifier
     * @return The FiniteStateMachine
     */
    Optional<FiniteStateMachine> findFiniteStateMachineById(long id);

    /**
     * Finds the {@link FiniteStateMachine} with the specified name
     * if such a FiniteStateMachine exists.
     *
     * @param name The name
     * @return The FiniteStateMachine
     */
    Optional<FiniteStateMachine> findFiniteStateMachineByName(String name);

    /**
     * Finds the {@link State} that is uniquely identified
     * by the specified id.
     *
     * @param id The unique identifier
     * @return The State
     */
    Optional<State> findFiniteStateById(long id);

}
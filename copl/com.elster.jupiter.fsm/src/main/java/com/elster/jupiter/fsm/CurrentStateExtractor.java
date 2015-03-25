package com.elster.jupiter.fsm;

import com.elster.jupiter.events.LocalEvent;

import java.util.Optional;

/**
 * Extracts {@link State} information from an event.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-05 (14:30)
 */
public interface CurrentStateExtractor {

    public class CurrentState {
        /**
         * The unique identifier of the object for which state information is provided.
         */
        public String sourceId;

        /**
         * The name of the current state.
         */
        public String name;
    }

    /**
     * Extracts information from the LocalEvent that may or may
     * not relate to the specified {@link FinateStateMachine}.
     * If the event does not relate to {@link State} then Optional.empty()
     * should be returned. Examples of why it does not relate to state could be:
     * <ul>
     * <li>The {@link LocalEvent#getSource() event's source} is not of the appropriate type</li>
     * <li>The {@link LocalEvent#getSource() event's source} is not linked to the FinateStateMachine</li>
     * <li>The event's properties do not provide sufficient information</li>
     * </ul>
     *
     * @param event The LocalEvent from which the information should be extraced
     * @param finateStateMachine The FinateStateMachine to which the event's source object should be related
     * @return The CurrentState
     */
    public Optional<CurrentState> extractFrom(LocalEvent event, FinateStateMachine finateStateMachine);

}
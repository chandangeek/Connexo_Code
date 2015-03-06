package com.elster.jupiter.fsm;

import java.time.Instant;
import java.util.List;

/**
 * A State of a {@link FinateStateMachine}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (11:54)
 */
public interface State {

    public long getId();

    public String getName();

    public FinateStateMachine getFinateStateMachine();

    public List<StateTransition> getOutgoingStateTransitions();

    public List<ProcessReference> getOnEntryProcesses();

    public List<ProcessReference> getOnExitProcesses();

    public long getVersion();

    /**
     * Gets the timestamp on which this State was created.
     *
     * @return The creation timestamp
     */
    public Instant getCreationTimestamp();

    /**
     * Gets the timestamp on which this State was last modified.
     *
     * @return The timestamp of last modification
     */
    public Instant getModifiedTimestamp();

}
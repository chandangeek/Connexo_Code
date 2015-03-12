package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.State;

import java.time.Instant;
import java.util.List;

/**
 * Models the life cycle of a device.
 * The states and supported transitions are actually
 * defined by a {@link FinateStateMachine}.
 * A DeviceLifeCycle will add:
 * <ul>
 * <li>actions that can be executed while the device is in a certain state</li>
 * <li>authorisation: which user(s) can execute these actions</li>
 * <li>checks that will be verified as part of the actions</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (10:18)
 */
public interface DeviceLifeCycle {

    public FinateStateMachine getFinateStateMachine();

    public long getId();

    public String getName();

    public long getVersion();

    /**
     * Gets the timestamp on which this DeviceLifeCycle was created.
     *
     * @return The creation timestamp
     */
    public Instant getCreationTimestamp();

    /**
     * Gets the timestamp on which this DeviceLifeCycle was last modified.
     *
     * @return The timestamp of last modification
     */
    public Instant getModifiedTimestamp();

    /**
     * Gets all {@link AuthorizedAction}s.
     *
     * @return The List of AuthorizedAction
     */
    public List<AuthorizedAction> getAuthorizedActions();

    /**
     * Gets all {@link AuthorizedAction}s for the specified {@link State}.
     *
     * @return The List of AuthorizedAction
     */
    public List<AuthorizedAction> getAuthorizedActions(State state);

    public void save();

    public void delete();

}
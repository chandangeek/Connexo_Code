package com.energyict.mdc.device.lifecycle;

import com.elster.jupiter.fsm.State;

import java.time.Instant;
import java.util.Set;

/**
 * Models an action that can be authorized to be initiated
 * by the users of the system when a device is in the related {@link State}.
 * It suffices that the user has one of the following {@link Privileges}
 * to be able to initiate the action.
 * <ul>
 * <li>{@link Privileges#INITIATE_ACTION_1}</li>
 * <li>{@link Privileges#INITIATE_ACTION_2}</li>
 * <li>{@link Privileges#INITIATE_ACTION_3}</li>
 * <li>{@link Privileges#INITIATE_ACTION_4}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (14:17)
 */
public interface AuthorizedAction {

    public enum Level {
        ONE, TWO, THREE, FOUR;

    }

    public long getId();

    public long getVersion();

    /**
     * Gets the timestamp on which this AuthorizedAction was created.
     *
     * @return The creation timestamp
     */
    public Instant getCreationTimestamp();

    /**
     * Gets the timestamp on which this AuthorizedAction was last modified.
     *
     * @return The timestamp of last modification
     */
    public Instant getModifiedTimestamp();

    /**
     * Gets the {@link Level}s that authorizes a user to
     * initiate this action, i.e. when the user's set of
     * privileges include at least one of the
     * {@link com.elster.jupiter.users.Privilege}s
     * that relates to the levels.
     *
     * @return The Set of Level
     */
    public Set<Level> getLevels();

    /**
     * Gets the {@link State} on which this action is authorized.
     *
     * @return The State
     */
    public State getState();

}
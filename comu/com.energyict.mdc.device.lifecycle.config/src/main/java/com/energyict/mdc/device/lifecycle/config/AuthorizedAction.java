package com.energyict.mdc.device.lifecycle.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.HasId;

import java.time.Instant;
import java.util.Optional;
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
 * Two kinds of AuthorizedAction exist at this point in time:
 * <ul>
 * <li>{@link AuthorizedBusinessProcessAction}</li>
 * <li>{@link AuthorizedTransitionAction}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (14:17)
 */
@ProviderType
public interface AuthorizedAction extends HasId, HasName {
    enum Level {
        ONE(Privileges.Constants.INITIATE_ACTION_1),
        TWO(Privileges.Constants.INITIATE_ACTION_2),
        THREE(Privileges.Constants.INITIATE_ACTION_3),
        FOUR(Privileges.Constants.INITIATE_ACTION_4);

        public String getPrivilege() {
            return privilege;
        }

        private String privilege;

        Level (String privilege) {
            this.privilege = privilege;
        }

        public static Optional<Level> forPrivilege(String privilege) {
            for (Level level : values()) {
                if (level.getPrivilege().equals(privilege)) {
                    return Optional.of(level);
                }
            }
            return Optional.empty();
        }

    }

    public long getVersion();

    public DeviceLifeCycle getDeviceLifeCycle();

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
package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;

import java.util.Set;

/**
 * Provides building services for {@link DeviceLifeCycle}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (16:31)
 */
public interface DeviceLifeCycleBuilder {

    /**
     * Authorizes the initiation of the external process definition
     * when the related device is in the specified {@link State}.
     *
     * @param state The State
     * @param deploymentId The deployment id of the external process
     * @param processId The process id of the external process
     * @return The AuthorizedBusinessProcessAction
     */
    public AuthorizedActionBuilder newCustomAction(State state, String deploymentId, String processId);

    /**
     * Gets the {@link DeviceLifeCycle} that was being constructed with this builder.
     * Note that it is your responsibility to save the DeviceLifeCycle.
     *
     * @return The DeviceLifeCycle
     * @see DeviceLifeCycle#save()
     */
    public DeviceLifeCycle complete();

    /**
     * Supports the building process of an {@link AuthorizedAction}.
     * It allows to specify the privilege levels that a user needs
     * to be able to initiate the action.
     * It is sufficient that a user has at least one of the
     * privilege levels to be able to initiate the new action.
     *
     * @param <T> The type of {@link AuthorizedAction} that is being built
     */
    public interface AuthorizedActionBuilder<T extends AuthorizedAction> {

        public AuthorizedActionBuilder<T> add(AuthorizedAction.Level level, AuthorizedAction.Level... otherLevels);

        public AuthorizedActionBuilder<T> addAll(Set<AuthorizedAction.Level> levels);

        public T complete();

    }

}
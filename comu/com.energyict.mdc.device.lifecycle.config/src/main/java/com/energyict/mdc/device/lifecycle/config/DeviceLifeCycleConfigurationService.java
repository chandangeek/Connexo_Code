package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.FiniteStateMachine;

import java.util.Optional;

/**
 * Provides services to manage {@link DeviceLifeCycle}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (10:22)
 */
public interface DeviceLifeCycleConfigurationService {

    String COMPONENT_NAME = "DLD";

    /**
     * Starts the building process of a new {@link DeviceLifeCycle}.
     *
     * @param name The unique name of the new DeviceLifeCycle
     * @param finiteStateMachine The FiniteStateMachine that is providing all the
     * {@link com.elster.jupiter.fsm.State}s and {@link com.elster.jupiter.fsm.StateTransition}s.
     *
     * @return The DeviceLifeCycleBuilder
     */
    public DeviceLifeCycleBuilder newDeviceLifeCycleUsing(String name, FiniteStateMachine finiteStateMachine);

    /**
     * Finds the {@link DeviceLifeCycle} that was created by default
     * when this bundle was first installed.
     * Note that somebody may have deleted that DeviceLifeCycle
     * in the meantime, which is why it is not guaranteed to be there.
     *
     * @return The DeviceLifeCycle
     */
    public Optional<DeviceLifeCycle> findDefaultDeviceLifeCycle();

    /**
     * Finds the {@link DeviceLifeCycle} with the specified identifier.
     *
     * @param id The identifier
     * @return The DeviceLifeCycle
     */
    public Optional<DeviceLifeCycle> findDeviceLifeCycle(long id);

    /**
     * Finds the {@link DeviceLifeCycle} with the specified name.
     *
     * @param name The name
     * @return The DeviceLifeCycle
     */
    public Optional<DeviceLifeCycle> findDeviceLifeCycleByName(String name);

}
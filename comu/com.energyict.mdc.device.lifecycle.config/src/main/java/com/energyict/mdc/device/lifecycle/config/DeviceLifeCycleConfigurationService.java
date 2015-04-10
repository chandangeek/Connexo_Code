package com.energyict.mdc.device.lifecycle.config;

import com.energyict.mdc.common.services.Finder;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.users.Privilege;

import java.util.Optional;

/**
 * Provides services to manage {@link DeviceLifeCycle}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (10:22)
 */
public interface DeviceLifeCycleConfigurationService {

    String COMPONENT_NAME = "DLD";

    public Optional<Privilege> findPrivilege(String userActionPrivilege);

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
     * Creates a new {@link DeviceLifeCycle} from the default template with the specified name.
     *
     * @param name The unique name of the new DeviceLifeCycle
     * @return The DeviceLifeCycle that was created from the template
     */
    public DeviceLifeCycle newDefaultDeviceLifeCycle(String name);

    /**
     * Clones the specified {@link DeviceLifeCycle} with the specified name.
     *
     * @param source The DeviceLifeCycle that is being cloned
     * @param name The unique name of the new DeviceLifeCycle
     * @return The cloned DeviceLifeCycle
     */
    public DeviceLifeCycle cloneDeviceLifeCycle(DeviceLifeCycle source, String name);

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

    /**
     * Finds all the {@link DeviceLifeCycle}s with options
     * to add external paging and sorting options.
     *
     * @return The Finder that supports paging and sorting
     */
    public Finder<DeviceLifeCycle> findAllDeviceLifeCycles();

}
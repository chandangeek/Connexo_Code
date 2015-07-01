package com.energyict.mdc.device.lifecycle.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Finder;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.Privilege;

import java.util.List;
import java.util.Optional;

/**
 * Provides services to manage {@link DeviceLifeCycle}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (10:22)
 */
@ProviderType
public interface DeviceLifeCycleConfigurationService {

    String COMPONENT_NAME = "DLD";

    /**
     * Finds one of the privileges that allows a user to initiate an {@link AuthorizedAction}.
     *
     * @param initiateActionPrivilegeName The name of the privilege
     * @return The Privilege
     */
    public Optional<Privilege> findInitiateActionPrivilege(String initiateActionPrivilegeName);

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

    /**
     * Gets the system wide maximum time shift in the future
     * that can be used for the effective timestamp of a
     * device life cycle transition.
     * Any attempt to set maximum future effective time shift
     * of a {@link DeviceLifeCycle} will result in a business
     * exception, failing the creation or update of the DeviceLifeCycle.
     *
     * @return The maximum time shift
     */
    public TimeDuration getMaximumFutureEffectiveTimeShift();

    /**
     * Gets the system wide default time shift in the future
     * that can be used for the effective timestamp of a
     * device life cycle transition.
     *
     * @return The default time shift
     */
    public TimeDuration getDefaultFutureEffectiveTimeShift();

    /**
     * Gets the system wide maximum time shift in the past
     * that can be used for the effective timestamp of a
     * device life cycle transition.
     * Any attempt to set maximum past effective time shift
     * of a {@link DeviceLifeCycle} will result in a business
     * exception, failing the creation or update of the DeviceLifeCycle.
     *
     * @return The maximum time shift
     */
    public TimeDuration getMaximumPastEffectiveTimeShift();

    /**
     * Gets the system wide default time shift in the past
     * that can be used for the effective timestamp of a
     * device life cycle transition.
     *
     * @return The default time shift
     */
    public TimeDuration getDefaultPastEffectiveTimeShift();

    /**
     * Finds all the {@link TransitionBusinessProcess}es.
     *
     * @return The list of TransitionBusinessProcess
     */
    public List<TransitionBusinessProcess> findTransitionBusinessProcesses();

    /**
     * Enables the external business process identified by the specified
     * deploymentId and processId to be executed as part of a
     * {@link DeviceLifeCycle} when a device is in a specific {@link State}.
     *
     * @param deploymentId The deployment id of the external process
     * @param processId The process id of the external process
     * @return The TransitionBusinessProcess
     */
    public TransitionBusinessProcess enableAsTransitionBusinessProcess(String deploymentId, String processId);

    /**
     * Disables the external business process identified by the specified
     * deploymentId and processId to be executed as part of a
     * {@link DeviceLifeCycle} when a device is in a specific {@link State}.
     * This will fail when there is at least one {@link AuthorizedBusinessProcessAction}
     * that is configured to execute the external business process.
     *
     * @param deploymentId The deployment id of the external process
     * @param processId The process id of the external process
     */
    public void disableAsTransitionBusinessProcess(String deploymentId, String processId);

}
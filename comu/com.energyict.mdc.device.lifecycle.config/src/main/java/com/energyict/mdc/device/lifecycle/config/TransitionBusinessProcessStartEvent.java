package com.energyict.mdc.device.lifecycle.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.fsm.State;

/**
 * Models the event that is produced when a {@link TransitionBusinessProcess} is executed.
 * @see TransitionBusinessProcess#executeOn(long, State)
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-02 (15:50)
 */
@ProviderType
public interface TransitionBusinessProcessStartEvent {

    /**
     * The topic on which this event is published.
     */
    String TOPIC = DeviceLifeCycleConfigurationService.EVENT_NAMESPACE + "/bpm/START";

    /**
     * The deployment id of the external business process.
     *
     * @return The deployment id
     */
    String deploymentId();

    /**
     * The process id of the external business process.
     *
     * @return The process id
     */
    String processId();

    /**
     * The unique identifier of the device for which state is changing.
     *
     * @return The unique identifier
     */
    long deviceId();

    /**
     * The current {@link State} of the device.
     *
     * @return The current State
     */
    State state();

}
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
    public static final String TOPIC = "com/elster/jupiter/fsm/bpm/START";

    /**
     * The deployment id of the external business process.
     *
     * @return The deployment id
     */
    public String deploymentId();

    /**
     * The process id of the external business process.
     *
     * @return The process id
     */
    public String processId();

    /**
     * The unique identifier of the device for which state is changing.
     *
     * @return The unique identifier
     */
    public long deviceId();

    /**
     * The current {@link State} of the device.
     *
     * @return The current State
     */
    public State state();

}
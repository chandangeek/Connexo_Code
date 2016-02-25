package com.elster.jupiter.servicecall;

import aQute.bnd.annotation.ConsumerType;

/**
 * The service call handler is called by the engine when the service call changes state.
 */
@ConsumerType
public interface ServiceCallHandler {

    /**
     * The default implementation returns true, so implementers who never disallow needn't implement this method.
     */
    default boolean allowStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        return true;
    }

    void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState);
}

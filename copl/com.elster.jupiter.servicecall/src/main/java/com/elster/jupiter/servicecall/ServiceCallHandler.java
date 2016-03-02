package com.elster.jupiter.servicecall;

import aQute.bnd.annotation.ConsumerType;

/**
 * The service call handler is called by the engine when the service call changes state.
 */
@ConsumerType
public interface ServiceCallHandler {
    public void onEntry(DefaultState state);

}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import aQute.bnd.annotation.ConsumerType;

/**
 * The service call handler is called by the engine when the service call changes state.
 */
@ConsumerType
public interface ServiceCallHandler {

    default String getDisplayName() {
        return this.getClass().getSimpleName();
    }

    /**
     * The default implementation returns true, so implementers who never disallow needn't implement this method.
     */
    default boolean allowStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        return true;
    }

    /**
     * @param parentServiceCall
     */
    default void onChildStateChange(ServiceCall parentServiceCall, ServiceCall childServiceCall, DefaultState oldState, DefaultState newState) {
        // do nothing by default
    }

    void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState);
}

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

    ServiceCallHandler DUMMY = (serviceCall, oldState, newState) -> {
        // does nothing
    };

    default String getDisplayName() {
        return this.getClass().getSimpleName();
    }

    /**
     * The default implementation returns true, so implementers who never disallow needn't implement this method.
     *
     * @deprecated This method may not always work during Connexo startup, when service call handler is not up yet. Better to use a proper service call lifecycle with required transitions instead
     */
    @Deprecated
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

    default void onCancel(ServiceCall serviceCall) {
    }
}

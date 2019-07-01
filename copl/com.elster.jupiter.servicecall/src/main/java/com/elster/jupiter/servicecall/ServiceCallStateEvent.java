/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ServiceCallStateEvent {

    DefaultState getOldState();

    DefaultState getNewState();

//    /**
//     * Gets the String that uniquely identifies the source of this event.
//     *
//     * @return The unique identifier of the source of this event
//     * @see StateTransitionTriggerEvent#getSourceId()
//     */
//    String getSourceId();
//
//    /**
//     * Gets the String that uniquely identifies the type of the source of this event.
//     *
//     * @return The unique identifier of the type of the source of this event
//     * @see StateTransitionTriggerEvent#getSourceId()
//     */
//    String getSourceType();
//
//    /**
//     * The point in time when the state change is effective.
//     *
//     * @return The point in time when the state change is effective
//     */
//    Instant getEffectiveTimestamp();
//
//    /**
//     * Gets properties that were provided by the {@link StateTransitionTriggerEvent}
//     * that caused the State change.
//     *
//     * @return The properties
//     * @see StateTransitionTriggerEvent#getProperties()
//     */
//    Map<String, Object> getProperties();

}
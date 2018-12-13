/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.events;

/**
 * Categorizes {@link ComServerEvent}s and facilitates
 * client applications to register an interest
 * in all events of a particular category.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-30 (17:03)
 */
public enum Category {

    /**
     * Categorizes all events that relate
     * to physical connections with devices.
     * Examples are:
     * <ul>
     * <li>establishing a connection</li>
     * <li>reading data from a connection</li>
     * <li>writing data on a connection</li>
     * </ul>
     * Note that all events of this category are guaranteed
     * to implement the {@link ConnectionEvent} interface.
     */
    CONNECTION,

    /**
     * Categorize all events that relate
     * to the execution of a communication task.
     */
    COMTASK,

    /**
     * Categorizes all events that relate
     * to the processing of data that was
     * collected during a communication session.
     */
    COLLECTED_DATA_PROCESSING,

    /**
     * Categorizes all events that relate to producing log messages.
     */
    LOGGING;

    public static Category valueOfIgnoreCase (String value) {
        return valueOf(value.toUpperCase());
    }

}
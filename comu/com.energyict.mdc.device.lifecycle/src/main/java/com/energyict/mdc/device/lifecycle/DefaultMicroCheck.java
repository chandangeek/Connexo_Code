/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;

/**
 * Models a number of tiny checks that will be executed
 * by the device life cycle engine to validate
 * that an {@link AuthorizedAction} can be initiated.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (09:04)
 */
public enum DefaultMicroCheck {

    /**
     * Checks that there is a default connection available on the device.
     */
    DEFAULT_CONNECTION_AVAILABLE,

    /**
     * Checks that there is at least one communication task scheduled on the device.
     */
    AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE,

    /**
     * Checks that there is at least one shared communication schedule on the device.
     */
    AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE,

    /**
     * Checks that all loadProfile data
     * has been collected on the device.
     * This holds true if the last reading is set and is
     * before the end of each <i>current</i> LoadProfile period.
     */
    ALL_LOAD_PROFILE_DATA_COLLECTED,

    /**
     * Checks that all data (in both load profiles and registers)
     * that has been collected on the device is also valid.(=No suspects)
     */
    ALL_DATA_VALID,

    /**
     * Checks that all general protocol property values are valid,
     * i.e. will check that all required attributes are specified
     * because the values itself are validated when saved.
     */
    GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,

    /**
     * Checks that all protocol dialect property values are valid,
     * i.e. will check that all required attributes are specified
     * because the values itself are validated when saved.
     */
    PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,

    /**
     * Checks that all security property values are valid,
     * i.e. will check that all required attributes are specified
     * because the values itself are validated when saved.
     */
    SECURITY_PROPERTIES_ARE_ALL_VALID,

    /**
     * Checks that all connection property values are valid,
     * i.e. will check that all required attributes are specified
     * because the values itself are validated when saved.
     */
    CONNECTION_PROPERTIES_ARE_ALL_VALID,

    /**
     * Checks that a slave device is connected to a gateway.
     */
    SLAVE_DEVICE_HAS_GATEWAY,

    /**
     * Checks that the device is linked to a usagepoint.
     */
    LINKED_WITH_USAGE_POINT,

    /**
     * Checks that all issues and alarms that were
     * registered against the device are closed.
     */
    ALL_ISSUES_AND_ALARMS_ARE_CLOSED,

    /**
     * Checks if all the collected data was validated (Validation has run)
     * This holds true if the last reading is equal to
     * the last checked timestamp.
     */
    ALL_DATA_VALIDATED,

    /**
     * Check if at least one connection is available on the device with the status: "Active".
     */
    AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE,

    /**
     * Check if no active service calls exist for the device.
     */
    NO_ACTIVE_SERVICE_CALLS,

    /**
     * Check if no slaves are linked to the multi-element device.
     */
    NO_LINKED_MULTI_ELEMENT_SLAVES,

    /**
     * Checks if the metrology configuration of the usage point is in a correct state (if any)
     */
    METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY,

    /**
     * Checks that there is at least one zone linked to this device.
     */
    AT_LEAST_ONE_ZONE_LINKED,

    /**
     * Check if at least one successful communication task has been executed on the device.
     */
    AT_LEAST_ONE_SUCCESSFUL_COMMUNICATION_TASK_EXECUTED,
}

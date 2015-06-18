package com.energyict.mdc.device.lifecycle.config;

/**
 * Models a number of tiny checks that will be executed
 * by the device life cycle engine to validate
 * that an {@link AuthorizedAction} can be initiated.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (09:04)
 */
public enum MicroCheck {

    /**
     * Checks that there is a default connection available on the device.
     */
    // check bits: 1
    DEFAULT_CONNECTION_AVAILABLE,

    /**
     * Checks that there is at least one communication task scheduled on the device.
     */
    // check bits: 2
    AT_LEAST_ONE_MANUALLY_SCHEDULED_COMMUNICATION_TASK_AVAILABLE,

    /**
     * Checks that there is at least one shared communication schedule on the device.
     */
    // check bits: 4
    AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE,

    /**
     * Checks that all loadProfile data
     * has been collected on the device.
     * This holds true if the last reading is set and is
     * before the end of each <i>current</i> LoadProfile period.
     */
    // check bits: 8
    ALL_LOADPROFILE_DATA_COLLECTED,

    /**
     * Checks that all data (in both load profiles and registers)
     * that has been collected on the device is also valid.
     */
    // check bits: 16
    ALL_DATA_VALID,

    /**
     * Checks that all general protocol property values are valid,
     * i.e. will check that all required attributes are specified
     * because the values itself are validated when saved.
     */
    // check bits: 32
    GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,

    /**
     * Checks that all protocol dialect property values are valid,
     * i.e. will check that all required attributes are specified
     * because the values itself are validated when saved.
     */
    // check bits: 64
    PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,

    /**
     * Checks that all security property values are valid,
     * i.e. will check that all required attributes are specified
     * because the values itself are validated when saved.
     */
    // check bits: 128
    SECURITY_PROPERTIES_ARE_ALL_VALID,

    /**
     * Checks that all connection property values are valid,
     * i.e. will check that all required attributes are specified
     * because the values itself are validated when saved.
     */
    // check bits: 256
    CONNECTION_PROPERTIES_ARE_ALL_VALID,

    /**
     * Checks that a slave device is connected to a gateway.
     */
    // check bits: 512
    SLAVE_DEVICE_HAS_GATEWAY,

    /**
     * Checks that the device is linked to a usagepoint.
     */
    // check bits: 1024
    LINKED_WITH_USAGE_POINT,

    /**
     * Checks that all issues and alarms that were
     * registered against the device are closed.
     */
    // check bits: 2048
    ALL_ISSUES_AND_ALARMS_ARE_CLOSED,

    /**
     * Checks if all the collected data was validated
     * that has been collected on the device is also valid.
     * This holds true if the last reading is equal to
     * the last checked timestamp.
     */
    // check bits: 4096
    ALL_DATA_VALIDATED,

    /**
     * Check if at least one connection is available on the device with the status: "Active".
     */
    // check bits: 8192
    AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE;


}
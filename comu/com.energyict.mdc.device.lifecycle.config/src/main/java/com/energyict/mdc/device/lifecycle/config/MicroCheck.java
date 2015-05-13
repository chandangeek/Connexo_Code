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
    DEFAULT_CONNECTION_AVAILABLE,

    /**
     * Checks that there is at least one communication task scheduled on the device.
     */
    AT_LEAST_ONE_MANUALLY_SCHEDULED_COMMUNICATION_TASK_AVAILABLE,

    /**
     * Checks that there is at least one shared communication schedule on the device.
     */
    AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE,

    /**
     * Checks that the last reading timestamp is set on all
     * load profiles and registers of the device.
     */
    LAST_READING_TIMESTAMP_SET,

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
    ALL_ISSUES_AND_ALARMS_ARE_CLOSED;

}
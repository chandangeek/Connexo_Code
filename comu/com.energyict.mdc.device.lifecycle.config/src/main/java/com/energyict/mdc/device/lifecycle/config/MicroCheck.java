/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    DEFAULT_CONNECTION_AVAILABLE(MicroCategory.COMMUNICATION),

    /**
     * Checks that there is at least one communication task scheduled on the device.
     */
    AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(MicroCategory.COMMUNICATION),

    /**
     * Checks that there is at least one shared communication schedule on the device.
     */
    AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE(MicroCategory.COMMUNICATION),

    /**
     * Checks that all general protocol property values are valid,
     * i.e. will check that all required attributes are specified
     * because the values itself are validated when saved.
     */
    GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID(MicroCategory.COMMUNICATION),

    /**
     * Checks that all protocol dialect property values are valid,
     * i.e. will check that all required attributes are specified
     * because the values itself are validated when saved.
     */
    PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID(MicroCategory.COMMUNICATION),

    /**
     * Checks that all security property values are valid,
     * i.e. will check that all required attributes are specified
     * because the values itself are validated when saved.
     */
    SECURITY_PROPERTIES_ARE_ALL_VALID(MicroCategory.COMMUNICATION),

    /**
     * Checks that all connection property values are valid,
     * i.e. will check that all required attributes are specified
     * because the values itself are validated when saved.
     */
    CONNECTION_PROPERTIES_ARE_ALL_VALID(MicroCategory.COMMUNICATION),

    /**
     * Checks that a slave device is connected to a gateway.
     */
    SLAVE_DEVICE_HAS_GATEWAY(MicroCategory.TOPOLOGY),

    /**
     * Checks that the device is linked to a usagepoint.
     */
    LINKED_WITH_USAGE_POINT(MicroCategory.INSTALLATION),


    /**
     * Check if no active service calls exist for the device.
     */
    NO_ACTIVE_SERVICE_CALLS(MicroCategory.MONITORING),

    /**
     * Check if no slaves are linked to the multi-element device.
     */
    NO_LINKED_MULTI_ELEMENT_SLAVES(MicroCategory.MULTIELEMENT),
    /**
     * Checks if the metrology configuration of the usage point is in a correct state (if any)
     */
    METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY(MicroCategory.INSTALLATION);

    private MicroCategory category;

    MicroCheck(MicroCategory category) {
        this.category = category;
    }

    public MicroCategory getCategory() {
        return category;
    }

}
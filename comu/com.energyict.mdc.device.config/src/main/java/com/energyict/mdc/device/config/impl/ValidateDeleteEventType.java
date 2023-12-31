/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.device.config.EventType;

/**
 * Subset of {@link EventType}s that relate to validation of deletion of persistent objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-09 (13:40)
 */
public enum ValidateDeleteEventType {

    COMTASKENABLEMENT(EventType.COMTASKENABLEMENT_VALIDATEDELETE),
    PROTOCOLCONFIGURATIONPROPS(EventType.PROTOCOLCONFIGURATIONPROPS_VALIDATEDELETE),
    PARTIAL_INBOUND_CONNECTION_TASK(EventType.PARTIAL_INBOUND_CONNECTION_TASK_VALIDATE_DELETE),
    PARTIAL_SCHEDULED_CONNECTION_TASK(EventType.PARTIAL_SCHEDULED_CONNECTION_TASK_VALIDATE_DELETE),
    PARTIAL_CONNECTION_INITIATION_TASK(EventType.PARTIAL_CONNECTION_INITIATION_TASK_VALIDATE_DELETE),
    ;

    private EventType eventType;

    ValidateDeleteEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String topic() {
        return this.eventType.topic();
    }

}
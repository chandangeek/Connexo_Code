/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

/**
 * Adds functionality to a DeviceMessage which should not be in the public API
 *
 *
 * Date: 11/5/14
 * Time: 10:57 AM
 */
public interface ServerDeviceMessage extends DeviceMessage {

    /**
     * Moves the DeviceMessage to the new status.
     *
     * @param status the new DeviceMessageStatus
     */
    void moveTo(DeviceMessageStatus status);

    /**
     * Cancels/revokes this DeviceMessage without notify updated.
     * This is required to revoke messages created with LimitsExceededForCommandException
     * to not decrement it if ot was not incremented during message creation.
     */
    void revokeNotNotifyUpdated();

}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.energyict.mdc.common.device.config.ComTaskEnablement;

/**
 * Defines a QueueMessage containing the id of the {@link ComTaskEnablement}
 *
 * @author sva
 * @since 2016-01-25 - 11:51
 */
public class ItemizeComTaskEnablementQueueMessage implements QueueMessage {

    public long comTaskEnablementId;

    @SuppressWarnings("unused")
    public ItemizeComTaskEnablementQueueMessage() {
    }

    public ItemizeComTaskEnablementQueueMessage(long comTaskEnablementId) {
        this.comTaskEnablementId = comTaskEnablementId;
    }
}
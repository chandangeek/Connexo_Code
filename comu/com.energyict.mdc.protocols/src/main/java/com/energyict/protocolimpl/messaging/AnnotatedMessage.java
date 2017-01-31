/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.messaging;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;

public interface AnnotatedMessage {

    /**
     * @return The matching message entry that is represented by this annotated message
     */
    MessageEntry getMessageEntry();

    /**
     * @return The matching RtuMessageDescription that was present on the annotated message
     */
    RtuMessageDescription getRtuMessageDescription();

}

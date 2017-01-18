package com.energyict.protocolimpl.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;

/**
 * Copyrights EnergyICT
 * Date: 6/29/12
 * Time: 8:37 AM
 */
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

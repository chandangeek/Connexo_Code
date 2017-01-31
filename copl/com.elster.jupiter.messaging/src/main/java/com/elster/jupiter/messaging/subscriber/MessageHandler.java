/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.subscriber;

import com.elster.jupiter.messaging.Message;


public interface MessageHandler {

    void process(Message message);

    /**
     * the message handler may do additional processing here that falls outside the Transaction that dequeued the message
     */
    default void onMessageDelete(Message message) {
    }
}

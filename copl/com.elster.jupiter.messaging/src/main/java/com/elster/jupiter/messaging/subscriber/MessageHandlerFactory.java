/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.subscriber;

public interface MessageHandlerFactory {

    MessageHandler newMessageHandler();
}

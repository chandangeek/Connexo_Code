package com.elster.jupiter.messaging.subscriber;

import com.elster.jupiter.messaging.Message;

public interface MessageHandler {

    void process(Message message);
}

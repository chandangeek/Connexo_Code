package com.elster.jupiter.messaging;

import org.joda.time.Seconds;

public interface MessageBuilder {

    void send();

    MessageBuilder expiringAfter(Seconds seconds);

}

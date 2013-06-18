package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.util.time.Clock;

public interface ServiceLocator {

    Clock getClock();

    MessageService getMessageService();
}

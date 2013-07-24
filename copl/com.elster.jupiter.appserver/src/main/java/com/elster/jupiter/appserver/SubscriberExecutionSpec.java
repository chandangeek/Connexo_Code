package com.elster.jupiter.appserver;

import com.elster.jupiter.messaging.SubscriberSpec;

/**
 * Copyrights EnergyICT
 * Date: 23/07/13
 * Time: 10:12
 */
public interface SubscriberExecutionSpec {

    int getThreadCount();

    SubscriberSpec getSubscriberSpec();

    AppServer getAppServer();
}

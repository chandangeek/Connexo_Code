/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver;

import com.elster.jupiter.messaging.SubscriberSpec;

public interface SubscriberExecutionSpec {

    int getThreadCount();

    SubscriberSpec getSubscriberSpec();

    AppServer getAppServer();

    void setActive(boolean active);

    boolean isActive();

}

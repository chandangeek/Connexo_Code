/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.rest.impl;

import java.util.List;

public class DestinationSpecInfo {

    public String name;
    public DestinationType type;
    public boolean active;
    public boolean buffered;
    public int retryDelayInSeconds;
    public int numberOfRetries;
    public long version;
    public Long numberOfMessages;
    public Long numberOFErrors;
    public List<SubscriberSpecInfo> subscriberSpecInfos;

}

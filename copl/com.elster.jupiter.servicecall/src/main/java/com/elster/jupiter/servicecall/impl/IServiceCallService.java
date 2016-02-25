package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.servicecall.ServiceCallService;

public interface IServiceCallService extends ServiceCallService {
    DestinationSpec getServiceCallQueue();
}

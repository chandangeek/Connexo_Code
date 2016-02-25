package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.servicecall.ServiceCallService;

/**
 * Copyrights EnergyICT
 * Date: 25/02/2016
 * Time: 10:32
 */
public interface IServiceCallService extends ServiceCallService {
    DestinationSpec getServiceCallQueue();
}

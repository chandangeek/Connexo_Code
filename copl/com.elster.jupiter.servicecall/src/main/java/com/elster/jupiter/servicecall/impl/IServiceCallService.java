/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;

import java.util.Map;
import java.util.Optional;

/**
 * Internal usage interface
 * Created by bvn on 2/26/16.
 */
public interface IServiceCallService extends ServiceCallService {
    void addServiceCallHandler(ServiceCallHandler serviceCallHandler, Map<String, Object> properties);

    void removeServiceCallHandler(ServiceCallHandler serviceCallHandler, Map<String, Object> properties);

    Optional<DestinationSpec> getServiceCallQueue(String destinationName);

    Thesaurus getThesaurus();
}

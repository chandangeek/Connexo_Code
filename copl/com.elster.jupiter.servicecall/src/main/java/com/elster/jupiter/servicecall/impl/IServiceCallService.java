package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;

import java.util.Map;

/**
 * Internal usage interface
 * Created by bvn on 2/26/16.
 */
public interface IServiceCallService extends ServiceCallService {
    void addServiceCallHandler(ServiceCallHandler serviceCallHandler, Map<String, Object> properties);

    void removeServiceCallHandler(ServiceCallHandler serviceCallHandler, Map<String, Object> properties);
}

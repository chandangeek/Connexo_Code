package com.elster.jupiter.servicecalls;

import java.util.Optional;

/**
 * Created by bvn on 2/4/16.
 */
public interface ServiceCallService {

    String COMPONENT_NAME = "SCS";

    public Optional<ServiceCallLifeCycle> getServiceCallLifeCycle(String name);

//    public ServiceCallType createServiceCallType();
}

package com.elster.jupiter.servicecall;

import com.elster.jupiter.domain.util.Finder;

import java.util.List;
import java.util.Optional;

/**
 * Created by bvn on 2/4/16.
 */
public interface ServiceCallService {

    String COMPONENT_NAME = "SCS";

    public Finder<ServiceCallLifeCycle> getServiceCallLifeCycles();
    public Optional<ServiceCallLifeCycle> getServiceCallLifeCycle(String name);
    public Optional<ServiceCallLifeCycle> getDefaultServiceCallLifeCycle();
    public ServiceCallLifeCycle createServiceCallLifeCycle(String name);
    public Finder<ServiceCallType> getServiceCallTypes();
    public ServiceCallType createServiceCallType(String name);

//    public ServiceCallType createServiceCallType();
}

package com.elster.jupiter.servicecall;

import com.elster.jupiter.domain.util.Finder;

import java.util.List;
import java.util.Optional;

/**
 * Created by bvn on 2/4/16.
 */
public interface ServiceCallService {

    String COMPONENT_NAME = "SCS";

    /**
     * Get all known service call life cycles in tghe system, support paging.
     * @return Finder
     */
    public Finder<ServiceCallLifeCycle> getServiceCallLifeCycles();

    /**
     * Return a service call life cycle identified by the name
     * @param name
     * @return Optional life cycle
     */
    public Optional<ServiceCallLifeCycle> getServiceCallLifeCycle(String name);

    /**
     * Returns the default service call life cycle. This default model is installed upon bundle init.
     * @return Will return empty if no init has been done yet
     */
    public Optional<ServiceCallLifeCycle> getDefaultServiceCallLifeCycle();
    public ServiceCallLifeCycle createServiceCallLifeCycle(String name);

    /**
     * Returns list of known service call types. This method supports paging.
     * @return Finder
     */
    public Finder<ServiceCallType> getServiceCallTypes();

    /**
     * Creates a new servic call type. This method start a builder.
     * @param name
     * @return
     */
    public ServiceCallService.ServiceCallTypeBuilder createServiceCallType(String name,String versionName);

    interface ServiceCallTypeBuilder {

    }

}

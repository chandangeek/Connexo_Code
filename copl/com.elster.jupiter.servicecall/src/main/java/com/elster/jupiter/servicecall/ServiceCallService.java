package com.elster.jupiter.servicecall;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Finder;

import java.util.Collection;
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

    public ServiceCallLifeCycleBuilder createServiceCallLifeCycle(String name);

    /**
     * Returns list of known service call types. This method supports paging.
     * @return Finder
     */
    public Finder<ServiceCallType> getServiceCallTypes();

    /**
     * Creates a new service call type, using provided name, version and life cycle. This method start a builder.
     * @param name
     * @return
     */
    public ServiceCallService.ServiceCallTypeBuilder createServiceCallType(String name,String versionName, ServiceCallLifeCycle serviceCallLifeCycle);

    /**
     * Creates a new service call type, using provided name and version. The default life cycle is used. This method start a builder.
     * @param name
     * @return
     */
    default public ServiceCallService.ServiceCallTypeBuilder createServiceCallType(String name,String versionName) {
        return createServiceCallType(name, versionName, getDefaultServiceCallLifeCycle().get());
    }

    /**
     * Fetch a service call type by name and version name.
     * @param name
     * @param versionName This is the named version of the type. Not to be confused with the concurrency version. This is version as applied by the user.
     * @return
     */
    Optional<ServiceCallType> findServiceCallType(String name, String versionName);

    /**
     * Fetch and lock a service call type by name, version name and version
     * @param id The id of the service call type
     * @param version This is the version number used for concurrency checks.
     * @return
     */
    Optional<ServiceCallType> findAndLockServiceCallType(long id, long version);

    Collection<String> findAllHandlers();

    interface ServiceCallTypeBuilder {
        public ServiceCallTypeBuilder logLevel(LogLevel logLevel);
        public ServiceCallTypeBuilder customPropertySet(RegisteredCustomPropertySet customPropertySet);
        public ServiceCallType add();
    }

}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.domain.util.Finder;

import aQute.bnd.annotation.ProviderType;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface ServiceCallService {

    String COMPONENT_NAME = "SCS";

    /**
     * Get all known service call life cycles in tghe system, support paging.
     * @return Finder
     */
    Finder<ServiceCallLifeCycle> getServiceCallLifeCycles();

    /**
     * Return a service call life cycle identified by the name
     * @param name
     * @return Optional life cycle
     */
    Optional<ServiceCallLifeCycle> getServiceCallLifeCycle(String name);

    /**
     * Returns the default service call life cycle. This default model is installed upon bundle init.
     * @return Will return empty if no init has been done yet
     */
    Optional<ServiceCallLifeCycle> getDefaultServiceCallLifeCycle();

    ServiceCallLifeCycleBuilder createServiceCallLifeCycle(String name);

    /**
     * Returns list of known service call types. This method supports paging.
     * @return Finder
     */
    Finder<ServiceCallType> getServiceCallTypes();

    /**
     * Creates a new service call type, using provided name, version and life cycle. This method start a builder.
     * @param name
     * @return
     */
    ServiceCallTypeBuilder createServiceCallType(String name, String versionName, ServiceCallLifeCycle serviceCallLifeCycle);

    /**
     * Creates a new service call type, using provided name and version. The default life cycle is used. This method start a builder.
     * @param name
     * @return
     */
    default public ServiceCallTypeBuilder createServiceCallType(String name, String versionName) {
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

    /**
     * Finds and returns a service call with the given id, if it exists
     *
     * @param id The id of the service call
     * @return The optional service call
     */
    Optional<ServiceCall> getServiceCall(long id);

    /**
     * Returns a finder which allows you to filter the found service calls
     *
     * @param serviceCallFilter a filter for servicecalls
     * @return Finder
     */
    Finder<ServiceCall> getServiceCallFinder(ServiceCallFilter serviceCallFilter);

    /**
     * Returns a finder for servicecalls
     *
     * @return Finder
     */
    Finder<ServiceCall> getServiceCallFinder();

    /**
     * Returns information about the status of the children in a given service call
     *
     * @param id The unique id that identifies the service call
     * @return Map of the names of the states, with their respective percentage
     */
    Map<DefaultState, Long> getChildrenStatus(long id);

    /**
     * Returns a list of names of all known service call handlers in the system
     */
    Collection<String> findAllHandlers();

    /**
     * Returns the service call handler identified by the name. Empty if none is found
     *
     * @param handler Service call handler name
     */
    Optional<ServiceCallHandler> findHandler(String handler);

    String getDisplayName(DefaultState state);

    Set<ServiceCall> findServiceCalls(Object targetObject, Set<DefaultState> inState);

    void cancelServiceCallsFor(Object target);

    /**
     * Registers the specified {@link ServiceCallHandler} on this service's whiteboard.
     *
     * @param serviceCallHandler
     * @param properties
     */
    void addServiceCallHandler(ServiceCallHandler serviceCallHandler, Map<String, Object> properties);

    Set<DefaultState> nonFinalStates();
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Provides services that relate to searching.
 * All searches are described with a meta-layer API.
 * The descriptive elements for this API are provided
 * by {@link SearchDomain}s that can register with
 * this service. In fact, the registration is done
 * automatically by the OSGi framework.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (13:14)
 */
public interface SearchService {

    String COMPONENT_NAME = "JSM";

    /**
     * Registers the {@link SearchDomain} with this SearchService.
     *
     * @param searchDomain The SearchDomain
     */
    void register(SearchDomain searchDomain);

    void register(SearchDomainExtension searchDomainExtension);

    /**
     * Unregisters the {@link SearchDomain} with this SearchService.
     *
     * @param searchDomain The SearchDomain
     */
    void unregister(SearchDomain searchDomain);

    void unregister(SearchDomainExtension searchDomainExtension);

    /**
     * Gets all the registered {@link SearchDomain}s.
     *
     * @return The List of SearchDomain
     */
    List<SearchDomain> getDomains();

    /**
     * Gets all the registered {@link SearchDomain}s
     * that are targetted to be used by the specified application.
     *
     * @return The List of SearchDomain
     * @see SearchDomain#targetApplications()
     */
    List<SearchDomain> getDomains(String application);

    /**
     * Finds the registered {@link SearchDomain}
     * with the specified identifier.
     *
     * @param id The identifier
     * @return The SearchDomain
     * @see SearchDomain#getId()
     */
    Optional<SearchDomain> findDomain(String id);

    /**
     * Finds the registered {@link SearchDomain}
     * with the specified identifier. Blocks and waits until such search domain is registered,
     * or until the timeout has passed.
     *
     * @param id The identifier
     * @param timeout The timeout
     * @return The SearchDomain
     */
    Optional<SearchDomain> pollDomain(String id, Duration timeout) throws InterruptedException;

    /**
     * Starts the building process of a search for instances of the specified domain class.
     * Will throw an IllegalArgumentException if no registered {@link SearchDomain}
     * supports the specified domain class.
     * <p>
     * Example code:
     * <blockquote><pre>
     * searchService
     *     .search(Device.class)
     *     .where("mRID").isEqualTo(mRID)
     *     .and("statusName").in(
     *         DefaultState.IN_STOCK.getKey(),
     *         DefaultState.DECOMMISSIONED.getKey())
     *     .and("deviceConfigId").isEqualTo(97L)
     *     .complete()
     *     .paged(1, 100)
     *     .sorted("statusName", true) // Sorts ascending on name of the device state
     *     .stream()
     *     .map(Device::getmRID)
     *     .collect(Collectors.joining(", "));
     * </pre></blockquote>
     * </p>
     *
     * @param domainClass The domain class
     * @return The SearchBuilder
     * @throws IllegalArgumentException
     * @see SearchDomain#getDomainClass()
     */
    @SuppressWarnings("unchecked")
    default <T> SearchBuilder<T> search(Class<T> domainClass) {
        SearchDomain domain = getDomains()
                .stream()
                .filter(p -> p.getDomainClass().isAssignableFrom(domainClass))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No registered domain for class " + domainClass.getName()));
        return (SearchBuilder<T>) search(domain);
    }

    /**
     * Starts the building process of a search for instances of the specified {@link SearchDomain}.
     * Note that this will throw an IllegalArgumentException when
     * the SearchDomain was not registered first.
     *
     * @param searchDomain The SearchDomain
     * @return The SearchBuilder
     */
    SearchBuilder<Object> search(SearchDomain searchDomain);

}
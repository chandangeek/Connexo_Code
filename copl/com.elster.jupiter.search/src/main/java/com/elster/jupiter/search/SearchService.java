package com.elster.jupiter.search;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.util.conditions.Condition;

import java.util.List;

/**
 * Provides services that relate to searching.
 * All searches are described with a meta-layer API.
 * The descriptive elements for this API are provided
 * by {@link SearchProvider}s that can register with
 * this service. In fact, the registration is done
 * automatically by the OSGi framework.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (13:14)
 */
public interface SearchService {

    public static String COMPONENT_NAME = "JSM";

    /**
     * Registers the {@link SearchProvider} with this SearchService.
     *
     * @param searchProvider The SearchProvider
     */
    public void register(SearchProvider searchProvider);

    /**
     * Unregisters the {@link SearchProvider} with this SearchService.
     *
     * @param searchProvider The SearchProvider
     */
    public void unregister(SearchProvider searchProvider);

    /**
     * Gets all the registered {@link SearchProvider}s.
     *
     * @return The List of SearchProvider
     */
    public List<SearchProvider> getProviders();

    /**
     * Starts the building process of a search for instances of the specified domain class.
     * Will throw an IllegalArgumentException if no registered {@link SearchProvider}
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
     * @see SearchProvider#supports(Class)
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public default <T> SearchBuilder<T> search(Class<T> domainClass) {
        SearchProvider searchProvider = getProviders()
                .stream()
                .filter(p -> p.supports(domainClass))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No registered provider for domain class " + domainClass.getName()));
        SearchDomain domain = searchProvider
                .getDomains()
                .stream()
                .filter(d -> d.supports(domainClass))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No registered provider for domain class " + domainClass.getName()));
        return (SearchBuilder<T>) search(domain);
    }

    /**
     * Starts the building process of a search for instances of the specified {@link SearchDomain}.
     *
     * @param searchDomain The SearchDomain
     * @return The SearchBuilder
     */
    public SearchBuilder<Object> search(SearchDomain searchDomain);

    @SuppressWarnings("unchecked")
    @Deprecated
    public default <T> Finder<T> search(Class<T> domainClass, Condition condition) {
        SearchProvider searchProvider = getProviders()
                .stream()
                .filter(p -> p.supports(domainClass))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No registered provider for domain class " + domainClass.getName()));
        SearchDomain domain = searchProvider
                .getDomains()
                .stream()
                .filter(d -> d.supports(domainClass))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No registered provider for domain class " + domainClass.getName()));
        return (Finder<T>) search(domain, condition);
    }

    @Deprecated
    public Finder<Object> search(SearchDomain searchDomain, Condition condition);

}
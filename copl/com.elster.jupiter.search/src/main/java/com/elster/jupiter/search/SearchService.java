package com.elster.jupiter.search;

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

}
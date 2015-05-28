package com.elster.jupiter.search;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-26 (13:20)
 */
@ConsumerType
public interface SearchProvider {

    public List<SearchDomain> getDomains();

    /**
     * Tests if this SearchProvider supports
     * searches for the specified domain class.
     * In other words, tests if this SearchProvider
     * searches for objects of the specified java class.
     * This is typically the case when at least one
     * of its {@link SearchDomain}s supports the domain class.
     *
     * @param domainClass The domain class
     * @return true iff this SearchProvider searches for objects of the specified java class
     */
    public default boolean supports(Class domainClass) {
        return getDomains()
                .stream()
                .filter(domain -> domain.supports(domainClass))
                .findAny()
                .isPresent();
    }

}
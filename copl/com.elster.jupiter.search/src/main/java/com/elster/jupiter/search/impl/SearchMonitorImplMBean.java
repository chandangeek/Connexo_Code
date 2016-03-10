package com.elster.jupiter.search.impl;

import com.elster.jupiter.search.SearchDomain;

import javax.management.openmbean.CompositeData;

/**
 * Defines the interface for the JMX layer
 * for a component that will monitor the
 * searches that are being executed on the Connexo platform.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-08 (16:28)
 */
public interface SearchMonitorImplMBean {

    /**
     * Gets the ExecutionStatistics (in nanos) of the searches
     * that have been executed across al {@link SearchDomain}s.
     *
     * @return The ExecutionStatistics
     */
    CompositeData getSearchExecutionStatisticsCompositeData();

    /**
     * Gets the ExecutionStatistics (in nanos) of the searches
     * that have been executed across al {@link SearchDomain}s.
     *
     * @param searchDomainId The SearchDomain's unique identifier
     * @return The ExecutionStatistics
     */
    CompositeData getSearchExecutionStatisticsCompositeData(String searchDomainId);

    /**
     * Gets the ExecutionStatistics (in nanos) of the requests
     * to count the number of results returned by a search
     * against any {@link SearchDomain}.
     *
     * @return The ExecutionStatistics
     */
    CompositeData getCountExecutionStatisticsCompositeData();

    /**
     * Gets the ExecutionStatistics (in nanos) of the requests
     * to count the number of results returned by a search
     * against the {@link SearchDomain}.
     *
     * @param searchDomainId The SearchDomain's unique identifier
     * @return The ExecutionStatistics
     */
    CompositeData getCountExecutionStatisticsCompositeData(String searchDomainId);

}
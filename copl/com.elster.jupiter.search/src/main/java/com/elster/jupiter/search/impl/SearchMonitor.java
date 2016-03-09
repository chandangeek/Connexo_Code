package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.search.SearchDomain;

/**
 * Defines the interface for a component that will monitor the
 * searches that are being executed on the Connexo platform.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-09 (08:48)
 */
public interface SearchMonitor extends SearchMonitorImplMBean {

    /**
     * Notifies this component that a search against the specified {@link SearchDomain}
     * has been executed and completed in the specified number of nano seconds.
     * One search is defined as a single call to {@link Finder#find()}.
     *
     * @param searchDomain The SearchDomain
     * @param nanos The number of nano seconds it took to execute the search
     */
    void searchExecuted(SearchDomain searchDomain, long nanos);

    /**
     * Notifies this component that a request to count the number of
     * matches of a search against the specified {@link SearchDomain}
     * completed in the specified number of nano seconds.
     *
     * @param searchDomain The SearchDomain
     * @param nanos The number of nano seconds it took to execute the search
     * @see Finder#count()
     */
    void countExecuted(SearchDomain searchDomain, long nanos);

    /**
     * Gets the ExecutionStatistics (in nanos) of the searches
     * that have been executed across al {@link SearchDomain}s.
     *
     * @return The ExecutionStatistics
     */
    ExecutionStatistics getSearchExecutionStatistics();

    /**
     * Gets the ExecutionStatistics (in nanos) of the searches
     * that have been executed across al {@link SearchDomain}s.
     *
     * @param searchDomain The SearchDomain
     * @return The ExecutionStatistics
     */
    ExecutionStatistics getSearchExecutionStatistics(SearchDomain searchDomain);

    /**
     * Gets the ExecutionStatistics (in nanos) of the requests
     * to count the number of results returned by a search
     * against any {@link SearchDomain}.
     *
     * @return The ExecutionStatistics
     */
    ExecutionStatistics getCountExecutionStatistics();

    /**
     * Gets the ExecutionStatistics (in nanos) of the requests
     * to count the number of results returned by a search
     * against the {@link SearchDomain}.
     *
     * @param searchDomain The SearchDomain
     * @return The ExecutionStatistics
     */
    ExecutionStatistics getCountExecutionStatistics(SearchDomain searchDomain);

    interface ExecutionStatistics {
        long getCount();
        long getTotal();
        long getMinimum();
        long getMaximum();
        long getAverage();
    }

}
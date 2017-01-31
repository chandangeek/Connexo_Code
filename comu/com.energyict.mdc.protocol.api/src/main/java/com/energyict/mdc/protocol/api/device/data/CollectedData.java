/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.issues.Issue;

import java.util.List;

/**
 * Provides basic functionality for a data object which is collected from a Device.
 */
public interface CollectedData {

    /**
     * @return the unsupported type
     */
    ResultType getResultType();

    /**
     * @return additional information about why the data is not complete
     */
    List<Issue> getIssues();

    /**
     * Set all failure information.
     *
     * @param resultType indication of what the resultType is
     * @param issue      indication of what the issue is
     */
    void setFailureInformation(ResultType resultType, Issue issue);

    /**
     * Tests if this type of CollectedData is configured
     * to be collected by the specified {@link DataCollectionConfiguration}.
     *
     * @param configuration The ComTask
     * @return A flag that indicates if the ComTask is configured
     *         to collect this type of CollectedData
     */
    boolean isConfiguredIn(DataCollectionConfiguration configuration);

}
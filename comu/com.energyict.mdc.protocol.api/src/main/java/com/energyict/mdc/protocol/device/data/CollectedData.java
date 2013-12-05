package com.energyict.mdc.protocol.device.data;

import com.energyict.mdc.issues.Issue;

import java.util.List;

/**
 * Provides basic functionality for a data object which is collected from a Device.
 */
public interface CollectedData {

    /**
     * @return the unsupported type
     */
    public ResultType getResultType();

    /**
     * @return additional information about why the data is not complete
     */
    public List<Issue> getIssues();

    /**
     * Set all failure information.
     *
     * @param resultType indication of what the resultType is
     * @param issue      indication of what the issue is
     */
    public void setFailureInformation(ResultType resultType, Issue issue);

    /**
     * Set all failure information.
     *
     * @param source The object that caused the problem
     * @param description A description that can be translated (can contain an optional pattern to put in some arguments)
     * @param arguments Additional arguments to put into the description
     */
    public void setFailureInformation(ResultType resultType, Object source, String description, Object... arguments);

    /**
     * Tests if this type of CollectedData is configured
     * to be collected by the specified {@link DataCollectionConfiguration}.
     *
     * @param configuration The ComTask
     * @return A flag that indicates if the ComTask is configured
     *         to collect this type of CollectedData
     */
    public boolean isConfiguredIn(DataCollectionConfiguration configuration);

}
package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;
import com.energyict.mdc.upl.issue.Issue;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Provides basic functionality for a data object which is collected from a Device.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
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
     * Set all failure information
     *
     * @param resultType indication of what the resultType is
     * @param issue      indication of what the issue is
     */
    void setFailureInformation(ResultType resultType, Issue issue);

    /**
     * Set all failure information
     *
     * @param resultType indication of what the resultType is
     * @param issues     indication of what the issues are
     */
    void setFailureInformation(ResultType resultType, List<Issue> issues);

    /**
     * Tests if this type of CollectedData is configured
     * to be collected by the specified {@link DataCollectionConfiguration}.
     *
     * @param comTask The DataCollectionConfiguration
     * @return A flag that indicates if the DataCollectionConfiguration is configured
     * to collect this type of CollectedData
     */
    boolean isConfiguredIn(DataCollectionConfiguration comTask);

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    default String getXmlType() {
        return this.getClass().getName();
    }

    default void setXmlType(String ignore) {
    }

}
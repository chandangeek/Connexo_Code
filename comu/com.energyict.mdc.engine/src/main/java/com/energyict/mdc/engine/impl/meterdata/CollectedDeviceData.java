package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.ResultType;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class containing standard functionality for a {@link com.energyict.mdc.protocol.api.device.data.CollectedData} object.
 *
 * @author gna
 * @since 4/04/12 - 14:41
 */
public abstract class CollectedDeviceData implements ServerCollectedData {

    /**
     * Indication for problems/warnings during the readout of the data.
     */
    private List<Issue> issueList = new ArrayList<>();

    /**
     * Indication of the <i>type</i> of the result.
     */
    private ResultType resultType = ResultType.Supported;

    public CollectedDeviceData() {
        super();
    }

    /**
     * Set all failure information.
     *
     * @param resultType indication of what the result is
     * @param issue The Issue
     */
    public void setFailureInformation(final ResultType resultType, final Issue issue) {
        if (resultType == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "setFailureInformation", "resultType");
        } else if (issue == null){
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "setFailureInformation", "issue");
        }
        this.resultType = resultType;
        addIssue(issue);
    }

    private void addIssue(Issue issue) {
        this.issueList.add(issue);
    }

    public void setFailureInformation(final ResultType resultType, final List<Issue> issues) {
        if (resultType == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "setFailureInformation", "resultType");
        } else if (issues == null || issues.isEmpty()){
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "setFailureInformation", "issues");
        }
        this.resultType = resultType;
        addAllIssues(issues);
    }

    private void addAllIssues(List<Issue> issues) {
        this.issueList.addAll(issues);
    }

    @Override
    public ResultType getResultType() {
        return this.resultType;
    }

    @Override
    public List<Issue> getIssues() {
        return this.issueList;
    }

    @Override
    public void postProcess (ConnectionTask connectionTask) {
        // No post processing by default
    }

}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.IssueFactoryImpl;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.issues.impl.ProblemImpl;
import com.energyict.mdc.issues.impl.WarningImpl;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class containing standard functionality for a {@link com.energyict.mdc.upl.meterdata.CollectedData} object.
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
    private ComTaskExecution comTaskExecution;

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
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "setFailureInformation", "resultType", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        } else if (issue == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "setFailureInformation", "issue", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        this.resultType = resultType;
        addIssue(issue);
    }

    private void addIssue(Issue issue) {
        this.issueList.add(issue);
    }

    public void setFailureInformation(final ResultType resultType, final List<Issue> issues) {
        if (resultType == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "setFailureInformation", "resultType", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        } else if (issues == null || issues.isEmpty()){
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "setFailureInformation", "issues", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
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
    @XmlElements( {
            @XmlElement(type = WarningImpl.class),
            @XmlElement(type = ProblemImpl.class),
            @XmlElement(type = IssueFactoryImpl.Warning.class),
            @XmlElement(type = IssueFactoryImpl.Problem.class),
    })
    public List<Issue> getIssues() {
        return this.issueList;
    }

    @Override
    public void postProcess (ConnectionTask connectionTask) {
        // No post processing by default
    }

    @JsonIgnore
    @XmlTransient
    public ComTaskExecution getComTaskExecution() {
        return comTaskExecution;
    }

    @Override
    public void injectComTaskExecution(ComTaskExecution comTaskExecution) {
        this.comTaskExecution = comTaskExecution;
    }

}
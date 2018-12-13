package com.elster.jupiter.issue.rest.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by H251853 on 7/27/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SetPriorityIssueRequest extends BulkIssueRequest {
    public String priority;
}

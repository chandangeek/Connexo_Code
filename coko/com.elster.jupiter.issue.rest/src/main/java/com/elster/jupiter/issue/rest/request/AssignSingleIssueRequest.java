package com.elster.jupiter.issue.rest.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignSingleIssueRequest extends SingleIssueRequest {
    public AssigneeReference assignee;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssigneeReference {
        public long userId;
        public long workGroupId;

    }
}

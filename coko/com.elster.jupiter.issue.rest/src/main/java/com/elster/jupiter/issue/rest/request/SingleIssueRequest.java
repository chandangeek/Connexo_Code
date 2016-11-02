package com.elster.jupiter.issue.rest.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleIssueRequest {
    public EntityReference issue;
    public String comment;
}

package com.elster.jupiter.issue.rest.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkIssueRequest {
    public boolean allIssues = false;
    public List<EntityReference> issues;
    public String comment;
}

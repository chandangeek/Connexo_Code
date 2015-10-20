package com.elster.jupiter.issue.rest.request;


import java.util.List;

import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PerformActionRequest {
    
    public long id;
    public IssueShortInfo issue;
    public List<PropertyInfo> properties;
}

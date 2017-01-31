/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.request;

import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PerformActionRequest {
    
    public long id;
    public IssueShortInfo issue;
    public List<PropertyInfo> properties;
}

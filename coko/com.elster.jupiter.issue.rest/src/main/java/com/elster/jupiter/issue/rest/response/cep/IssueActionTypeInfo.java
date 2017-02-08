/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueActionTypeInfo {
    public long id;
    public String name;
    public IssueTypeInfo issueType;
    public List<PropertyInfo> properties;

}
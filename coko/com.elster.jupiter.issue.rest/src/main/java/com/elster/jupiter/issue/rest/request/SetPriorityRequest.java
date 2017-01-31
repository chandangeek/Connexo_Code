/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.request;

import com.elster.jupiter.issue.rest.response.PriorityInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SetPriorityRequest {

    public long id;
    public IssueShortInfo issue;
    public PriorityInfo priority;

}

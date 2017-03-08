/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;


import com.elster.jupiter.issue.share.entity.IssueReason;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueReasonInfo {
    public String id;
    public String name;
}

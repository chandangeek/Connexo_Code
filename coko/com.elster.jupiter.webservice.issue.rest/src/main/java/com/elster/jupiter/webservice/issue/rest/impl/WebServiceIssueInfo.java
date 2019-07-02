/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.rest.impl;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.webservice.issue.WebServiceIssue;

public class WebServiceIssueInfo<T extends DeviceInfo> extends IssueInfo<T, WebServiceIssue> {
    public long webServiceCallOccurrenceId;

    public WebServiceIssueInfo(WebServiceIssue issue, Class<T> deviceInfoClass) {
        super(issue, deviceInfoClass);
        webServiceCallOccurrenceId = issue.getWebServiceCallOccurrence().getId();
    }
}

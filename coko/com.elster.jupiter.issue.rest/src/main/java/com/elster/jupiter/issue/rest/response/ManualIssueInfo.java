/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.share.entity.Issue;

public class ManualIssueInfo<T extends DeviceInfo> extends IssueInfo<T, Issue> {

    public ManualIssueInfo(Issue manualIssue, Class<T> deviceInfoClass) {
        super(manualIssue, deviceInfoClass);
    }

}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import java.util.List;
import java.util.stream.Collectors;

public class IssueInfoFactory {

    public IssueInfo<? extends DeviceInfo, ? extends Issue> asInfo(Issue issue, Class<? extends DeviceInfo> deviceInfoClass) {
        return new IssueInfo<>(issue, deviceInfoClass);
    }

    public List<IssueInfo<? extends DeviceInfo, ? extends Issue>> asInfos(List<? extends Issue> issues) {
        return issues.stream().map(issue -> this.asInfo(issue, DeviceInfo.class)).collect(Collectors.toList());
    }
}

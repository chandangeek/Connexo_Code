/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;

import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.List;

@Component(name = "com.elster.jupiter.issue.rest.response.ManualIssueInfoFactory", service = {InfoFactory.class}, immediate = true)
public class ManualIssueInfoFactory implements InfoFactory<Issue> {

    public ManualIssueInfoFactory() {

    }

    @Override
    public Object from(Issue manualIssue) {
        return asInfo(manualIssue, DeviceInfo.class);
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        return new ArrayList<>();
    }

    @Override
    public Class<Issue> getDomainClass() {
        return Issue.class;
    }

    public ManualIssueInfo asInfo(Issue manualIssue, Class<? extends DeviceInfo> deviceInfoClass) {
        ManualIssueInfo info = new ManualIssueInfo(manualIssue, deviceInfoClass);
        return info;
    }

}

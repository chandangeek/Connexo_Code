/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.rest.impl;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.jupiter.webservice.issue.WebServiceIssue;

import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.List;

@Component(name="com.elster.jupiter.webservice.issue.rest.impl.WebServiceIssueInfoFactory", service = { InfoFactory.class }, immediate = true)
public class WebServiceIssueInfoFactory implements InfoFactory<WebServiceIssue> {
    public WebServiceIssueInfoFactory() {
        // for OSGi
    }

    @Override
    public Object from(WebServiceIssue webServiceIssue) {
        return new WebServiceIssueInfo<>(webServiceIssue, DeviceInfo.class);
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        return new ArrayList<>();
    }

    @Override
    public Class<WebServiceIssue> getDomainClass() {
        return WebServiceIssue.class;
    }
}

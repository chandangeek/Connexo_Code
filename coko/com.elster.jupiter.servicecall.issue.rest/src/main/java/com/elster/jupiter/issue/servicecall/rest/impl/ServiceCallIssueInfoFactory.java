/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.rest.impl;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceShortInfo;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.jupiter.servicecall.issue.ServiceCallIssue;
import com.elster.jupiter.servicecall.issue.ServiceCallIssueService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component(name="issue.servicecall.info.factory", service = { InfoFactory.class }, immediate = true)
public class ServiceCallIssueInfoFactory implements InfoFactory<ServiceCallIssue> {


    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;

    public ServiceCallIssueInfoFactory() {
    }

    @Inject
    public ServiceCallIssueInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        Thesaurus domainThesaurus = nlsService.getThesaurus(ServiceCallIssueService.COMPONENT_NAME, com.elster.jupiter.nls.Layer.DOMAIN);
        Thesaurus restThesaurus = nlsService.getThesaurus(IssueServiceCallApplication.ISSUE_SERVICE_CALL_REST_COMPONENT, com.elster.jupiter.nls.Layer.REST);
        this.thesaurus = domainThesaurus.join(restThesaurus);
    }

    @Override
    public Object from(ServiceCallIssue issueServiceCall) {
        return asInfo(issueServiceCall, DeviceInfo.class);
    }

    @Override
    public List<Object> from(List<ServiceCallIssue> domainObjects) {
        return null;
    }

    public ServiceCallIssueInfo<?> asInfo(ServiceCallIssue issue, Class<? extends DeviceInfo> deviceInfoClass) {
        ServiceCallIssueInfo<?> info = new ServiceCallIssueInfo<>(issue, deviceInfoClass);
        addServiceCallIssueInfo(info, issue);
        return info;
    }


    public List<ServiceCallIssueInfo<?>> asInfos(List<? extends ServiceCallIssue> issues) {
        return issues.stream().map(issue -> this.asInfo(issue, DeviceShortInfo.class)).collect(Collectors.toList());
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        return new ArrayList<>();
    }

    @Override
    public Class<ServiceCallIssue> getDomainClass() {
        return ServiceCallIssue.class;
    }


    private void addServiceCallIssueInfo(ServiceCallIssueInfo<?> info, ServiceCallIssue issue) throws LocalizedFieldValidationException {
//        info.serviceCall = new IdWithNameInfo(issue.getServiceCall());
//        info.onState = new IdWithNameInfo(issue.getNewState().ordinal(), issue.getNewState().name());
    }

}

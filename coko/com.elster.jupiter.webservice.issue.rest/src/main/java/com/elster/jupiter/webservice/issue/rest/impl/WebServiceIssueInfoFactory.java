/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.rest.impl;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.jupiter.webservice.issue.WebServiceIssue;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Component(name="com.elster.jupiter.webservice.issue.rest.impl.WebServiceIssueInfoFactory", service = { InfoFactory.class }, immediate = true)
public class WebServiceIssueInfoFactory implements InfoFactory<WebServiceIssue> {
    private WebServiceCallOccurrenceInfoFactory webServiceCallOccurrenceInfoFactory;

    public WebServiceIssueInfoFactory() {
        // for OSGi
    }

    @Inject
    public WebServiceIssueInfoFactory(WebServiceCallOccurrenceInfoFactory webServiceCallOccurrenceInfoFactory) {
        this.webServiceCallOccurrenceInfoFactory = webServiceCallOccurrenceInfoFactory;
    }

    @Reference
    public void setThesaurusProvider(WebServiceIssueApplication application) {
        Thesaurus thesaurus = application.getThesaurus();
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
            }
        });
        webServiceCallOccurrenceInfoFactory = injector.getInstance(WebServiceCallOccurrenceInfoFactory.class);
    }

    @Override
    public WebServiceIssueInfo from(WebServiceIssue webServiceIssue) {
        WebServiceIssueInfo info = new WebServiceIssueInfo<>(webServiceIssue, DeviceInfo.class);
        info.webServiceCallOccurrence = webServiceCallOccurrenceInfoFactory.from(webServiceIssue.getWebServiceCallOccurrence());
        return info;
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

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.impl;

import com.elster.jupiter.issue.rest.response.issue.IssueInfoFactoryService;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.rest.util.InfoFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component(name = "com.elster.jupiter.issue.info.whiteboard.implementation" , immediate = true , service = {IssueInfoFactoryService.class} )
public class IssueInfoFactoryWhiteBoard implements IssueInfoFactoryService {

    private final List<InfoFactory> factories = new CopyOnWriteArrayList<>();

    @Reference(name="Info-Order",cardinality= ReferenceCardinality.MULTIPLE,policy= ReferencePolicy.DYNAMIC)
    public void addFactory(InfoFactory issueInfoFactory) {
        factories.add(issueInfoFactory);
    }

    public void removeFactory(InfoFactory issueInfoFactory) {
        factories.removeIf(fac -> issueInfoFactory.getDomainClass().equals(fac.getDomainClass()));
    }


    @Override
    public InfoFactory getInfoFactoryFor(Issue issue) {
        return factories.stream().
                filter(fac -> isApplicable(issue, fac)).
                findFirst().
                orElseThrow(() -> new IllegalStateException("No registered factory for issue type " + issue.getReason()
                        .getIssueType()));
    }

    private boolean isApplicable(Issue issue, InfoFactory infoFactory) {
        return infoFactory.getDomainClass().isInstance(issue);
    }
}

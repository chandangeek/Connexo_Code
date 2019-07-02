/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.UnableToCreateIssueException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.webservice.issue.WebServiceIssueFilter;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;
import com.elster.jupiter.webservice.issue.impl.MessageSeeds;
import com.elster.jupiter.webservice.issue.impl.entity.WebServiceOpenIssueImpl;

import com.google.inject.Inject;

import java.util.Map;
import java.util.Optional;

public class WebServiceEvent implements IssueEvent {
    private final Thesaurus thesaurus;
    private final WebServiceIssueService webServiceIssueService;
    private final WebServiceCallOccurrenceService webServiceCallOccurrenceService;

    private long occurrenceId;
    private WebServiceEventDescription eventType;

    @Inject
    public WebServiceEvent(Thesaurus thesaurus,
                           WebServiceIssueService webServiceIssueService,
                           WebServiceCallOccurrenceService webServiceCallOccurrenceService) {
        this.thesaurus = thesaurus;
        this.webServiceIssueService = webServiceIssueService;
        this.webServiceCallOccurrenceService = webServiceCallOccurrenceService;
    }

    WebServiceEvent init(WebServiceEventDescription eventType, Map<?, ?> jsonPayload) {
        try {
            this.eventType = eventType;
            occurrenceId = ((Number) jsonPayload.get("id")).longValue();
        } catch (Exception e) {
            throw new UnableToCreateIssueException(thesaurus, MessageSeeds.UNABLE_TO_CREATE_EVENT, e, jsonPayload.toString());
        }
        return this;
    }

    @Override
    public String getEventType() {
        return eventType.name();
    }

    @Override
    public Optional<EndDevice> getEndDevice() {
        return Optional.empty();
    }

    @Override
    public Optional<? extends OpenIssue> findExistingIssue() {
        WebServiceIssueFilter filter = new WebServiceIssueFilter();
        filter.addWebServiceCallOccurrenceId(occurrenceId);
        return webServiceIssueService.findAllWebServiceIssues(filter).stream().findFirst()
                .filter(OpenIssue.class::isInstance)
                .map(OpenIssue.class::cast);
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof WebServiceOpenIssueImpl) {
            ((WebServiceOpenIssueImpl) issue).setWebServiceCallOccurrence(getOccurrenceOrThrowException());
        }
    }

    public Optional<WebServiceCallOccurrence> getOccurrence() {
        return webServiceCallOccurrenceService.getEndPointOccurrence(occurrenceId);
    }

    private WebServiceCallOccurrence getOccurrenceOrThrowException() {
        return getOccurrence()
                .orElseThrow(() -> new UnableToCreateIssueException(thesaurus, MessageSeeds.UNABLE_TO_CREATE_ISSUE_MISSING_WSC_OCCURRENCE, occurrenceId));
    }

    /**
     * Used by issue creation rule template
     */
    public long getEndPointConfigurationId() {
        return getOccurrence()
                .map(WebServiceCallOccurrence::getEndPointConfiguration)
                .map(EndPointConfiguration::getId)
                .orElse(0L);
    }
}

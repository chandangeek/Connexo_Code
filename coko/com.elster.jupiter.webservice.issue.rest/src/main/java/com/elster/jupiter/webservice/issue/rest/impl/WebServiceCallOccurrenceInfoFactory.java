/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class WebServiceCallOccurrenceInfoFactory {
    private final Thesaurus thesaurus;
    private final EndPointLogEntryInfoFactory endPointLogEntryInfoFactory;

    @Inject
    WebServiceCallOccurrenceInfoFactory(Thesaurus thesaurus, EndPointLogEntryInfoFactory endPointLogEntryInfoFactory) {
        this.thesaurus = thesaurus;
        this.endPointLogEntryInfoFactory = endPointLogEntryInfoFactory;
    }

    WebServiceCallOccurrenceInfo from(WebServiceCallOccurrence occurrence) {
        WebServiceCallOccurrenceInfo info = new WebServiceCallOccurrenceInfo();
        info.id = occurrence.getId();
        info.endpoint = new IdWithNameInfo(occurrence.getEndPointConfiguration());
        info.webServiceName = occurrence.getEndPointConfiguration().getWebServiceName();
        info.startTime = occurrence.getStartTime();
        occurrence.getEndTime().ifPresent(end -> info.endTime = end);
        info.status = new IdWithNameInfo(occurrence.getStatus().name(), occurrence.getStatus().translate(thesaurus));
        info.logs = occurrence.getLogs().stream()
                .map(endPointLogEntryInfoFactory::from)
                .collect(Collectors.toList());
        return info;
    }
}

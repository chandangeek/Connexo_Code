package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.domain.util.Finder;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Set;

@ProviderType
public interface WebServiceCallOccurrenceFinderBuilder {

    WebServiceCallOccurrenceFinderBuilder withApplicationNames(Set<String> applicationName);

    WebServiceCallOccurrenceFinderBuilder withStatuses(Set<WebServiceCallOccurrenceStatus> statuses);

    WebServiceCallOccurrenceFinderBuilder withWebServiceName(String webServiceName);

    WebServiceCallOccurrenceFinderBuilder withEndPointConfiguration(EndPointConfiguration epc);

    WebServiceCallOccurrenceFinderBuilder withStartTime(Range<Instant> interval);

    WebServiceCallOccurrenceFinderBuilder withEndTime(Range<Instant> interval);

    WebServiceCallOccurrenceFinderBuilder onlyInbound();

    WebServiceCallOccurrenceFinderBuilder onlyOutbound();

    Finder<WebServiceCallOccurrence> build();

}

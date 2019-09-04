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

    WebServiceCallOccurrenceFinderBuilder withWebServiceNames(Set<String> webServiceNames);

    WebServiceCallOccurrenceFinderBuilder withEndPointConfigurations(Set<EndPointConfiguration> endPointConfigurations);

    WebServiceCallOccurrenceFinderBuilder withStartTimeIn(Range<Instant> interval);

    WebServiceCallOccurrenceFinderBuilder withEndTimeIn(Range<Instant> interval);

    WebServiceCallOccurrenceFinderBuilder onlyInbound();

    WebServiceCallOccurrenceFinderBuilder onlyOutbound();

    WebServiceCallOccurrenceFinderBuilder withDomain(Set<String> domains);

    WebServiceCallOccurrenceFinderBuilder withDomainAndValues(Set<String> domains, Set<String> values);

    Finder<WebServiceCallOccurrence> build();

}

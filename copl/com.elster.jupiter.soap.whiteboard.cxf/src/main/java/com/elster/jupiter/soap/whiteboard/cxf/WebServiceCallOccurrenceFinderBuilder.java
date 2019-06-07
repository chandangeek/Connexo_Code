package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.domain.util.Finder;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@ProviderType
public interface WebServiceCallOccurrenceFinderBuilder {

    public WebServiceCallOccurrenceFinderBuilder withApplicationName(Set<String> applicationName);

    public WebServiceCallOccurrenceFinderBuilder withStatusIn(List<String> statuses);

    public WebServiceCallOccurrenceFinderBuilder withWebServiceName(String webServiceName);

    public WebServiceCallOccurrenceFinderBuilder withEndPointConfiguration(EndPointConfiguration epc);

    public WebServiceCallOccurrenceFinderBuilder withStartTimeIn(Range<Instant> interval);

    public WebServiceCallOccurrenceFinderBuilder withEndTimeIn(Range<Instant> interval);

    public Finder<WebServiceCallOccurrence> build();

}

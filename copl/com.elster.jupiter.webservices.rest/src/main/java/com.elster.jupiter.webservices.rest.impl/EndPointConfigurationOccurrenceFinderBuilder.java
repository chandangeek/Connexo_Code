package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointOccurrence;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

@ProviderType
public interface EndPointConfigurationOccurrenceFinderBuilder {

    public EndPointConfigurationOccurrenceFinderBuilder withApplicationName(String applicationName);

    public EndPointConfigurationOccurrenceFinderBuilder withStatusIn(List<String> statuses);

    public EndPointConfigurationOccurrenceFinderBuilder withWebServiceName(String webServiceName);

    public EndPointConfigurationOccurrenceFinderBuilder withStartTimeIn(Range<Instant> interval);

    public EndPointConfigurationOccurrenceFinderBuilder withEndTimeIn(Range<Instant> interval);

    public Finder<EndPointOccurrence> build();
}

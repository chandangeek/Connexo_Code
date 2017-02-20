package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeteringService;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SourceChannelSetFactory {

    static final String SOURCE_CHANNEL_IDS_SEPARATOR = ",";

    private final MeteringService meteringService;

    public SourceChannelSetFactory(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public SourceChannelSet parseFromString(String value) {
        try {
            return new SourceChannelSet(meteringService, parseListOfLongs(value, SOURCE_CHANNEL_IDS_SEPARATOR));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to parse the list of source channel ids from string: " + value
                    + ". Expected: the list of numbers separated by '" + SOURCE_CHANNEL_IDS_SEPARATOR + "'.");
        }
    }

    private Set<Long> parseListOfLongs(String value, String separator) {
        return Stream.of(value.split(separator)).map(Long::parseLong).collect(Collectors.toSet());
    }

    public SourceChannelSet merge(SourceChannelSet... sets) {
        Set<Long> uniqueSourceChannelsIds = Stream.of(sets)
                .flatMap(sourceChannelSet -> sourceChannelSet.getSourceChannelIds().stream())
                .collect(Collectors.toSet());
        return new SourceChannelSet(meteringService, uniqueSourceChannelsIds);
    }
}

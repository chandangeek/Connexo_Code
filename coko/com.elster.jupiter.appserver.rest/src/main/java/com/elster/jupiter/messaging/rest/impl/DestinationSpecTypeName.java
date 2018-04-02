package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.messaging.DestinationSpec;

import java.util.List;
import java.util.stream.Collectors;

public class DestinationSpecTypeName {

    public List<DestinationSpecTypeNameInfo> data;

    private DestinationSpecTypeName(List<DestinationSpecTypeNameInfo> data) {
        this.data = data;
    }

    public static DestinationSpecTypeName from(List<DestinationSpec> destinationSpec) {
        List<DestinationSpecTypeNameInfo> data = destinationSpec.stream()
                .filter(DestinationSpec::isExtraQueueCreationEnabled)
                .filter(DestinationSpec::isDefault)
                .map(d -> new DestinationSpecTypeNameInfo(d.getQueueTypeName()))
                .collect(Collectors.toList());

        return new DestinationSpecTypeName(data);
    }

}

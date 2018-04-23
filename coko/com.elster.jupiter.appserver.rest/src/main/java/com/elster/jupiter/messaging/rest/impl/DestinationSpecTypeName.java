package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.messaging.DestinationSpec;

import java.util.List;
import java.util.stream.Collectors;

public class DestinationSpecTypeName {

    public List<DestinationSpecTypeNameInfo> data;

    public DestinationSpecTypeName() {

    }

    private DestinationSpecTypeName(List<DestinationSpecTypeNameInfo> data) {
        this.data = data;
    }

    public static DestinationSpecTypeName from(List<DestinationSpec> destinationSpecs) {
        List<DestinationSpecTypeNameInfo> data = destinationSpecs.stream()
                .filter(DestinationSpec::isExtraQueueCreationEnabled)
                .filter(DestinationSpec::isDefault)
                .map(destinationSpec -> new DestinationSpecTypeNameInfo(destinationSpec.getQueueTypeName()))
                .collect(Collectors.toList());

        return new DestinationSpecTypeName(data);
    }

}

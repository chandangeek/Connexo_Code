package com.elster.jupiter.issue.share.entity.values;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

public final class EndPointValue extends HasIdAndName {

    private final EndPointConfiguration endPointConfiguration;

    public EndPointValue(final EndPointConfiguration endPointConfiguration) {
        this.endPointConfiguration = endPointConfiguration;
    }

    @Override
    public Object getId() {
        return endPointConfiguration.getId();
    }

    @Override
    public String getName() {
        return endPointConfiguration.getName();
    }

    public EndPointConfiguration getEndPointConfiguration() {
        return endPointConfiguration;
    }
}

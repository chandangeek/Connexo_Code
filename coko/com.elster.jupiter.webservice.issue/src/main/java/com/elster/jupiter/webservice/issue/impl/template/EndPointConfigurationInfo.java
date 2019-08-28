/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl.template;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.webservice.issue.impl.TranslationKeys;

public class EndPointConfigurationInfo extends HasIdAndName {
    private final Thesaurus thesaurus;
    private final EndPointConfiguration endPointConfiguration;

    public EndPointConfigurationInfo(EndPointConfiguration endPointConfiguration, Thesaurus thesaurus) {
        this.endPointConfiguration = endPointConfiguration;
        this.thesaurus = thesaurus;
    }

    @Override
    public Long getId() {
        return endPointConfiguration.getId();
    }

    @Override
    public String getName() {
        return endPointConfiguration.getName();
    }

    public String getDirection() {
        return (endPointConfiguration.isInbound() ?
                TranslationKeys.END_POINT_CONFIGURATION_DIRECTION_INBOUND :
                TranslationKeys.END_POINT_CONFIGURATION_DIRECTION_OUTBOUND)
                .translate(thesaurus);
    }

    EndPointConfiguration getEndPointConfiguration() {
        return endPointConfiguration;
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || o instanceof EndPointConfigurationInfo
                && super.equals(o);
    }
}

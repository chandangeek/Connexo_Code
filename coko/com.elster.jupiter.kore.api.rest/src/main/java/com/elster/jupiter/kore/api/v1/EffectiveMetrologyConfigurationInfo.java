package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.util.List;

public class EffectiveMetrologyConfigurationInfo extends LinkInfo<Long> {
    public LinkInfo<Long> metrologyConfiguration;
    public List<MetrologyConfigurationPurposeInfo> purposes;
}

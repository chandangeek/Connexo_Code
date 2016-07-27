package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.rest.util.hypermedia.LinkInfo;

import java.util.List;

public class EffectiveMetrologyConfigurationInfo extends LinkInfo<Long> {
    public LinkInfo<Long> metrologyConfiguration;
    public List<MetrologyConfigurationPurposeInfo> purposes;
}

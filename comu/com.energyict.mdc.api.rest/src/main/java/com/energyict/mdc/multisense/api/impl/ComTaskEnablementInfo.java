package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.hypermedia.LinkInfo;

public class ComTaskEnablementInfo extends LinkInfo<Long> {
    public LinkInfo comTask;
    public LinkInfo securityPropertySet;
    public LinkInfo partialConnectionTask;
    public LinkInfo protocolDialectConfigurationProperties;
    public Integer priority;
    public Boolean suspended;
}

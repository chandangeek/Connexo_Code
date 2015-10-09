package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ComTaskEnablement;

import java.util.ArrayList;
import java.util.List;

public class ComTaskEnablementInfo extends LinkInfo{
    public LinkInfo comTask;
    public LinkInfo securityPropertySet;
    public LinkInfo partialConnectionTask;
    public LinkInfo protocolDialectConfigurationProperties;
    public Integer priority;
    public Boolean suspended;
}

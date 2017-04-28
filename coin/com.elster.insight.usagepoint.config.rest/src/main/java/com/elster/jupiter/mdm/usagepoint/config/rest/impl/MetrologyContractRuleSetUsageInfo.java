/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetrologyContractRuleSetUsageInfo {
    public long total;
    public List<MetrologyContractInfo> contracts;
    public List<UsagePointLifeCycleStateInfo> lifeCycleStates;
}

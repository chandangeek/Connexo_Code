/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetVersionInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class ValidationRuleSetInfo {
    public long id;
    public String name;
    public String description;
    public Long startDate;
    public Long endDate;
    public int numberOfVersions;
    public boolean hasCurrent;
    public long version;
    public Long currentVersionId;
    public ValidationRuleSetVersionInfo currentVersion;
    public List<UsagePointLifeCycleStateInfo> lifeCycleStates;
}

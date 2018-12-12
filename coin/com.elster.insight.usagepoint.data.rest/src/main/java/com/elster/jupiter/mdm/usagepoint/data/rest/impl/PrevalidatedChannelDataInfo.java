/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.validation.rest.ValidationRuleInfo;

import java.time.Instant;
import java.util.Set;

public class PrevalidatedChannelDataInfo {

    public Instant readingTime;

    public Set<ValidationRuleInfo> validationRules;
}

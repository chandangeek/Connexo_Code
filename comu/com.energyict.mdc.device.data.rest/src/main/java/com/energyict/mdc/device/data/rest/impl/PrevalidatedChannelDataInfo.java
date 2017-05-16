/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.rest.ValidationRuleInfo;

import java.time.Instant;
import java.util.Set;

public class PrevalidatedChannelDataInfo {

    public Instant readingTime;

    public Set<ValidationRuleInfo> validationRules;

    public Set<ValidationRuleInfo> bulkValidationRules;
}
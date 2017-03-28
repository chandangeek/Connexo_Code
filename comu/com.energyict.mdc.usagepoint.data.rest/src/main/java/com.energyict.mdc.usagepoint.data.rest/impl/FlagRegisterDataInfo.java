/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by dantonov on 28.03.2017.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class FlagRegisterDataInfo extends RegisterDataInfo {
    /**
     * Collected value for flag register reading
     */
    public Long value;
}

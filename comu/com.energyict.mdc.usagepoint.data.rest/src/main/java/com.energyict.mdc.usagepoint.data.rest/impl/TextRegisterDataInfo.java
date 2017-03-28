/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by dantonov on 28.03.2017.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class TextRegisterDataInfo extends RegisterDataInfo {

      /*
        when corresponding deliverable type is TEXT
     */
    /**
     * Collected value for text register reading
     */
    public String value;

}

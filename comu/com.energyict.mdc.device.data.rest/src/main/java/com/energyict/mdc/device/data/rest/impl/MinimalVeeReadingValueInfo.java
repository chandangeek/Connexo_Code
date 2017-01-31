/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.validation.ValidationAction;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-01-07 (16:50)
 */
class MinimalVeeReadingValueInfo {
    @JsonProperty("validationResult")
    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;

    @JsonProperty("action")
    public ValidationAction action;

    @JsonProperty("estimatedByRule")
    public Boolean estimatedByRule;

    @JsonProperty("valueModificationFlag")
    @XmlJavaTypeAdapter(ReadingModificationFlagAdapter.class)
    public ReadingModificationFlag valueModificationFlag;

    @JsonProperty("editedInApp")
    public IdWithNameInfo editedInApp;

    @JsonProperty("isConfirmed")
    public Boolean isConfirmed;

}
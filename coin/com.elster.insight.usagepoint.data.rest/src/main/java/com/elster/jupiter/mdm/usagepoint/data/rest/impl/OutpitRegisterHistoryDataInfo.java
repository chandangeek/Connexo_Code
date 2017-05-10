/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.validation.ValidationAction;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OutpitRegisterHistoryDataInfo {

    public IntervalInfo interval;
    public String userName;
    public List<ReadingQualityInfo> readingQualities;
    public Instant reportedDateTime;
    public Instant timeStamp;
    public BigDecimal value;

    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;

    public boolean dataValidated;
    public ValidationAction validationAction;
}

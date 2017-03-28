/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Created by dantonov on 28.03.2017.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class NumericalRegisterDataInfo extends RegisterDataInfo {

        /*

    BigDecimal or Text or...


    reading.getValue for numeric
    com.elster.jupiter.metering.readings.Reading.getText for text

     */
    /**
     * Collected value for numeric register reading
     */
    public BigDecimal value;


}

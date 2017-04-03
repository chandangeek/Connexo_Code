/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by dantonov on 28.03.2017.
 */
@XmlRootElement
public class TwoValuesPercentDifference extends TwoValuesDifference {

    public TwoValuesPercentDifference() {
        super(Type.percent);
    }

    @JsonProperty(value = "value")
    public Double percent;
}

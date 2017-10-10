/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ReadingTypeAliasInfo {

    public String name;
    public int numberOfReadingTypes;

    public String commodity;
    public String measurementKind;
    public String flowDirection;
    public String unit;
    public String macroPeriod;
    public String timePeriod;
    public String accumulation;
    public String aggregate;

    public int multiplier;
    public String phases;
    public int tou;
    public int cpp;
    public int consumptionTier;

}

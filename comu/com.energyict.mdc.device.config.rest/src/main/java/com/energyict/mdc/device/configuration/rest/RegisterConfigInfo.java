/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RegisterConfigInfo {
    @JsonProperty("id")
    public Long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("readingType")
    public ReadingTypeInfo readingType;
    @JsonProperty("registerType")
    public Long registerType;
    @JsonProperty("obisCode")
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @JsonProperty("overruledObisCode")
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode overruledObisCode;
    @JsonProperty("obisCodeDescription")
    public String obisCodeDescription;
    @JsonProperty("numberOfFractionDigits")
    public Integer numberOfFractionDigits;
    @JsonProperty("overflow")
    public BigDecimal overflow;
    @JsonProperty("asText")
    public boolean asText;
    @JsonProperty("useMultiplier")
    public Boolean useMultiplier;
    @JsonProperty("calculatedReadingType")
    public ReadingTypeInfo calculatedReadingType;
    public List<ReadingTypeInfo> possibleCalculatedReadingTypes = new ArrayList<>();
    @JsonProperty("collectedReadingType")
    public ReadingTypeInfo collectedReadingType;
    public long version;
    public VersionInfo<Long> parent;

    public void writeTo(TextualRegisterSpec.Updater textualRegisterSpecUpdater){
        textualRegisterSpecUpdater.overruledObisCode(this.overruledObisCode);
    }

    public void writeTo(NumericalRegisterSpec.Updater numericalRegisterSpecUpdater, ReadingType calculatedReadingType){
        numericalRegisterSpecUpdater.overruledObisCode(this.overruledObisCode);
        numericalRegisterSpecUpdater.overflowValue(this.overflow);
        numericalRegisterSpecUpdater.numberOfFractionDigits(this.numberOfFractionDigits != null ? this.numberOfFractionDigits : 0);
        if(this.useMultiplier) {
            numericalRegisterSpecUpdater.useMultiplierWithCalculatedReadingType(calculatedReadingType);
        } else {
            numericalRegisterSpecUpdater.noMultiplier();
        }
    }

}

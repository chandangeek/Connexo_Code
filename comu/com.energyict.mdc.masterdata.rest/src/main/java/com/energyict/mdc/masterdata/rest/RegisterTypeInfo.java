/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.masterdata.MeasurementType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
public class RegisterTypeInfo {

    public long id;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public boolean isLinkedByDeviceType;
    public Boolean isLinkedByActiveRegisterConfig; // This property makes no sense if the register mapping was retrieved outside the scope of a device type. It will be null.
    public Boolean isLinkedByInactiveRegisterConfig; // This property makes no sense if the register mapping was retrieved outside the scope of a device type. It will be null.
    public ReadingTypeInfo readingType;
    @JsonDeserialize(using = NullifyingDeserializer.class)
    public ReadingTypeInfo calculatedReadingType;
    public long version;
    public VersionInfo<Long> parent;

    public void writeTo(MeasurementType measurementType, ReadingType readingType) {
        measurementType.setObisCode(this.obisCode);
        measurementType.setReadingType(readingType);
    }

}
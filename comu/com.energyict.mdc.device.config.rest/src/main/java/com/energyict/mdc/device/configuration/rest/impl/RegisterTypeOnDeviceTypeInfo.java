/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterTypeOnDeviceTypeInfo {

    public long id;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public boolean isLinkedByDeviceType;
    public Boolean isLinkedByActiveRegisterConfig;
    public Boolean isLinkedByInactiveRegisterConfig;
    public Boolean isCumulative;

    /* The ReadingType from the RegisterType */
    public ReadingTypeInfo readingType;

    public ReadingTypeInfo collectedReadingType;

    /* A list of possible readingTypes where the 'multiplied' value can be stored */
    public List<ReadingTypeInfo> possibleCalculatedReadingTypes = new ArrayList<>();

    public DeviceTypeCustomPropertySetInfo customPropertySet;
    public long version;
    public VersionInfo<Long> parent;
}
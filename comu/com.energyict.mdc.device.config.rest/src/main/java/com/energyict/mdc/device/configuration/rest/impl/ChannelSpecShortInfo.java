/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ChannelSpecShortInfo {

    public long id;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public Boolean isCumulative;

    /* The ReadingType from the RegisterType */
    public ReadingTypeInfo readingType;

    /* This will be the actual readingType which is collected  */
    public ReadingTypeInfo collectedReadingType;
    /* This will contain the delta in case of a bulk channel, otherwise it will be empty */
    public ReadingTypeInfo calculatedReadingType;

    /* A list of possible readingTypes where the 'multiplied' value can be stored */
    public List<ReadingTypeInfo> possibleCalculatedReadingTypes = new ArrayList<>();
    public long version;
    public VersionInfo<Long> parent;
}
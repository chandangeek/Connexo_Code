/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.metering.ReadingType;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class ReadingTypeInfo {

    public String mRID;

    public ReadingTypeInfo() {
    }

    public ReadingTypeInfo(Map<String, Object> map) {
        this.mRID = (String) map.get("mRID");
    }

    public ReadingTypeInfo(ReadingType readingType) {
        mRID = readingType.getMRID();
    }

}
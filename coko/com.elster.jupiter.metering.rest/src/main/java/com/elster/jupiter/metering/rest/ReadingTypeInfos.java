/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ReadingTypeInfos {
    public int total;
    public List<ReadingTypeInfo> readingTypes = new ArrayList<>();

    public ReadingTypeInfos() {
    }

    public ReadingTypeInfos(ReadingType usagePoint) {
        add(usagePoint);
    }

    public ReadingTypeInfos(Iterable<? extends ReadingType> readingTypes) {
        addAll(readingTypes);
    }

    public ReadingTypeInfo add(ReadingType readingType) {
        ReadingTypeInfo result = new ReadingTypeInfo(readingType);
        readingTypes.add(result);
        total++;
        return result;
    }

    public ReadingTypeInfo add(ReadingTypeInfo readingTypeInfo) {
        readingTypes.add(readingTypeInfo);
        total++;
        return readingTypeInfo;
    }

    void addAll(Iterable<? extends ReadingType> usagePoints) {
        for (ReadingType each : usagePoints) {
            add(each);
        }
    }

}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.ReadingType;

import java.util.ArrayList;
import java.util.List;

public class ReadingTypeAliasInfos {

    public int total;
    public List<ReadingTypeAliasInfo> readingTypeAliases = new ArrayList<>();

    public ReadingTypeAliasInfos() {
    }

//    public ReadingTypeAliasInfos(ReadingType usagePoint) {
//        add(usagePoint);
//    }
//
//    public ReadingTypeAliasInfos(Iterable<? extends ReadingType> readingTypes) {
//        addAll(readingTypes);
//    }

//    public ReadingTypeInfo add(ReadingType readingType) {
//        ReadingTypeInfo result = new ReadingTypeInfo(readingType);
//        readingTypeAliases.add(result);
//        total++;
//        return result;
//    }

    public ReadingTypeAliasInfo add(ReadingTypeAliasInfo readingTypeAliasInfo) {
        readingTypeAliases.add(readingTypeAliasInfo);
        total++;
        return readingTypeAliasInfo;
    }

//    void addAll(Iterable<? extends ReadingType> usagePoints) {
//        for (ReadingType each : usagePoints) {
//            add(each);
//        }
//    }
}

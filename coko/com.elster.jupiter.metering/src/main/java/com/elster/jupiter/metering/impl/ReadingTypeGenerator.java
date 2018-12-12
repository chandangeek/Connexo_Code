/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public final class ReadingTypeGenerator {

    private ReadingTypeGenerator() {
    }

    static List<Pair<String, String>> generate() {
        List<Pair<String, String>> readingTypes = new ArrayList<>();
        readingTypes.addAll(new ReadingTypeGeneratorForElectricity().generateReadingTypes());
        readingTypes.addAll(new ReadingTypeGeneratorForGas().generateReadingTypes());
        readingTypes.addAll(new ReadingTypeGeneratorForWater().generateReadingTypes());
        readingTypes.addAll(new ReadingTypeGeneratorForParameters().generateReadingTypes());
        readingTypes.addAll(new ReadingTypeGeneratorForDataLogger().generateReadingTypes());
        return readingTypes;
    }

    static List<ReadingType> generateSelectedReadingTypes(MeteringService meteringService, String... readingTypes) {
        return Stream.of(readingTypes)
                .filter(r -> !r.isEmpty())
                .map(readingType -> meteringService.createReadingType(readingType, readingType))
                .collect(Collectors.toList());
    }

}

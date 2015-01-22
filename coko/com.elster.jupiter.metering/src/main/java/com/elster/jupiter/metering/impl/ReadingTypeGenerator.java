package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public final class ReadingTypeGenerator {

    private ReadingTypeGenerator() {
    }

    static List<ReadingType> generate(MeteringService meteringService) {
        List<ReadingType> readingTypes = new ArrayList<>();
        readingTypes.addAll(new ReadingTypeGeneratorForElectricity(meteringService).generateReadingTypes());
        readingTypes.addAll(new ReadingTypeGeneratorForGas(meteringService).generateReadingTypes());
        readingTypes.addAll(new ReadingTypeGeneratorForWater(meteringService).generateReadingTypes());
        readingTypes.addAll(new ReadingTypeGeneratorForParameters(meteringService).generateReadingTypes());
        return readingTypes;
    }

    static List<ReadingType> generateSelectedReadingTypes(MeteringService meteringService,String... readingTypes){
        return Stream.of(readingTypes)
                .filter(r -> !r.equals(""))
                .map(readingType -> meteringService.createReadingType(readingType, readingType))
                .collect(Collectors.toList());
    }

}

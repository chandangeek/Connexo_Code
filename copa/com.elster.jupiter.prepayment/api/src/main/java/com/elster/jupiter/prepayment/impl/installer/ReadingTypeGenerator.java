package com.elster.jupiter.prepayment.impl.installer;

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
        readingTypes.addAll(new ReadingTypeGeneratorForPrepayment().generateReadingTypes());
        return readingTypes;
    }

    static List<ReadingType> generateSelectedReadingTypes(MeteringService meteringService, String... readingTypes) {
        return Stream.of(readingTypes)
                .filter(r -> !r.isEmpty())
                .map(readingType -> meteringService.createReadingType(readingType, readingType))
                .collect(Collectors.toList());
    }

}

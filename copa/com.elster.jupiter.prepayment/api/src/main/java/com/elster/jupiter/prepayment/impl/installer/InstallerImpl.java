package com.elster.jupiter.prepayment.impl.installer;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.Pair;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class InstallerImpl {

    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());

    private final MeteringService meteringService;

    public InstallerImpl(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public void install() {
        createReadingTypes();
    }

    private void createReadingTypes() {
        try {
            createAllReadingTypes(ReadingTypeGenerator.generate());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating readingtypes : " + e.getMessage(), e);
        }
    }

    public void createAllReadingTypes(List<Pair<String, String>> readingTypes) {
        List<ReadingType> availableReadingTypes = meteringService.getAvailableReadingTypes();
        List<String> availableReadingTypeCodes = availableReadingTypes.parallelStream()
                .map(ReadingType::getMRID)
                .collect(Collectors.toList());
        List<Pair<String, String>> filteredReadingTypes = readingTypes.parallelStream()
                .filter(readingTypePair -> !availableReadingTypeCodes.contains(readingTypePair.getFirst()))
                .collect(Collectors.toList());

        filteredReadingTypes.stream()
                .forEach(readingType -> meteringService.createReadingType(readingType.getFirst(), readingType.getLast()));
    }
}
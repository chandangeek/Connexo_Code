/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.slp.importers.impl.correctionfactor;

import com.elster.jupiter.slp.DurationAttribute;
import com.elster.jupiter.slp.importers.impl.FileImportParser;
import com.elster.jupiter.slp.importers.impl.MessageSeeds;
import com.elster.jupiter.slp.importers.impl.SyntheticLoadProfileDataImporterContext;
import com.elster.jupiter.slp.importers.impl.exceptions.FileImportParserException;
import com.elster.jupiter.slp.importers.impl.parsers.BigDecimalParser;
import com.elster.jupiter.slp.importers.impl.parsers.InstantParser;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CorectionFactorFileImportParser implements FileImportParser<CorrectionFactorImportRecord> {


    private List<String> headers;
    private SyntheticLoadProfileDataImporterContext context;
    private InstantParser instantParser;
    private BigDecimalParser bigDecimalParser;

    public CorectionFactorFileImportParser(SyntheticLoadProfileDataImporterContext context, InstantParser instantParser, BigDecimalParser bigDecimalParser) {
        this.instantParser = instantParser;
        this.bigDecimalParser = bigDecimalParser;
        this.context = context;
    }

    @Override
    public void init(CSVParser csvParser) {
        this.initHeaders(csvParser);
    }

    @Override
    public CorrectionFactorImportRecord parse(CSVRecord csvRecord) throws FileImportParserException {
        CorrectionFactorImportRecord record = new CorrectionFactorImportRecord();
        record.setLineNumber(csvRecord.getRecordNumber());
        record.setTimeStamp(this.instantParser.parse(csvRecord.get("timeStamp")));
        headers.stream().filter(header -> !header.equalsIgnoreCase("timeStamp"))
                .forEach(header -> record.addCorrectionFactorValue(header, bigDecimalParser.parse(csvRecord.get(header))));

        return record;
    }

    private void initHeaders(CSVParser parser) {
        headers = parser.getHeaderMap().entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null)
                .sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (headers.size() < 2) {
            throw new FileImportParserException(MessageSeeds.CORRECTIONFACTOR_COLUMNS_LESS_THAN_2);
        }
        if (headers.stream().noneMatch(e -> e.equalsIgnoreCase("timeStamp"))) {
            throw new FileImportParserException(MessageSeeds.CORRECTIONFACTOR_TIMESTAMP_COLUMN_NOT_DEFINED);
        }
        validateCorrectionFactorDurations();
    }

    private void validateCorrectionFactorDurations() {
        List<DurationAttribute> durationAttributes = headers.stream()
                .filter(header -> !header.equalsIgnoreCase("timeStamp"))
                .map(header -> context.getSyntheticLoadProfileService().findCorrectionFactor(header)
                        .orElseThrow(() -> new FileImportParserException(MessageSeeds.CORRECTIONFACTOR_HEADER_NOT_FOUND, header)).getDuration())
                .collect(Collectors.toList());

        if (durationAttributes.stream().filter(durationAttribute -> !durationAttribute.equals(durationAttributes.get(0))).findAny().isPresent()) {
            throw new FileImportParserException(MessageSeeds.CORRECTIONFACTOR_DURATION_ATTRIBUTE_NOT_THE_SAME);
        }
    }
}

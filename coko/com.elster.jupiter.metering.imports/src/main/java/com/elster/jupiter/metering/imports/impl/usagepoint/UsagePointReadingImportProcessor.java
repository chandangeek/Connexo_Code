/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;
import com.elster.jupiter.metering.imports.impl.TranslationKeys;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.nls.Thesaurus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class UsagePointReadingImportProcessor {

    private MeteringService meteringService;
    private DataAggregationService dataAggregationService;
    private Thesaurus thesaurus;
    private String dateFormat;


    public UsagePointReadingImportProcessor(MeteringDataImporterContext context, DataAggregationService dataAggregationService, String dateFormat) {
        this.meteringService = context.getMeteringService();
        this.dataAggregationService = dataAggregationService;
        this.thesaurus = context.getThesaurus();
        this.dateFormat = dateFormat;
    }

    public void process(UsagePointImportRecordModel usagePointRecord) throws UsagePointReadingImportProcessorException {
        UsagePoint usagePoint = this.meteringService.findUsagePointByName(usagePointRecord.getUsagePointName())
                .orElseThrow(() -> new UsagePointReadingImportProcessorException(thesaurus.getFormat(TranslationKeys.Labels.UP_READING_INVALID_UP_NAME).format(usagePointRecord.getUsagePointName())));
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = usagePoint.getEffectiveMetrologyConfiguration(usagePointRecord.getReadingDate())
                .orElseThrow(() -> new UsagePointReadingImportProcessorException(thesaurus.getFormat(TranslationKeys.Labels.UP_READING_NO_LINKED_METRO).format(DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneId
                        .systemDefault()).format(Instant.ofEpochSecond(usagePointRecord.getReadingDate().getEpochSecond())))));
        List<MetrologyContract> contracts = effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration().getContracts();
        MetrologyContract metrologyContract = contracts.stream()
                .filter(contract -> contract.getMetrologyPurpose().getName().equals(usagePointRecord.getPurpose()))
                .findFirst()
                .orElseThrow(() -> new UsagePointReadingImportProcessorException(thesaurus.getFormat(TranslationKeys.Labels.UP_READING_INVALID_PURPOSE).format(usagePointRecord.getPurpose())));
        for (Map.Entry<String, BigDecimal> typeValue : usagePointRecord.getMapBetweenReadingTypeAndValue().entrySet()) {
            ReadingTypeDeliverable readingTypeDeliverable = metrologyContract.getDeliverables()
                    .stream()
                    .filter(red -> red.getReadingType().getMRID().equals(typeValue.getKey()))
                    .findFirst()
                    .orElseThrow(() -> new UsagePointReadingImportProcessorException(thesaurus.getFormat(TranslationKeys.Labels.UP_READING_INVALID_MRID).format(typeValue.getKey())));
            IntervalReadingImpl intervalReading = IntervalReadingImpl.of(usagePointRecord.getReadingDate(), typeValue.getValue());
            // intervalReading.addQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC).getCode(), "");
            intervalReading.addQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED).getCode(), ""); // lori
            dataAggregationService.edit(usagePoint, metrologyContract, readingTypeDeliverable, QualityCodeSystem.MDM).update(intervalReading).save();
        }
    }
}

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
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component(name = "com.elster.jupiter.metering.imports.impl.usagepoint.UsagePointReadingImportProcessor", service = {UsagePointReadingImportProcessor.class})
public class UsagePointReadingImportProcessor {

    private MeteringService meteringService;
    private DataAggregationService dataAggregationService;


    public UsagePointReadingImportProcessor() {
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setDataAggregationService(DataAggregationService dataAggregationService) {
        this.dataAggregationService = dataAggregationService;
    }

    public void process(UsagePointImportRecordModel usagePointRecord) throws UsagePointReadingImportProcessorException {
        UsagePoint usagePoint = this.meteringService.findUsagePointByName(usagePointRecord.getUsagePointName())
                .orElseThrow(() -> new UsagePointReadingImportProcessorException("Invalid usage point name"));
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .orElseThrow(() -> new UsagePointReadingImportProcessorException("No meterology config found!"));
        List<MetrologyContract> contracts = effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration().getContracts();
        MetrologyContract metrologyContract = contracts.stream()
                .filter(contract -> contract.getMetrologyPurpose().getName().equals(usagePointRecord.getPurpose()))
                .findFirst()
                .orElseThrow(() -> new UsagePointReadingImportProcessorException("Invalid purpose"));
        for (Map.Entry<String, BigDecimal> typeValue : usagePointRecord.getMapBetweenReadingTypeAndValue().entrySet()) {
            ReadingTypeDeliverable readingTypeDeliverable = metrologyContract.getDeliverables()
                    .stream()
                    .filter(red -> red.getReadingType().getMRID().equals(typeValue.getKey()))
                    .findFirst()
                    .orElseThrow(() -> new UsagePointReadingImportProcessorException("Invalid Reading type"));
            IntervalReadingImpl intervalReading = IntervalReadingImpl.of(usagePointRecord.getReadingDate(), typeValue.getValue());
            intervalReading.addQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.PROJECTEDGENERIC).getCode(), "");
            dataAggregationService.edit(usagePoint, metrologyContract, readingTypeDeliverable, QualityCodeSystem.MDM).update(intervalReading).save();
        }
    }
}

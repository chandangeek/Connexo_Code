/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.config.DefaultReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttribute;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

public class InstallerV10_7_1Impl implements FullInstaller {
    private final ServerMetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public InstallerV10_7_1Impl(ServerMetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Upgrade MeasurementKind for AverageVoltage",
                this::upgradeMeasurementKindForAverageVoltage,
                logger
        );
    }

    private void upgradeMeasurementKindForAverageVoltage() {
        Optional<? extends ReadingTypeTemplate> readingTypeTemplateOpt = metrologyConfigurationService.findReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE);
        if (readingTypeTemplateOpt.isPresent()) {
            ReadingTypeTemplateAttribute attribute = readingTypeTemplateOpt.get().getAttribute(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND);
            readingTypeTemplateOpt.get().startUpdate()
                    .setAttribute(ReadingTypeTemplateAttributeName.MEASUREMENT_KIND, 158, 158)
                    .done();
        }
    }
}

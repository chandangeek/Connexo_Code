package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.DefaultReadingTypeTemplate;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Optional;

class UpgraderV10_3 implements Upgrader {

    private static final String BULK_A_PLUS_WH = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private final DataModel dataModel;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final MeteringService meteringService;

    @Inject
    UpgraderV10_3(DataModel dataModel, MetrologyConfigurationService metrologyConfigurationService, MeteringService meteringService) {
        super();
        this.dataModel = dataModel;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringService = meteringService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());

        upgradeMetrologyConfiguration();
    }

    private void upgradeMetrologyConfiguration() {
        Optional<MetrologyConfiguration> metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration("Residential net metering (consumption)");
        Optional<MeterRole> meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey());
        Optional<ReadingTypeTemplate> readingTypeTemplate = metrologyConfigurationService.findReadingTypeTemplate(DefaultReadingTypeTemplate.BULK_A_PLUS
                .getNameTranslation()
                .getDefaultFormat());
        Optional<MetrologyPurpose> purposeInformation = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION);
        Optional<MetrologyContract> contract = metrologyConfiguration.flatMap(mc -> mc.getContracts()
                .stream()
                .filter(mct -> purposeInformation.isPresent() && purposeInformation.get()
                        .equals(mct.getMetrologyPurpose()))
                .findFirst());

        if (contract.isPresent() && meterRole.isPresent() && readingTypeTemplate.isPresent()) {
            ReadingType readingTypeAplusWh = meteringService.findReadingTypes(Collections.singletonList(BULK_A_PLUS_WH))
                    .stream()
                    .findFirst()
                    .orElseGet(() -> meteringService.createReadingType(BULK_A_PLUS_WH, "A+"));
            ReadingTypeRequirement requirementAplusRegister = ((UsagePointMetrologyConfiguration) metrologyConfiguration
                    .get()).newReadingTypeRequirement(DefaultReadingTypeTemplate.BULK_A_PLUS.getNameTranslation()
                    .getDefaultFormat(), meterRole.get())
                    .withReadingTypeTemplate(readingTypeTemplate.get());
            contract.get()
                    .addDeliverable(buildFormulaSingleRequirement(((UsagePointMetrologyConfiguration) metrologyConfiguration
                            .get()), readingTypeAplusWh, requirementAplusRegister, "A+ kWh"));
        }
    }

    private ReadingTypeDeliverable buildFormulaSingleRequirement(UsagePointMetrologyConfiguration config, ReadingType readingType, ReadingTypeRequirement requirement, String name) {
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        return builder.build(builder.requirement(requirement));
    }
}
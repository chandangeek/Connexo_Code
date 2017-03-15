package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

class UpgraderV10_3 implements Upgrader {

    private final MetrologyConfigurationService metrologyConfigurationService;
    private final MeteringService meteringService;
    private final MeteringCustomPropertySetsDemoInstaller meteringCustomPropertySetsDemoInstaller;

    @Inject
    UpgraderV10_3(MetrologyConfigurationService metrologyConfigurationService, MeteringService meteringService, MeteringCustomPropertySetsDemoInstaller meteringCustomPropertySetsDemoInstaller) {
        super();
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringService = meteringService;
        this.meteringCustomPropertySetsDemoInstaller = meteringCustomPropertySetsDemoInstaller;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        upgradeUnmeasuredAntennaInstallation();
        meteringCustomPropertySetsDemoInstaller.residentialGasWithCorrection();
    }

    private void upgradeUnmeasuredAntennaInstallation(){
        Optional<MetrologyConfiguration> usagePointMetrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration("Unmeasured antenna installation");
        if (usagePointMetrologyConfiguration.isPresent() && usagePointMetrologyConfiguration.get()
                .getDeliverables()
                .stream()
                .noneMatch(d -> d.getName().equals("Yearly A+ kWh"))) {
            UsagePointMetrologyConfiguration config = (UsagePointMetrologyConfiguration) usagePointMetrologyConfiguration
                    .get();
            ReadingType readingTypeYearlyAplusWh = meteringService.findReadingTypes(Collections.singletonList("1001.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                    .stream()
                    .findFirst()
                    .orElseGet(() -> meteringService.createReadingType("1001.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));

            MetrologyPurpose purposeBilling = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                    .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
            MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);

            RegisteredCustomPropertySet registeredAntennaCPS = config.getCustomPropertySets().stream()
                    .filter(customPropertySet -> customPropertySet.getCustomPropertySetId().equals("com.elster.jupiter.metering.cps.impl.metrology.UsagePointAntennaDomExt"))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Antenna custom property set not found"));
            CustomPropertySet antennaCPS = registeredAntennaCPS.getCustomPropertySet();
            List<PropertySpec> propertySpecs = antennaCPS.getPropertySpecs();

            metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey()).ifPresent(
                    meterRole -> {
                        ReadingTypeDeliverableBuilder yearlyBuilder = config.newReadingTypeDeliverable("Yearly A+ kWh", readingTypeYearlyAplusWh, Formula.Mode.AUTO);
                        FormulaBuilder yearlyAntennaPower = yearlyBuilder.property(antennaCPS, propertySpecs.stream()
                                .filter(propertySpec -> "antennaPower".equals(propertySpec.getName())).findFirst()
                                .orElseThrow(() -> new NoSuchElementException("antennaPower property spec not found")));
                        FormulaBuilder yearlyAntennaCount = yearlyBuilder.property(antennaCPS, propertySpecs.stream()
                                .filter(propertySpec -> "antennaCount".equals(propertySpec.getName())).findFirst()
                                .orElseThrow(() -> new NoSuchElementException("antennaCount property spec not found")));
                        FormulaBuilder yearlyCompositionCPS = yearlyBuilder.multiply(yearlyAntennaPower, yearlyAntennaCount);
                        FormulaBuilder yearlyConstant = yearlyBuilder.multiply(yearlyBuilder.constant(24), yearlyBuilder.constant(365));

                        contractBilling.addDeliverable(yearlyBuilder.build(yearlyBuilder.multiply(yearlyCompositionCPS, yearlyConstant)));
                    });
        }
    }
}

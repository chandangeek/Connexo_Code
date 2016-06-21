package com.elster.jupiter.metering.impl.config;


import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePointTypeInfo;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class MetrologyConfigurationInstaller {

    private ServerMetrologyConfigurationService metrologyConfigurationService;
    private MeteringService meteringService;
    private CustomPropertySetService customPropertySetService;

    public MetrologyConfigurationInstaller(ServerMetrologyConfigurationService metrologyConfigurationService, MeteringService meteringService, CustomPropertySetService customPropertySetService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringService = meteringService;
        this.customPropertySetService = customPropertySetService;
    }

    public void install() {
        residentialProsumerWith1Meter();
        residentialProsumerWith2Meters();
        residentialNetMeteringProduction();
        residentialNetMeteringConsumption();
        threePhasedConsumerWith2ToU();
        residentialConsumerWith4ToU();
        waterConfigurationCI();
        residentialGas();
        unmeasuredAntennaInstallation();
        residentialPrepay();
    }

    private void residentialPrepay() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential prepay").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential prepay", serviceCategory)
                .withDescription("Residential consumer with budget/prepayment meter").create();

        config.addUsagePointRequirement(getUsagePointRequirement("SERVICEKIND", SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("detail.phaseCode", SearchablePropertyOperator.EQUAL,
                PhaseCode.S1N.name(),
                PhaseCode.S2N.name(),
                PhaseCode.S12N.name(),
                PhaseCode.S1.name(),
                PhaseCode.S2.name(),
                PhaseCode.S12.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("usage.point.cps.general.properties.general.prepay", SearchablePropertyOperator.EQUAL, "Y"));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
        config.addMeterRole(meterRole);

        ReadingType readingType15minAplusWh = meteringService.findReadingTypes(Collections.singletonList("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "A+"));

        MetrologyPurpose purposeInformation = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)
                .orElseThrow(() -> new NoSuchElementException("Information metrology purpose not found"));

        MetrologyContract contractInformation = config.addMandatoryMetrologyContract(purposeInformation);

        ReadingTypeRequirement requirementAplus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat())
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingType15minAplusWh, requirementAplus, "Active energy+"));
    }

    private void unmeasuredAntennaInstallation() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Unmeasured antenna installation").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Unmeasured antenna installation", serviceCategory)
                .withDescription("Unmeasured installations which use attributes-based calculations").create();

        config.addUsagePointRequirement(getUsagePointRequirement("SERVICEKIND", SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.UNMEASURED_NON_SDP.name()));

        ReadingType readingTypeMonthlyAplusWh = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));

        MetrologyPurpose purposeBilling = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);

        RegisteredCustomPropertySet registeredExampleCPS = customPropertySetService.findActiveCustomPropertySet("com.elster.jupiter.metering.cps.ExampleCPS")
                .orElseThrow(() -> new NoSuchElementException("ExampleCPS custom property set not found"));
        config.addCustomPropertySet(registeredExampleCPS);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Monthly A+ kWh", readingTypeMonthlyAplusWh, Formula.Mode.EXPERT);
        CustomPropertySet exampleCPS = registeredExampleCPS.getCustomPropertySet();
        List<PropertySpec> propertySpecs = exampleCPS.getPropertySpecs();
        FormulaBuilder antennaPower = builder.property(exampleCPS, propertySpecs.stream().filter("antennaPower"::equals).findFirst()
                .orElseThrow(() -> new NoSuchElementException("antennaPower property spec not found")));
        FormulaBuilder antennaCount = builder.property(exampleCPS, propertySpecs.stream().filter("antennaCount"::equals).findFirst()
                .orElseThrow(() -> new NoSuchElementException("antennaCount property spec not found")));
        FormulaBuilder compositionCPS = builder.multiply(antennaPower, antennaCount);
        FormulaBuilder monthlyConstant = builder.multiply(builder.constant(24), builder.constant(30));

        contractBilling.addDeliverable(builder.build(builder.multiply(compositionCPS, monthlyConstant)));
    }

    private void residentialProsumerWith1Meter() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential prosumer with 1 meter").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential prosumer with 1 meter", serviceCategory)
                .withDescription("Typical installation for residential prosumers with smart meter").create();

        config.addUsagePointRequirement(getUsagePointRequirement("SERVICEKIND", SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("detail.phaseCode", SearchablePropertyOperator.EQUAL,
                PhaseCode.S1N.name(),
                PhaseCode.S2N.name(),
                PhaseCode.S12N.name(),
                PhaseCode.S1.name(),
                PhaseCode.S2.name(),
                PhaseCode.S12.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
        config.addMeterRole(meterRole);

        ReadingType readingTypeMonthlyAplusWh = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeMonthlyAminusWh = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));
        ReadingType readingTypeDailyAplusWh = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeDailyAminusWh = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("11.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));
        ReadingType readingType15minAplusWh = meteringService.findReadingTypes(Collections.singletonList("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
        ReadingType readingType15minAminusWh = meteringService.findReadingTypes(Collections.singletonList("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));

        MetrologyPurpose purposeBilling = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
        MetrologyPurpose purposeInformation = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)
                .orElseThrow(() -> new NoSuchElementException("Information metrology purpose not found"));

        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);
        MetrologyContract contractInformation = config.addMetrologyContract(purposeInformation);

        ReadingTypeRequirement requirementAplus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat())
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        ReadingTypeRequirement requirementAminus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation().getDefaultFormat())
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_MINUS));

        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusWh, requirementAplus, "Monthly A+ kWh"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAminusWh, requirementAminus, "Monthly A- kWh"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyAplusWh, requirementAplus, "Daily A+ kWh"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyAminusWh, requirementAminus, "Daily A- kWh"));
        contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingType15minAplusWh, requirementAplus, "15-min A+ kWh"));
        contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingType15minAminusWh, requirementAminus, "15-min A- kWh"));
    }

    private void residentialProsumerWith2Meters() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential prosumer with 2 meters")
                .isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential prosumer with 2 meters", serviceCategory)
                .withDescription("Typical installation for residential prosumers with dumb meters").create();

        config.addUsagePointRequirement(getUsagePointRequirement("SERVICEKIND", SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("detail.phaseCode", SearchablePropertyOperator.EQUAL,
                PhaseCode.S1N.name(),
                PhaseCode.S2N.name(),
                PhaseCode.S12N.name(),
                PhaseCode.S1.name(),
                PhaseCode.S2.name(),
                PhaseCode.S12.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRoleConsumption = metrologyConfigurationService.findMeterRole(DefaultMeterRole.CONSUMPTION.getKey())
                .orElseThrow(() -> new NoSuchElementException("Consumption meter role not found"));
        config.addMeterRole(meterRoleConsumption);
        MeterRole meterRoleProduction = metrologyConfigurationService.findMeterRole(DefaultMeterRole.PRODUCTION.getKey())
                .orElseThrow(() -> new NoSuchElementException("Production meter role not found"));
        config.addMeterRole(meterRoleProduction);

        ReadingType readingTypeMonthlyAplusWh = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeMonthlyAminusWh = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));

        MetrologyPurpose purposeBilling = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));

        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);

        ReadingTypeRequirement requirementAplus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat())
                .withMeterRole(meterRoleConsumption).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        ReadingTypeRequirement requirementAminus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation().getDefaultFormat())
                .withMeterRole(meterRoleProduction).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_MINUS));

        contractBilling.addDeliverable(buildFormulaRequirementMax(config, readingTypeMonthlyAplusWh, requirementAplus, requirementAminus, "Monthly A+ kWh"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAminusWh, requirementAminus, "Monthly A- kWh"));
    }

    private void residentialNetMeteringProduction() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential net metering (production)")
                .isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential net metering (production)", serviceCategory)
                .withDescription("Residential producer").create();

        config.addUsagePointRequirement(getUsagePointRequirement("SERVICEKIND", SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("detail.phaseCode", SearchablePropertyOperator.EQUAL,
                PhaseCode.S1N.name(),
                PhaseCode.S2N.name(),
                PhaseCode.S12N.name(),
                PhaseCode.S1.name(),
                PhaseCode.S2.name(),
                PhaseCode.S12.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
        config.addMeterRole(meterRole);

        ReadingType readingTypeDailyAplusWh = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeMonthlyAplusWh = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));

        MetrologyPurpose purposeBilling = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);

        ReadingTypeRequirement requirementAminus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation().getDefaultFormat())
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_MINUS));

        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyAplusWh, requirementAminus, "Daily A- kWh"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusWh, requirementAminus, "Monthly A- kWh"));
    }

    private void residentialNetMeteringConsumption() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential net metering (consumption)")
                .isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential net metering (consumption)", serviceCategory)
                .withDescription("Residential consumer").create();

        config.addUsagePointRequirement(getUsagePointRequirement("SERVICEKIND", SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("detail.phaseCode", SearchablePropertyOperator.EQUAL,
                PhaseCode.S1N.name(),
                PhaseCode.S2N.name(),
                PhaseCode.S12N.name(),
                PhaseCode.S1.name(),
                PhaseCode.S2.name(),
                PhaseCode.S12.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
        config.addMeterRole(meterRole);

        ReadingType readingTypeDailyAplusWh = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeMonthlyAplusWh = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
        ReadingType readingType15minAplusWh = meteringService.findReadingTypes(Collections.singletonList("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));

        MetrologyPurpose purposeBilling = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
        MetrologyPurpose purposeInformation = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)
                .orElseThrow(() -> new NoSuchElementException("Information metrology purpose not found"));

        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);
        MetrologyContract contractInformation = config.addMandatoryMetrologyContract(purposeInformation);

        ReadingTypeRequirement requirementAplus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat())
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyAplusWh, requirementAplus, "Daily A+ kWh"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusWh, requirementAplus, "Monthly A+ kWh"));
        contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingType15minAplusWh, requirementAplus, "15-min A+ kWh"));
    }

    private void threePhasedConsumerWith2ToU() {
        if (metrologyConfigurationService.findMetrologyConfiguration("C&I 3-phased consumer with smart meter with 2 ToU")
                .isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("C&I 3-phased consumer with smart meter with 2 ToU", serviceCategory)
                .withDescription("C&I 3-phased consumer with smart meter 2 ToU").create();

        config.addUsagePointRequirement(getUsagePointRequirement("SERVICEKIND", SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("detail.phaseCode", SearchablePropertyOperator.EQUAL,
                PhaseCode.ABC.name(),
                PhaseCode.ABCN.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
        config.addMeterRole(meterRole);

        ReadingType readingTypeDailyActiveEnergyToU1 = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("11.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeDailyActiveEnergyToU2 = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.1.1.12.0.0.0.0.2.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("11.0.0.4.1.1.12.0.0.0.0.2.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeDailyReactiveEnergyToU1 = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.2.1.12.0.0.0.0.1.0.0.0.3.73.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("11.0.0.4.2.1.12.0.0.0.0.1.0.0.0.3.73.0", "Reactive energy+"));
        ReadingType readingTypeDailyReactiveEnergyToU2 = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.2.1.12.0.0.0.0.2.0.0.0.3.73.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("11.0.0.4.2.1.12.0.0.0.0.2.0.0.0.3.73.0", "Reactive energy+"));
        ReadingType readingTypeMonthlyActiveEnergyToU1 = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeMonthlyActiveEnergyToU2 = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.1.12.0.0.0.0.2.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.2.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeMonthlyReactiveEnergyToU1 = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.2.1.12.0.0.0.0.1.0.0.0.3.73.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.2.1.12.0.0.0.0.1.0.0.0.3.73.0", "Reactive energy+"));
        ReadingType readingTypeMonthlyReactiveEnergyToU2 = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.2.1.12.0.0.0.0.2.0.0.0.3.73.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.2.1.12.0.0.0.0.2.0.0.0.3.73.0", "Reactive energy+"));
        ReadingType readingTypeAverageVoltagePhaseA = meteringService.findReadingTypes(Collections.singletonList("0.2.7.6.0.1.158.0.0.0.0.0.0.0.128.0.29.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.128.0.29.0", "Average voltage"));
        ReadingType readingTypeAverageVoltagePhaseB = meteringService.findReadingTypes(Collections.singletonList("0.2.7.6.0.1.158.0.0.0.0.0.0.0.64.0.29.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.64.0.29.0", "Average voltage"));
        ReadingType readingTypeAverageVoltagePhaseC = meteringService.findReadingTypes(Collections.singletonList("0.2.7.6.0.1.158.0.0.0.0.0.0.0.32.0.29.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.2.7.6.0.1.158.0.0.0.0.0.0.0.32.0.29.0", "Average voltage"));

        MetrologyPurpose purposeBilling = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
        MetrologyPurpose purposeVoltageMonitoring = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.VOLTAGE_MONITORING)
                .orElseThrow(() -> new NoSuchElementException("Voltage monitoring metrology purpose not found"));

        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);
        MetrologyContract contractVoltageMonitoring = config.addMandatoryMetrologyContract(purposeVoltageMonitoring);

        ReadingTypeRequirement requirementAplusToU1 = config.newReadingTypeRequirement("Active energy+ ToU1")
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 1);
        ReadingTypeRequirement requirementAplusToU2 = config.newReadingTypeRequirement("Active energy+ ToU2")
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 2);
        ReadingTypeRequirement requirementReactiveEnergyPlusToU1 = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.REACTIVE_ENERGY_PLUS.getNameTranslation().getDefaultFormat() + " ToU1")
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.REACTIVE_ENERGY_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 1);
        ReadingTypeRequirement requirementReactiveEnergyPlusToU2 = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.REACTIVE_ENERGY_PLUS.getNameTranslation().getDefaultFormat() + " ToU2")
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.REACTIVE_ENERGY_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 2);
        ReadingTypeRequirement requirementAverageVoltagePhaseA = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase A")
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 128);
        ReadingTypeRequirement requirementAverageVoltagePhaseB = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase B")
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 64);
        ReadingTypeRequirement requirementAverageVoltagePhaseC = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE.getNameTranslation().getDefaultFormat() + " phase C")
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.AVERAGE_VOLTAGE))
                .overrideAttribute(ReadingTypeTemplateAttributeName.PHASE, 32);

        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyActiveEnergyToU1, requirementAplusToU1, "Daily active energy kWh ToU1"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyActiveEnergyToU2, requirementAplusToU2, "Daily active energy kWh ToU2"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyReactiveEnergyToU1, requirementReactiveEnergyPlusToU1, "Daily reactive energy kVArh ToU1"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyReactiveEnergyToU2, requirementReactiveEnergyPlusToU2, "Daily reactive energy kVArh ToU2"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyActiveEnergyToU1, requirementAplusToU1, "Monthly active energy kWh ToU1"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyActiveEnergyToU2, requirementAplusToU2, "Monthly active energy kWh ToU2"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyReactiveEnergyToU1, requirementReactiveEnergyPlusToU1, "Monthly reactive energy kVArh ToU1"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyReactiveEnergyToU2, requirementReactiveEnergyPlusToU2, "Monthly reactive energy kVArh ToU2"));
        contractVoltageMonitoring.addDeliverable(buildFormulaSingleRequirement(config, readingTypeAverageVoltagePhaseA, requirementAverageVoltagePhaseA, "Hourly average voltage V phase 1 vs N"));
        contractVoltageMonitoring.addDeliverable(buildFormulaSingleRequirement(config, readingTypeAverageVoltagePhaseB, requirementAverageVoltagePhaseB, "Hourly average voltage V phase 2 vs N"));
        contractVoltageMonitoring.addDeliverable(buildFormulaSingleRequirement(config, readingTypeAverageVoltagePhaseC, requirementAverageVoltagePhaseC, "Hourly average voltage V phase 3 vs N"));
    }

    private void residentialConsumerWith4ToU() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential consumer with 4 ToU").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential consumer with 4 ToU", serviceCategory)
                .withDescription("Residential consumer with 4 ToU").create();

        config.addUsagePointRequirement(getUsagePointRequirement("SERVICEKIND", SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("detail.phaseCode", SearchablePropertyOperator.EQUAL,
                PhaseCode.S1N.name(),
                PhaseCode.S2N.name(),
                PhaseCode.S12N.name(),
                PhaseCode.S1.name(),
                PhaseCode.S2.name(),
                PhaseCode.S12.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
        config.addMeterRole(meterRole);

        ReadingType readingTypeMonthlyAplusToU1 = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeMonthlyAplusToU2 = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.1.12.0.0.0.0.2.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.2.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeMonthlyAplusToU3 = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.1.12.0.0.0.0.3.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.3.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeMonthlyAplusToU4 = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.1.12.0.0.0.0.4.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.4.0.0.0.3.72.0", "A+"));

        MetrologyPurpose purposeBilling = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);

        ReadingTypeRequirement requirementAplusToU1 = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat() + " ToU1")
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 1);
        ReadingTypeRequirement requirementAplusToU2 = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat() + " ToU2")
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 2);
        ReadingTypeRequirement requirementAplusToU3 = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat() + " ToU3")
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 3);
        ReadingTypeRequirement requirementAplusToU4 = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat() + " ToU4")
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 4);

        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusToU1, requirementAplusToU1, "Monthly A+ kWh ToU1"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusToU2, requirementAplusToU2, "Monthly A+ kWh ToU2"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusToU3, requirementAplusToU3, "Monthly A+ kWh ToU3"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusToU4, requirementAplusToU4, "Monthly A+ kWh ToU4"));
    }

    private void residentialGas() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential gas").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.GAS)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.GAS));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential gas", serviceCategory)
                .withDescription("Residential gas installation").create();

        config.addUsagePointRequirement(getUsagePointRequirement("SERVICEKIND", SearchablePropertyOperator.EQUAL, ServiceKind.GAS
                .name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP
                .name()));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
        config.addMeterRole(meterRole);

        ReadingType readingTypeDailyVolume = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("11.0.0.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0", "Daily volume m³"));
        ReadingType readingTypeMonthlyVolume = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0", "Monthly volume m³"));
        ReadingType readingTypeHourlyVolume = meteringService.findReadingTypes(Collections.singletonList("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0", "Hourly volume m³"));


        MetrologyPurpose purposeBilling = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);
        MetrologyPurpose purposeInformation = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)
                .orElseThrow(() -> new NoSuchElementException("Information metrology purpose not found"));
        MetrologyContract contractInformation = config.addMetrologyContract(purposeInformation);

        ReadingTypeRequirement requirementGasVolume = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.GAS_VOLUME
                .getNameTranslation()
                .getDefaultFormat())
                .withMeterRole(meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.GAS_VOLUME))
                .overrideAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, 42);

        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyVolume, requirementGasVolume, "Daily volume m³"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyVolume, requirementGasVolume, "Monthly volume m³"));
        contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingTypeHourlyVolume, requirementGasVolume, "Hourly volume m³"));
    }

    private void waterConfigurationCI() {
        if (metrologyConfigurationService.findMetrologyConfiguration("C&I water configuration").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.WATER)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.WATER));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("C&I water configuration", serviceCategory)
                .withDescription("C&I water configuration with 2 meters").create();

        config.addUsagePointRequirement(getUsagePointRequirement("SERVICEKIND", SearchablePropertyOperator.EQUAL, ServiceKind.WATER
                .name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP
                .name()));

        MeterRole meterRolePeakConsumption = metrologyConfigurationService.findMeterRole(DefaultMeterRole.PEAK_CONSUMPTION
                .getKey())
                .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
        config.addMeterRole(meterRolePeakConsumption);
        MeterRole meterRoleOffPeakConsumption = metrologyConfigurationService.findMeterRole(DefaultMeterRole.OFF_PEAK_CONSUMPTION
                .getKey())
                .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
        config.addMeterRole(meterRoleOffPeakConsumption);

        ReadingType readingTypeMonthlyConsumption = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0"))
                .stream()
                .findFirst()
                .orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.9.58.0.0.0.0.0.0.0.0.0.42.0", "Monthly consumption m³"));


        MetrologyPurpose purposeBilling = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);

        ReadingTypeRequirement requiremenPeakConsumption = config.newReadingTypeRequirement("Peak consumption")
                .withMeterRole(meterRolePeakConsumption)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.WATER_VOLUME))
                .overrideAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, 42);
        ReadingTypeRequirement requirementOffPeakConsumption = config.newReadingTypeRequirement("Off peak consumption")
                .withMeterRole(meterRoleOffPeakConsumption)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.WATER_VOLUME))
                .overrideAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, 42);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Monthly consumption m³", readingTypeMonthlyConsumption, Formula.Mode.EXPERT);
        contractBilling.addDeliverable(builder.build(builder.plus(builder.requirement(requiremenPeakConsumption), builder
                .requirement(requirementOffPeakConsumption))));

    }

    private ReadingTypeDeliverable buildFormulaSingleRequirement(UsagePointMetrologyConfiguration config, ReadingType readingType, ReadingTypeRequirement requirement, String name) {
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable(name, readingType, Formula.Mode.EXPERT);
        return builder.build(builder.requirement(requirement));
    }

    private ReadingTypeDeliverable buildFormulaRequirementMax(UsagePointMetrologyConfiguration config, ReadingType readingType,
                                                              ReadingTypeRequirement requirementPlus, ReadingTypeRequirement requirementMinus, String name) {

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable(name, readingType, Formula.Mode.EXPERT);
        return builder.build(builder.maximum(builder.minus(builder.requirement(requirementPlus), builder.requirement(requirementMinus)), builder.constant(0)));
    }

    private ReadingTypeTemplate getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate defaultReadingTypeTemplate) {
        return metrologyConfigurationService.findReadingTypeTemplate(defaultReadingTypeTemplate)
                .orElseThrow(() -> new NoSuchElementException("Default reading type template not found"));
    }

    private SearchablePropertyValue.ValueBean getUsagePointRequirement(String property, SearchablePropertyOperator operator, String... values) {
        SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
        valueBean.propertyName = property;
        valueBean.operator = operator;
        valueBean.values = Arrays.asList(values);
        return valueBean;
    }
}

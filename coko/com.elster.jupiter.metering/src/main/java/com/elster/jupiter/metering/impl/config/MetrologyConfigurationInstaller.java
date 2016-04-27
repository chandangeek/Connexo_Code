package com.elster.jupiter.metering.impl.config;


import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePointTypeInfo;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;

public class MetrologyConfigurationInstaller {

    private ServerMetrologyConfigurationService metrologyConfigurationService;
    private MeteringService meteringService;

    public MetrologyConfigurationInstaller(ServerMetrologyConfigurationService metrologyConfigurationService, MeteringService meteringService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringService = meteringService;
    }

    public void install() {
        residentialProsumerWith1Meter();
        residentialProsumerWith2Meters();
        residentialNetMeteringProduction();
        residentialNetMeteringConsumption();
        threePhasedConsumerWith2ToU();
    }

    private void residentialProsumerWith1Meter() {
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
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeMonthlyAminusWh = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));
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

        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusWh, requirementAplus, "Monthly A+"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAminusWh, requirementAminus, "Monthly A- kWh"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyAplusWh, requirementAplus, "Daily A+ kWh"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyAminusWh, requirementAminus, "Daily A- kWh"));
        contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingType15minAplusWh, requirementAplus, "15-min A+ kWh = A+"));
        contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingType15minAminusWh, requirementAminus, "15-min A- kWh"));
    }

    private void residentialProsumerWith2Meters() {
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

        contractBilling.addDeliverable(buildFormulaSingleRequirementMax(config, readingTypeMonthlyAplusWh, requirementAplus, requirementAminus, "Monthly A+ kWh"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAminusWh, requirementAminus, "Monthly A- kWh"));
    }

    private void residentialNetMeteringProduction() {
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

        ReadingType readingTypeDailyAminusWh = meteringService.findReadingTypes(Collections.singletonList("11.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("11.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));
        ReadingType readingTypeMonthlyAminusWh = meteringService.findReadingTypes(Collections.singletonList("13.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));
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

        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyAminusWh, requirementAplus, "Daily A+ kWh"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAminusWh, requirementAplus, "Monthly A+ kWh"));
        contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingType15minAplusWh, requirementAplus, "15-min A+ kWh = A+"));
    }

    private void threePhasedConsumerWith2ToU() {
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
        ReadingType readingTypeMonthlyActivePower = meteringService.findReadingTypes(Collections.singletonList("13.0.0.12.1.1.37.0.0.0.0.0.0.0.0.3.38.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.12.1.1.37.0.0.0.0.0.0.0.0.3.38.0", "Active power+"));
        ReadingType readingTypeMonthlyReactivePower = meteringService.findReadingTypes(Collections.singletonList("13.0.0.12.1.1.37.0.0.0.0.0.0.0.0.3.63.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.12.1.1.37.0.0.0.0.0.0.0.0.3.63.0", "Reactive power+"));
        ReadingType readingTypeMonthlyApparentPower = meteringService.findReadingTypes(Collections.singletonList("13.2.0.6.1.1.37.0.0.0.0.0.0.0.0.0.61.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.2.0.6.1.1.37.0.0.0.0.0.0.0.0.0.61.0", "Apparent power+"));
        ReadingType readingTypeMonthlyPowerFactor = meteringService.findReadingTypes(Collections.singletonList("13.0.0.6.1.1.38.0.0.0.0.0.0.0.0.0.0.0"))
                .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.6.1.1.38.0.0.0.0.0.0.0.0.0.0.0", "Active power factor+"));

        MetrologyPurpose purposeBilling = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);

        ReadingTypeRequirement requirementAplusToU1 = config.newReadingTypeRequirement("Daily active energy kWh ToU1")
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 1);
        ReadingTypeRequirement requirementAplusToU2 = config.newReadingTypeRequirement("Daily active energy kWh ToU2")
                .withMeterRole(meterRole).withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS))
                .overrideAttribute(ReadingTypeTemplateAttributeName.TIME_OF_USE, 2);

        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyActiveEnergyToU1, requirementAplusToU1, "Daily active energy kWh ToU1"));
        contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeDailyActiveEnergyToU2, requirementAplusToU2, "Daily active energy kWh ToU2"));
    }

    private ReadingTypeDeliverable buildFormulaSingleRequirement(UsagePointMetrologyConfiguration config, ReadingType readingType, ReadingTypeRequirement requirement, String name) {
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        return builder.build(builder.requirement(requirement));
    }

    private ReadingTypeDeliverable buildFormulaSingleRequirementMax(UsagePointMetrologyConfiguration config, ReadingType readingType,
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

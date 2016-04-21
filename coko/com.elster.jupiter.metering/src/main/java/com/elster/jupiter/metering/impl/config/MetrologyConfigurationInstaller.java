package com.elster.jupiter.metering.impl.config;


import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.transaction.TransactionContext;

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

    public void install(){
        residentialProsumerWith1Meter();
        residentialProsumerWith2Meters();
    }

    private void residentialProsumerWith1Meter(){
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                    .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
            UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential prosumer with 1 meter", serviceCategory).create();


            config.addUsagePointRequirement(getUsagePointRequirement("SERVICEKIND", SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY.name()));
//            config.addUsagePointRequirement(getUsagePointRequirement("PHASECODE", SearchablePropertyOperator.EQUAL));
//            config.addUsagePointRequirement(getUsagePointRequirement("TYPE", SearchablePropertyOperator.EQUAL));

            MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                    .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
            config.addMeterRole(meterRole);

            ReadingType readingTypeMonthlyAplusWh = meteringService.findReadingTypes(Collections.singletonList("13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                    .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
            ReadingType readingTypeMonthlyAminusWh = meteringService.findReadingTypes(Collections.singletonList("13.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                    .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));
            ReadingType readingType15minAplusWh = meteringService.findReadingTypes(Collections.singletonList("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                    .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
            ReadingType readingType15minAminusWh = meteringService.findReadingTypes(Collections.singletonList("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                    .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));
            ReadingType readingTypeHourlyAplusWh = meteringService.findReadingTypes(Collections.singletonList("0.0.7.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                    .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.0.7.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
            ReadingType readingTypeHourlyAminusWh = meteringService.findReadingTypes(Collections.singletonList("0.0.7.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                    .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.0.7.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));

            MetrologyPurpose purposeBilling = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                    .orElseThrow(() -> new NoSuchElementException("Default metrology purpose not found"));
            MetrologyPurpose purposeInformation = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)
                    .orElseThrow(() -> new NoSuchElementException("Default metrology purpose not found"));

            MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);
            MetrologyContract contractInformation = config.addMetrologyContract(purposeInformation);


            ReadingTypeRequirement requirementAplus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat())
                    .withMeterRole(meterRole).withReadingTypeTemplate(getDeaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

            ReadingTypeRequirement requirementAminus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation().getDefaultFormat())
                    .withMeterRole(meterRole).withReadingTypeTemplate(getDeaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

            contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAplusWh, requirementAplus));
            contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAminusWh, requirementAminus));
            contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingType15minAplusWh, requirementAplus));
            contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingType15minAminusWh, requirementAminus));
            contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingTypeHourlyAplusWh, requirementAplus));
            contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingTypeHourlyAminusWh, requirementAminus));

            config.activate();
    }

    private void residentialProsumerWith2Meters(){
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                    .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
            UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential prosumer with 2 meters", serviceCategory).create();


            config.addUsagePointRequirement(getUsagePointRequirement("SERVICEKIND", SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY.name()));
//            config.addUsagePointRequirement(getUsagePointRequirement("PHASECODE", SearchablePropertyOperator.EQUAL));
//            config.addUsagePointRequirement(getUsagePointRequirement("TYPE", SearchablePropertyOperator.EQUAL));

            MeterRole meterRoleConsumption = metrologyConfigurationService.findMeterRole(DefaultMeterRole.CONSUMPTION.getKey())
                    .orElseThrow(() -> new NoSuchElementException("Consumption meter role not found"));
            config.addMeterRole(meterRoleConsumption);
            MeterRole meterRoleProduction = metrologyConfigurationService.findMeterRole(DefaultMeterRole.PRODUCTION.getKey())
                    .orElseThrow(() -> new NoSuchElementException("Production meter role not found"));
            config.addMeterRole(meterRoleProduction);

            ReadingType readingTypeMonthlyAplusWh = meteringService.findReadingTypes(Collections.singletonList("13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                    .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
            ReadingType readingTypeMonthlyAminusWh = meteringService.findReadingTypes(Collections.singletonList("13.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                    .stream().findFirst().orElseGet(() -> meteringService.createReadingType("13.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));
            ReadingType readingType15minAplusWh = meteringService.findReadingTypes(Collections.singletonList("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                    .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
            ReadingType readingType15minAminusWh = meteringService.findReadingTypes(Collections.singletonList("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                    .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));
            ReadingType readingTypeHourlyAplusWh = meteringService.findReadingTypes(Collections.singletonList("0.0.7.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                    .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.0.7.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
            ReadingType readingTypeHourlyAminusWh = meteringService.findReadingTypes(Collections.singletonList("0.0.7.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0"))
                    .stream().findFirst().orElseGet(() -> meteringService.createReadingType("0.0.7.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "A-"));

            MetrologyPurpose purposeBilling = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                    .orElseThrow(() -> new NoSuchElementException("Default metrology purpose not found"));
            MetrologyPurpose purposeInformation = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)
                    .orElseThrow(() -> new NoSuchElementException("Default metrology purpose not found"));

            MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);
            MetrologyContract contractInformation = config.addMetrologyContract(purposeInformation);

            ReadingTypeRequirement requirementAplus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat())
                    .withMeterRole(meterRoleConsumption).withReadingTypeTemplate(getDeaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

            ReadingTypeRequirement requirementAminus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_MINUS.getNameTranslation().getDefaultFormat())
                    .withMeterRole(meterRoleProduction).withReadingTypeTemplate(getDeaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

            contractBilling.addDeliverable(buildFormulaSingleRequirementMax(config, readingTypeMonthlyAplusWh, requirementAplus, requirementAminus));
            contractBilling.addDeliverable(buildFormulaSingleRequirement(config, readingTypeMonthlyAminusWh, requirementAminus));
            contractInformation.addDeliverable(buildFormulaSingleRequirementMax(config, readingType15minAplusWh, requirementAplus, requirementAminus));
            contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingType15minAminusWh, requirementAminus));
            contractInformation.addDeliverable(buildFormulaSingleRequirementMax(config, readingTypeHourlyAplusWh, requirementAplus, requirementAminus));
            contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingTypeHourlyAminusWh, requirementAminus));

            config.activate();
    }

    private ReadingTypeDeliverable buildFormulaSingleRequirement(UsagePointMetrologyConfiguration config, ReadingType readingType, ReadingTypeRequirement requirement){
        String name = readingType.getFullAliasName();
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        return builder.build(builder.requirement(requirement));
    }

    private ReadingTypeDeliverable buildFormulaSingleRequirementMax(UsagePointMetrologyConfiguration config, ReadingType readingType,
                                                                    ReadingTypeRequirement requirementPlus, ReadingTypeRequirement requirementMinus){
        String name = readingType.getFullAliasName();
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable(name, readingType, Formula.Mode.EXPERT);
        return builder.build(builder.maximum(builder.minus(builder.requirement(requirementPlus),builder.requirement(requirementMinus)),builder.constant(0)));
    }

    private ReadingTypeTemplate getDeaultReadingTypeTemplate(DefaultReadingTypeTemplate defaultReadingTypeTemplate){
        return metrologyConfigurationService.findReadingTypeTemplate(defaultReadingTypeTemplate)
                .orElseThrow(() -> new NoSuchElementException("Default reading type template not found"));
    }

    private SearchablePropertyValue.ValueBean getUsagePointRequirement(String property, SearchablePropertyOperator operator, String ... values){
        SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
        valueBean.propertyName = property;
        valueBean.operator = operator;
        valueBean.values = Arrays.asList(values);
        return valueBean;
    }
}

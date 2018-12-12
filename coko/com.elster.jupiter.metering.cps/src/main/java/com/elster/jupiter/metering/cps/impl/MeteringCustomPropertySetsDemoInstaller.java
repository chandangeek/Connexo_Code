/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePointTypeInfo;
import com.elster.jupiter.metering.aggregation.ReadingQualityCommentCategory;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.DefaultReadingTypeTemplate;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttributeName;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.cps.impl.metrology.TranslationKeys;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointAntennaCPS;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointAntennaDomExt;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointContCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointContDomainExtension;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointDecentProdCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointDecentProdDomExt;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointElectricityCorrectionFactorsCPS;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointElectricityCorrectionFactorsDomExt;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointGasCorrectionFactorsCPS;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointGasCorrectionFactorsDomExt;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointMetrologyGeneralCPS;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointMetrologyGeneralDomExt;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointSettlementCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointSettlementDomExt;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstAllCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstAllDomExt;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstEGCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstEGDomExt;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstElectrCPS;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstElectrDE;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TimeZone;

import static com.elster.jupiter.orm.Version.version;

@Component(name = "com.elster.jupiter.metering.cps", service = {TranslationKeyProvider.class, MessageSeedProvider.class}, property = "name=CPM", immediate = true)
public class MeteringCustomPropertySetsDemoInstaller implements TranslationKeyProvider, MessageSeedProvider {

    public static final String COMPONENT_NAME = "CPM";

    private volatile MeteringService meteringService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private MetrologyConfigurationService metrologyConfigurationService;
    private volatile UpgradeService upgradeService;
    private volatile SyntheticLoadProfileService syntheticLoadProfileService;
    private volatile Clock clock;

    @Reference
    @SuppressWarnings("unused") // Used by OSGi framework
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    @SuppressWarnings("unused") // Used by OSGi framework
    public void setSyntheticLoadProfileService(SyntheticLoadProfileService syntheticLoadProfileService){
        this.syntheticLoadProfileService = syntheticLoadProfileService;
    }

    @Reference
    @SuppressWarnings("unused") // Used by OSGi framework
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    @SuppressWarnings("unused") // Used by OSGi framework
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    @SuppressWarnings("unused") // Used by OSGi framework
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    @SuppressWarnings("unused") // Used by OSGi framework
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    @SuppressWarnings("unused") // Used by OSGi framework
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    @SuppressWarnings("unused") // Used by OSGi framework
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Activate
    @SuppressWarnings("unused") // Used by OSGi framework
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        InstallIdentifier identifier = InstallIdentifier.identifier(InsightServiceCategoryCustomPropertySetsCheckList.APPLICATION_NAME, "CPM");
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MeteringService.class).toInstance(meteringService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MetrologyConfigurationService.class).toInstance(metrologyConfigurationService);
                bind(MeteringCustomPropertySetsDemoInstaller.class).toInstance(MeteringCustomPropertySetsDemoInstaller.this);
            }
        });

        this.registerCustomPropertySets();

        upgradeService.register(InstallIdentifier.identifier("Example", "CPM"), dataModel, Installer.class, ImmutableMap.of(version(10, 3), UpgraderV10_3.class));
    }

    @Deactivate
    @SuppressWarnings("unused") // Used by OSGi framework
    public void deactivate() {
        Map<String, CustomPropertySet> customPropertySets = this.getMeteringCustomPropertySets();
        customPropertySets.values().forEach(customPropertySetService::removeCustomPropertySet);
    }

    Map<String, CustomPropertySet>  registerCustomPropertySets() {
        Map<String, CustomPropertySet>  customPropertySets = this.getMeteringCustomPropertySets();
        customPropertySets.values().forEach(customPropertySetService::addCustomPropertySet);
        return customPropertySets;
    }

    private Map<String, CustomPropertySet> getMeteringCustomPropertySets() {
        Map<String, CustomPropertySet> customPropertySetsMap = new HashMap<>();
        customPropertySetsMap.put(UsagePointContrElectrDomExt.class.getName(), new UsagePointContrElectrCPS(propertySpecService, thesaurus));
        customPropertySetsMap.put(UsagePointGeneralDomainExtension.class.getName(), new UsagePointGeneralCustomPropertySet(propertySpecService, thesaurus));
        customPropertySetsMap.put(UsagePointTechElDomExt.class.getName(), new UsagePointTechElCPS(propertySpecService, thesaurus));
        customPropertySetsMap.put(UsagePointTechnicalWGTDomExt.class.getName(), new UsagePointTechnicalWGTCustomPropertySet(propertySpecService, thesaurus));
        customPropertySetsMap.put(UsagePointContDomainExtension.class.getName(), new UsagePointContCustomPropertySet(propertySpecService, thesaurus));
        customPropertySetsMap.put(UsagePointDecentProdDomExt.class.getName(), new UsagePointDecentProdCustomPropertySet(propertySpecService, thesaurus));
        customPropertySetsMap.put(UsagePointMetrologyGeneralDomExt.class.getName(), new UsagePointMetrologyGeneralCPS(propertySpecService, thesaurus));
        customPropertySetsMap.put(UsagePointSettlementDomExt.class.getName(), new UsagePointSettlementCustomPropertySet(propertySpecService, thesaurus));
        customPropertySetsMap.put(UsagePointTechInstAllDomExt.class.getName(), new UsagePointTechInstAllCustomPropertySet(propertySpecService, thesaurus));
        customPropertySetsMap.put(UsagePointTechInstEGDomExt.class.getName(), new UsagePointTechInstEGCustomPropertySet(propertySpecService, thesaurus));
        customPropertySetsMap.put(UsagePointTechInstElectrDE.class.getName(), new UsagePointTechInstElectrCPS(propertySpecService, thesaurus));
        customPropertySetsMap.put(UsagePointAntennaDomExt.class.getName(), new UsagePointAntennaCPS(propertySpecService, thesaurus));
        customPropertySetsMap.put(UsagePointElectricityCorrectionFactorsDomExt.class.getName(), new UsagePointElectricityCorrectionFactorsCPS(propertySpecService, syntheticLoadProfileService, thesaurus));
        customPropertySetsMap.put(UsagePointGasCorrectionFactorsDomExt.class.getName(), new UsagePointGasCorrectionFactorsCPS(propertySpecService, syntheticLoadProfileService, thesaurus));

        return customPropertySetsMap;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }


    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    void unmeasuredAntennaInstallation() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Unmeasured antenna installation").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
        RegisteredCustomPropertySet registeredAntennaCPS = customPropertySetService.findActiveCustomPropertySet("com.elster.jupiter.metering.cps.impl.metrology.UsagePointAntennaDomExt")
                .orElseThrow(() -> new NoSuchElementException("Antenna custom property set not found"));
        UsagePointMetrologyConfiguration config =
                metrologyConfigurationService
                        .newUsagePointMetrologyConfiguration("Unmeasured antenna installation", serviceCategory)
                        .withDescription("Unmeasured installations which use attributes-based calculations")
                        .withCustomPropertySet(registeredAntennaCPS)
                        .withUsagePointRequirement(
                                getUsagePointRequirement(
                                        "SERVICEKIND",
                                        SearchablePropertyOperator.EQUAL,
                                        ServiceKind.ELECTRICITY.name()))
                        .withUsagePointRequirement(
                                getUsagePointRequirement(
                                        "type",
                                        SearchablePropertyOperator.EQUAL,
                                        UsagePointTypeInfo.UsagePointType.MEASURED_NON_SDP.name()))
                        .create();

        ReadingType readingTypeMonthlyAplusWh = meteringService.getReadingType("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0")
                .orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
        ReadingType readingTypeYearlyAplusWh = meteringService.getReadingType("1001.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0")
                .orElseGet(() -> meteringService.createReadingType("1001.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));

        MetrologyPurpose billingPurpose = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
        MetrologyContract billingContract = config.addMandatoryMetrologyContract(billingPurpose);

        config.addCustomPropertySet(registeredAntennaCPS);

        CustomPropertySet antennaCPS = registeredAntennaCPS.getCustomPropertySet();
        List<PropertySpec> propertySpecs = antennaCPS.getPropertySpecs();
        PropertySpec antennaPowerPropertySpec = propertySpecs.stream()
                .filter(propertySpec -> "antennaPower".equals(propertySpec.getName())).findFirst()
                .orElseThrow(() -> new NoSuchElementException("antennaPower property spec not found"));
        PropertySpec antennaCountPropertySpec = propertySpecs.stream()
                .filter(propertySpec -> "antennaCount".equals(propertySpec.getName())).findFirst()
                .orElseThrow(() -> new NoSuchElementException("antennaCount property spec not found"));

        ReadingTypeDeliverableBuilder monthlyBuilder = billingContract.newReadingTypeDeliverable("Monthly A+ kWh", readingTypeMonthlyAplusWh, Formula.Mode.AUTO);
        FormulaBuilder monthlyAntennaPower = monthlyBuilder.property(antennaCPS, antennaPowerPropertySpec);
        FormulaBuilder monthlyAntennaCount = monthlyBuilder.property(antennaCPS, antennaCountPropertySpec);
        FormulaBuilder monthlyCompositionCPS = monthlyBuilder.multiply(monthlyAntennaPower, monthlyAntennaCount);
        FormulaBuilder monthlyConstant = monthlyBuilder.multiply(monthlyBuilder.constant(24), monthlyBuilder.constant(30));

        ReadingTypeDeliverableBuilder yearlyBuilder = billingContract.newReadingTypeDeliverable("Yearly A+ kWh", readingTypeYearlyAplusWh, Formula.Mode.AUTO);
        FormulaBuilder yearlyAntennaPower = yearlyBuilder.property(antennaCPS, antennaPowerPropertySpec);
        FormulaBuilder yearlyAntennaCount = yearlyBuilder.property(antennaCPS, antennaCountPropertySpec);
        FormulaBuilder yearlyCompositionCPS = yearlyBuilder.multiply(yearlyAntennaPower, yearlyAntennaCount);
        FormulaBuilder yearlyConstant = yearlyBuilder.multiply(yearlyBuilder.constant(24), yearlyBuilder.constant(365));

        monthlyBuilder.build(monthlyBuilder.multiply(monthlyCompositionCPS, monthlyConstant));
        yearlyBuilder.build(yearlyBuilder.multiply(yearlyCompositionCPS, yearlyConstant));
    }

    void residentialPrepay() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential prepay").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
        RegisteredCustomPropertySet registeredPrepayCPS = customPropertySetService.findActiveCustomPropertySet("com.elster.jupiter.metering.cps.impl.UsagePointGeneralDomainExtension")
                .orElseThrow(() -> new NoSuchElementException("Prepay custom property set not found"));
        UsagePointMetrologyConfiguration config =
                metrologyConfigurationService
                        .newUsagePointMetrologyConfiguration("Residential prepay", serviceCategory)
                        .withDescription("Residential consumer with budget/prepayment meter")
                        .withCustomPropertySet(registeredPrepayCPS)
                        .withUsagePointRequirement(
                                getUsagePointRequirement(
                                        "SERVICEKIND",
                                        SearchablePropertyOperator.EQUAL,
                                        ServiceKind.ELECTRICITY.name()))
                        .withUsagePointRequirement(
                                getUsagePointRequirement(
                                        "detail.phaseCode",
                                        SearchablePropertyOperator.EQUAL,
                                        PhaseCode.S1N.name(),
                                        PhaseCode.S2N.name(),
                                        PhaseCode.S12N.name(),
                                        PhaseCode.S1.name(),
                                        PhaseCode.S2.name(),
                                        PhaseCode.S12.name()))
                        .withUsagePointRequirement(
                                getUsagePointRequirement(
                                        "type",
                                        SearchablePropertyOperator.EQUAL,
                                        UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()))
                        .withUsagePointRequirement(
                                getUsagePointRequirement(
                                        "com.elster.jupiter.metering.cps.impl.UsagePointGeneralDomainExtension.prepay",
                                        SearchablePropertyOperator.EQUAL,
                                        "1"))
                        .create();

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
        config.addMeterRole(meterRole);

        ReadingType readingType15minAplusWh = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .orElseGet(() -> meteringService.createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "A+"));

        MetrologyPurpose informationPurpose = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)
                .orElseThrow(() -> new NoSuchElementException("Information metrology purpose not found"));

        MetrologyContract informationContract = config.addMandatoryMetrologyContract(informationPurpose);

        ReadingTypeRequirement requirementAplus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        buildFormulaSingleRequirement(informationContract, readingType15minAplusWh, requirementAplus, "Active energy+");
    }

    void correctionFactors() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential consumer with correction").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
        RegisteredCustomPropertySet registeredCorrectionFactorCPS = customPropertySetService.findActiveCustomPropertySet(UsagePointElectricityCorrectionFactorsDomExt.class.getName())
                .orElseThrow(() -> new NoSuchElementException("Correction factors custom property set not found"));
        UsagePointMetrologyConfiguration configuration =
                metrologyConfigurationService
                        .newUsagePointMetrologyConfiguration("Residential consumer with correction", serviceCategory)
                        .withDescription("Residential consumer with correction based on loss factor")
                        .withCustomPropertySet(registeredCorrectionFactorCPS)
                        .withUsagePointRequirement(
                                getUsagePointRequirement(
                                        "SERVICEKIND",
                                        SearchablePropertyOperator.EQUAL,
                                        ServiceKind.ELECTRICITY.name()))
                        .withUsagePointRequirement(
                                getUsagePointRequirement(
                                        "type",
                                        SearchablePropertyOperator.EQUAL,
                                        UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()))
                        .create();


        ReadingType readingTypeDailyApluskWh = meteringService.getReadingType("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0")
                .orElseGet(() -> meteringService.createReadingType("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));

        MetrologyPurpose billingPurpose = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
        MetrologyContract billingContract = configuration.addMandatoryMetrologyContract(billingPurpose);

        MetrologyPurpose informationPurpose = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)
                .orElseThrow(() -> new NoSuchElementException("Information metrology purpose not found"));
        MetrologyContract informationContract = configuration.addMandatoryMetrologyContract(informationPurpose);

        configuration.addCustomPropertySet(registeredCorrectionFactorCPS);

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
        configuration.addMeterRole(meterRole);

        ReadingTypeRequirement requirementAplus = configuration.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        ReadingTypeDeliverableBuilder builder = billingContract.newReadingTypeDeliverable("Corrected Daily A+ kWh", readingTypeDailyApluskWh, Formula.Mode.AUTO);
        CustomPropertySet correctionFactorCPS = registeredCorrectionFactorCPS.getCustomPropertySet();
        List<PropertySpec> propertySpecs = correctionFactorCPS.getPropertySpecs();
        FormulaBuilder lossFactor = builder.property(correctionFactorCPS, propertySpecs.stream()
                .filter(propertySpec -> "lossFactor".equals(propertySpec.getName())).findFirst()
                .orElseThrow(() -> new NoSuchElementException("lossFactor property spec not found")));


        builder.build(builder.multiply(builder.requirement(requirementAplus), lossFactor));
        buildFormulaSingleRequirement(informationContract, readingTypeDailyApluskWh, requirementAplus, "Daily A+ kWh");
    }

    void residentialGasWithCorrection(){
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential gas with correction").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.GAS)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.GAS));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential gas with correction", serviceCategory)
                .withDescription("Residential gas installation with climate correction factor")
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                "SERVICEKIND",
                                SearchablePropertyOperator.EQUAL,
                                ServiceKind.GAS.name()))
                .withUsagePointRequirement(
                        getUsagePointRequirement(
                                "type",
                                SearchablePropertyOperator.EQUAL,
                                UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()))
                .create();

        ReadingType readingTypeHourlyCorrectedVolume = meteringService.getReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0")
                .orElseGet(() -> meteringService.createReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0", "Hourly volume m³"));

        ReadingType readingTypeCorrectedDailyVolume = meteringService.getReadingType("11.0.0.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0")
                .orElseGet(() -> meteringService.createReadingType("11.0.0.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0", "Daily volume m³"));

        ReadingType readingTypeHourlyVolume = meteringService.getReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0")
                .orElseGet(() -> meteringService.createReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0", "Hourly volume m³"));

        MetrologyPurpose billingPurpose = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
        MetrologyContract billingContract = config.addMandatoryMetrologyContract(billingPurpose);

        MetrologyPurpose informationPurpose = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)
                .orElseThrow(() -> new NoSuchElementException("Information metrology purpose not found"));
        MetrologyContract contractInformation = config.addMandatoryMetrologyContract(informationPurpose);

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
        config.addMeterRole(meterRole);

        RegisteredCustomPropertySet registeredCorrectionFactorCPS = customPropertySetService.findActiveCustomPropertySet(UsagePointGasCorrectionFactorsDomExt.class.getName())
                .orElseThrow(() -> new NoSuchElementException("Correction factors custom property set not found"));
        config.addCustomPropertySet(registeredCorrectionFactorCPS);

        ReadingTypeRequirement requirementGasVolume = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.GAS_VOLUME
                .getNameTranslation().getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.GAS_VOLUME))
                .overrideAttribute(ReadingTypeTemplateAttributeName.UNIT_OF_MEASURE, 42);

        CustomPropertySet correctionFactorCPS = registeredCorrectionFactorCPS.getCustomPropertySet();
        List<PropertySpec> propertySpecs = correctionFactorCPS.getPropertySpecs();
        PropertySpec climateCorrectionFactorSpec = propertySpecs.stream()
                .filter(propertySpec -> "climateCorrectionFactor".equals(propertySpec.getName())).findFirst()
                .orElseThrow(() -> new NoSuchElementException("climateCorrectionFactor property spec not found"));

        ReadingTypeDeliverableBuilder hourlyBuilder = billingContract.newReadingTypeDeliverable("Hourly corrected volume m³", readingTypeHourlyCorrectedVolume, Formula.Mode.AUTO);
        FormulaBuilder hourlyClimateCorrectionFactor = hourlyBuilder.property(correctionFactorCPS, climateCorrectionFactorSpec);

        ReadingTypeDeliverableBuilder dailyBuilder = billingContract.newReadingTypeDeliverable("Daily corrected volume m³", readingTypeCorrectedDailyVolume, Formula.Mode.AUTO);

        ReadingTypeDeliverable hourlyDeliverable = hourlyBuilder.build(hourlyBuilder.multiply(hourlyBuilder.requirement(requirementGasVolume), hourlyClimateCorrectionFactor));
        dailyBuilder.build(dailyBuilder.deliverable(hourlyDeliverable));
        buildFormulaSingleRequirement(contractInformation, readingTypeHourlyVolume, requirementGasVolume, "Hourly volume m³");
    }

    void createSyntheticLoadProfiles(){
        TimeZone timeZone = TimeZone.getTimeZone(clock.getZone());
        Instant startTime =  LocalDate.parse("2017-01-01").atStartOfDay().atZone(timeZone.toZoneId()).toInstant();
        ReadingType readingTypeDailyApluskWh = meteringService.getReadingType("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0")
                .orElseGet(() -> meteringService.createReadingType("11.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));
        syntheticLoadProfileService.newSyntheticLoadProfile("Loss factor", Period.ofYears(1), startTime, readingTypeDailyApluskWh, timeZone)
                .withDescription("Loss factor")
                .build();

        ReadingType readingTypeHourlyVolume = meteringService.getReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0")
                .orElseGet(() -> meteringService.createReadingType("0.0.7.4.1.7.58.0.0.0.0.0.0.0.0.0.42.0", "Hourly volume m³"));
        syntheticLoadProfileService.newSyntheticLoadProfile("CCF", Period.ofMonths(1), startTime, readingTypeHourlyVolume, timeZone)
                .withDescription("Climate Correction Factor")
                .build();
    }

    void createDemoEstimationComments() {
        getEstimationComments().forEach(comment -> meteringService.createReadingQualityComment(ReadingQualityCommentCategory.ESTIMATION, comment));
    }

    private Set<String> getEstimationComments() {
        Set<String> comments = new HashSet<>();

        comments.add("Estimated by market rule 11");
        comments.add("Estimated by market rule 12");
        comments.add("Estimated by market rule 13");
        comments.add("Estimated by market rule 14");
        comments.add("Estimated by market rule 15");
        comments.add("Estimated by market rule 16");
        comments.add("Estimated by market rule 17");
        comments.add("Estimated by market rule 18");
        comments.add("Estimated by market rule 19");

        return comments;
    }

    private SearchablePropertyValue.ValueBean getUsagePointRequirement(String property, SearchablePropertyOperator operator, String... values) {
        return new SearchablePropertyValue.ValueBean(property, operator,Arrays.asList(values));
    }

    private ReadingTypeTemplate getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate defaultReadingTypeTemplate) {
        return metrologyConfigurationService.findReadingTypeTemplate(defaultReadingTypeTemplate.getNameTranslation()
                .getDefaultFormat())
                .orElseThrow(() -> new NoSuchElementException("Default reading type template not found"));
    }

    private ReadingTypeDeliverable buildFormulaSingleRequirement(MetrologyContract contract, ReadingType readingType, ReadingTypeRequirement requirement, String name) {
        ReadingTypeDeliverableBuilder builder = contract.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        return builder.build(builder.requirement(requirement));
    }
}
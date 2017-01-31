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
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.cps.impl.metrology.TranslationKeys;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointAntennaCPS;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointAntennaDomExt;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointContCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointContDomainExtension;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointDecentProdCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointDecentProdDomExt;
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

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component(name = "com.elster.jupiter.metering.cps", service = {TranslationKeyProvider.class, MessageSeedProvider.class}, property = "name=CPM")
public class MeteringCustomPropertySetsDemoInstaller implements TranslationKeyProvider, MessageSeedProvider {

    public static final String COMPONENT_NAME = "CPM";

    private volatile MeteringService meteringService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private MetrologyConfigurationService metrologyConfigurationService;
    private volatile UpgradeService upgradeService;

    @Reference
    @SuppressWarnings("unused") // Used by OSGi framework
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
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
                bind(MeteringCustomPropertySetsDemoInstaller.class).toInstance(MeteringCustomPropertySetsDemoInstaller.this);
            }
        });

        upgradeService.register(InstallIdentifier.identifier("Example", "CPM"), dataModel, Installer.class, Collections.emptyMap());

        this.registerCustomPropertySets();
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
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Unmeasured antenna installation", serviceCategory)
                .withDescription("Unmeasured installations which use attributes-based calculations").create();

        config.addUsagePointRequirement(getUsagePointRequirement("SERVICEKIND", SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_NON_SDP.name()));

        ReadingType readingTypeMonthlyAplusWh = meteringService.getReadingType("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0")
                .orElseGet(() -> meteringService.createReadingType("13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "A+"));

        MetrologyPurpose purposeBilling = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING)
                .orElseThrow(() -> new NoSuchElementException("Billing metrology purpose not found"));
        MetrologyContract contractBilling = config.addMandatoryMetrologyContract(purposeBilling);

        RegisteredCustomPropertySet registeredAntennaCPS = customPropertySetService.findActiveCustomPropertySet("com.elster.jupiter.metering.cps.impl.metrology.UsagePointAntennaDomExt")
                .orElseThrow(() -> new NoSuchElementException("Antenna custom property set not found"));
        config.addCustomPropertySet(registeredAntennaCPS);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Monthly A+ kWh", readingTypeMonthlyAplusWh, Formula.Mode.AUTO);
        CustomPropertySet antennaCPS = registeredAntennaCPS.getCustomPropertySet();
        List<PropertySpec> propertySpecs = antennaCPS.getPropertySpecs();
        FormulaBuilder antennaPower = builder.property(antennaCPS, propertySpecs.stream()
                .filter(propertySpec -> "antennaPower".equals(propertySpec.getName())).findFirst()
                .orElseThrow(() -> new NoSuchElementException("antennaPower property spec not found")));
        FormulaBuilder antennaCount = builder.property(antennaCPS, propertySpecs.stream()
                .filter(propertySpec -> "antennaCount".equals(propertySpec.getName())).findFirst()
                .orElseThrow(() -> new NoSuchElementException("antennaCount property spec not found")));
        FormulaBuilder compositionCPS = builder.multiply(antennaPower, antennaCount);
        FormulaBuilder monthlyConstant = builder.multiply(builder.constant(24), builder.constant(30));

        contractBilling.addDeliverable(builder.build(builder.multiply(compositionCPS, monthlyConstant)));
    }

    void residentialPrepay() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential prepay").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential prepay", serviceCategory)
                .withDescription("Residential consumer with budget/prepayment meter").create();

        RegisteredCustomPropertySet registeredPrepayCPS = customPropertySetService.findActiveCustomPropertySet("com.elster.jupiter.metering.cps.impl.UsagePointGeneralDomainExtension")
                .orElseThrow(() -> new NoSuchElementException("Prepay custom property set not found"));
        config.addCustomPropertySet(registeredPrepayCPS);

        config.addUsagePointRequirement(getUsagePointRequirement("SERVICEKIND", SearchablePropertyOperator.EQUAL, ServiceKind.ELECTRICITY.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("detail.phaseCode", SearchablePropertyOperator.EQUAL,
                PhaseCode.S1N.name(),
                PhaseCode.S2N.name(),
                PhaseCode.S12N.name(),
                PhaseCode.S1.name(),
                PhaseCode.S2.name(),
                PhaseCode.S12.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("type", SearchablePropertyOperator.EQUAL, UsagePointTypeInfo.UsagePointType.MEASURED_SDP.name()));
        config.addUsagePointRequirement(getUsagePointRequirement("com.elster.jupiter.metering.cps.impl.UsagePointGeneralDomainExtension.prepay", SearchablePropertyOperator.EQUAL, "1"));

        MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
        config.addMeterRole(meterRole);

        ReadingType readingType15minAplusWh = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0")
                .orElseGet(() -> meteringService.createReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "A+"));

        MetrologyPurpose purposeInformation = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)
                .orElseThrow(() -> new NoSuchElementException("Information metrology purpose not found"));

        MetrologyContract contractInformation = config.addMandatoryMetrologyContract(purposeInformation);

        ReadingTypeRequirement requirementAplus = config.newReadingTypeRequirement(DefaultReadingTypeTemplate.A_PLUS.getNameTranslation().getDefaultFormat(), meterRole)
                .withReadingTypeTemplate(getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate.A_PLUS));

        contractInformation.addDeliverable(buildFormulaSingleRequirement(config, readingType15minAplusWh, requirementAplus, "Active energy+"));
    }

    private SearchablePropertyValue.ValueBean getUsagePointRequirement(String property, SearchablePropertyOperator operator, String... values) {
        SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
        valueBean.propertyName = property;
        valueBean.operator = operator;
        valueBean.values = Arrays.asList(values);
        return valueBean;
    }

    private ReadingTypeTemplate getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate defaultReadingTypeTemplate) {
        return metrologyConfigurationService.findReadingTypeTemplate(defaultReadingTypeTemplate.getNameTranslation()
                .getDefaultFormat())
                .orElseThrow(() -> new NoSuchElementException("Default reading type template not found"));
    }

    private ReadingTypeDeliverable buildFormulaSingleRequirement(UsagePointMetrologyConfiguration config, ReadingType readingType, ReadingTypeRequirement requirement, String name) {
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable(name, readingType, Formula.Mode.AUTO);
        return builder.build(builder.requirement(requirement));
    }
}












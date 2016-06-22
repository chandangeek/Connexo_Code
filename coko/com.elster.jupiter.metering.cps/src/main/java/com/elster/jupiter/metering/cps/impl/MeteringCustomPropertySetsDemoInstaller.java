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
import com.elster.jupiter.metering.impl.config.DefaultReadingTypeTemplate;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@Component(name = "com.elster.jupiter.metering.cps", service = {InstallService.class, TranslationKeyProvider.class, MessageSeedProvider.class}, property = "name=CPM")
public class MeteringCustomPropertySetsDemoInstaller implements InstallService, TranslationKeyProvider, MessageSeedProvider {

    public static final String COMPONENT_NAME = "CPM";

    private volatile MeteringService meteringService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile TransactionService transactionService;
    private volatile NlsService nlsService;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private MetrologyConfigurationService metrologyConfigurationService;
    private List<CustomPropertySet> customPropertySets;
    private Map<String, CustomPropertySet> customPropertySetsMap;

    private ServiceCategory electricity;
    private ServiceCategory gas;
    private ServiceCategory water;
    private ServiceCategory internet;
    private ServiceCategory thermal;

    @Reference
    public void setmetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Activate
    public void activate() {
        this.registerCustomPropertySets();
    }

    @Override
    public void install() {
        this.registerCustomPropertySets();
        assign(UsagePointGeneralDomainExtension.class.getName(), this::addAllSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointMetrologyGeneralDomExt.class.getName())
//                .ifPresent(this::addAllSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointMeterGnrDomainExtension.class.getName())
//                .ifPresent(this::addAllSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointContDomainExtension.class.getName())
//                .ifPresent(this::addAllSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointMeterTechInfAllDomExt.class.getName())
//                .ifPresent(this::addAllSets);

        electricity = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
//        customPropertySetService.findActiveCustomPropertySet(UsagePointOneDomainExtension.class.getName())
//                .ifPresent(this::addElectricitySets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointTwoDomainExtension.class.getName())
//                .ifPresent(this::addElectricitySets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointVersionedDomainExtension.class.getName())
//                .ifPresent(this::addElectricitySets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointContrElectrDomExt.class.getName())
//                .ifPresent(this::addElectricitySets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointTechElDomExt.class.getName())
//                .ifPresent(this::addElectricitySets);
        assign(UsagePointTechElDomExt.class.getName(), this::addElectricitySets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointSettlementDomExt.class.getName())
//                .ifPresent(this::addElectricitySets);

        assign(UsagePointTechInstElectrDE.class
                .getName(), this::addElectricitySets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointDecentProdDomExt.class.getName())
//                .ifPresent(this::addElectricitySets);

        gas = meteringService.getServiceCategory(ServiceKind.GAS).get();
//        customPropertySetService.findActiveCustomPropertySet(UsagePointOneDomainExtension.class.getName())
//                .ifPresent(this::addGasSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointVersionedDomainExtension.class.getName())
//                .ifPresent(this::addGasSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointSettlementDomExt.class.getName())
//                .ifPresent(this::addGasSets);
        assign(UsagePointTechnicalWGTDomExt.class.getName(), this::addGasSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointMeterTechInfGTWDomExt.class.getName())
//                .ifPresent(this::addGasSets);

        water = meteringService.getServiceCategory(ServiceKind.WATER).get();
//        customPropertySetService.findActiveCustomPropertySet(UsagePointOneDomainExtension.class.getName())
//                .ifPresent(this::addWaterSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointTwoDomainExtension.class.getName())
//                .ifPresent(this::addWaterSets);
        assign(UsagePointTechnicalWGTDomExt.class.getName(), this::addWaterSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointMeterTechInfGTWDomExt.class.getName())
//                .ifPresent(this::addWaterSets);

        internet = meteringService.getServiceCategory(ServiceKind.INTERNET).get();
//        customPropertySetService.findActiveCustomPropertySet(UsagePointTwoDomainExtension.class.getName())
//                .ifPresent(this::addInternetSets);

        thermal = meteringService.getServiceCategory(ServiceKind.HEAT).get();
        assign(UsagePointTechnicalWGTDomExt.class.getName(), this::addThermalSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointMeterTechInfGTWDomExt.class.getName())
//                .ifPresent(this::addThermalSets);
        unmeasuredAntennaInstallation();
        residentialPrepay();
    }

    private void assign(String cps, Consumer<RegisteredCustomPropertySet> action) {
        CustomPropertySet customPropertySet = customPropertySetsMap.get(cps);
        if (customPropertySet != null) {
            action.accept(customPropertySetService.findActiveCustomPropertySet(customPropertySet.getId())
                    .orElseGet(() -> {
                                customPropertySetService.addCustomPropertySet(customPropertySet);
                                return customPropertySetService.findActiveCustomPropertySet(customPropertySet.getId()).get();
                            }
                    ));

        }
    }

    private void addElectricitySets(RegisteredCustomPropertySet registeredCustomPropertySet) {
        electricity.addCustomPropertySet(registeredCustomPropertySet);
    }

    private void addGasSets(RegisteredCustomPropertySet registeredCustomPropertySet) {
        gas.addCustomPropertySet(registeredCustomPropertySet);
    }

    private void addWaterSets(RegisteredCustomPropertySet registeredCustomPropertySet) {
        water.addCustomPropertySet(registeredCustomPropertySet);
    }

    private void addInternetSets(RegisteredCustomPropertySet registeredCustomPropertySet) {
        internet.addCustomPropertySet(registeredCustomPropertySet);
    }

    private void addThermalSets(RegisteredCustomPropertySet registeredCustomPropertySet) {
        thermal.addCustomPropertySet(registeredCustomPropertySet);
    }

    private void addAllSets(RegisteredCustomPropertySet registeredCustomPropertySet) {
        EnumSet.allOf(ServiceKind.class).stream().forEach(serviceKind ->
                meteringService.getServiceCategory(serviceKind).get()
                        .addCustomPropertySet(registeredCustomPropertySet));
    }

    private void registerCustomPropertySets() {
        this.customPropertySets = this.getMeteringCustomPropertySets();
        customPropertySets.stream().forEach(customPropertySetService::addCustomPropertySet);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("NLS", "CPS", "MTR");
    }

    private List<CustomPropertySet> getMeteringCustomPropertySets() {
        customPropertySetsMap = new HashMap<>();
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

        return new ArrayList<>(customPropertySetsMap.values());
//        return Arrays.asList(
////                new UsagePointOneCustomPropertySet(propertySpecService, thesaurus),
////                new UsagePointVersionedCustomPropertySet(propertySpecService, thesaurus),
////                new UsagePointTwoCustomPropertySet(propertySpecService, thesaurus));
//                new UsagePointContrElectrCPS(propertySpecService, thesaurus),
//                new UsagePointGeneralCustomPropertySet(propertySpecService, thesaurus),
//                new UsagePointTechElCPS(propertySpecService, thesaurus),
//                new UsagePointTechnicalWGTCustomPropertySet(propertySpecService, thesaurus),
//                new UsagePointContCustomPropertySet(propertySpecService, thesaurus),
//                new UsagePointDecentProdCustomPropertySet(propertySpecService, thesaurus),
//                //  new UsagePointMeterGnrCustomPropertySet(propertySpecService, thesaurus),
//                //  new UsagePointMeterTechInfAllCPS(propertySpecService, thesaurus),
//                // new UsagePointMeterTechInfGTWCustomPropertySet(propertySpecService, thesaurus),
//                new UsagePointMetrologyGeneralCPS(propertySpecService, thesaurus),
//                new UsagePointSettlementCustomPropertySet(propertySpecService, thesaurus),
//                new UsagePointTechInstAllCustomPropertySet(propertySpecService, thesaurus),
//                new UsagePointTechInstEGCustomPropertySet(propertySpecService, thesaurus),
//                new UsagePointTechInstElectrCPS(propertySpecService, thesaurus));
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

        RegisteredCustomPropertySet registeredAntennaCPS = customPropertySetService.findActiveCustomPropertySet("com.elster.jupiter.metering.cps.impl.metrology.UsagePointAntennaCPS")
                .orElseThrow(() -> new NoSuchElementException("Antenna custom property set not found"));
        config.addCustomPropertySet(registeredAntennaCPS);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Monthly A+ kWh", readingTypeMonthlyAplusWh, Formula.Mode.EXPERT);
        CustomPropertySet antennaCPS = registeredAntennaCPS.getCustomPropertySet();
        List<PropertySpec> propertySpecs = antennaCPS.getPropertySpecs();
        FormulaBuilder antennaPower = builder.property(antennaCPS, propertySpecs.stream().filter("antennaPower"::equals).findFirst()
                .orElseThrow(() -> new NoSuchElementException("antennaPower property spec not found")));
        FormulaBuilder antennaCount = builder.property(antennaCPS, propertySpecs.stream().filter("antennaCount"::equals).findFirst()
                .orElseThrow(() -> new NoSuchElementException("antennaCount property spec not found")));
        FormulaBuilder compositionCPS = builder.multiply(antennaPower, antennaCount);
        FormulaBuilder monthlyConstant = builder.multiply(builder.constant(24), builder.constant(30));

        contractBilling.addDeliverable(builder.build(builder.multiply(compositionCPS, monthlyConstant)));
    }

    private void residentialPrepay() {
        if (metrologyConfigurationService.findMetrologyConfiguration("Residential prepay").isPresent()) {
            return;
        }
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
        UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration("Residential prepay", serviceCategory)
                .withDescription("Residential consumer with budget/prepayment meter").create();

        RegisteredCustomPropertySet registeredPrepayCPS = customPropertySetService.findActiveCustomPropertySet("usage.point.cps.general.properties.general.prepay")
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

    private SearchablePropertyValue.ValueBean getUsagePointRequirement(String property, SearchablePropertyOperator operator, String... values) {
        SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
        valueBean.propertyName = property;
        valueBean.operator = operator;
        valueBean.values = Arrays.asList(values);
        return valueBean;
    }

    private ReadingTypeTemplate getDefaultReadingTypeTemplate(DefaultReadingTypeTemplate defaultReadingTypeTemplate) {
        return metrologyConfigurationService.findReadingTypeTemplate(defaultReadingTypeTemplate.name())
                .orElseThrow(() -> new NoSuchElementException("Default reading type template not found"));
    }

    private ReadingTypeDeliverable buildFormulaSingleRequirement(UsagePointMetrologyConfiguration config, ReadingType readingType, ReadingTypeRequirement requirement, String name) {
        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable(name, readingType, Formula.Mode.EXPERT);
        return builder.build(builder.requirement(requirement));
    }
}












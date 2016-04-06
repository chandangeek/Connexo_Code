package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.cps.impl.metrology.TranslationKeys;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointContCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointDecentProdCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointMetrologyGeneralCPS;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointSettlementCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstAllCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstEGCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstElectrCPS;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstElectrDE;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

@Component(name = "com.elster.jupiter.metering.cps", service = {InstallService.class, TranslationKeyProvider.class, MessageSeedProvider.class}, property = "name=CPM")
public class MeteringCustomPropertySetsDemoInstaller implements InstallService, TranslationKeyProvider, MessageSeedProvider {

    public static final String COMPONENT_NAME = "CPM";

    private volatile MeteringService meteringService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile TransactionService transactionService;
    private volatile NlsService nlsService;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    private ServiceCategory electricity;
    private ServiceCategory gas;
    private ServiceCategory water;
    private ServiceCategory internet;
    private ServiceCategory thermal;

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
        getMeteringCustomPropertySets().forEach(customPropertySetService::addCustomPropertySet);
    }

    @Override
    public void install() {
        customPropertySetService.findActiveCustomPropertySet(UsagePointGeneralDomainExtension.class.getName())
                .ifPresent(this::addAllSets);
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
        customPropertySetService.findActiveCustomPropertySet(UsagePointTechElDomExt.class.getName())
                .ifPresent(this::addElectricitySets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointSettlementDomExt.class.getName())
//                .ifPresent(this::addElectricitySets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointTechInstElectrDE.class
                .getName()).ifPresent(this::addElectricitySets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointDecentProdDomExt.class.getName())
//                .ifPresent(this::addElectricitySets);

        gas = meteringService.getServiceCategory(ServiceKind.GAS).get();
//        customPropertySetService.findActiveCustomPropertySet(UsagePointOneDomainExtension.class.getName())
//                .ifPresent(this::addGasSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointVersionedDomainExtension.class.getName())
//                .ifPresent(this::addGasSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointSettlementDomExt.class.getName())
//                .ifPresent(this::addGasSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointTechnicalWGTDomExt.class.getName())
                .ifPresent(this::addGasSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointMeterTechInfGTWDomExt.class.getName())
//                .ifPresent(this::addGasSets);

        water = meteringService.getServiceCategory(ServiceKind.WATER).get();
//        customPropertySetService.findActiveCustomPropertySet(UsagePointOneDomainExtension.class.getName())
//                .ifPresent(this::addWaterSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointTwoDomainExtension.class.getName())
//                .ifPresent(this::addWaterSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointTechnicalWGTDomExt.class.getName())
                .ifPresent(this::addWaterSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointMeterTechInfGTWDomExt.class.getName())
//                .ifPresent(this::addWaterSets);

        internet = meteringService.getServiceCategory(ServiceKind.INTERNET).get();
//        customPropertySetService.findActiveCustomPropertySet(UsagePointTwoDomainExtension.class.getName())
//                .ifPresent(this::addInternetSets);

        thermal = meteringService.getServiceCategory(ServiceKind.HEAT).get();
        customPropertySetService.findActiveCustomPropertySet(UsagePointTechnicalWGTDomExt.class.getName())
                .ifPresent(this::addThermalSets);
//        customPropertySetService.findActiveCustomPropertySet(UsagePointMeterTechInfGTWDomExt.class.getName())
//                .ifPresent(this::addThermalSets);
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

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("NLS", "CPS", "MTR");
    }

    private List<CustomPropertySet> getMeteringCustomPropertySets() {
        return Arrays.asList(
//                new UsagePointOneCustomPropertySet(propertySpecService, thesaurus),
//                new UsagePointVersionedCustomPropertySet(propertySpecService, thesaurus),
//                new UsagePointTwoCustomPropertySet(propertySpecService, thesaurus));
                new UsagePointContrElectrCPS(propertySpecService, thesaurus),
                new UsagePointGeneralCustomPropertySet(propertySpecService, thesaurus),
                new UsagePointTechElCPS(propertySpecService, thesaurus),
                new UsagePointTechnicalWGTCustomPropertySet(propertySpecService, thesaurus),
                new UsagePointContCustomPropertySet(propertySpecService, thesaurus),
                new UsagePointDecentProdCustomPropertySet(propertySpecService, thesaurus),
                //  new UsagePointMeterGnrCustomPropertySet(propertySpecService, thesaurus),
                //  new UsagePointMeterTechInfAllCPS(propertySpecService, thesaurus),
                // new UsagePointMeterTechInfGTWCustomPropertySet(propertySpecService, thesaurus),
                new UsagePointMetrologyGeneralCPS(propertySpecService, thesaurus),
                new UsagePointSettlementCustomPropertySet(propertySpecService, thesaurus),
                new UsagePointTechInstAllCustomPropertySet(propertySpecService, thesaurus),
                new UsagePointTechInstEGCustomPropertySet(propertySpecService, thesaurus),
                new UsagePointTechInstElectrCPS(propertySpecService, thesaurus));
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
}












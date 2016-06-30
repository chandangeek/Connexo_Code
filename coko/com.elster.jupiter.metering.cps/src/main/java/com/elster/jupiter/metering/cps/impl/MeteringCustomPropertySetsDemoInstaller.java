package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.cps.impl.metrology.TranslationKeys;
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
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(name = "com.elster.jupiter.metering.cps", service = {TranslationKeyProvider.class, MessageSeedProvider.class}, property = "name=CPM")
public class MeteringCustomPropertySetsDemoInstaller implements TranslationKeyProvider, MessageSeedProvider {

    public static final String COMPONENT_NAME = "CPM";

    private volatile MeteringService meteringService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile TransactionService transactionService;
    private volatile NlsService nlsService;
    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile UpgradeService upgradeService;

//    private List<CustomPropertySet> customPropertySets;
//    private Map<String, CustomPropertySet> customPropertySetsMap;

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

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Activate
    public void activate() {
        this.registerCustomPropertySets();
        DataModel dataModel = upgradeService.newNonOrmDataModel();
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
    }

    Map<String, CustomPropertySet>  registerCustomPropertySets() {
        Map<String, CustomPropertySet>  customPropertySets = this.getMeteringCustomPropertySets();
        customPropertySets.values().stream().forEach(customPropertySetService::addCustomPropertySet);
        return customPropertySets;
    }

    Map<String, CustomPropertySet> getMeteringCustomPropertySets() {
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

        return customPropertySetsMap;
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
}












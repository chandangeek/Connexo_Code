package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.cps.impl.metrology.TranslationKeys;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointSimpleCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointVersionedCustomPropertySet;
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
        getMeteringCustomPropertySets().forEach(customPropertySetService::addCustomPropertySet);

        electricity = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        customPropertySetService.findActiveCustomPropertySet(UsagePointOneDomainExtension.class.getName())
                .ifPresent(this::addElecttricitySets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointTwoDomainExtension.class.getName())
                .ifPresent(this::addElecttricitySets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointVersionedDomainExtension.class.getName())
                .ifPresent(this::addElecttricitySets);

        gas = meteringService.getServiceCategory(ServiceKind.GAS).get();
        customPropertySetService.findActiveCustomPropertySet(UsagePointOneDomainExtension.class.getName())
                .ifPresent(this::addGasSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointVersionedDomainExtension.class.getName())
                .ifPresent(this::addGasSets);

        water = meteringService.getServiceCategory(ServiceKind.WATER).get();
        customPropertySetService.findActiveCustomPropertySet(UsagePointOneDomainExtension.class.getName())
                .ifPresent(this::addWaterSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointTwoDomainExtension.class.getName())
                .ifPresent(this::addWaterSets);

        internet = meteringService.getServiceCategory(ServiceKind.INTERNET).get();
        customPropertySetService.findActiveCustomPropertySet(UsagePointTwoDomainExtension.class.getName())
                .ifPresent(this::addInternetSets);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("NLS", "CPS", "MTR");
    }

    private void addElecttricitySets(RegisteredCustomPropertySet registeredCustomPropertySet) {
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

    private List<CustomPropertySet> getMeteringCustomPropertySets() {
        return Arrays.asList(
                new UsagePointOneCustomPropertySet(propertySpecService, thesaurus),
                new UsagePointTwoCustomPropertySet(propertySpecService, thesaurus),
                new UsagePointVersionedCustomPropertySet(propertySpecService, thesaurus),
                new UsagePointSimpleCustomPropertySet(propertySpecService, thesaurus),
                new com.elster.jupiter.metering.cps.impl.UsagePointVersionedCustomPropertySet(propertySpecService, thesaurus)
        );
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

package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

@Component(name = "com.elster.jupiter.metering.cps", service = {InstallService.class}, property = "name=CPSM")
public class MeteringCustomPropertySetsDemoInstaller implements InstallService {

    private volatile MeteringService meteringService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile TransactionService transactionService;

    private ServiceCategory electricity;
    private ServiceCategory gas;
    private ServiceCategory water;
    private ServiceCategory internet;

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

    @Override
    public void install() {

        electricity = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        customPropertySetService.findActiveCustomPropertySet(UsagePointOneDomainExtension.class.getName()).ifPresent(this::addElecttricitySets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointTwoDomainExtension.class.getName()).ifPresent(this::addElecttricitySets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointVersionedDomainExtension.class.getName()).ifPresent(this::addElecttricitySets);

        gas = meteringService.getServiceCategory(ServiceKind.GAS).get();
        customPropertySetService.findActiveCustomPropertySet(UsagePointOneDomainExtension.class.getName()).ifPresent(this::addGasSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointVersionedDomainExtension.class.getName()).ifPresent(this::addGasSets);

        water = meteringService.getServiceCategory(ServiceKind.WATER).get();
        customPropertySetService.findActiveCustomPropertySet(UsagePointOneDomainExtension.class.getName()).ifPresent(this::addWaterSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointTwoDomainExtension.class.getName()).ifPresent(this::addWaterSets);

        internet = meteringService.getServiceCategory(ServiceKind.INTERNET).get();
        customPropertySetService.findActiveCustomPropertySet(UsagePointTwoDomainExtension.class.getName()).ifPresent(this::addInternetSets);
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

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("CPS", "MTR");
    }
}

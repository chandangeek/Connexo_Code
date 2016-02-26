package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointContractualDomainExtension;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointConvertorDomainExtension;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointDecentralizedProductionDomainExtension;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointMeterGeneralCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointMeterTechInformationAllDomainExtension;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointMeterTechInformationElectricityDomainExtension;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointMeterTechInformationGTWDomainExtension;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointMetrologyGeneralDomainExtension;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointSettlementDomainExtension;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechnicalInstallationElectricityDomainExtension;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.EnumSet;
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
    private ServiceCategory thermal;

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

        customPropertySetService.findActiveCustomPropertySet(UsagePointGeneralDomainExtension.class.getName())
                .ifPresent(this::addAllSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointMetrologyGeneralDomainExtension.class.getName())
                .ifPresent(this::addAllSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointMeterGeneralCustomPropertySet.class.getName())
                .ifPresent(this::addAllSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointContractualDomainExtension.class.getName())
                .ifPresent(this::addAllSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointMeterTechInformationAllDomainExtension.class.getName())
                .ifPresent(this::addAllSets);

        electricity = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        customPropertySetService.findActiveCustomPropertySet(UsagePointOneDomainExtension.class.getName())
                .ifPresent(this::addElectricitySets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointTwoDomainExtension.class.getName())
                .ifPresent(this::addElectricitySets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointVersionedDomainExtension.class.getName())
                .ifPresent(this::addElectricitySets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointTechnicalElectricityDomainExtension.class.getName())
                .ifPresent(this::addElectricitySets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointLicenseDomainExtension.class.getName())
                .ifPresent(this::addElectricitySets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointSettlementDomainExtension.class.getName())
                .ifPresent(this::addElectricitySets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointTechnicalInstallationElectricityDomainExtension.class
                .getName()).ifPresent(this::addElectricitySets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointDecentralizedProductionDomainExtension.class.getName())
                .ifPresent(this::addElectricitySets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointMeterTechInformationElectricityDomainExtension.class
                .getName()).ifPresent(this::addElectricitySets);

        gas = meteringService.getServiceCategory(ServiceKind.GAS).get();
        customPropertySetService.findActiveCustomPropertySet(UsagePointOneDomainExtension.class.getName()).ifPresent(this::addGasSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointVersionedDomainExtension.class.getName()).ifPresent(this::addGasSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointSettlementDomainExtension.class.getName())
                .ifPresent(this::addGasSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointConvertorDomainExtension.class.getName())
                .ifPresent(this::addGasSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointTechnicalWGTDomainExtension.class.getName())
                .ifPresent(this::addGasSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointMeterTechInformationGTWDomainExtension.class.getName())
                .ifPresent(this::addGasSets);

        water = meteringService.getServiceCategory(ServiceKind.WATER).get();
        customPropertySetService.findActiveCustomPropertySet(UsagePointOneDomainExtension.class.getName()).ifPresent(this::addWaterSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointTwoDomainExtension.class.getName()).ifPresent(this::addWaterSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointTechnicalWGTDomainExtension.class.getName())
                .ifPresent(this::addWaterSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointMeterTechInformationGTWDomainExtension.class.getName())
                .ifPresent(this::addWaterSets);

        internet = meteringService.getServiceCategory(ServiceKind.INTERNET).get();
        customPropertySetService.findActiveCustomPropertySet(UsagePointTwoDomainExtension.class.getName()).ifPresent(this::addInternetSets);

        thermal = meteringService.getServiceCategory(ServiceKind.HEAT).get();
        customPropertySetService.findActiveCustomPropertySet(UsagePointTechnicalWGTDomainExtension.class.getName())
                .ifPresent(this::addThermalSets);
        customPropertySetService.findActiveCustomPropertySet(UsagePointMeterTechInformationGTWDomainExtension.class.getName())
                .ifPresent(this::addThermalSets);
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
}

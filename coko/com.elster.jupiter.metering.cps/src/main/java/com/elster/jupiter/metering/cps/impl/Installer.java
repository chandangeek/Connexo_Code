package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstElectrDE;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 15/06/2016
 * Time: 14:15
 */
class Installer implements FullInstaller {

    private final MeteringService meteringService;
    private final CustomPropertySetService customPropertySetService;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private final MeteringCustomPropertySetsDemoInstaller meteringCustomPropertySetsDemoInstaller;

    private Map<String, CustomPropertySet> customPropertySetsMap;
    private ServiceCategory electricity;
    private ServiceCategory gas;
    private ServiceCategory water;
    private ServiceCategory internet;
    private ServiceCategory thermal;

    @Inject
    Installer(MeteringService meteringService, CustomPropertySetService customPropertySetService, PropertySpecService propertySpecService, Thesaurus thesaurus, MeteringCustomPropertySetsDemoInstaller meteringCustomPropertySetsDemoInstaller) {
        this.meteringService = meteringService;
        this.customPropertySetService = customPropertySetService;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
        this.meteringCustomPropertySetsDemoInstaller = meteringCustomPropertySetsDemoInstaller;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        customPropertySetsMap = meteringCustomPropertySetsDemoInstaller.registerCustomPropertySets();
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

        meteringCustomPropertySetsDemoInstaller.unmeasuredAntennaInstallation();
        meteringCustomPropertySetsDemoInstaller.residentialPrepay();
    }

    private void assign(String cps, Consumer<RegisteredCustomPropertySet> action) {
        CustomPropertySet customPropertySet = customPropertySetsMap.get(cps);
        if (customPropertySet != null) {
            action.accept(customPropertySetService.findActiveCustomPropertySet(customPropertySet.getId())
                    .orElseGet(() -> {
                                customPropertySetService.addCustomPropertySet(customPropertySet);
                                return customPropertySetService.findActiveCustomPropertySet(customPropertySet.getId())
                                        .get();
                            }
                    ));

        }
    }

    private void addAllSets(RegisteredCustomPropertySet registeredCustomPropertySet) {
        EnumSet.allOf(ServiceKind.class).stream().forEach(serviceKind ->
                meteringService.getServiceCategory(serviceKind).get()
                        .addCustomPropertySet(registeredCustomPropertySet));
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

}

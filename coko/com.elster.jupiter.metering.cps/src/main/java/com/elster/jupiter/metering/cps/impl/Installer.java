/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstElectrDE;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

class Installer implements FullInstaller {

    private final MeteringService meteringService;
    private final CustomPropertySetService customPropertySetService;
    private final MeteringCustomPropertySetsDemoInstaller meteringCustomPropertySetsDemoInstaller;

    private Map<String, CustomPropertySet> customPropertySetsMap;
    private ServiceCategory electricity;
    private ServiceCategory gas;
    private ServiceCategory water;
    private ServiceCategory internet;
    private ServiceCategory thermal;

    @Inject
    Installer(MeteringService meteringService, CustomPropertySetService customPropertySetService, MeteringCustomPropertySetsDemoInstaller meteringCustomPropertySetsDemoInstaller) {
        this.meteringService = meteringService;
        this.customPropertySetService = customPropertySetService;
        this.meteringCustomPropertySetsDemoInstaller = meteringCustomPropertySetsDemoInstaller;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        customPropertySetsMap = meteringCustomPropertySetsDemoInstaller.registerCustomPropertySets();
        assign(UsagePointGeneralDomainExtension.class.getName(), this::addAllSets);

        electricity = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        assign(UsagePointTechElDomExt.class.getName(), this::addElectricitySets);

        assign(UsagePointTechInstElectrDE.class.getName(), this::addElectricitySets);

        gas = meteringService.getServiceCategory(ServiceKind.GAS).get();
        assign(UsagePointTechnicalWGTDomExt.class.getName(), this::addGasSets);

        water = meteringService.getServiceCategory(ServiceKind.WATER).get();
        assign(UsagePointTechnicalWGTDomExt.class.getName(), this::addWaterSets);

        internet = meteringService.getServiceCategory(ServiceKind.INTERNET).get();

        thermal = meteringService.getServiceCategory(ServiceKind.HEAT).get();
        assign(UsagePointTechnicalWGTDomExt.class.getName(), this::addThermalSets);

        meteringCustomPropertySetsDemoInstaller.unmeasuredAntennaInstallation();
        meteringCustomPropertySetsDemoInstaller.residentialPrepay();
        // TODO: 01.03.2017 install metrology configuration with correction factor
        /*meteringCustomPropertySetsDemoInstaller.correctionFactors();*/
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

    private void addAllSets(RegisteredCustomPropertySet registeredCustomPropertySet) {
        EnumSet.allOf(ServiceKind.class).forEach(serviceKind ->
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
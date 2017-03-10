/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.metering.cps.impl.metrology.UsagePointAntennaCPS;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointContCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointCorrectionFactorsCPS;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointDecentProdCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointMetrologyGeneralCPS;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointSettlementCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstAllCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstEGCustomPropertySet;
import com.elster.jupiter.metering.cps.impl.metrology.UsagePointTechInstElectrCPS;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeCheckList;

import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.annotations.Component;

import java.util.Set;

/**
 * Provides an implementation for the {@link UpgradeCheckList} interface
 * that lists all expected CustomPropetySets that are provided out of the box
 * for the different service categories.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-02 (13:06)
 */
@Component(
        name = "com.energyict.mdm.cps.checklist",
        service = {UpgradeCheckList.class},
        property = {"com.elster.jupiter.checklist=Insight.cps"},
        immediate = true)
public class InsightServiceCategoryCustomPropertySetsCheckList implements UpgradeCheckList {

    public static final String APPLICATION_NAME = "Example";

    @Override
    public String application() {
        return APPLICATION_NAME;
    }

    @Override
    public Set<InstallIdentifier> componentsToInstall() {
        return ImmutableSet.of(
                InstallIdentifier.identifier(application(), UsagePointAntennaCPS.COMPONENT_NAME),
                InstallIdentifier.identifier(application(), UsagePointContCustomPropertySet.COMPONENT_NAME),
                InstallIdentifier.identifier(application(), UsagePointDecentProdCustomPropertySet.COMPONENT_NAME),
                InstallIdentifier.identifier(application(), UsagePointMetrologyGeneralCPS.COMPONENT_NAME),
                InstallIdentifier.identifier(application(), UsagePointSettlementCustomPropertySet.COMPONENT_NAME),
                InstallIdentifier.identifier(application(), UsagePointTechInstAllCustomPropertySet.COMPONENT_NAME),
                InstallIdentifier.identifier(application(), UsagePointTechInstEGCustomPropertySet.COMPONENT_NAME),
                InstallIdentifier.identifier(application(), UsagePointTechInstElectrCPS.COMPONENT_NAME),
                InstallIdentifier.identifier(application(), UsagePointTechnicalSeeds.COMPONENT_NAME.get()),
                InstallIdentifier.identifier(application(), UsagePointGeneralCustomPropertySet.COMPONENT_NAME),
                InstallIdentifier.identifier(application(), UsagePointTechElCPS.COMPONENT_NAME),
                InstallIdentifier.identifier(application(), UsagePointContrElectrCPS.COMPONENT_NAME),
                InstallIdentifier.identifier(application(), UsagePointCorrectionFactorsCPS.COMPONENT_NAME));
    }

}
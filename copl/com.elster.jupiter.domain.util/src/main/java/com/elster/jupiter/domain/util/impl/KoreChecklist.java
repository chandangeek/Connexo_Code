/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util.impl;

import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeCheckList;

import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.annotations.Component;

import java.util.Set;

@Component(name = "com.elster.jupiter.kore.checklist", service = {UpgradeCheckList.class}, property = {"com.elster.jupiter.checklist=Pulse"})
public class KoreChecklist implements UpgradeCheckList {
    @Override
    public String application() {
        return "Pulse";
    }

    @Override
    public Set<InstallIdentifier> componentsToInstall() {
        return ImmutableSet.<InstallIdentifier>builder()
                .add(
                        InstallIdentifier.identifier(application(), "DVA"),
                        InstallIdentifier.identifier(application(), "NLS"),
                        InstallIdentifier.identifier(application(), "EVT"),
                        InstallIdentifier.identifier(application(), "IDS"),
                        InstallIdentifier.identifier(application(), "MSG"),
                        InstallIdentifier.identifier(application(), "TME"),
                        InstallIdentifier.identifier(application(), "USR"),
                        InstallIdentifier.identifier(application(), "YFA"),
                        InstallIdentifier.identifier(application(), "TSK"),
                        InstallIdentifier.identifier(application(), "LIC"),
                        InstallIdentifier.identifier(application(), "SBS"),
                        InstallIdentifier.identifier(application(), "FIS"),
                        InstallIdentifier.identifier(application(), "HTP"),
                        InstallIdentifier.identifier(application(), "PRT"),
                        InstallIdentifier.identifier(application(), "KPI"),
                        InstallIdentifier.identifier(application(), "BPM"),
                        InstallIdentifier.identifier(application(), "CAL"),
                        InstallIdentifier.identifier(application(), "FSM"),
                        InstallIdentifier.identifier(application(), "CPS"),
                        InstallIdentifier.identifier(application(), "SCS"),
                        InstallIdentifier.identifier(application(), "MTR"),
                        InstallIdentifier.identifier(application(), "MTI"),
                        InstallIdentifier.identifier(application(), "MTG"),
                        InstallIdentifier.identifier(application(), "YFG"),
                        InstallIdentifier.identifier(application(), "VAL"),
                        InstallIdentifier.identifier(application(), "ISU"),
                        InstallIdentifier.identifier(application(), "EST"),
                        InstallIdentifier.identifier(application(), "LFC"),
                        InstallIdentifier.identifier(application(), "APS"),
                        InstallIdentifier.identifier(application(), "DES"),
                        InstallIdentifier.identifier(application(), "SSA"),
                        InstallIdentifier.identifier(application(), "COU")
                ).build();
    }

}
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util.impl;

import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeCheckList;

import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.annotations.Component;

import java.util.Set;

import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;

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
                    identifier(application(), "DVA"),
                    identifier(application(), "NLS"),
                    identifier(application(), "EVT"),
                    identifier(application(), "IDS"),
                    identifier(application(), "MSG"),
                    identifier(application(), "TME"),
                    identifier(application(), "USR"),
                    identifier(application(), "YFA"),
                    identifier(application(), "TSK"),
                    identifier(application(), "LIC"),
                    identifier(application(), "SBS"),
                    identifier(application(), "FIS"),
                    identifier(application(), "HTP"),
                    identifier(application(), "PRT"),
                    identifier(application(), "KPI"),
                    identifier(application(), "BPM"),
                    identifier(application(), "CAL"),
                    identifier(application(), "FSM"),
                    identifier(application(), "CPS"),
                    identifier(application(), "SCS"),
                    identifier(application(), "MTR"),
                    identifier(application(), "MTI"),
                    identifier(application(), "MTG"),
                    identifier(application(), "YFG"),
                    identifier(application(), "VAL"),
                    identifier(application(), "ISU"),
                    identifier(application(), "EST"),
                    identifier(application(), "LFC"),
                    identifier(application(), "APS"),
                    identifier(application(), "DES"),
                    identifier(application(), "SSA")
                ).build();
    }

}
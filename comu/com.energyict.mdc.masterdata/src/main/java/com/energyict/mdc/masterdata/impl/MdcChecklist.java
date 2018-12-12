/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeCheckList;

import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.annotations.Component;

import java.util.Set;

@Component(name = "com.energyict.mdc.checklist", service = {UpgradeCheckList.class}, property = {"com.elster.jupiter.checklist=MultiSense"})
public class MdcChecklist implements UpgradeCheckList {
    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public Set<InstallIdentifier> componentsToInstall() {
        return ImmutableSet.<InstallIdentifier>builder()
                .add(
                        InstallIdentifier.identifier(application(), "MDS"),
                        InstallIdentifier.identifier(application(), "DLD"),
                        InstallIdentifier.identifier(application(), "PPC"),
                        InstallIdentifier.identifier(application(), "MDC"),
                        InstallIdentifier.identifier(application(), "CTS"),
                        InstallIdentifier.identifier(application(), "SCH"),
                        InstallIdentifier.identifier(application(), "DTC"),
                        InstallIdentifier.identifier(application(), "DDC"),
                        InstallIdentifier.identifier(application(), "IDV"),
                        InstallIdentifier.identifier(application(), "FAV"),
                        InstallIdentifier.identifier(application(), "DTL"),
                        InstallIdentifier.identifier(application(), "IDC"),
                        InstallIdentifier.identifier(application(), "FWC"),
                        InstallIdentifier.identifier(application(), "DDI"),
                        InstallIdentifier.identifier(application(), "MRI"),
                        InstallIdentifier.identifier(application(), "CES"),
                        InstallIdentifier.identifier(application(), "CSM"),
                        InstallIdentifier.identifier(application(), "MDA"),
                        InstallIdentifier.identifier(application(), "FLI"),
                        InstallIdentifier.identifier(application(), "CPC"),
                        InstallIdentifier.identifier(application(), "DSI")
                ).build();
    }
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeCheckList;

import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.annotations.Component;

import java.util.Set;

@Component(name = "com.energyict.mdm.checklist", service = {UpgradeCheckList.class}, property = {"com.elster.jupiter.checklist=Insight"})
public class InsightCheckList implements UpgradeCheckList {
    @Override
    public String application() {
        return "Insight";
    }

    @Override
    public Set<InstallIdentifier> componentsToInstall() {
        return ImmutableSet.of(
                InstallIdentifier.identifier(application(), "UPC"),
                InstallIdentifier.identifier(application(), "DMA"),
                InstallIdentifier.identifier(application(), "UDC")
        );
    }
}

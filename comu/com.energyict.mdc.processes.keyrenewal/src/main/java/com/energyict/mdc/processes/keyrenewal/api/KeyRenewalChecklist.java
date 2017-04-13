/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api;

import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeCheckList;
import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.annotations.Component;

import java.util.Set;

@Component(name = "com.energyict.mdc.checklist", service = {UpgradeCheckList.class}, property = {"com.energyict.mdc.checklist=KeyRenewal"})
public class KeyRenewalChecklist implements UpgradeCheckList {

    public static final String APPLICATION_NAME = "MDC";

    @Override
    public String application() {
        return APPLICATION_NAME;
    }

    @Override
    public Set<InstallIdentifier> componentsToInstall() {
        return ImmutableSet.<InstallIdentifier>builder()
                .add(
                        InstallIdentifier.identifier(application(), KeyRenewalApplication.COMPONENT_NAME)
                ).build();
    }
}
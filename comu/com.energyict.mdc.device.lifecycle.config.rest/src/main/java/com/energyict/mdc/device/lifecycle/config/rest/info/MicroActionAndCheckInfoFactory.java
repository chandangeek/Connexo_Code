/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheckNew;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class MicroActionAndCheckInfoFactory {

    public static final List<String> CONSOLIDATED_MICRO_CHECKS = Arrays.asList(
            "ConnectionPropertiesAreValid",
            "GeneralProtocolPropertiesAreValid",
            "ProtocolDialectPropertiesAreValid",
            "SecurityPropertiesAreValid"
    );

    public static final String CONSOLIDATED_MICRO_CHECKS_KEY = "MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE";

    private final DeviceLifeCycleService deviceLifeCycleService;
    private final Thesaurus thesaurus;

    @Inject
    public MicroActionAndCheckInfoFactory(DeviceLifeCycleService deviceLifeCycleService, Thesaurus thesaurus) {
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.thesaurus = thesaurus;
    }

    public MicroActionAndCheckInfo required(MicroAction microAction) {
        MicroActionAndCheckInfo info = common(microAction);
        info.isRequired = true;
        info.checked = true;
        return info;
    }

    public MicroActionAndCheckInfo optional(MicroAction microAction) {
        MicroActionAndCheckInfo info = common(microAction);
        info.isRequired = false;
        return info;
    }

    private MicroActionAndCheckInfo common(MicroAction microAction) {
        MicroActionAndCheckInfo info = new MicroActionAndCheckInfo();
        if (microAction != null) {
            info.key = microAction.name();
            info.name = deviceLifeCycleService.getName(microAction);
            info.description = deviceLifeCycleService.getDescription(microAction);
            info.category = new IdWithNameInfo();
            info.category.id = microAction.getCategory().name();
            info.category.name = deviceLifeCycleService.getCategoryName(microAction);
            microAction.getConflictGroupKey().ifPresent(key -> info.conflictGroup = new MicroActionConflictGroupInfo(key, thesaurus));
        }
        return info;
    }

    public MicroActionAndCheckInfo required(MicroCheckNew microCheck) {
        MicroActionAndCheckInfo info = common(microCheck);
        info.isRequired = true;
        info.checked = true;
        return info;
    }

    public MicroActionAndCheckInfo optional(MicroCheckNew microCheck) {
        MicroActionAndCheckInfo info = common(microCheck);
        info.isRequired = false;
        return info;
    }

    private MicroActionAndCheckInfo common(MicroCheckNew microCheck) {
        MicroActionAndCheckInfo info = new MicroActionAndCheckInfo();
        if (microCheck != null) {
            //TODO: Refactoring
            info.key = CONSOLIDATED_MICRO_CHECKS.contains(microCheck.getKey()) ? CONSOLIDATED_MICRO_CHECKS_KEY : microCheck.getKey();
            info.name = microCheck.getName();
            info.description = microCheck.getDescription();
            info.category = new IdWithNameInfo();
            info.category.id = microCheck.getCategory();
            info.category.name = microCheck.getCategoryName();
        }
        return info;
    }
}
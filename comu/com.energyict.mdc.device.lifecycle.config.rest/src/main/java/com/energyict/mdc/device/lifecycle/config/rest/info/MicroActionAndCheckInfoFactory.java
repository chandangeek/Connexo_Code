package com.energyict.mdc.device.lifecycle.config.rest.info;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Set;

public class MicroActionAndCheckInfoFactory {
    public static final Set<MicroCheck> CONSOLIDATED_MICRO_CHECKS = EnumSet.of(
            MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID,
            MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID,
            MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID,
            MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID
    );
    public static final String CONSOLIDATED_MICRO_CHECKS_KEY = "MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE";

    private final DeviceLifeCycleService deviceLifeCycleService;
    private final Thesaurus thesaurus;

    @Inject
    public MicroActionAndCheckInfoFactory(DeviceLifeCycleService deviceLifeCycleService, Thesaurus thesaurus) {
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.thesaurus = thesaurus;
    }

    public MicroActionAndCheckInfo required(MicroAction microAction){
        MicroActionAndCheckInfo info = common(microAction);
        info.isRequired = true;
        info.checked = true;
        return info;
    }

    public MicroActionAndCheckInfo optional(MicroAction microAction){
        MicroActionAndCheckInfo info = common(microAction);
        info.isRequired = false;
        return info;
    }

    private MicroActionAndCheckInfo common(MicroAction microAction){
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

    public MicroActionAndCheckInfo required(MicroCheck microCheck){
        MicroActionAndCheckInfo info = common(microCheck);
        info.isRequired = true;
        info.checked = true;
        return info;
    }

    public MicroActionAndCheckInfo optional(MicroCheck microCheck){
        MicroActionAndCheckInfo info = common(microCheck);
        info.isRequired = false;
        return info;
    }

    private MicroActionAndCheckInfo common(MicroCheck microCheck){
        MicroActionAndCheckInfo info = new MicroActionAndCheckInfo();
        if (microCheck != null) {
            if (CONSOLIDATED_MICRO_CHECKS.contains(microCheck)){
                info.key = CONSOLIDATED_MICRO_CHECKS_KEY;
            } else {
                info.key = microCheck.name();
            }
            info.name = deviceLifeCycleService.getName(microCheck);
            info.description = deviceLifeCycleService.getDescription(microCheck);
            info.category = new IdWithNameInfo();
            info.category.id = microCheck.getCategory().name();
            info.category.name = deviceLifeCycleService.getCategoryName(microCheck);
        }
        return info;
    }

}
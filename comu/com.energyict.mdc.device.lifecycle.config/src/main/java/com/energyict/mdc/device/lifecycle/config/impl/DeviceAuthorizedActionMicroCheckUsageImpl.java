/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.MicroCheckNew;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.logging.Logger;

public class DeviceAuthorizedActionMicroCheckUsageImpl {

    public enum Fields {

        TRANSITION("transition"),
        MICRO_CHECK("microCheckKey"),
        ;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private static final Logger LOG = Logger.getLogger("DeviceAuthorizedAction MicroCheck");

    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private Reference<AuthorizedAction> transition = ValueReference.absent();
    @Size(max = 80)
    @NotEmpty(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private String microCheckKey;
    private MicroCheckNew microCheck;

    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @Inject
    public DeviceAuthorizedActionMicroCheckUsageImpl(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    DeviceAuthorizedActionMicroCheckUsageImpl init(AuthorizedAction transition, MicroCheckNew microCheck) {
        this.transition.set(transition);
        this.microCheckKey = microCheck.getKey();
        this.microCheck = microCheck;
        return this;
    }

    public String getKey() {
        return this.microCheckKey;
    }

    public MicroCheckNew getCheck() {
        if (this.microCheck == null) {
            this.microCheck = this.deviceLifeCycleConfigurationService.getMicroCheckByKey(getKey())
                    .orElseGet(() -> {
                        LOG.warning("Unknown micro check with key = " + getKey());
                        return null;
                    });
        }
        return this.microCheck;
    }
}
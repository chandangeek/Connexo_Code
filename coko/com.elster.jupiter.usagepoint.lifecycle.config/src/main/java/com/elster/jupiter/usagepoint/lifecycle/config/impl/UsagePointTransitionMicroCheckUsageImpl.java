/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.logging.Logger;

public class UsagePointTransitionMicroCheckUsageImpl {

    public enum Fields {
        // Common fields
        TRANSITION("transition"),
        MICRO_CHECK("microCheck"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private static final Logger LOG = Logger.getLogger("UsagePointTransition MicroCheck");

    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private Reference<UsagePointTransition> transition = ValueReference.absent();
    @Size(max = 80)
    @NotEmpty(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private String microCheck;
    private MicroCheck microCheckObj;

    private final UsagePointLifeCycleConfigurationService lifeCycleConfService;

    @Inject
    public UsagePointTransitionMicroCheckUsageImpl(UsagePointLifeCycleConfigurationService lifeCycleConfService) {
        this.lifeCycleConfService = lifeCycleConfService;
    }

    UsagePointTransitionMicroCheckUsageImpl init(UsagePointTransition transition, MicroCheck microCheck) {
        this.transition.set(transition);
        this.microCheck = microCheck.getKey();
        this.microCheckObj = microCheck;
        return this;
    }

    public String getKey() {
        return this.microCheck;
    }

    public MicroCheck getCheck() {
        if (this.microCheckObj == null) {
            this.microCheckObj = this.lifeCycleConfService.getMicroCheckByKey(getKey())
                    .orElseGet(() -> {
                        LOG.warning("Unknown micro check with key = " + getKey());
                        return null;
                    });
        }
        return this.microCheckObj;
    }
}

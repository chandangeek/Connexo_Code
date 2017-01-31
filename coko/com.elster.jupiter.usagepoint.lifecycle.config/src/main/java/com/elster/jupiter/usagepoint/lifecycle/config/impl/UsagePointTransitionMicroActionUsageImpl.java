/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.logging.Logger;

public class UsagePointTransitionMicroActionUsageImpl {

    public enum Fields {
        // Common fields
        TRANSITION("transition"),
        MICRO_ACTION("microAction"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private static final Logger LOG = Logger.getLogger("UsagePointTransition MicroAction");

    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private Reference<UsagePointTransition> transition = ValueReference.absent();
    @Size(max = 80)
    @NotEmpty(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private String microAction;
    private MicroAction microActionObj;

    private final UsagePointLifeCycleConfigurationService lifeCycleConfService;

    @Inject
    public UsagePointTransitionMicroActionUsageImpl(UsagePointLifeCycleConfigurationService lifeCycleConfService) {
        this.lifeCycleConfService = lifeCycleConfService;
    }

    UsagePointTransitionMicroActionUsageImpl init(UsagePointTransition transition, MicroAction microAction) {
        this.transition.set(transition);
        this.microAction = microAction.getKey();
        this.microActionObj = microAction;
        return this;
    }

    public String getKey() {
        return this.microAction;
    }

    public MicroAction getAction() {
        if (this.microActionObj == null) {
            this.microActionObj = this.lifeCycleConfService.getMicroActionByKey(getKey())
                    .orElseGet(() -> {
                        LOG.warning("Unknown micro action with key = " + getKey());
                        return null;
                    });
        }
        return this.microActionObj;
    }
}

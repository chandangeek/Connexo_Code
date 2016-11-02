package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroAction;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.Size;

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

    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private Reference<UsagePointTransition> transition = ValueReference.absent();
    @Size(max = 80)
    @NotEmpty(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private String microAction;
    private MicroAction microActionObj;

    private final UsagePointLifeCycleService lifeCycleService;

    @Inject
    public UsagePointTransitionMicroActionUsageImpl(UsagePointLifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
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
            this.microActionObj = this.lifeCycleService.getMicroActionByKey(getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown micro action with key = " + getKey()));
        }
        return this.microActionObj;
    }
}

package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCheck;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.Size;

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

    @IsPresent(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private Reference<UsagePointTransition> transition = ValueReference.absent();
    @Size(max = 80)
    @NotEmpty(message = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    private String microCheck;
    private MicroCheck microCheckObj;

    private final UsagePointLifeCycleService lifeCycleService;

    @Inject
    public UsagePointTransitionMicroCheckUsageImpl(UsagePointLifeCycleService lifeCycleService) {
        this.lifeCycleService = lifeCycleService;
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
            this.microCheckObj = this.lifeCycleService.getMicroCheckByKey(getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown micro check with key = " + getKey()));
        }
        return this.microCheckObj;
    }
}

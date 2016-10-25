package com.elster.jupiter.mdm.usagepoint.lifecycle;

/**
 * Models pre-transition checks for {@link UsagePointTransition}.
 */
public enum MicroCheck {

    ;

    private MicroCategory category;

    MicroCheck(MicroCategory category) {
        this.category = category;
    }

    public MicroCategory getCategory() {
        return category;
    }

}
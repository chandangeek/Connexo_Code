package com.elster.jupiter.mdm.usagepoint.lifecycle;

import java.util.Optional;

/**
 * Models a number of tiny actions that will be executed by the
 * usage point life cycle engine as part of an {@link UsagePointTransition}.
 */
public enum MicroAction {

    ;

    private MicroCategory category;
    private String conflictGroupKey;

    MicroAction(MicroCategory category) {
        this.category = category;
    }

    MicroAction(MicroCategory category, String conflictGroupKey) {
        this(category);
        this.conflictGroupKey = conflictGroupKey;
    }

    public MicroCategory getCategory() {
        return this.category;
    }

    public Optional<String> getConflictGroupKey() {
        return Optional.ofNullable(this.conflictGroupKey);
    }
}
package com.elster.jupiter.dualcontrol;

/**
 * Abstraction for the changes that need approval under dual control rules.
 */
public interface PendingUpdate {

    boolean isActivation();

    boolean isRemoval();

    default boolean isUpdate() {
        return !isActivation() && !isRemoval();
    }
}

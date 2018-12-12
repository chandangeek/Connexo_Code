/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.concurrent;

import java.util.concurrent.Semaphore;

/**
 * Models a {@link Semaphore} that can be resided after initial creation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-22 (13:12)
 */
public class ResizeableSemaphore extends Semaphore {

    public ResizeableSemaphore (int permits) {
        super(permits);
    }

    public ResizeableSemaphore (int permits, boolean fair) {
        super(permits, fair);
    }

    /**
     * Increases the number of permits by the specified amount.
     *
     * @param amount The extra number of permits
     */
    public void increasePermits (int amount) {
        this.release(amount);
    }

    /**
     * Recudes the number of permits by the specified amount.
     *
     * @param amount The reduced number of permits
     */
    @Override
    public void reducePermits (int amount) {
        super.reducePermits(amount);
    }

}
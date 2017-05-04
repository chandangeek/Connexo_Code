/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.generic.messages;

/**
 * Produces unique and sequential index numbers that can be used to index arrays.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-04-08 (16:08)
 */
public class ArrayIndexGenerator {

    private int index;

    public int next() {
        return this.index++;
    }

    private ArrayIndexGenerator(int initialValue) {
        this.index = initialValue;
    }

    public static ArrayIndexGenerator zeroBased() {
        return new ArrayIndexGenerator(0);
    }

    public static ArrayIndexGenerator oneBased() {
        return new ArrayIndexGenerator(1);
    }

}
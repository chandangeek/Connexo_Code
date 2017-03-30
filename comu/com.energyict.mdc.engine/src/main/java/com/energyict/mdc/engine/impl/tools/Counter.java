/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.tools;

import java.util.concurrent.atomic.AtomicInteger;

/**
* A very simple but yet thread safe counter that will e.g. allow you to count
* the occurrence of say an event.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-07-19 (13:38)
*/
public class Counter {

    private AtomicInteger value = new AtomicInteger();

    public void reset () {
        this.value.getAndSet(0);
    }

    public void increment () {
        this.value.incrementAndGet();
    }

    public void add (int value) {
        this.value.addAndGet(value);
    }

    public int getValue () {
        return this.value.get();
    }

}
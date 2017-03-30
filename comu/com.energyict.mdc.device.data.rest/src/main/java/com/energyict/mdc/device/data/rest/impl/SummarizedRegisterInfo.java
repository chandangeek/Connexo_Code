/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

public class SummarizedRegisterInfo {
    public Long id;
    public String name;
    public boolean isBilling;
    public boolean hasEvent;
    public boolean isCumulative;
    public boolean useMultiplier;

    public SummarizedRegisterInfo(Long id, String name, boolean isBilling, boolean hasEvent, boolean isCumulative, boolean useMultiplier) {
        this.id = id;
        this.name = name;
        this.isBilling = isBilling;
        this.hasEvent = hasEvent;
        this.isCumulative = isCumulative;
        this.useMultiplier = useMultiplier;

    }
}

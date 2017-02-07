/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.rest.impl;

import com.elster.jupiter.rest.util.DualControlChangeInfo;

import java.util.List;

public class DualControlInfo {
    public boolean hasCurrentUserAccepted;
    public List<DualControlChangeInfo> changes;
    public PendingChangesType pendingChangesType;

    public DualControlInfo() {
    }
}

package com.energyict.mdc.device.command.rest.impl;

import com.elster.jupiter.rest.util.DualControlChangeInfo;

import java.util.List;

public class DualControlInfo {
    public boolean hasCurrentUserAccepted;
    public List<DualControlChangeInfo> changes;
    public PendingChangesType pendingChangesType;
}

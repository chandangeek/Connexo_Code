package com.energyict.mdc.device.command;

import com.elster.jupiter.dualcontrol.PendingUpdate;

public interface CommandRulePendingUpdate extends ServerCommandRule, PendingUpdate {

    long getVersion();

}

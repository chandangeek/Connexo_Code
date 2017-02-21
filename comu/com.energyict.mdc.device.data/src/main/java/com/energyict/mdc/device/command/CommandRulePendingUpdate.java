/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command;

import com.elster.jupiter.dualcontrol.PendingUpdate;

public interface CommandRulePendingUpdate extends ServerCommandRule, PendingUpdate {

    long getVersion();

}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;

import java.util.Optional;

public interface CommandInRule extends HasId {
    DeviceMessageSpec getCommand();

    CommandRule getCommandRule();

    Optional<CommandRulePendingUpdate> getCommandRulePendingUpdate();
}

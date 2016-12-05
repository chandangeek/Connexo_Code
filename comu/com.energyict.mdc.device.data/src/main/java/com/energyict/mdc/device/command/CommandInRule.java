package com.energyict.mdc.device.command;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageCategories;
import com.energyict.mdc.protocol.api.impl.device.messages.DeviceMessageSpecEnum;

import java.util.Optional;

public interface CommandInRule extends HasId {
    DeviceMessageSpec getCommand();

    CommandRule getCommandRule();

    Optional<CommandRuleTemplate> getCommandRuleTemplate();
}

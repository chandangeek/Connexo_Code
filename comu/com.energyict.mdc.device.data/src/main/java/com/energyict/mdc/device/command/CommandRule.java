package com.energyict.mdc.device.command;

import java.util.Optional;

public interface CommandRule extends ServerCommandRule {

    Optional<CommandRulePendingUpdate> getCommandRulePendingUpdate();

    void activate();

    void deactivate();

    void approve();

    void reject();
}

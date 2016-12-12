package com.energyict.mdc.device.command;

import java.util.List;
import java.util.Optional;

public interface CommandRule extends ServerCommandRule {

    Optional<CommandRulePendingUpdate> getCommandRulePendingUpdate();

    void activate();

    void deactivate();

    void approve();

    void reject();

    boolean hasCurrentUserAccepted();

    void update(String name, long dayLimit, long weekLimit, long monthLimit, List<String> commands);
}

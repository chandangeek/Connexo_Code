/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command;

import com.energyict.mdc.device.command.impl.CommandRuleCounter;

import java.util.List;
import java.util.Optional;

public interface CommandRule extends ServerCommandRule {

    List<ICommandRuleCounter> getCounters();

    Optional<CommandRulePendingUpdate> getCommandRulePendingUpdate();

    void activate();

    void deactivate();

    void approve();

    void reject();

    boolean hasCurrentUserAccepted();

    void update(String name, long dayLimit, long weekLimit, long monthLimit, List<String> commands);

    void addCounter(CommandRuleCounter counter);
}

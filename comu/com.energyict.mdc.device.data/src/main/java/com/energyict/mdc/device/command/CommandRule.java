package com.energyict.mdc.device.command;

import com.energyict.mdc.device.command.impl.CommandRuleCounter;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CommandRule extends ServerCommandRule {

    List<CommandRuleCounter> getCounters();

    Optional<CommandRulePendingUpdate> getCommandRulePendingUpdate();

    void activate();

    void deactivate();

    void approve();

    void reject();

    boolean hasCurrentUserAccepted();

    void update(String name, long dayLimit, long weekLimit, long monthLimit, List<String> commands);

    void createCounterFor(Range<Instant> range);
}

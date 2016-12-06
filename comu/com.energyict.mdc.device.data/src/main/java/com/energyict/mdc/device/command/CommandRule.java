package com.energyict.mdc.device.command;

import java.util.List;
import java.util.Optional;

public interface CommandRule extends ServerCommandRule {

    List<CommandInRule> getCommands();

    Optional<CommandRuleTemplate> getCommandRuleTemplate();

    long getVersion();

    void save();

    void delete();
}

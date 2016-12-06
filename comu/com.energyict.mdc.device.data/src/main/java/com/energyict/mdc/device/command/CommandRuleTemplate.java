package com.energyict.mdc.device.command;

import java.util.List;

public interface CommandRuleTemplate extends ServerCommandRule {

    List<CommandInRule> getCommands();

    long getVersion();

    void save();

    void delete();
}

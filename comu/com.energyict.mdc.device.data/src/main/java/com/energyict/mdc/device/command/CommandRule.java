package com.energyict.mdc.device.command;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.util.List;
import java.util.Optional;

public interface CommandRule extends HasId, HasName {

    long getDayLimit();

    long getWeekLimit();

    long getMonthLimit();

    List<CommandInRule> getCommands();

    Optional<CommandRuleTemplate> getCommandRuleTemplate();

    long getVersion();

    void save();

    void delete();
}

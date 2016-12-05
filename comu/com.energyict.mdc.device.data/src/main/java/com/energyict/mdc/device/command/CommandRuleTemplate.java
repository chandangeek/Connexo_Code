package com.energyict.mdc.device.command;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.util.List;

public interface CommandRuleTemplate extends HasId, HasName {

    long getDayLimit();

    long getWeekLimit();

    long getMonthLimit();

    List<CommandInRule> getCommands();

    long getVersion();

    void save();

    void delete();
}

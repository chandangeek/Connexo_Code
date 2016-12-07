package com.energyict.mdc.device.command.rest.impl;

import com.energyict.mdc.device.command.CommandRule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandRuleInfo {
    public long id;
    public String name;
    public boolean active;
    public long dayLimit;
    public long weekLimit;
    public long monthLimit;
    public String statusMessage;
    public long version;
    public List<CommandInfo> commands = new ArrayList<>();

    static CommandRuleInfo from(CommandRule commandRule) {
        CommandRuleInfo commandRuleInfo = new CommandRuleInfo();
        commandRuleInfo.id = commandRule.getId();
        commandRuleInfo.name = commandRule.getName();
        commandRuleInfo.dayLimit = commandRule.getDayLimit();
        commandRuleInfo.weekLimit = commandRule.getWeekLimit();
        commandRuleInfo.monthLimit = commandRule.getMonthLimit();
        commandRuleInfo.version = commandRule.getVersion();
        commandRuleInfo.commands = commandRule.getCommands()
                .stream()
                .map(commandInRule -> new CommandInfo(commandInRule.getCommand().getCategory().getName(), commandInRule.getCommand().getName(), commandInRule.getCommand().getId().name()))
                .sorted((c1, c2) -> c1.compareTo(c2))
                .collect(Collectors.toList());

        return commandRuleInfo;
    }
}

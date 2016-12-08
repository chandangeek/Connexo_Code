package com.energyict.mdc.device.command.rest.impl;

import com.elster.jupiter.rest.util.DualControlChangeInfo;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRulePendingUpdate;

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
    public List<DualControlChangeInfo> changes;

    static CommandRuleInfo create(CommandRule commandRule) {
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
                .sorted(CommandInfo::compareTo)
                .collect(Collectors.toList());


        if(commandRule.getCommandRulePendingUpdate().isPresent()) {
            CommandRulePendingUpdate pendingUpdate = commandRule.getCommandRulePendingUpdate().get();
            if(pendingUpdate.isActivation()) {
                commandRuleInfo.statusMessage = "pendingActivation";
            } else if (pendingUpdate.isDeactivation()) {
                commandRuleInfo.statusMessage = "pendingDeactivation";
            } else if (pendingUpdate.isRemoval()) {
                commandRuleInfo.statusMessage = "pendingRemoval";
            } else if (pendingUpdate.isUpdate()) {
                commandRuleInfo.statusMessage = "pendingUpdate";
            }
        }
        return commandRuleInfo;
    }

    static CommandRuleInfo createWithChanges(CommandRule commandRule) {
        CommandRuleInfo commandRuleInfo= create(commandRule);
        if(commandRule.getCommandRulePendingUpdate().isPresent()) {
            CommandRulePendingUpdate pendingUpdate = commandRule.getCommandRulePendingUpdate().get();
            commandRuleInfo.changes = new ArrayList<>();
            checkBasicChanges(commandRuleInfo.changes, commandRule, pendingUpdate);
        }
        return commandRuleInfo;
    }

    private static void checkBasicChanges(List<DualControlChangeInfo> changes, CommandRule commandRule, CommandRulePendingUpdate pendingUpdate) {
        if(commandRule.isActive() != pendingUpdate.isActive()) {
            changes.add(new DualControlChangeInfo("Day Limit", String.valueOf(commandRule.isActive()), String.valueOf(pendingUpdate.isActive())));
        }

        if(!commandRule.getName().equals(pendingUpdate.getName())) {
            changes.add(new DualControlChangeInfo("Name", commandRule.getName(), pendingUpdate.getName()));
        }
        if(commandRule.getDayLimit() != pendingUpdate.getDayLimit()) {
            changes.add(new DualControlChangeInfo("Day limit", String.valueOf(commandRule.getDayLimit()), String.valueOf(pendingUpdate.getDayLimit())));
        }
        if(commandRule.getWeekLimit() != pendingUpdate.getWeekLimit()) {
            changes.add(new DualControlChangeInfo("Week limit", String.valueOf(commandRule.getWeekLimit()), String.valueOf(pendingUpdate.getWeekLimit())));
        }
        if(commandRule.getMonthLimit() != pendingUpdate.getMonthLimit()) {
            changes.add(new DualControlChangeInfo("Day limit", String.valueOf(commandRule.getMonthLimit()), String.valueOf(pendingUpdate.getMonthLimit())));
        }
    }
}
